#!/bin/bash

cd ../
ROOT_PATH="$(cd "$(dirname "$0")" && pwd)"
TEST_DIR=$(cd "$WORK_DIR")
cd "src/test"
(
  found_nonempty=''
  for file in cypress.json ; do
    if [[ -s "$file" ]] ; then
      found_nonempty=1
    fi
  done
  if [[ "$found_nonempty" ]] ; then
    echo "$file File is available"
    cypress cache clear
    sleep 25
    npm run test
  else
    echo "$file File is not available"
    exit 1
  fi
)

