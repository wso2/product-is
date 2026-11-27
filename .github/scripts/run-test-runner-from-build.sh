#!/bin/bash
# Runs one integration test runner's share of the suite against a product-is build made by another job (see
# package-build-for-test-runners.sh): unpacks the build, installs the IS pack, builds only modules/integration,
# and runs the [runnerN] section of the lanes file.
# Usage: run-test-runner-from-build.sh <runner number> <build artifact dir>
# Environment: LANES_FILE (relative to tests-backend), LANES_RUNNER (section; empty to run the whole file),
#              LANES_ALLOW_PARTIAL=true (experiments), JAVA_21_HOME.
set -uo pipefail
N=$1
BUILD=$(cd "$2" && pwd)
WS=product-is-$N
export JAVA_HOME=${JAVA_21_HOME:-$JAVA_HOME}

mkdir -p "$WS" ~/.m2/repository
tar -xzf "$BUILD/product-is-src.tgz" -C "$WS"
tar -xf "$BUILD/pack.tar" -C "$WS"
tar -xzf "$BUILD/m2-snapshots.tgz" -C ~/.m2/repository
cd "$WS"

# Memory (RSS, MB) per kind of process every 30 s, so a runner that runs out of resources leaves evidence.
( echo "cores=$(nproc) $(free -m | awk '/Mem:/ {print "mem_total_mb=" $2}')"
  while true; do
    procs=$(ps -eo rss=,args= | awk '
      /awk / {next}
      /-Dcarbon.home=/ {is+=$1; nis++; next}
      /org.testng.TestNG/ {t+=$1; nt++; next}
      /plexus-classworlds|org.codehaus.plexus.classworlds/ {m+=$1; next}
      /java/ {o+=$1}
      END {printf "is_mb=%d(%d) testjvm_mb=%d(%d) maven_mb=%d otherjava_mb=%d", is/1024, nis, t/1024, nt, m/1024, o/1024}')
    echo "$(date -u +%H:%M:%S) used_mb=$(free -m | awk '/Mem:/ {print $3}') avail_mb=$(free -m | awk '/Mem:/ {print $7}') swap_used_mb=$(free -m | awk '/Swap:/ {print $3}') load=$(cut -d' ' -f1 /proc/loadavg) $procs"
    sleep 30
  done ) > resource-usage.log 2>&1 &
MONITOR=$!

VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout --non-recursive)
ZIP=modules/distribution/target/wso2is-$VERSION.zip
mvn install:install-file -q --batch-mode -Dfile="$ZIP" -DgroupId=org.wso2.is -DartifactId=wso2is \
    -Dversion="$VERSION" -Dpackaging=zip

LANE_ARGS=()
[ -n "${LANES_FILE:-}" ] && LANE_ARGS+=("-Dtest.lanes.file=$PWD/modules/integration/tests-integration/tests-backend/$LANES_FILE")
[ -n "${LANES_RUNNER:-}" ] && LANE_ARGS+=("-Dlanes.runner=$LANES_RUNNER")
[ "${LANES_ALLOW_PARTIAL:-}" = "true" ] && LANE_ARGS+=("-Dlanes.allowPartial=true")
MAVEN_OPTS="${MAVEN_OPTS:-} -XX:+UseG1GC -XX:G1PeriodicGCInterval=10000 -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=30" \
  mvn install --batch-mode -f modules/integration/pom.xml "${LANE_ARGS[@]}" | tee mvn-build.log
kill $MONITOR 2>/dev/null || true

RESULT=$(sed -n -e '/\[INFO\] Results:/,/\[INFO\] Tests run:/ p' mvn-build.log | tail -20)
echo "::warning::Integration tests, runner $N:%0A$(echo "$RESULT" | sed 's/$/%0A/' | tr -d '\n')"
if [ "$(grep -oE '\[INFO\] BUILD (SUCCESS|FAILURE)' mvn-build.log | tail -1)" != "[INFO] BUILD SUCCESS" ]; then
  echo "::error::Integration tests failed on runner $N. Check the artifacts for logs."
  exit 1
fi
