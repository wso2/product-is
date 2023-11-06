#!/bin/bash +x
# ----------------------------------------------------------------------------
#  Copyright 2021 WSO2, Inc. http://www.wso2.org
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


CONFORMANCE_SUITE_PATH=./conformance-suite
PATH_TO_SCRIPTS=./product-is/oidc-conformance-tests
IS_SUCCESSFUL=false
IS_LOCAL=false

if $IS_LOCAL; then
  PATH_TO_SCRIPTS=.
  PRODUCT_IS_ZIP_PATH=./wso2is-5.12.0-m7.zip
  echo "====================="
  echo "Identity Server Setup"
  echo "====================="
  echo
  python3 ./configure_is.py $PRODUCT_IS_ZIP_PATH
fi
echo "========================"
echo "Running Tests"
echo "========================"
echo
echo "Basic certification test plan"
echo "-----------------------------"
echo
python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py oidcc-basic-certification-test-plan[server_metadata=static][client_registration=static_client] $PATH_TO_SCRIPTS/basic/IS_config_basic.json 2>&1 | tee basic-certification-test-plan-log.txt
echo
echo "Implicit certification test plan"
echo "-----------------------------"
echo
python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py oidcc-implicit-certification-test-plan[server_metadata=static][client_registration=static_client] $PATH_TO_SCRIPTS/implicit/IS_config_implicit.json 2>&1 | tee implicit-certification-test-plan-log.txt
echo
echo "Hybrid certification test plan"
echo "-----------------------------"
echo
python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py oidcc-hybrid-certification-test-plan[server_metadata=static][client_registration=static_client] $PATH_TO_SCRIPTS/hybrid/IS_config_hybrid.json 2>&1 | tee hybrid-certification-test-plan-log.txt
echo
echo "Formpost basic certification test plan"
echo "-----------------------------"
echo
python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py oidcc-formpost-basic-certification-test-plan[server_metadata=static][client_registration=static_client] $PATH_TO_SCRIPTS/formpost-basic/IS_config_formpost_basic.json 2>&1 | tee formpost-basic-certification-test-plan-log.txt
echo
echo "Formpost implicit certification test plan"
echo "-----------------------------"
echo
python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py oidcc-formpost-implicit-certification-test-plan[server_metadata=static][client_registration=static_client] $PATH_TO_SCRIPTS/formpost-implicit/IS_config_formpost_implicit.json 2>&1 | tee formpost-implicit-certification-test-plan-log.txt
echo
echo "Formpost hybrid certification test plan"
echo "-----------------------------"
echo
python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py oidcc-formpost-hybrid-certification-test-plan[server_metadata=static][client_registration=static_client] $PATH_TO_SCRIPTS/formpost-hybrid/IS_config_formpost_hybrid.json 2>&1 | tee formpost-hybrid-certification-test-plan-log.txt
echo


if $IS_LOCAL; then
  pkill -f wso2
fi

