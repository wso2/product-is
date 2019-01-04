#!/bin/bash

# Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -o xtrace
DIR=$2
export DATA_BUCKET_LOCATION=${DIR}

# Build scenarios-commons
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
-fae -B -f ../pom.xml

while [[ $# -gt 0 ]]; do
  case "$1" in
    --mvn_opts=*)
      MVN_OPTS="${1#*=}"
      ;;
  esac
  shift
done

mvn clean install ${MVN_OPTS}

echo "Copying surefire-reports to data bucket"

BASE_SCENARIO=2-single-sign-on

#2.1.1.1-sso-with-with-oauth
TEST_SCENARIO_DIR=2.1-login/2.1.1-single-protocol-single-sign-on/2.1.1.1-sso-with-with-oauth
mkdir -p ${DIR}/${BASE_SCENARIO}/${TEST_SCENARIO_DIR}
cp -r ${TEST_SCENARIO_DIR}/target/surefire-reports ${DIR}/${BASE_SCENARIO}/${TEST_SCENARIO_DIR}

ls ${DIR}