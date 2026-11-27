#!/bin/bash
# Packages a product-is build (made by pr-builder.sh with BUILD_ONLY=true) for the integration test runners, so
# they test exactly what was built without building it again:
# - product-is-src.tgz: the product-is source tree as built (with the PR diff or dependency versions applied),
#   without build output, plus the IS pack zip.
# - m2-snapshots.tgz: the SNAPSHOT artifacts this build installed into ~/.m2 (product-is modules and, for a
#   component PR, the component), which the test modules compile against and which aren't published anywhere.
# Usage: package-build-for-test-runners.sh <product-is dir> <build start marker file> <output dir>
set -euo pipefail
SRC=$1
MARKER=$2
OUT=$3
mkdir -p "$OUT"

ZIP=$(ls "$SRC"/modules/distribution/target/wso2is-*.zip | grep -v -- '-src\.zip$' | head -1)
tar -czf "$OUT/product-is-src.tgz" -C "$SRC" \
    --exclude=./.git --exclude='*/node_modules' --exclude='./target' --exclude='*/target' \
    --exclude='./mvn-build.log' --exclude='./resource-usage.log' .
tar -rf "$OUT/pack.tar" -C "$SRC" "${ZIP#$SRC/}"

# SNAPSHOT artifact files written during this build, except the IS pack (test runners install it from pack.tar),
# the integration test modules (test runners build them) and source/javadoc jars.
(cd ~/.m2/repository && find . -path '*-SNAPSHOT*' -type f -newer "$MARKER" ! -path './org/wso2/is/wso2is/*' \
    ! -path './org/wso2/is/org.wso2.carbon.identity.integration.backend.test/*' \
    ! -name '*-sources.jar' ! -name '*-javadoc.jar' -print0 \
    | tar --null -czf "$OUT/m2-snapshots.tgz" -T -)
ls -lh "$OUT"
