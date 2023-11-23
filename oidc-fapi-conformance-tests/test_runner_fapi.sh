#!/bin/bash +x

CONFORMANCE_SUITE_PATH=./conformance-suite
PATH_TO_SCRIPTS=./product-is/oidc-fapi-conformance-tests
IS_SUCCESSFUL=false

#  sed -i '/^.*all_test_modules\ =.*/a \ \ \ \ print("All available OIDC test modules:")\n\ \ \ \ print("==============================================")\n\ \ \ \ print(sorted(all_test_modules.keys()))' $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py

echo "========================"
echo "Running Tests"
echo "========================"

echo
echo "FAPI Basic test plan - private_key_jwt, plain_fapi, plain_response, by_value"
echo "-----------------------------"
echo
python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py fapi1-advanced-final-test-plan[client_auth_type=private_key_jwt][fapi_profile=plain_fapi][fapi_response_mode=plain_response][fapi_auth_request_method=by_value] $PATH_TO_SCRIPTS/config/IS_config_fapi_pvtkeyjwt.json 2>&1 | tee fapi-testplan-pvtkeyjwt-log.txt
echo

echo
echo "FAPI Basic test plan - mtls, plain_fapi, plain_response, by_value"
echo "-----------------------------"
echo
python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py fapi1-advanced-final-test-plan[client_auth_type=mtls][fapi_profile=plain_fapi][fapi_response_mode=plain_response][fapi_auth_request_method=by_value] $PATH_TO_SCRIPTS/config/IS_config_fapi_mtls.json 2>&1 | tee fapi-testplan-mtls-log.txt
echo

# echo
# echo "FAPI Basic test plan - private_key_jwt, plain_fapi, plain_response, pushed"
# echo "-----------------------------"
# echo
# python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py fapi1-advanced-final-test-plan[client_auth_type=private_key_jwt][fapi_profile=plain_fapi][fapi_response_mode=plain_response][fapi_auth_request_method=pushed] $PATH_TO_SCRIPTS/config/IS_config_fapi_pvtkeyjwt_par.json 2>&1 | tee fapi-testplan-pvtkeyjwt-par-log.txt
# echo

# echo
# echo "FAPI Basic test plan - mtls, plain_fapi, plain_response, pushed"
# echo "-----------------------------"
# echo
# python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py fapi1-advanced-final-test-plan[client_auth_type=mtls][fapi_profile=plain_fapi][fapi_response_mode=plain_response][fapi_auth_request_method=pushed] $PATH_TO_SCRIPTS/config/IS_config_fapi_mtls_par.json 2>&1 | tee fapi-testplan-mtls-par-log.txt
# echo

# echo
# echo "FAPI Basic test plan - private_key_jwt, plain_fapi, jarm, by_value"
# echo "-----------------------------"
# echo
# python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py fapi1-advanced-final-test-plan[client_auth_type=private_key_jwt][fapi_profile=plain_fapi][fapi_response_mode=jarm][fapi_auth_request_method=by_value] $PATH_TO_SCRIPTS/config/IS_config_fapi_pvtkeyjwt.json 2>&1 | tee fapi-testplan-pvtkeyjwt-jarm-log.txt
# echo

# echo
# echo "FAPI Basic test plan - mtls, plain_fapi, jarm, by_value"
# echo "-----------------------------"
# echo
# python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py fapi1-advanced-final-test-plan[client_auth_type=mtls][fapi_profile=plain_fapi][fapi_response_mode=jarm][fapi_auth_request_method=by_value] $PATH_TO_SCRIPTS/config/IS_config_fapi_mtls.json 2>&1 | tee fapi-testplan-mtls-jarm-log.txt
# echo