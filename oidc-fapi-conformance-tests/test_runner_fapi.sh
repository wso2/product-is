#!/bin/bash +x

CONFORMANCE_SUITE_PATH=./conformance-suite
PATH_TO_SCRIPTS=./product-is/oidc-fapi-conformance-tests
IS_SUCCESSFUL=false

#  sed -i '/^.*all_test_modules\ =.*/a \ \ \ \ print("All available OIDC test modules:")\n\ \ \ \ print("==============================================")\n\ \ \ \ print(sorted(all_test_modules.keys()))' $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py

echo
echo "FAPI Basic test plan - private_key_jwt, plain_fapi, plain_response, by_value"
echo "-----------------------------"
echo
python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py fapi1-advanced-final-test-plan[client_auth_type=private_key_jwt][fapi_profile=plain_fapi][fapi_response_mode=plain_response][fapi_auth_request_method=by_value] $PATH_TO_SCRIPTS/config/IS_config_fapi.json 2>&1 | tee fapi-testplan-pvtkeyjwt-log.txt
echo

echo
echo "FAPI Basic test plan - mtls, plain_fapi, plain_response, by_value"
echo "-----------------------------"
echo
python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py fapi1-advanced-final-test-plan[client_auth_type=mtls][fapi_profile=plain_fapi][fapi_response_mode=plain_response][fapi_auth_request_method=by_value] $PATH_TO_SCRIPTS/config/IS_config_fapi.json 2>&1 | tee fapi-testplan-mtls-log.txt
echo