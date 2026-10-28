#!/usr/bin/env bash
# Checks (or removes) the root CAs distrusted after the eMudhra CA compromise
# (Mozilla Bugzilla 2083449) from one or more JKS/PKCS12 trust stores.
#
# Usage: distrusted-cas.sh [--fix] <storepass> <truststore>...
#   default: exit 1 if any store still contains a distrusted root.
#   --fix:   delete the matching entries in place.

set -euo pipefail

PATTERN='AAA Certificate Services|emSign|eMudhra'

fix=false
if [[ "${1:-}" == "--fix" ]]; then
  fix=true
  shift
fi
if [[ $# -lt 2 ]]; then
  echo "Usage: $0 [--fix] <storepass> <truststore>..." >&2
  exit 2
fi
storepass=$1
shift

status=0
for store in "$@"; do
  aliases=$(keytool -list -v -keystore "$store" -storepass "$storepass" 2>/dev/null |
    awk -v pat="$PATTERN" '/^Alias name:/ { a = $0; sub(/^Alias name: /, "", a) } /^Owner:/ && $0 ~ pat { print a }')
  if [[ -z "$aliases" ]]; then
    echo "OK    $store"
    continue
  fi
  if $fix; then
    while IFS= read -r alias; do
      keytool -delete -keystore "$store" -storepass "$storepass" -alias "$alias" >/dev/null 2>&1
      echo "REMOVED $alias from $store"
    done <<<"$aliases"
  else
    echo "FOUND $store:" >&2
    sed 's/^/        /' <<<"$aliases" >&2
    status=1
  fi
done
exit $status
