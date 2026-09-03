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
# Local reproduction of .github/workflows/fapi-oidc-conformance-test.yml.
#
# It mirrors the workflow's critical path so failures can be reproduced and
# debugged on a laptop or any CI runner without the full Actions machinery:
#
#   Get IS zip -> Patch deployment.toml + jacoco agent -> Run IS
#     (configure_is_fapi.py) -> [--conformance] resource server + suite + tests
#
# Unlike the OIDC suite, the FAPI server MUST be reachable as "https://iam:9443"
# (deployment-fapi-config.toml sets server.hostname = "iam", and
# constants_fapi.py hardcodes BASE_URL/RESOURCE_ENDPOINT_URL against "iam").
# So this script ensures "iam" resolves to 127.0.0.1 via /etc/hosts (sudo,
# skip with --skip-hosts if you already manage this yourself).
#
# By default the harness stops after configure_is_fapi.py (Run IS). Use
# --conformance to also start the FAPI resource server (nginx + Flask) and the
# OpenID conformance suite (needs Docker) and run the tests.
#
# Everything happens in an isolated work directory; the repo is never mutated.
# The one exception is /etc/hosts, which is a real system-wide edit (sudo) --
# see --skip-hosts below if you'd rather manage it yourself.
#
# Usage:
#   ./run-local.sh [options]
#
# Options:
#   --zip <path>       Use this WSO2 IS distribution zip
#                      (default: newest modules/distribution/target/wso2is-*.zip)
#   --build            Build the IS distribution from source first
#                      (mvn clean install -Dmaven.test.skip=true)
#   --conformance      Also start the resource server + conformance suite + tests
#                      (needs Docker, nginx, and sudo)
#   --suite-branch <b> Conformance suite branch (default: latest release branch)
#   --skip-hosts       Do not touch /etc/hosts; assume "iam" already resolves
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
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"   # .../oidc-fapi-conformance-tests
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"                    # product-is root

# --------------------------------------------------------------------------- #
# Defaults / arg parsing
# --------------------------------------------------------------------------- #
IS_ZIP="${IS_ZIP:-}"
DO_BUILD="no"
DO_CONFORMANCE="no"
SUITE_BRANCH=""
SKIP_HOSTS="no"
WORK=""
KEEP="no"

log()  { printf '\n\033[1;34m>>> %s\033[0m\n' "$*"; }
info() { printf '    %s\n' "$*"; }
err()  { printf '\033[1;31m!!! %s\033[0m\n' "$*" >&2; }
die()  { err "$*"; exit 1; }

while [[ $# -gt 0 ]]; do
  case "$1" in
    --zip)          IS_ZIP="${2:?}"; shift 2 ;;
    --build)        DO_BUILD="yes"; shift ;;
    --conformance)  DO_CONFORMANCE="yes"; shift ;;
    --suite-branch) SUITE_BRANCH="${2:?}"; shift 2 ;;
    --skip-hosts)   SKIP_HOSTS="yes"; shift ;;
    --work)         WORK="${2:?}"; shift 2 ;;
    --keep)         KEEP="yes"; shift ;;
    -h|--help)      sed -n '2,59p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *)              die "Unknown option: $1 (use --help)" ;;
  esac
done

[[ -z "$WORK" ]] && WORK="$(mktemp -d "${TMPDIR:-/tmp}/wso2is-fapi-local.XXXXXX")"
mkdir -p "$WORK"
CONF_LOG="$WORK/configure_is_fapi.log"
RS_LOG="$WORK/resource-server.log"

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
RS_PID=""
STARTED_NGINX="no"

cleanup() {
  local rc=$?
  if [[ "$KEEP" == "yes" ]]; then
    log "--keep set: leaving server/resource-server running and work dir at:"; info "$WORK"
    return $rc
  fi
  log "Cleanup: stopping server(s) and removing work dir"
  # graceful IS stop
  for d in "$WORK"/product-is/oidc-fapi-conformance-tests/wso2is-*; do
    [[ -x "$d/bin/wso2server.sh" ]] && "$d/bin/wso2server.sh" stop >/dev/null 2>&1 || true
  done
  pkill -f "$WORK/product-is/oidc-fapi-conformance-tests/wso2is-" 2>/dev/null || true
  # resource server (Flask) we started
  [[ -n "$RS_PID" ]] && kill "$RS_PID" 2>/dev/null || true
  # only stop nginx if we're the ones who started it
  if [[ "$STARTED_NGINX" == "yes" ]]; then
    if [[ "$(uname -s)" == "Darwin" ]]; then
      brew services stop nginx >/dev/null 2>&1 || true
    else
      sudo service nginx stop >/dev/null 2>&1 || true
    fi
  fi
  rm -rf "$WORK" 2>/dev/null || true
  return $rc
}
trap cleanup EXIT

# Refuse to run if something is already bound to 9443 (would confuse configure_is_fapi).
if command -v lsof >/dev/null 2>&1 && lsof -nP -iTCP:9443 -sTCP:LISTEN >/dev/null 2>&1; then
  die "Port 9443 is already in use. Stop the running IS first."
fi

# --------------------------------------------------------------------------- #
# Step: Ensure "iam" resolves to this machine
# --------------------------------------------------------------------------- #
# The FAPI server is configured with hostname "iam" (see config/deployment-
# fapi-config.toml) and configure_is_fapi.py / constants_fapi.py talk to
# https://iam:9443 and https://iam/resource unconditionally, so this is
# required even without --conformance.
log "Step: Ensure 'iam' resolves locally"
if [[ "$SKIP_HOSTS" == "yes" ]]; then
  info "--skip-hosts set: assuming 'iam' already resolves."
elif python3 -c "import socket; socket.gethostbyname('iam')" >/dev/null 2>&1; then
  info "'iam' already resolves."
else
  info "Adding '127.0.0.1 iam www.iam.com' to /etc/hosts (requires sudo)..."
  echo "127.0.0.1 iam www.iam.com" | sudo tee -a /etc/hosts >/dev/null \
    || die "Failed to update /etc/hosts. Re-run with --skip-hosts and add the entry yourself."
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
# Step: Setup Python venv (deps configure_is_fapi.py needs, as the workflow
# pip-installs)
# --------------------------------------------------------------------------- #
log "Step: Setup Python"
VENV="$WORK/venv"
python3 -m venv "$VENV" || die "Failed to create venv"
PY="$VENV/bin/python3"
"$PY" -m pip install --quiet --upgrade pip >/dev/null
"$PY" -m pip install --quiet psutil requests httpx httplib2 PyJWT cryptography \
  || die "pip install failed"
info "venv ready: $("$PY" -c 'import psutil,requests;print("psutil",psutil.__version__,"requests",requests.__version__)')"

# --------------------------------------------------------------------------- #
# Step: Download + extract Jacoco  (fallback: bundled copy from the OIDC dir)
# --------------------------------------------------------------------------- #
log "Step: Download Jacoco agent"
JACOCO_ZIP="$WORK/jacoco-0.8.12.zip"
JACOCO_DIR="$WORK/jacoco-0.8.12"
if curl -fsSL -o "$JACOCO_ZIP" \
     "https://search.maven.org/remotecontent?filepath=org/jacoco/jacoco/0.8.12/jacoco-0.8.12.zip" \
     2>/dev/null && [[ -s "$JACOCO_ZIP" ]]; then
  mkdir -p "$JACOCO_DIR"
  unzip -qq "$JACOCO_ZIP" -d "$JACOCO_DIR"
  info "Downloaded and extracted jacoco-0.8.12"
elif [[ -d "$REPO_ROOT/oidc-conformance-tests/jacoco-0.8.12/lib" ]]; then
  info "Download failed; using bundled jacoco-0.8.12/ from oidc-conformance-tests"
  JACOCO_DIR="$REPO_ROOT/oidc-conformance-tests/jacoco-0.8.12"
else
  die "Could not obtain jacoco-0.8.12 (no network and no bundled copy)"
fi
[[ -f "$JACOCO_DIR/lib/jacocoagent.jar" ]] || die "jacocoagent.jar not found under $JACOCO_DIR"

# --------------------------------------------------------------------------- #
# Recreate the workflow's directory layout inside WORK:
#   WORK/                              (== the dir configure_is_fapi.py's ../.. resolves to)
#     <IS zip, patched>, jacoco.exec
#     product-is/oidc-fapi-conformance-tests/   (copy of this repo dir)
# configure_is_fapi.py is invoked from product-is/oidc-fapi-conformance-tests
# with the zip referenced as ../../<zip>, exactly as the workflow does.
# --------------------------------------------------------------------------- #
log "Step: Prepare work layout"
RUN_DIR="$WORK/product-is/oidc-fapi-conformance-tests"
mkdir -p "$RUN_DIR"
# Copy the tests dir, excluding: the resource-server's checked-in venv,
# __pycache__, this script, and any leftover extracted server dirs (wso2is-*).
( cd "$SCRIPT_DIR" && \
  find . -maxdepth 1 -mindepth 1 \
       ! -name '__pycache__' ! -name 'wso2is-*' ! -name 'run-local.sh' \
       -exec cp -R {} "$RUN_DIR/" \; )
rm -rf "$RUN_DIR/resource-server/venv"
cp "$IS_ZIP" "$WORK/$ZIP_NAME"
info "Work dir: $WORK"

# --------------------------------------------------------------------------- #
# Step: Patch deployment.toml + jacoco agent into the IS zip
# --------------------------------------------------------------------------- #
# configure_is_fapi.py (unlike configure_is.py) does not patch deployment.toml
# or wire up the jacoco agent itself - it just unpacks and starts the server.
# The workflow does this in a separate "Add deployment toml configs" step
# before invoking it; we replicate that here.
log "Step: Patch deployment.toml + jacoco agent into the IS zip"
( cd "$WORK" \
    && unzip -qq "$ZIP_NAME" \
    && dir_name="$(basename "$(find . -maxdepth 1 -mindepth 1 -type d -name 'wso2is*' | head -1)")" \
    && [[ -n "$dir_name" ]] \
    && cp -f "$RUN_DIR/config/deployment-fapi-config.toml" "$dir_name/repository/conf/deployment.toml" \
    && touch "$WORK/jacoco.exec" \
    && sed -i.bak '/-Dwso2.server.standalone=true \\/a\
-javaagent:'"$JACOCO_DIR"'/lib/jacocoagent.jar=destfile='"$WORK"'/jacoco.exec,append=true,includes=org.wso2.carbon.idp.mgt*:org.wso2.carbon.sts*:org.wso2.carbon.user.core*:org.wso2.carbon.user.mgt*:org.wso2.carbon.claim.mgt*:org.wso2.carbon.identity.*:org.wso2.carbon.xkms.mgt* \\' \
       "$dir_name/bin/wso2server.sh" \
    && rm -f "$dir_name/bin/wso2server.sh.bak" \
    && zip -qq -r "$ZIP_NAME" "$dir_name" \
    && rm -rf "$dir_name" ) \
  || die "Failed to patch deployment.toml/jacoco agent into $ZIP_NAME"
info "Patched zip ready: $WORK/$ZIP_NAME"

# --------------------------------------------------------------------------- #
# Step: Run IS  (configure_is_fapi.py)
# --------------------------------------------------------------------------- #
log "Step: Run IS (configure_is_fapi.py)"
# Same rationale as the OIDC harness: configure_is_fapi.py starts the WSO2
# server as a long-lived child; write to a file and tail it rather than piping
# directly, or `tee` would hang after the python process exits (the still-
# running daemon holds the pipe open). -u keeps python unbuffered.
( cd "$RUN_DIR" && "$PY" -u ./configure_is_fapi.py "../../$ZIP_NAME" ) \
  >"$CONF_LOG" 2>&1 &
CONF_PID=$!
tail -f "$CONF_LOG" 2>/dev/null & TAIL_PID=$!
wait "$CONF_PID"; RUN_IS_RC=$?
sleep 1                                    # let tail flush the final lines
kill "$TAIL_PID" 2>/dev/null; wait "$TAIL_PID" 2>/dev/null || true

echo
if [[ $RUN_IS_RC -ne 0 ]]; then
  err "configure_is_fapi.py FAILED (exit $RUN_IS_RC) — this is the reproduction."
  echo "----- last 25 lines of configure_is_fapi.log -----"
  tail -25 "$CONF_LOG"
  echo "---------------------------------------------------"
  info "Full log: $CONF_LOG"
  info "Server log: $RUN_DIR/wso2is-*/repository/logs/wso2carbon.log"
  info "Re-run with --keep to inspect the running server."
  exit $RUN_IS_RC
fi
log "configure_is_fapi.py completed successfully — IS is configured for FAPI conformance tests."

# --------------------------------------------------------------------------- #
# Step: FAPI resource server + Conformance Suite + Tests  (optional; needs Docker + nginx)
# --------------------------------------------------------------------------- #
if [[ "$DO_CONFORMANCE" == "yes" ]]; then
  command -v docker >/dev/null 2>&1 || die "--conformance needs Docker installed"
  command -v docker-compose >/dev/null 2>&1 || die "--conformance needs docker-compose installed"
  command -v jq >/dev/null 2>&1 || die "--conformance needs jq installed"

  # ------------------------------------------------------------------------- #
  # Step: Start FAPI resource server (nginx TLS proxy on :443 -> Flask on :5002)
  # ------------------------------------------------------------------------- #
  log "Step: Start FAPI resource server"
  RS_DIR="$RUN_DIR/resource-server"
  RS_VENV="$WORK/rs-venv"
  python3 -m venv "$RS_VENV" || die "Failed to create resource-server venv"
  RS_PY="$RS_VENV/bin/python3"
  "$RS_PY" -m pip install --quiet --upgrade pip >/dev/null
  "$RS_PY" -m pip install --quiet -r "$RS_DIR/requirements.txt" || die "resource-server pip install failed"

  if command -v nginx >/dev/null 2>&1; then
    info "nginx already installed"
  else
    STARTED_NGINX="yes"
    if [[ "$(uname -s)" == "Darwin" ]]; then
      command -v brew >/dev/null 2>&1 || die "nginx not found and Homebrew unavailable to install it"
      info "Installing nginx via Homebrew..."
      brew install nginx || die "brew install nginx failed"
    else
      info "Installing nginx via apt..."
      sudo apt update && sudo apt install -y nginx || die "apt install nginx failed"
    fi
  fi

  SSL_DIR="$WORK/nginx-ssl"
  mkdir -p "$SSL_DIR"
  openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout "$SSL_DIR/serverCA.key" -out "$SSL_DIR/serverCA.crt" \
    -subj "/C=US/ST=California/L=San Francisco/O=My Company/OU=IT Department/CN=mycompany.com" \
    2>/dev/null || die "Failed to generate self-signed cert"

  # render the bundled nginx-proxy conf with our SSL paths + resource-server port
  NGINX_CONF="$WORK/fapi-resource-nginx.conf"
  sed -e "s#/etc/nginx/ssl/serverCA.crt#$SSL_DIR/serverCA.crt#" \
      -e "s#/etc/nginx/ssl/serverCA.key#$SSL_DIR/serverCA.key#" \
      "$RS_DIR/nginx-proxy" > "$NGINX_CONF"

  if [[ "$(uname -s)" == "Darwin" ]]; then
    NGINX_ETC="$(brew --prefix)/etc/nginx"
    mkdir -p "$NGINX_ETC/servers"
    cp "$NGINX_CONF" "$NGINX_ETC/servers/fapi-resource.conf" \
      || die "Failed to install nginx server conf (Homebrew nginx 'servers/' dir)"
    STARTED_NGINX="yes"
    brew services restart nginx || die "Failed to (re)start nginx"
  else
    sudo cp "$NGINX_CONF" /etc/nginx/sites-enabled/fapi-resource-nginx.conf \
      || die "Failed to install nginx site conf"
    sudo nginx -t || die "nginx config test failed"
    STARTED_NGINX="yes"
    sudo service nginx restart || die "Failed to (re)start nginx"
  fi
  info "nginx TLS proxy listening on :443 -> localhost:5002"

  info "Starting Flask resource server (port 5002)..."
  ( cd "$RS_DIR" && "$RS_PY" resource-server.py ) >"$RS_LOG" 2>&1 &
  RS_PID=$!
  sleep 3
  kill -0 "$RS_PID" 2>/dev/null || { cat "$RS_LOG"; die "resource-server.py failed to start (see log above)"; }
  info "resource-server.py running (pid $RS_PID), log: $RS_LOG"

  # ------------------------------------------------------------------------- #
  # Step: Clone conformance suite
  # ------------------------------------------------------------------------- #
  log "Step: Clone conformance suite"
  if [[ -z "$SUITE_BRANCH" ]]; then
    SUITE_BRANCH="$(curl -s https://gitlab.com/api/v4/projects/4175605/releases/ \
                    | jq -r '.[0].name')"
  fi
  info "Conformance suite branch: $SUITE_BRANCH"
  ( cd "$WORK" && git clone --depth 1 --branch "$SUITE_BRANCH" \
      https://gitlab.com/openid/conformance-suite.git ) || die "suite clone failed"

  # add extra_hosts so the containers can reach the host IS + resource server.
  # (uses awk, not "sed -i ... i \...\n...", because BSD/macOS sed treats \n in an
  # i-command as a literal two-char escape, not a newline, and errors out --
  # which silently no-ops here since the caller only had `|| true` around it)
  #
  # Maps to host.docker.internal's actual resolved IP, not the host's real LAN
  # IP and not Docker Compose's "host-gateway" alias: on Docker Desktop for
  # Mac, a container connecting to the host's own LAN IP hits a "hairpin NAT"
  # path that corrupts larger packets (e.g. a multi-extension TLS
  # ClientHello), surfacing as a mid-handshake "unexpected eof"/decode-error
  # TLS failure -- and "host-gateway" resolves to the *default bridge's*
  # gateway (typically 172.17.0.1), which isn't host-reachable at all here.
  # host.docker.internal's own resolved address is the one path confirmed to
  # avoid both problems, but extra_hosts needs a literal IP, so resolve it
  # via a throwaway container first (Docker Desktop injects that DNS entry
  # into every container automatically). host.docker.internal is a Docker
  # Desktop (Mac/Windows) feature, not native Docker Engine on Linux, so fall
  # back to a real host IP there.
  HOST_IP="$(docker run --rm alpine getent hosts host.docker.internal 2>/dev/null | awk '{print $1}')"
  if [[ -z "$HOST_IP" ]]; then
    if command -v ip >/dev/null 2>&1; then
      HOST_IP="$(ip -o -4 addr list 2>/dev/null | awk '$2!="lo"{print $4}' | cut -d/ -f1 | head -1)"
    else
      HOST_IP="$(ipconfig getifaddr en0 2>/dev/null)"
    fi
  fi
  [[ -n "$HOST_IP" ]] || die "Could not determine a host IP reachable from containers"
  info "Host reachable at: $HOST_IP"
  DC="$WORK/conformance-suite/docker-compose.yml"
  awk -v ip="$HOST_IP" '
    /^    volumes:/ {
      print "    extra_hosts:"
      print "    - \"localhost:" ip "\""
      print "    - \"iam:" ip "\""
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
  ( cd "$WORK/conformance-suite" && chmod 777 docker-compose.yml \
      && DOCKER_BUILDKIT=0 docker-compose -f docker-compose.yml up -d --build ) \
    || die "conformance suite start failed"

  info "Waiting for conformance suite server to start..."
  suite_ready=""
  for _ in $(seq 1 60); do
    if ( cd "$WORK/conformance-suite" && docker-compose -f docker-compose.yml logs 2>/dev/null | grep -q "Starting application" ); then
      suite_ready="yes"
      break
    fi
    if ( cd "$WORK/conformance-suite" && docker-compose -f docker-compose.yml ps 2>/dev/null | grep -qi "exit\|exited" ); then
      die "Conformance suite container exited unexpectedly"
    fi
    sleep 5
  done
  [[ -n "$suite_ready" ]] || die "Conformance suite did not report ready within timeout"
  info "Conformance suite started."

  log "Step: Run tests"
  ( cd "$WORK" && PATH="$VENV/bin:$PATH" bash "$RUN_DIR/test_runner_fapi.sh" )

  log "Step: Export results"
  if ( cd "$WORK" && "$PY" "$RUN_DIR/export_results_fapi.py" https://localhost:8443 ); then
    log "ALL TEST CASES PASSED"
  else
    err "Failed test cases found."
  fi
fi

log "Done. Work dir: $WORK"
