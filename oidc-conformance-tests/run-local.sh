#!/usr/bin/env bash
# ----------------------------------------------------------------------------
#  Copyright 2026 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# ----------------------------------------------------------------------------
#
# Local reproduction of .github/workflows/oidc-conformance-test.yml.
#
# It mirrors the workflow's critical path so failures can be reproduced and
# debugged on a laptop or any CI runner without the full Actions machinery:
#
#   Get IS zip  ->  Download Jacoco  ->  Run IS (configure_is.py)
#               ->  [--conformance] Run Conformance Suite  ->  Run Tests
#
# The "Run IS" step (configure_is.py) is where the nightly currently fails
# ("Subscribe to the required APIs -> Error occurred: list index out of range"),
# so by default the harness stops after that step. Use --conformance to also
# clone + start the OpenID conformance suite (requires Docker) and run the tests.
#
# Everything happens in an isolated work directory; the repo is never mutated.
#
# Usage:
#   ./run-local.sh [options]
#
# Options:
#   --zip <path>       Use this WSO2 IS distribution zip
#                      (default: newest modules/distribution/target/wso2is-*.zip)
#   --build            Build the IS distribution from source first
#                      (mvn clean install -Dmaven.test.skip=true)
#   --conformance      Also run the conformance suite + tests (needs Docker)
#   --suite-branch <b> Conformance suite branch (default: latest release branch)
#   --work <dir>       Work directory (default: a fresh temp dir)
#   --keep             Do not stop the server / delete the work dir on exit
#   -h, --help         Show this help
#
# Environment overrides:
#   JAVA_HOME_21   Path to a JDK 21 home (else auto-detected)
#   IS_ZIP         Same as --zip
#
set -uo pipefail

# --------------------------------------------------------------------------- #
# Paths
# --------------------------------------------------------------------------- #
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"   # .../oidc-conformance-tests
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"                    # product-is root

# --------------------------------------------------------------------------- #
# Defaults / arg parsing
# --------------------------------------------------------------------------- #
IS_ZIP="${IS_ZIP:-}"
DO_BUILD="no"
DO_CONFORMANCE="no"
SUITE_BRANCH=""
WORK=""
KEEP="no"

log()  { printf '\n\033[1;34m>>> %s\033[0m\n' "$*"; }
info() { printf '    %s\n' "$*"; }
err()  { printf '\033[1;31m!!! %s\033[0m\n' "$*" >&2; }
die()  { err "$*"; exit 1; }

# best-effort host IP (Linux non-loopback / macOS en0)
_host_ip() {
  if command -v ip >/dev/null 2>&1; then
    ip -o -4 addr list 2>/dev/null | awk '$2!="lo"{print $4}' | cut -d/ -f1 | head -1
  else
    ipconfig getifaddr en0 2>/dev/null || echo "127.0.0.1"
  fi
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --zip)          IS_ZIP="${2:?}"; shift 2 ;;
    --build)        DO_BUILD="yes"; shift ;;
    --conformance)  DO_CONFORMANCE="yes"; shift ;;
    --suite-branch) SUITE_BRANCH="${2:?}"; shift 2 ;;
    --work)         WORK="${2:?}"; shift 2 ;;
    --keep)         KEEP="yes"; shift ;;
    -h|--help)      sed -n '2,60p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *)              die "Unknown option: $1 (use --help)" ;;
  esac
done

[[ -z "$WORK" ]] && WORK="$(mktemp -d "${TMPDIR:-/tmp}/wso2is-oidc-local.XXXXXX")"
mkdir -p "$WORK"
CONF_LOG="$WORK/configure_is.log"

# --------------------------------------------------------------------------- #
# JDK 21
# --------------------------------------------------------------------------- #
detect_java_home() {
  if [[ -n "${JAVA_HOME_21:-}" && -x "$JAVA_HOME_21/bin/java" ]]; then
    echo "$JAVA_HOME_21"; return
  fi
  if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]] \
     && "$JAVA_HOME/bin/java" -version 2>&1 | grep -q '"21'; then
    echo "$JAVA_HOME"; return
  fi
  if [[ -x /usr/libexec/java_home ]]; then                     # macOS
    /usr/libexec/java_home -v 21 2>/dev/null && return
  fi
  # Linux common locations
  for d in /usr/lib/jvm/temurin-21* /usr/lib/jvm/java-21* /opt/hostedtoolcache/Java_*/21*/x64; do
    [[ -x "$d/bin/java" ]] && { echo "$d"; return; }
  done
  return 1
}

JAVA_HOME="$(detect_java_home)" || die "JDK 21 not found. Set JAVA_HOME_21 to a JDK 21 home."
export JAVA_HOME
info "JAVA_HOME = $JAVA_HOME"
"$JAVA_HOME/bin/java" -version 2>&1 | head -1 | sed 's/^/    /'

# --------------------------------------------------------------------------- #
# Cleanup / traps
# --------------------------------------------------------------------------- #
cleanup() {
  local rc=$?
  if [[ "$KEEP" == "yes" ]]; then
    log "--keep set: leaving server running and work dir at:"; info "$WORK"
    return $rc
  fi
  log "Cleanup: stopping server and removing work dir"
  # graceful stop
  for d in "$WORK"/product-is/oidc-conformance-tests/wso2is-*; do
    [[ -x "$d/bin/wso2server.sh" ]] && "$d/bin/wso2server.sh" stop >/dev/null 2>&1 || true
  done
  # backstop: kill anything still running out of the work dir
  pkill -f "$WORK/product-is/oidc-conformance-tests/wso2is-" 2>/dev/null || true
  rm -rf "$WORK" 2>/dev/null || true
  return $rc
}
trap cleanup EXIT

# Refuse to run if something is already bound to 9443 (would confuse configure_is).
if command -v lsof >/dev/null 2>&1 && lsof -nP -iTCP:9443 -sTCP:LISTEN >/dev/null 2>&1; then
  die "Port 9443 is already in use. Stop the running IS first."
fi

# --------------------------------------------------------------------------- #
# Step: Get IS zip  (build or reuse)
# --------------------------------------------------------------------------- #
log "Step: Get IS zip"
if [[ "$DO_BUILD" == "yes" ]]; then
  info "Building IS from source (mvn clean install -Dmaven.test.skip=true)..."
  ( cd "$REPO_ROOT" && mvn clean install -Dmaven.test.skip=true --batch-mode ) \
    || die "Maven build failed"
fi
if [[ -z "$IS_ZIP" ]]; then
  # Prefer the zip that matches the current source version (i.e. the local
  # snapshot build), so a stale zip from a different checkout state doesn't
  # get picked just for being newest on disk.
  PROJECT_VERSION="$(awk '/<\/parent>/{p=1} p && /<version>/{gsub(/^[ \t]*<version>|<\/version>[ \t]*$/,""); print; exit}' "$REPO_ROOT/pom.xml" 2>/dev/null)"
  if [[ -n "$PROJECT_VERSION" && -f "$REPO_ROOT/modules/distribution/target/wso2is-$PROJECT_VERSION.zip" ]]; then
    IS_ZIP="$REPO_ROOT/modules/distribution/target/wso2is-$PROJECT_VERSION.zip"
    info "Found local snapshot build matching pom.xml version ($PROJECT_VERSION)"
  else
    # fall back: newest wso2is-*.zip that is not a -src.zip
    newest=""
    for z in "$REPO_ROOT"/modules/distribution/target/wso2is-*.zip; do
      [[ -f "$z" ]] || continue
      [[ "$z" == *-src.zip ]] && continue
      [[ -z "$newest" || "$z" -nt "$newest" ]] && newest="$z"
    done
    [[ -n "$newest" && -n "$PROJECT_VERSION" ]] \
      && info "No zip matching source version $PROJECT_VERSION found; using newest available: $(basename "$newest")"
    IS_ZIP="$newest"
  fi
fi
[[ -n "$IS_ZIP" && -f "$IS_ZIP" ]] \
  || die "IS zip not found. Build with --build or pass --zip <path>."
IS_ZIP="$(cd "$(dirname "$IS_ZIP")" && pwd)/$(basename "$IS_ZIP")"   # absolutise
ZIP_NAME="$(basename "$IS_ZIP")"
info "Using zip: $IS_ZIP"

# --------------------------------------------------------------------------- #
# Step: Setup Python venv (psutil + requests, as the workflow pip-installs)
# --------------------------------------------------------------------------- #
log "Step: Setup Python"
VENV="$WORK/venv"
python3 -m venv "$VENV" || die "Failed to create venv"
PY="$VENV/bin/python3"
"$PY" -m pip install --quiet --upgrade pip >/dev/null
"$PY" -m pip install --quiet psutil requests httpx httplib2 \
  || die "pip install failed"
info "venv ready: $("$PY" -c 'import psutil,requests;print("psutil",psutil.__version__,"requests",requests.__version__)')"

# --------------------------------------------------------------------------- #
# Step: Download Jacoco  (fallback: bundled copy in the repo)
# --------------------------------------------------------------------------- #
log "Step: Download Jacoco agent"
JACOCO_ZIP="$WORK/jacoco-0.8.12.zip"
if curl -fsSL -o "$JACOCO_ZIP" \
     "https://search.maven.org/remotecontent?filepath=org/jacoco/jacoco/0.8.12/jacoco-0.8.12.zip" \
     2>/dev/null && [[ -s "$JACOCO_ZIP" ]]; then
  info "Downloaded jacoco-0.8.12.zip"
elif [[ -d "$SCRIPT_DIR/jacoco-0.8.12/lib" ]]; then
  info "Download failed; packing bundled jacoco-0.8.12/ from the repo"
  ( cd "$SCRIPT_DIR/jacoco-0.8.12" && zip -qr "$JACOCO_ZIP" . )
else
  die "Could not obtain jacoco-0.8.12.zip (no network and no bundled copy)"
fi

# --------------------------------------------------------------------------- #
# Recreate the workflow's directory layout inside WORK:
#   WORK/                          (== ROOT_DIR passed to configure_is.py)
#     <IS zip>, jacoco-0.8.12.zip, jacoco.exec
#     product-is/oidc-conformance-tests/   (copy of this repo dir)
# configure_is.py is invoked from product-is/oidc-conformance-tests with the zip
# referenced as ../../<zip>, exactly as the workflow does.
# --------------------------------------------------------------------------- #
log "Step: Prepare work layout"
RUN_DIR="$WORK/product-is/oidc-conformance-tests"
mkdir -p "$RUN_DIR"
# Copy the tests dir, excluding: the heavy bundled jacoco html, __pycache__, and
# any leftover extracted server dirs (wso2is-*). configure_is.py selects the
# server directory as the first "wso2is*" entry listdir() returns, so a stray
# extracted server in the source tree would be picked instead of our fresh zip.
( cd "$SCRIPT_DIR" && \
  find . -maxdepth 1 -mindepth 1 \
       ! -name 'jacoco-0.8.12' ! -name '__pycache__' ! -name 'wso2is-*' \
       ! -name 'run-local.sh' \
       -exec cp -R {} "$RUN_DIR/" \; )
cp "$IS_ZIP" "$WORK/$ZIP_NAME"
touch "$WORK/jacoco.exec"
info "Work dir: $WORK"

# --------------------------------------------------------------------------- #
# Step: Run IS  (the actual failing step in CI)
# --------------------------------------------------------------------------- #
log "Step: Run IS (configure_is.py)"
# configure_is.py starts the WSO2 server as a long-lived child and pipes only
# its stdout; the server inherits our stderr. Piping this to `tee` would hang
# forever after configure_is exits, because the still-running daemon holds the
# pipe's write end open and tee never sees EOF. So write to a file, tail it for
# live output, and wait on the python PID only (the daemon reparents to init).
# -u keeps Python unbuffered so progress streams live (it block-buffers stdout
# when writing to a file). tail -f mirrors it to the console.
( cd "$RUN_DIR" && "$PY" -u ./configure_is.py "../../$ZIP_NAME" "$WORK" "$WORK/jacoco.exec" ) \
  >"$CONF_LOG" 2>&1 &
CONF_PID=$!
tail -f "$CONF_LOG" 2>/dev/null & TAIL_PID=$!
wait "$CONF_PID"; RUN_IS_RC=$?
sleep 1                                    # let tail flush the final lines
kill "$TAIL_PID" 2>/dev/null; wait "$TAIL_PID" 2>/dev/null || true

echo
if [[ $RUN_IS_RC -ne 0 ]]; then
  err "configure_is.py FAILED (exit $RUN_IS_RC) — this is the reproduction."
  echo "----- last 25 lines of configure_is.log -----"
  tail -25 "$CONF_LOG"
  echo "---------------------------------------------"
  info "Full log: $CONF_LOG"
  info "Server log: $WORK/product-is/oidc-conformance-tests/wso2is-*/repository/logs/wso2carbon.log"
  info "Re-run with --keep to inspect the running server."
  exit $RUN_IS_RC
fi
log "configure_is.py completed successfully — IS is configured for conformance tests."

# --------------------------------------------------------------------------- #
# Step: Run Conformance Suite + Tests  (optional; needs Docker)
# --------------------------------------------------------------------------- #
if [[ "$DO_CONFORMANCE" == "yes" ]]; then
  command -v docker >/dev/null 2>&1 || die "--conformance needs Docker installed"
  command -v jq >/dev/null 2>&1 || die "--conformance needs jq installed"

  log "Step: Clone conformance suite"
  if [[ -z "$SUITE_BRANCH" ]]; then
    SUITE_BRANCH="$(curl -s https://gitlab.com/api/v4/projects/4175605/releases/ \
                    | jq -r '.[0].name')"
  fi
  info "Conformance suite branch: $SUITE_BRANCH"
  ( cd "$WORK" && git clone --depth 1 --branch "$SUITE_BRANCH" \
      https://gitlab.com/openid/conformance-suite.git ) || die "suite clone failed"

  # add extra_hosts so the containers can reach the host IS on localhost.
  # (uses awk, not "sed -i ... i \...\n...", because BSD/macOS sed treats \n in an
  # i-command as a literal two-char escape, not a newline, and errors out --
  # which silently no-ops here since the caller only had `|| true` around it)
  #
  # Maps to host.docker.internal's actual resolved IP, not the host's real LAN
  # IP: on Docker Desktop for Mac, a container connecting to the host's own
  # LAN IP hits a "hairpin NAT" path that corrupts larger packets (e.g. a
  # multi-extension TLS ClientHello), surfacing as a mid-handshake
  # "unexpected eof"/decode-error TLS failure. host.docker.internal's own
  # resolved address avoids it, but extra_hosts needs a literal IP, so
  # resolve it via a throwaway container first (Docker Desktop injects that
  # DNS entry into every container automatically). host.docker.internal is a
  # Docker Desktop (Mac/Windows) feature, not native Docker Engine on Linux,
  # so fall back to a real host IP there.
  HOST_IP="$(docker run --rm alpine getent hosts host.docker.internal 2>/dev/null | awk '{print $1}')"
  [[ -n "$HOST_IP" ]] || HOST_IP="$(_host_ip)"
  info "Host reachable at: $HOST_IP"
  DC="$WORK/conformance-suite/docker-compose-dev.yml"
  awk -v ip="$HOST_IP" '
    /^    volumes:/ {
      print "    extra_hosts:"
      print "    - \"localhost:" ip "\""
    }
    { print }
  ' "$DC" > "$DC.new" && mv "$DC.new" "$DC"

  log "Step: Run conformance suite"
  ( cd "$WORK/conformance-suite" && mvn clean package ) || die "conformance suite build failed"

  # start_conformance_suite.py shells out to "sudo docker-compose up", which needs an
  # interactive TTY for a password. Locally, docker-compose already works without sudo
  # (Docker Desktop), so drive it directly here instead, waiting for the same
  # "Starting application" marker the shared script looks for.
  # DOCKER_BUILDKIT=0 forces the legacy (non-buildx) builder. buildx keeps a lock
  # file under ~/.docker/buildx/ that can be left root-owned by an unrelated prior
  # sudo docker invocation on the host; the classic builder avoids that entirely.
  ( cd "$WORK/conformance-suite" && chmod 777 docker-compose-dev.yml \
      && DOCKER_BUILDKIT=0 docker-compose -f docker-compose-dev.yml up -d --build ) \
    || die "conformance suite start failed"

  info "Waiting for conformance suite server to start..."
  suite_ready=""
  for _ in $(seq 1 60); do
    if ( cd "$WORK/conformance-suite" && docker-compose -f docker-compose-dev.yml logs 2>/dev/null | grep -q "Starting application" ); then
      suite_ready="yes"
      break
    fi
    if ( cd "$WORK/conformance-suite" && docker-compose -f docker-compose-dev.yml ps 2>/dev/null | grep -qi "exit\|exited" ); then
      die "Conformance suite container exited unexpectedly"
    fi
    sleep 5
  done
  [[ -n "$suite_ready" ]] || die "Conformance suite did not report ready within timeout"
  info "Conformance suite started."

  log "Step: Run tests"
  ( cd "$WORK" && PATH="$VENV/bin:$PATH" bash "$RUN_DIR/test_runner.sh" )

  log "Step: Export results"
  if ( cd "$WORK" && "$PY" "$RUN_DIR/export_results.py" https://localhost:8443 ); then
    log "ALL TEST CASES PASSED"
  else
    err "Failed test cases found."
  fi
fi

log "Done. Work dir: $WORK"
