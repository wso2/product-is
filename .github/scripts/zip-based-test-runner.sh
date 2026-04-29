#!/bin/bash +x

# Define workflow branch (selected branch for the workflow run).
# Prefer WORKFLOW_BRANCH (explicit), otherwise GITHUB_REF_NAME, otherwise "master".
WORKFLOW_BRANCH=${WORKFLOW_BRANCH:-${GITHUB_REF_NAME:-master}}
WORKFLOW_BRANCH=${WORKFLOW_BRANCH#refs/heads/}

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

    # If no tests specified, skip disabling and run all tests.
    if [ ${#ENABLED_ARRAY[@]} -eq 0 ] || [ -z "${ENABLED_ARRAY[0]}" ]; then
        echo "No enabled tests specified. Running all tests."
        return
    fi

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
BUILDER_NUMBER=$1
ENABLED_TESTS=$2

echo ""
echo "=========================================================="
ZIP_URL=${ZIP_URL}
JAVA_21_HOME=${JAVA_21_HOME%/}
echo "    ZIP_URL: $ZIP_URL"
echo "    JAVA 21 Home: $JAVA_21_HOME"
echo "    WORKFLOW_BRANCH (product-is): $WORKFLOW_BRANCH"
echo "::warning::Zip-based integration test run started for runner $BUILDER_NUMBER"

echo ""
echo "=========================================================="
echo "Cloning product-is"
echo "=========================================================="

git clone --branch "$WORKFLOW_BRANCH" --single-branch https://github.com/wso2/product-is product-is-$BUILDER_NUMBER

disable_tests "$ENABLED_TESTS"

cd product-is-$BUILDER_NUMBER

echo ""
echo "=========================================================="
echo "Resolving project version..."
echo "=========================================================="
export JAVA_HOME=$JAVA_21_HOME
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout --non-recursive)
echo "    Project version: $VERSION"

echo ""
echo "=========================================================="
echo "Placing zip in modules/distribution/target/..."
echo "=========================================================="
mkdir -p modules/distribution/target
wget -q --output-document="modules/distribution/target/wso2is-${VERSION}.zip" "$ZIP_URL"
if [ $? -ne 0 ]; then
    echo "Failed to download zip from: $ZIP_URL"
    echo "::error::Failed to download zip. Check the ZIP_URL input."
    exit 1
fi
ls -lh "modules/distribution/target/wso2is-${VERSION}.zip"

echo ""
echo "=========================================================="
echo "Verifying zip internal directory matches expected version..."
echo "=========================================================="
ZIP_FILE="modules/distribution/target/wso2is-${VERSION}.zip"
ZIP_INTERNAL_DIR=$(unzip -Z1 "$ZIP_FILE" | head -1 | sed 's|/.*||')
EXPECTED_DIR="wso2is-${VERSION}"
if [ "$ZIP_INTERNAL_DIR" != "$EXPECTED_DIR" ]; then
    echo "    Zip internal dir: $ZIP_INTERNAL_DIR"
    echo "    Expected dir:     $EXPECTED_DIR"
    echo "    Repacking zip with correct directory name..."
    pushd modules/distribution/target > /dev/null
    unzip -q "wso2is-${VERSION}.zip"
    mv "$ZIP_INTERNAL_DIR" "$EXPECTED_DIR"
    zip -qr "wso2is-${VERSION}.zip.new" "$EXPECTED_DIR"
    mv "wso2is-${VERSION}.zip.new" "wso2is-${VERSION}.zip"
    rm -rf "$EXPECTED_DIR"
    popd > /dev/null
    echo "    Repacking done."
else
    echo "    Zip internal dir matches expected: $EXPECTED_DIR"
fi

echo ""
echo "=========================================================="
echo "Installing wso2is zip artifact to local Maven repository..."
echo "=========================================================="
mvn install:install-file \
    -Dfile="modules/distribution/target/wso2is-${VERSION}.zip" \
    -DgroupId=org.wso2.is \
    -DartifactId=wso2is \
    -Dversion="${VERSION}" \
    -Dpackaging=zip \
    --batch-mode
if [ $? -ne 0 ]; then
    echo "Failed to install wso2is artifact to local Maven repository."
    echo "::error::mvn install:install-file failed."
    exit 1
fi

echo ""
echo "=========================================================="
echo "Building test support modules (modules/tests-utils)..."
echo "=========================================================="
mvn install -Dmaven.test.skip=true --batch-mode -pl modules/tests-utils | tee mvn-prep.log

PREP_STATUS=$(cat mvn-prep.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$')
if [ "$PREP_STATUS" != "SUCCESS" ]; then
    echo "Test support build failed. Aborting."
    echo "::error::modules/tests-utils build not successful. Check artifacts for logs."
    exit 1
fi

echo ""
echo "=========================================================="
echo "Running integration tests (runner $BUILDER_NUMBER)..."
echo "=========================================================="
mvn install --batch-mode -f modules/integration/pom.xml | tee mvn-build.log

IT_BUILD_STATUS=$(cat mvn-build.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$' | tail -1)
IT_TEST_RESULT=$(sed -n -e '/\[INFO\] Results:/,/\[INFO\] Tests run:/ p' mvn-build.log)

IT_BUILD_FINAL_RESULT=$(
    echo "==========================================================="
    echo "Integration Tests BUILD $IT_BUILD_STATUS"
    echo "=========================================================="
    echo ""
    echo "$IT_TEST_RESULT"
)

IT_BUILD_RESULT_LOG_TEMP=$(echo "$IT_BUILD_FINAL_RESULT" | sed 's/$/%0A/')
IT_BUILD_RESULT_LOG=$(echo $IT_BUILD_RESULT_LOG_TEMP)
echo "::warning::$IT_BUILD_RESULT_LOG"

IT_BUILD_SUCCESS_COUNT=$(grep -o -i "\[INFO\] BUILD SUCCESS" mvn-build.log | wc -l)
if [ "$IT_BUILD_SUCCESS_COUNT" -lt 1 ]; then
    echo "Integration tests not successful. Aborting."
    echo "::error::Integration tests failed. Check artifacts for logs."
    exit 1
fi

echo ""
echo "=========================================================="
echo "Integration tests completed successfully (runner $BUILDER_NUMBER)"
echo "=========================================================="
echo ""
