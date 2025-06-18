#!/bin/bash +x
OUTBOUND_AUTH_OIDC_REPO=identity-outbound-auth-oidc
OUTBOUND_AUTH_OIDC_REPO_CLONE_LINK=https://github.com/wso2-extensions/identity-outbound-auth-oidc.git
SCIM2_REPO=identity-inbound-provisioning-scim2
SCIM2_REPO_CLONE_LINK=https://github.com/wso2-extensions/identity-inbound-provisioning-scim2.git

# Define all available tests.
declare -a ALL_TESTS=(
    "is-tests-default-configuration"
    "is-test-rest-api"
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

# Main execution starts here.
BUILDER_NUMBER=$1
ENABLED_TESTS=$2

echo ""
echo "=========================================================="
PR_LINK=${PR_LINK%/}
JDK_VERSION=${JDK_VERSION%/}
JAVA_8_HOME=${JAVA_8_HOME%/}
JAVA_11_HOME=${JAVA_11_HOME%/}
echo "    PR_LINK: $PR_LINK"
echo "    JAVA 8 Home: $JAVA_8_HOME"
echo "    JAVA 11 Home: $JAVA_11_HOME"
echo "    User Input: $JDK_VERSION"
echo "::warning::Build ran for PR $PR_LINK"

USER=$(echo $PR_LINK | awk -F'/' '{print $4}')
REPO=$(echo $PR_LINK | awk -F'/' '{print $5}')
PULL_NUMBER=$(echo $PR_LINK | awk -F'/' '{print $7}')

echo "    USER: $USER"
echo "    REPO: $REPO"
echo "    PULL_NUMBER: $PULL_NUMBER"
echo "REPO_NAME=$REPO" >> "$GITHUB_OUTPUT"
echo "=========================================================="
echo "Cloning product-is"
echo "=========================================================="

git clone https://github.com/wso2/product-is product-is-$BUILDER_NUMBER

disable_tests "$ENABLED_TESTS"

if [ "$REPO" = "product-is" ]; then

  echo ""
  echo "PR is for the product-is itself. Start building with test..."
  echo "=========================================================="
  cd product-is-$BUILDER_NUMBER

  echo ""
  echo "Applying PR $PULL_NUMBER as a diff..."
  echo "=========================================================="
  wget -q --output-document=diff.diff $PR_LINK.diff
  cat diff.diff
  echo "=========================================================="
  git apply diff.diff || {
    echo 'Applying diff failed. Exiting...'
    echo "::error::Applying diff failed."
    exit 1
  }

  echo "Last 3 changes:"
  COMMIT1=$(git log --oneline -1)
  COMMIT2=$(git log --oneline -2 | tail -1)
  COMMIT3=$(git log --oneline -3 | tail -1)
  echo "$COMMIT1"
  echo "$COMMIT2"
  echo "$COMMIT3"

  cat pom.xml
  export JAVA_HOME=$JAVA_11_HOME
  mvn clean install --batch-mode | tee mvn-build.log

  PR_BUILD_STATUS=$(cat mvn-build.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$')
  PR_TEST_RESULT=$(sed -n -e '/\[INFO\] Results:/,/\[INFO\] Tests run:/ p' mvn-build.log)

  PR_BUILD_FINAL_RESULT=$(
    echo "==========================================================="
    echo "product-is BUILD $PR_BUILD_STATUS"
    echo "=========================================================="
    echo ""
    echo "$PR_TEST_RESULT"
  )

  PR_BUILD_RESULT_LOG_TEMP=$(echo "$PR_BUILD_FINAL_RESULT" | sed 's/$/%0A/')
  PR_BUILD_RESULT_LOG=$(echo $PR_BUILD_RESULT_LOG_TEMP)
  echo "::warning::$PR_BUILD_RESULT_LOG"

  PR_BUILD_SUCCESS_COUNT=$(grep -o -i "\[INFO\] BUILD SUCCESS" mvn-build.log | wc -l)
  if [ "$PR_BUILD_SUCCESS_COUNT" != "1" ]; then
    echo "PR BUILD not successfull. Aborting."
    echo "::error::PR BUILD not successfull. Check artifacts for logs."
    exit 1
  fi
else
  echo ""
  echo "PR is for the dependency repository $REPO."
  echo ""
  echo "Cloning $USER/$REPO"
  echo "=========================================================="
  git clone https://github.com/$USER/$REPO
  echo ""
  echo "Determining dependency version property key..."
  echo "=========================================================="
  wget https://raw.githubusercontent.com/wso2/product-is/master/.github/scripts/version_property_finder.py
  VERSION_PROPERTY=$(python version_property_finder.py $REPO product-is-$BUILDER_NUMBER 2>&1)
  VERSION_PROPERTY_KEY=""
  if [ "$VERSION_PROPERTY" != "invalid" ]; then
    echo "Version property key for the $REPO is $VERSION_PROPERTY"
    VERSION_PROPERTY_KEY=$VERSION_PROPERTY
  else
    echo ""
    echo "=========================================================="
    echo "$REPO is not yet supported! Exiting..."
    echo "=========================================================="
    echo ""
    echo "::error::PR builder not supprted"
    exit 1
  fi

  echo ""
  echo "Property key found: $VERSION_PROPERTY_KEY"
  cd $REPO
  if [ "$REPO" = "carbon-kernel" ]; then
    echo ""
    echo "Checking out for 4.10.x branch..."
    echo "=========================================================="
    git checkout 4.10.x
  elif [ "$REPO" = "carbon-deployment" ]; then
    echo ""
    echo "Checking out for 4.x.x branch in carbon-deployment..."
    echo "=========================================================="
    git checkout 4.x.x
  elif [ "$REPO" = "carbon-analytics-common" ]; then
      echo ""
      echo "Checking out for 5.2.x branch in carbon-analytics-common..."
      echo "=========================================================="
      git checkout 5.2.x
  elif [ "$REPO" = "identity-extension-utils" ]; then
      echo ""
      echo "Checking out for 1.0.x branch in identity-extension-utils..."
      echo "=========================================================="
      git checkout 1.0.x
  fi
  DEPENDENCY_VERSION=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)
  echo "Dependency Version: $DEPENDENCY_VERSION"
  echo ""
  echo "Applying PR $PULL_NUMBER as a diff..."
  echo "=========================================================="
  wget -q --output-document=diff.diff $PR_LINK.diff
  cat diff.diff
  echo "=========================================================="
  git apply diff.diff || {
    echo 'Applying diff failed. Exiting...'
    echo "::error::Applying diff failed."
    exit 1
  }

  echo ""
  echo "Building dependency repo $REPO..."
  echo "=========================================================="

  if [ "$JDK_VERSION" = "11" ]; then
    export JAVA_HOME=$JAVA_11_HOME
  else
    export JAVA_HOME=$JAVA_8_HOME
  fi


  mvn clean install -Dmaven.test.skip=true --batch-mode | tee mvn-build.log

  echo ""
  echo "Dependency repo $REPO build complete."
  echo "Built version: $DEPENDENCY_VERSION"
  echo "=========================================================="
  echo ""

  REPO_BUILD_STATUS=$(cat mvn-build.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$')
  REPO_TEST_RESULT_1=$(sed -n -e '/Results :/,/Tests run:/ p' mvn-build.log)
  REPO_TEST_RESULT_2=$(sed -n -e '/\[INFO\] Results:/,/\[INFO\] Tests run:/ p' mvn-build.log)

  REPO_FINAL_RESULT=$(
    echo "==========================================================="
    echo "$REPO BUILD $REPO_BUILD_STATUS"
    echo "=========================================================="
    echo ""
    echo "Built version: $DEPENDENCY_VERSION"
    echo ""
    echo "$REPO_TEST_RESULT_1"
    echo ""
    echo "$REPO_TEST_RESULT_2"
  )

  if [ $REPO = "identity-apps" ]; then
      MAIN_COMPONENT_BUILD_STATUS=$(cat mvn-build.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$' | tail -1)
      SUB_COMPONENT_BUILD_STATUS=$(cat mvn-build.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$' | head -1)
      REPO_BUILD_STATUS="SUCCESS"
      if [ $MAIN_COMPONENT_BUILD_STATUS != "SUCCESS" ] || [ $SUB_COMPONENT_BUILD_STATUS != "SUCCESS" ]; then
          REPO_BUILD_STATUS="FAILED"
      fi
      REPO_TEST_RESULT_1=$(sed -n -e '/Results :/,/Tests run:/ p' mvn-build.log)
      REPO_TEST_RESULT_2=$(sed -n -e '/\[INFO\] Results:/,/\[INFO\] Tests run:/ p' mvn-build.log)

      REPO_FINAL_RESULT=$(
          echo "==========================================================="
          if [ $REPO_BUILD_STATUS = "SUCCESS" ]; then
              echo "BUILD $REPO_BUILD_STATUS"
          else
              if [ $MAIN_COMPONENT_BUILD_STATUS != "SUCCESS" ]; then
              echo "WSO2 Identity Server Apps - Parent Build Failed."
              fi
              if [ $SUB_COMPONENT_BUILD_STATUS != "SUCCESS" ]; then
              echo "WSO2 Identity Server Apps - Login Portal Layouts Build Failed."
              fi
          fi
          echo "=========================================================="
          echo ""
          echo "Built version: $DEPENDENCY_VERSION"
          echo ""
          echo "$REPO_TEST_RESULT_1"
          echo ""
          echo "$REPO_TEST_RESULT_2"
      )
  fi

  REPO_BUILD_RESULT_LOG_TEMP=$(echo "$REPO_FINAL_RESULT" | sed 's/$/%0A/')
  REPO_BUILD_RESULT_LOG=$(echo $REPO_BUILD_RESULT_LOG_TEMP)
  echo "::warning::$REPO_BUILD_RESULT_LOG"

  if [ "$REPO_BUILD_STATUS" != "SUCCESS" ]; then
    echo "$REPO BUILD not successfull. Aborting."
    echo "::error::$REPO BUILD not successfull. Check artifacts for logs."
    exit 1
  fi
  cd ..

  OUTBOUND_AUTH_OIDC_VERSION_PROPERTY_KEY=""
  OUTBOUND_AUTH_OIDC_DEPENDENCY_VERSION=""
  if [ "$REPO" = "carbon-kernel" ]; then
    echo ""
    echo "Building Outbound Auth OIDC repo..."
    echo "=========================================================="
    git clone $OUTBOUND_AUTH_OIDC_REPO_CLONE_LINK
    OUTBOUND_AUTH_OIDC_VERSION_PROPERTY=$(python version_property_finder.py $OUTBOUND_AUTH_OIDC_REPO product-is-$BUILDER_NUMBER 2>&1)
    if [ "$OUTBOUND_AUTH_OIDC_VERSION_PROPERTY" != "invalid" ]; then
      echo "Version property key for the $OUTBOUND_AUTH_OIDC_REPO is $OUTBOUND_AUTH_OIDC_VERSION_PROPERTY"
      OUTBOUND_AUTH_OIDC_VERSION_PROPERTY_KEY=$OUTBOUND_AUTH_OIDC_VERSION_PROPERTY
    else
      echo ""
      echo "=========================================================="
      echo "Unable to find the version property for $OUTBOUND_AUTH_OIDC_REPO..."
      echo "=========================================================="
      echo ""
      echo "::error::Unable to find the version property for $OUTBOUND_AUTH_OIDC_REPO..."
      exit 1
    fi
    cd $OUTBOUND_AUTH_OIDC_REPO
    OUTBOUND_AUTH_OIDC_DEPENDENCY_VERSION=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)
    echo "Outbound Auth OIDC Dependency Version: $OUTBOUND_AUTH_OIDC_DEPENDENCY_VERSION"
    echo ""

    KERNEL_VERSION_PROPERTY_KEY=carbon.kernel.version
    echo "Updating carbon-kernel dependency version in identity-outbound-auth-oidc repo..."
    echo "=========================================================="
    echo ""
    sed -i "s/<$KERNEL_VERSION_PROPERTY_KEY>.*<\/$KERNEL_VERSION_PROPERTY_KEY>/<$KERNEL_VERSION_PROPERTY_KEY>$DEPENDENCY_VERSION<\/$KERNEL_VERSION_PROPERTY_KEY>/" pom.xml

    echo ""
    echo "Building repo $OUTBOUND_AUTH_OIDC_REPO..."
    echo "=========================================================="


    export JAVA_HOME=$JAVA_11_HOME
    mvn clean install -Dmaven.test.skip=true --batch-mode | tee mvn-build.log

    echo "Repo $OUTBOUND_AUTH_OIDC_REPO build complete."
    SUB_REPO_BUILD_STATUS=$(cat mvn-build.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$')

    if [ "$SUB_REPO_BUILD_STATUS" != "SUCCESS" ]; then
      echo "$OUTBOUND_AUTH_OIDC_REPO repo build not successfull. Aborting."
      echo "::error::$OUTBOUND_AUTH_OIDC_REPO repo build not successfull. Aborting."
      exit 1
    fi

    echo ""
    echo "Built version: $OUTBOUND_AUTH_OIDC_DEPENDENCY_VERSION"
    echo "=========================================================="
    echo ""
    cd ..
  fi

  SCIM2_VERSION_PROPERTY_KEY=""
  SCIM2_DEPENDENCY_VERSION=""
  if [ "$REPO" = "charon" ]; then
    echo ""
    echo "Building SCIM2 repo..."
    echo "=========================================================="
    git clone $SCIM2_REPO_CLONE_LINK
    SCIM2_VERSION_PROPERTY=$(python version_property_finder.py $SCIM2_REPO product-is-$BUILDER_NUMBER 2>&1)
    if [ "$SCIM2_VERSION_PROPERTY" != "invalid" ]; then
      echo "Version property key for the $SCIM2_REPO is $SCIM2_VERSION_PROPERTY"
      SCIM2_VERSION_PROPERTY_KEY=$SCIM2_VERSION_PROPERTY
    else
      echo ""
      echo "=========================================================="
      echo "Unable to find the version property for $SCIM2_REPO..."
      echo "=========================================================="
      echo ""
      echo "::error::Unable to find the version property for $SCIM2_REPO..."
      exit 1
    fi
    cd $SCIM2_REPO
    SCIM2_DEPENDENCY_VERSION=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)
    echo "SCIM2 Dependency Version: $SCIM2_DEPENDENCY_VERSION"
    echo ""

    CHARON_VERSION_PROPERTY_KEY=charon.version
    echo "Updating charon dependency version in $SCIM2_REPO repo..."
    echo "=========================================================="
    echo ""
    sed -i "s/<$CHARON_VERSION_PROPERTY_KEY>.*<\/$CHARON_VERSION_PROPERTY_KEY>/<$CHARON_VERSION_PROPERTY_KEY>$DEPENDENCY_VERSION<\/$CHARON_VERSION_PROPERTY_KEY>/" pom.xml

    echo ""
    echo "Building $SCIM2_REPO repo..."
    echo "=========================================================="

    export JAVA_HOME=$JAVA_8_HOME
    mvn clean install -Dmaven.test.skip=true --batch-mode | tee mvn-build.log

    echo "Repo $SCIM2_REPO build complete."
    SUB_REPO_BUILD_STATUS=$(cat mvn-build.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$')

    if [ "$SUB_REPO_BUILD_STATUS" != "SUCCESS" ]; then
      echo "$SCIM2_REPO repo build not successfull. Aborting."
      echo "::error::$SCIM2_REPO repo build not successfull. Aborting."
      exit 1
    fi

    echo ""
    echo "Built version: $SCIM2_DEPENDENCY_VERSION"
    echo "=========================================================="
    echo ""
    cd ..
  fi

  cd product-is-$BUILDER_NUMBER

  echo "Updating dependency version in product-is..."
  echo "=========================================================="
  echo ""
  if [ "$REPO" = "charon" ]; then
    echo "Updating SCIM2 version in product-is..."
    echo "=========================================================="
    echo ""
    sed -i "s/<$SCIM2_VERSION_PROPERTY_KEY>.*<\/$SCIM2_VERSION_PROPERTY_KEY>/<$SCIM2_VERSION_PROPERTY_KEY>$SCIM2_DEPENDENCY_VERSION<\/$SCIM2_VERSION_PROPERTY_KEY>/" pom.xml
  else
    sed -i "s/<$VERSION_PROPERTY_KEY>.*<\/$VERSION_PROPERTY_KEY>/<$VERSION_PROPERTY_KEY>$DEPENDENCY_VERSION<\/$VERSION_PROPERTY_KEY>/" pom.xml
    if [ "$REPO" = "carbon-kernel" ]; then
      echo "Updating Outbound Auth OIDC version in product-is..."
      echo "=========================================================="
      echo ""
      sed -i "s/<$OUTBOUND_AUTH_OIDC_VERSION_PROPERTY_KEY>.*<\/$OUTBOUND_AUTH_OIDC_VERSION_PROPERTY_KEY>/<$OUTBOUND_AUTH_OIDC_VERSION_PROPERTY_KEY>$OUTBOUND_AUTH_OIDC_DEPENDENCY_VERSION<\/$OUTBOUND_AUTH_OIDC_VERSION_PROPERTY_KEY>/" pom.xml
      echo "Updating caron-kernel version in carbon.product..."
      echo "=========================================================="
      echo ""
      KERNEL_DEPENDENCY_VERSION=$(echo $DEPENDENCY_VERSION | sed -e "s/-/./g")
      echo "Dependency version for carbon.product : $KERNEL_DEPENDENCY_VERSION"
      sed -i "s/version=\"4.10.*\"/version=\"$KERNEL_DEPENDENCY_VERSION\"/g" modules/p2-profile-gen/carbon.product
    fi
  fi

  export JAVA_HOME=$JAVA_11_HOME
  cat pom.xml
  mvn clean install --batch-mode | tee mvn-build.log

  PR_BUILD_STATUS=$(cat mvn-build.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$')
  PR_TEST_RESULT=$(sed -n -e '/\[INFO\] Results:/,/\[INFO\] Tests run:/ p' mvn-build.log)

  PR_BUILD_FINAL_RESULT=$(
    echo "==========================================================="
    echo "product-is BUILD $PR_BUILD_STATUS"
    echo "=========================================================="
    echo ""
    echo "$PR_TEST_RESULT"
  )

  PR_BUILD_RESULT_LOG_TEMP=$(echo "$PR_BUILD_FINAL_RESULT" | sed 's/$/%0A/')
  PR_BUILD_RESULT_LOG=$(echo $PR_BUILD_RESULT_LOG_TEMP)
  echo "::warning::$PR_BUILD_RESULT_LOG"

  PR_BUILD_SUCCESS_COUNT=$(grep -o -i "\[INFO\] BUILD SUCCESS" mvn-build.log | wc -l)
  if [ "$PR_BUILD_SUCCESS_COUNT" != "1" ]; then
    echo "PR BUILD not successfull. Aborting."
    echo "::error::PR BUILD not successfull. Check artifacts for logs."
    exit 1
  fi
fi

echo ""
echo "=========================================================="
echo "Build completed"
echo "=========================================================="
echo ""
