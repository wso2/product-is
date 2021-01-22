#!/bin/bash +x

CONFORMANCE_SUITE_URL=https://localhost:8443
CONFORMANCE_SUITE_PATH=./conformance-suite
PATH_TO_SCRIPTS=./product-is/oidc-conformance-tests
IS_SUCCESSFUL=false
IS_LOCAL=false

if $IS_LOCAL; then
  PATH_TO_SCRIPTS=.
  PRODUCT_IS_ZIP_PATH=path
  echo "====================="
  echo "Identity Server Setup"
  echo "====================="
  echo
  sudo python3 ./configure_is.py $PRODUCT_IS_ZIP_PATH
fi
echo "========================"
echo "Running Tests"
echo "========================"
echo
echo "Basic certification test plan"
echo "-----------------------------"
echo
sudo python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py oidcc-basic-certification-test-plan[server_metadata=static][client_registration=static_client] ./product-is/oidc-conformance-tests/basic/IS_config_basic.json 2>&1 | tee basic-certification-test-plan-log.txt
echo
echo "Implicit certification test plan"
echo "-----------------------------"
echo
sudo python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py oidcc-implicit-certification-test-plan[server_metadata=static][client_registration=static_client] ./product-is/oidc-conformance-tests/implicit/IS_config_implicit.json 2>&1 | tee implicit-certification-test-plan-log.txt
echo
echo "Hybrid certification test plan"
echo "-----------------------------"
echo
sudo python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py oidcc-hybrid-certification-test-plan[server_metadata=static][client_registration=static_client] ./product-is/oidc-conformance-tests/hybrid/IS_config_hybrid.json 2>&1 | tee hybrid-certification-test-plan-log.txt
echo
echo "Formpost basic certification test plan"
echo "-----------------------------"
echo
sudo python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py oidcc-formpost-basic-certification-test-plan[server_metadata=static][client_registration=static_client] ./product-is/oidc-conformance-tests/formpost-basic/IS_config_formpost_basic.json 2>&1 | tee formpost-basic-certification-test-plan-log.txt
echo
echo "Formpost implicit certification test plan"
echo "-----------------------------"
echo
sudo python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py oidcc-formpost-implicit-certification-test-plan[server_metadata=static][client_registration=static_client] ./product-is/oidc-conformance-tests/formpost-implicit/IS_config_formpost_implicit.json 2>&1 | tee formpost-implicit-certification-test-plan-log.txt
echo
echo "Formpost hybrid certification test plan"
echo "-----------------------------"
echo
sudo python3 $CONFORMANCE_SUITE_PATH/scripts/run-test-plan.py oidcc-formpost-hybrid-certification-test-plan[server_metadata=static][client_registration=static_client] ./product-is/oidc-conformance-tests/formpost-hybrid/IS_config_formpost_hybrid.json 2>&1 | tee formpost-hybrid-certification-test-plan-log.txt
echo

if sudo python3 $PATH_TO_SCRIPTS/export_results.py $CONFORMANCE_SUITE_URL
then
  IS_SUCCESSFUL=true
fi

if $IS_LOCAL; then
  sudo pkill -f wso2
fi

if $IS_SUCCESSFUL
then
  exit 0
else
	exit 1
fi
