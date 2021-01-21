#!/bin/bash +x
CONFORMANCE_SUITE_URL=https://localhost:8443
IS_SUCCESSFUL=false
echo "========================"
echo "Running Tests"
echo "========================"
echo
echo "Basic certification test plan"
echo "-----------------------------"
echo
sudo python3 ./conformance-suite/scripts/run-test-plan.py oidcc-basic-certification-test-plan[server_metadata=static][client_registration=static_client] ./wso2-identity-server-automated-config/Basic_test_plan/IS_config_basic.json 2>&1 | tee basic-certification-test-plan-log.txt
echo
echo "Implicit certification test plan"
echo "-----------------------------"
echo
sudo python3 ./conformance-suite/scripts/run-test-plan.py oidcc-implicit-certification-test-plan[server_metadata=static][client_registration=static_client] ./wso2-identity-server-automated-config/Implicit_test_plan/IS_config_implicit.json 2>&1 | tee implicit-certification-test-plan-log.txt
echo
echo "Hybrid certification test plan"
echo "-----------------------------"
echo
sudo python3 ./conformance-suite/scripts/run-test-plan.py oidcc-hybrid-certification-test-plan[server_metadata=static][client_registration=static_client] ./wso2-identity-server-automated-config/Hybrid_test_plan/IS_config_hybrid.json 2>&1 | tee hybrid-certification-test-plan-log.txt
echo
echo "Formpost basic certification test plan"
echo "-----------------------------"
echo
sudo python3 ./conformance-suite/scripts/run-test-plan.py oidcc-formpost-basic-certification-test-plan[server_metadata=static][client_registration=static_client] ./wso2-identity-server-automated-config/Formpost_basic_test_plan/IS_config_formpost_basic.json 2>&1 | tee formpost-basic-certification-test-plan-log.txt
echo
echo "Formpost implicit certification test plan"
echo "-----------------------------"
echo
sudo python3 ./conformance-suite/scripts/run-test-plan.py oidcc-formpost-implicit-certification-test-plan[server_metadata=static][client_registration=static_client] ./wso2-identity-server-automated-config/Formpost_implicit_test_plan/IS_config_formpost_implicit.json 2>&1 | tee formpost-implicit-certification-test-plan-log.txt
echo
echo "Formpost hybrid certification test plan"
echo "-----------------------------"
echo
sudo python3 ./conformance-suite/scripts/run-test-plan.py oidcc-formpost-hybrid-certification-test-plan[server_metadata=static][client_registration=static_client] ./wso2-identity-server-automated-config/Formpost_hybrid_test_plan/IS_config_formpost_hybrid.json 2>&1 | tee formpost-hybrid-certification-test-plan-log.txt
echo
if sudo python3 ./wso2-identity-server-automated-config/export_results.py $CONFORMANCE_SUITE_URL

then
  IS_SUCCESSFUL=true
fi

if $IS_SUCCESSFUL
then
  exit 0
else
	exit 1
fi
