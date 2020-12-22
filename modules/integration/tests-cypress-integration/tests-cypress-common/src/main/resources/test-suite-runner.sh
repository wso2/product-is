#!/bin/sh

# ----------------------------------------------------------------------------
#  Copyright 2020 WSO2, Inc. http://www.wso2.org
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

cd "test-classes/tests" || exit
(
  found_nonempty=''
  for file in cypress.json ; do
    if [[ -s "$file" ]] ; then
      found_nonempty=1
    fi
  done
  if [ "$found_nonempty" ] ; then
    echo "Running identity apps cypress integration tests..."
    cypress cache clear
    sleep 25
    npm run cy:headless
  else
    echo "Failed to find the $file file."
    exit 1
  fi
)
