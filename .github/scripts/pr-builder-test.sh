#!/bin/bash +x
#
# Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
#
# WSO2 LLC. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#

# This script runs integration tests against a pre-built product-is directory.
# It expects the product-is source tree (already built without tests) to be
# present at product-is-build/, extracted from the build job artifact.
# It copies the pre-built tree to product-is-$BUILDER_NUMBER/ (one copy per
# runner) and then runs only the integration test module.

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
    "is-test-session-mgt"
    "is-tests-password-update-api"
)

# Function to disable tests not in the enabled list.
disable_tests() {
    local enabled_tests=$1
    local testng_path="product-is-$BUILDER_NUMBER/modules/integration/tests-integration/tests-backend/src/test/resources/testng.xml"

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

# Function to get expected BUILD SUCCESS count based on enabled tests.
get_expected_build_success_count() {
    local enabled_tests=$1
    # If is-test-adaptive-authentication-nashorn is enabled, expect 17 BUILD SUCCESS messages.
    if [[ "$enabled_tests" == *"is-test-adaptive-authentication-nashorn"* ]]; then
        echo "17"
    else
        echo "1"
    fi
}

# Main execution starts here.
BUILDER_NUMBER=$1
ENABLED_TESTS=$2

echo ""
echo "=========================================================="
PR_LINK=${PR_LINK%/}
JAVA_21_HOME=${JAVA_21_HOME%/}
echo "    Running test runner $BUILDER_NUMBER"
echo "    PR_LINK: $PR_LINK"
echo "    JAVA 21 Home: $JAVA_21_HOME"
echo "    Enabled tests: $ENABLED_TESTS"
echo "=========================================================="

# Each runner gets its own copy of the pre-built product-is directory so that
# the per-runner testng.xml edits (disable_tests) do not interfere with each
# other when runners share an agent (they don't in this workflow, but it keeps
# things safe and consistent with the original naming convention).
echo "Copying pre-built product-is to product-is-$BUILDER_NUMBER..."
cp -r product-is-build product-is-$BUILDER_NUMBER

disable_tests "$ENABLED_TESTS"

echo ""
echo "Running integration tests for runner $BUILDER_NUMBER..."
echo "=========================================================="
cd product-is-$BUILDER_NUMBER

export JAVA_HOME=$JAVA_21_HOME

# Run only the integration test module; everything else is already compiled and
# installed in the local .m2 cache from the build job.
mvn install -pl modules/integration -am --batch-mode | tee mvn-build.log

PR_BUILD_STATUS=$(cat mvn-build.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$')
PR_TEST_RESULT=$(sed -n -e '/\[INFO\] Results:/,/\[INFO\] Tests run:/ p' mvn-build.log)

PR_BUILD_FINAL_RESULT=$(
  echo "==========================================================="
  echo "product-is TEST RUNNER $BUILDER_NUMBER BUILD $PR_BUILD_STATUS"
  echo "=========================================================="
  echo ""
  echo "$PR_TEST_RESULT"
)

PR_BUILD_RESULT_LOG_TEMP=$(echo "$PR_BUILD_FINAL_RESULT" | sed 's/$/%0A/')
PR_BUILD_RESULT_LOG=$(echo $PR_BUILD_RESULT_LOG_TEMP)
echo "::warning::$PR_BUILD_RESULT_LOG"

PR_BUILD_SUCCESS_COUNT=$(grep -o -i "\[INFO\] BUILD SUCCESS" mvn-build.log | wc -l)
EXPECTED_BUILD_SUCCESS_COUNT=$(get_expected_build_success_count "$ENABLED_TESTS")
if [ "$PR_BUILD_SUCCESS_COUNT" != "$EXPECTED_BUILD_SUCCESS_COUNT" ]; then
  echo "PR TEST BUILD not successful. Aborting."
  echo "::error::PR TEST BUILD not successful. Check artifacts for logs."
  exit 1
fi

echo ""
echo "=========================================================="
echo "Test runner $BUILDER_NUMBER completed"
echo "=========================================================="
echo ""

