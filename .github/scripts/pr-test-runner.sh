#!/bin/bash +x

# Define all available tests.
declare -a ALL_TESTS=(
    "is-tests-default-configuration"
    "is-test-rest-api"
    "is-test-webhooks"
    "is-tests-scim2"
    "is-test-adaptive-authentication"
    "is-test-adaptive-authentication-nashorn"
    "is-test-adaptive-authentication-nashorn-with-restart"
    "is-tests-default-configuration-ldap"
    "is-tests-uuid-user-store"
    "is-tests-federation"
    "is-tests-federation-restart"
    "is-tests-jdbc-userstore"
    "is-tests-read-only-userstore"
    "is-tests-oauth-jwt-token-gen-enabled"
    "is-tests-email-username"
    "is-tests-with-individual-configuration-changes"
    "is-tests-saml-query-profile"
    "is-tests-default-encryption"
)

# Function to disable tests not in the enabled list.
disable_tests() {
    local enabled_tests=$1
    local testng_path="product-is-$RUNNER_NUMBER/modules/integration/tests-integration/tests-backend/src/test/resources/testng.xml"

    # Convert comma-separated string to array.
    IFS=',' read -ra ENABLED_ARRAY <<< "$enabled_tests"

    echo "Tests that will run:"
    printf '%s\n' "${ENABLED_ARRAY[@]}"

    echo -e "\nDisabling other tests:"
    for test in "${ALL_TESTS[@]}"; do
        if [[ ! " ${ENABLED_ARRAY[@]} " =~ " ${test} " ]]; then
            echo "- Disabling: $test"
            sed -i.bak "s/name=\"$test\"/& enabled=\"false\"/" "$testng_path"
        fi
    done
}

# Main execution starts here.
RUNNER_NUMBER=$1
ENABLED_TESTS=$2

echo ""
echo "=========================================================="
echo "    Running Integration Tests"
echo "    Runner: $RUNNER_NUMBER"
echo "    Tests: $ENABLED_TESTS"
echo "=========================================================="
echo ""

disable_tests "$ENABLED_TESTS"

cd product-is-$RUNNER_NUMBER

echo ""
echo "Running integration tests..."
echo "=========================================================="

export JAVA_HOME=$JAVA_11_HOME
mvn clean verify --batch-mode -Dmaven.test.failure.ignore=true | tee mvn-test.log

TEST_STATUS=$(cat mvn-test.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$')
TEST_RESULT=$(sed -n -e '/\[INFO\] Results:/,/\[INFO\] Tests run:/ p' mvn-test.log)

TEST_FINAL_RESULT=$(
  echo "==========================================================="
  echo "Test Runner $RUNNER_NUMBER - BUILD $TEST_STATUS"
  echo "=========================================================="
  echo ""
  echo "$TEST_RESULT"
)

TEST_RESULT_LOG_TEMP=$(echo "$TEST_FINAL_RESULT" | sed 's/$/%0A/')
TEST_RESULT_LOG=$(echo $TEST_RESULT_LOG_TEMP)
echo "::warning::$TEST_RESULT_LOG"

TEST_SUCCESS_COUNT=$(grep -o -i "\[INFO\] BUILD SUCCESS" mvn-test.log | wc -l)
if [ "$TEST_SUCCESS_COUNT" != "1" ]; then
  echo "Tests not successful. Check logs for details."
  echo "::error::Tests not successful for runner $RUNNER_NUMBER. Check artifacts for logs."
  exit 1
fi

echo ""
echo "=========================================================="
echo "Tests completed successfully"
echo "=========================================================="
echo ""
