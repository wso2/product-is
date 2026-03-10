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

OUTBOUND_AUTH_OIDC_REPO=identity-outbound-auth-oidc
OUTBOUND_AUTH_OIDC_REPO_CLONE_LINK=https://github.com/wso2-extensions/identity-outbound-auth-oidc.git
SCIM2_REPO=identity-inbound-provisioning-scim2
SCIM2_REPO_CLONE_LINK=https://github.com/wso2-extensions/identity-inbound-provisioning-scim2.git

# Main execution starts here.
echo ""
echo "=========================================================="
PR_LINK=${PR_LINK%/}
JAVA_21_HOME=${JAVA_21_HOME%/}
echo "    PR_LINK: $PR_LINK"
echo "    JAVA 21 Home: $JAVA_21_HOME"
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

git clone https://github.com/wso2/product-is product-is-build

if [ "$REPO" = "product-is" ]; then

  echo ""
  echo "PR is for the product-is itself. Applying diff and building without tests..."
  echo "=========================================================="
  cd product-is-build

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
  git log --oneline -3

  cat pom.xml
  export JAVA_HOME=$JAVA_21_HOME
  mvn clean install -Dmaven.test.skip=true --batch-mode | tee mvn-build.log

  BUILD_STATUS=$(cat mvn-build.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$')
  if [ "$BUILD_STATUS" != "SUCCESS" ]; then
    echo "product-is BUILD not successful. Aborting."
    echo "::error::product-is BUILD not successful. Check artifacts for logs."
    exit 1
  fi

  cd ..

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
  # version_property_finder.py is already available from the actions/checkout step.
  VERSION_PROPERTY_FINDER=".github/scripts/version_property_finder.py"
  VERSION_PROPERTY=$(python $VERSION_PROPERTY_FINDER $REPO product-is-build 2>&1)
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
    echo "::error::PR builder not supported"
    exit 1
  fi

  echo ""
  echo "Property key found: $VERSION_PROPERTY_KEY"
  cd $REPO
  if [ "$REPO" = "carbon-kernel" ]; then
    echo ""
    echo "Checking out for 4.12.x branch..."
    echo "=========================================================="
    git checkout 4.12.x
  elif [ "$REPO" = "carbon-deployment" ]; then
    echo ""
    echo "Checking out for 4.14.x branch in carbon-deployment..."
    echo "=========================================================="
    git checkout 4.14.x
  elif [ "$REPO" = "carbon-analytics-common" ]; then
      echo ""
      echo "Checking out for 5.5.x branch in carbon-analytics-common..."
      echo "=========================================================="
      git checkout 5.5.x
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

  export JAVA_HOME=$JAVA_21_HOME

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
    echo "$REPO BUILD not successful. Aborting."
    echo "::error::$REPO BUILD not successful. Check artifacts for logs."
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
    OUTBOUND_AUTH_OIDC_VERSION_PROPERTY=$(python $VERSION_PROPERTY_FINDER $OUTBOUND_AUTH_OIDC_REPO product-is-build 2>&1)
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

    export JAVA_HOME=$JAVA_21_HOME
    mvn clean install -Dmaven.test.skip=true --batch-mode | tee mvn-build.log

    echo "Repo $OUTBOUND_AUTH_OIDC_REPO build complete."
    SUB_REPO_BUILD_STATUS=$(cat mvn-build.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$')

    if [ "$SUB_REPO_BUILD_STATUS" != "SUCCESS" ]; then
      echo "$OUTBOUND_AUTH_OIDC_REPO repo build not successful. Aborting."
      echo "::error::$OUTBOUND_AUTH_OIDC_REPO repo build not successful. Aborting."
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
    SCIM2_VERSION_PROPERTY=$(python $VERSION_PROPERTY_FINDER $SCIM2_REPO product-is-build 2>&1)
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

    export JAVA_HOME=$JAVA_21_HOME
    mvn clean install -Dmaven.test.skip=true --batch-mode | tee mvn-build.log

    echo "Repo $SCIM2_REPO build complete."
    SUB_REPO_BUILD_STATUS=$(cat mvn-build.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$')

    if [ "$SUB_REPO_BUILD_STATUS" != "SUCCESS" ]; then
      echo "$SCIM2_REPO repo build not successful. Aborting."
      echo "::error::$SCIM2_REPO repo build not successful. Aborting."
      exit 1
    fi

    echo ""
    echo "Built version: $SCIM2_DEPENDENCY_VERSION"
    echo "=========================================================="
    echo ""
    cd ..
  fi

  cd product-is-build

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
      echo "Updating carbon-kernel version in carbon.product..."
      echo "=========================================================="
      echo ""
      KERNEL_DEPENDENCY_VERSION=$(echo $DEPENDENCY_VERSION | sed -e "s/-/./g")
      echo "Dependency version for carbon.product : $KERNEL_DEPENDENCY_VERSION"
      sed -i "s/version=\"4.12.*\"/version=\"$KERNEL_DEPENDENCY_VERSION\"/g" modules/p2-profile-gen/carbon.product
    fi
  fi

  export JAVA_HOME=$JAVA_21_HOME
  cat pom.xml

  # Build the full product pack without running integration tests.
  # The test runners will reuse this pre-built pack.
  mvn clean install -Dmaven.test.skip=true --batch-mode | tee mvn-build.log

  BUILD_STATUS=$(cat mvn-build.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$')
  if [ "$BUILD_STATUS" != "SUCCESS" ]; then
    echo "product-is BUILD not successful. Aborting."
    echo "::error::product-is BUILD not successful. Check artifacts for logs."
    exit 1
  fi

  cd ..
fi

echo ""
echo "=========================================================="
echo "Build phase completed successfully."
echo "The pre-built product-is directory is ready at: product-is-build/"
echo "=========================================================="
echo ""

