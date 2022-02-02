#!/bin/bash +x
MULTITENANCY_REPO=carbon-multitenancy
MULTITENANCY_REPO_CLONE_LINK=https://github.com/wso2/carbon-multitenancy.git
SCIM2_REPO=identity-inbound-provisioning-scim2
SCIM2_REPO_CLONE_LINK=https://github.com/wso2-extensions/identity-inbound-provisioning-scim2.git

echo ""
echo "=========================================================="
PR_LINK=${PR_LINK%/}
echo "    PR_LINK: $PR_LINK"
echo "::warning::Build ran for PR $PR_LINK"

USER=$(echo $PR_LINK | awk -F'/' '{print $4}')
REPO=$(echo $PR_LINK | awk -F'/' '{print $5}')
PULL_NUMBER=$(echo $PR_LINK | awk -F'/' '{print $7}')

echo "    USER: $USER"
echo "    REPO: $REPO"
echo "    PULL_NUMBER: $PULL_NUMBER"
echo "::set-output name=REPO_NAME::$REPO"
echo "=========================================================="
echo "Cloning product-is"
echo "=========================================================="
git clone https://github.com/wso2/product-is
echo "Checking out to 5.12.x branch..."
git checkout 5.12.x

if [ "$REPO" = "product-is" ]; then

  echo ""
  echo "PR is for the product-is itself. Start building with test..."
  echo "=========================================================="
  cd product-is

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

  if [ "$PR_BUILD_STATUS" != "SUCCESS" ]; then
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
  echo "Getting dependency repository branch list."
  echo "=========================================================="
  declare -A repo_dictionary
  wget https://raw.githubusercontent.com/wso2/product-is/5.12.x/.github/scripts/branches.csv
  while IFS="," read -r rec_repo rec_branch
  do
    repo_dictionary[$rec_repo]=$rec_branch
  done < branches.csv
  echo ""
  echo "Determining dependency version property key..."
  echo "=========================================================="
  wget https://raw.githubusercontent.com/wso2/product-is/5.12.x/.github/scripts/version_property_finder.py
  VERSION_PROPERTY=$(python version_property_finder.py $REPO product-is 2>&1)
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
  if [[ -n ${repo_dictionary[$REPO]} ]]; then
    echo ""
    REPO_BRANCH=${repo_dictionary[$REPO]}
    echo "Checking out to $REPO_BRANCH branch..."
    echo "=========================================================="
    git checkout $REPO_BRANCH
  else
    echo "$REPO repository is not currently supported for this PR builder."
    echo "::error::$REPO repository not supported. Aborting."
    exit 1
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
  if [ "$REPO" = "carbon-kernel" ]; then
    mvn clean install -Dmaven.test.skip=true --batch-mode | tee mvn-build.log
  else
    mvn clean install --batch-mode | tee mvn-build.log
  fi

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

  REPO_BUILD_RESULT_LOG_TEMP=$(echo "$REPO_FINAL_RESULT" | sed 's/$/%0A/')
  REPO_BUILD_RESULT_LOG=$(echo $REPO_BUILD_RESULT_LOG_TEMP)
  echo "::warning::$REPO_BUILD_RESULT_LOG"

  if [ "$REPO_BUILD_STATUS" != "SUCCESS" ]; then
    echo "$REPO BUILD not successfull. Aborting."
    echo "::error::$REPO BUILD not successfull. Check artifacts for logs."
    exit 1
  fi
  cd ..

  MULTITENANCY_VERSION_PROPERTY_KEY=""
  MULTITENANCY_DEPENDENCY_VERSION=""
  if [ "$REPO" = "carbon-kernel" ]; then
    echo ""
    echo "Building Multitenancy repo..."
    echo "=========================================================="
    git clone $MULTITENANCY_REPO_CLONE_LINK
    if [[ -n ${repo_dictionary[$MULTITENANCY_REPO]} ]]; then
      echo ""
      MULTITENANCY_BRANCH=${repo_dictionary[$MULTITENANCY_REPO]}
      echo "Checking out to $MULTITENANCY_BRANCH branch..."
      echo "=========================================================="
      git checkout $MULTITENANCY_BRANCH
    else
      echo "$MULTITENANCY_REPO repository branch version not found."
      echo "::error::$MULTITENANCY_REPO repository branch version not found. Aborting."
      exit 1
    fi
    MULTITENANCY_VERSION_PROPERTY=$(python version_property_finder.py $MULTITENANCY_REPO product-is 2>&1)
    if [ "$MULTITENANCY_VERSION_PROPERTY" != "invalid" ]; then
      echo "Version property key for the $MULTITENANCY_REPO is $MULTITENANCY_VERSION_PROPERTY"
      MULTITENANCY_VERSION_PROPERTY_KEY=$MULTITENANCY_VERSION_PROPERTY
    else
      echo ""
      echo "=========================================================="
      echo "Unable to find the version property for $MULTITENANCY_REPO..."
      echo "=========================================================="
      echo ""
      echo "::error::Unable to find the version property for $MULTITENANCY_REPO..."
      exit 1
    fi
    cd $MULTITENANCY_REPO
    MULTITENANCY_DEPENDENCY_VERSION=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)
    echo "Multitenancy Dependency Version: $MULTITENANCY_DEPENDENCY_VERSION"
    echo ""

    KERNEL_VERSION_PROPERTY_KEY=carbon.kernel.version
    echo "Updating carbon-kernel dependency version in carbon-multitenancy repo..."
    echo "=========================================================="
    echo ""
    sed -i "s/<$KERNEL_VERSION_PROPERTY_KEY>.*<\/$KERNEL_VERSION_PROPERTY_KEY>/<$KERNEL_VERSION_PROPERTY_KEY>$DEPENDENCY_VERSION<\/$KERNEL_VERSION_PROPERTY_KEY>/" pom.xml

    echo ""
    echo "Building repo $MULTITENANCY_REPO..."
    echo "=========================================================="
    mvn clean install -Dmaven.test.skip=true --batch-mode | tee mvn-build.log

    echo "Repo $MULTITENANCY_REPO build complete."
    SUB_REPO_BUILD_STATUS=$(cat mvn-build.log | grep "\[INFO\] BUILD" | grep -oE '[^ ]+$')

    if [ "$SUB_REPO_BUILD_STATUS" != "SUCCESS" ]; then
      echo "$MULTITENANCY_REPO repo build not successfull. Aborting."
      echo "::error::$MULTITENANCY_REPO repo build not successfull. Aborting."
      exit 1
    fi

    echo ""
    echo "Built version: $MULTITENANCY_DEPENDENCY_VERSION"
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
    if [[ -n ${repo_dictionary[$SCIM2_REPO]} ]]; then
      echo ""
      SCIM_BRANCH=${repo_dictionary[$SCIM2_REPO]}
      echo "Checking out to $SCIM_BRANCH branch..."
      echo "=========================================================="
      git checkout $SCIM_BRANCH
    else
      echo "$SCIM2_REPO repository branch version not found."
      echo "::error::$SCIM2_REPO repository branch version not found. Aborting."
      exit 1
    fi
    SCIM2_VERSION_PROPERTY=$(python version_property_finder.py $SCIM2_REPO product-is 2>&1)
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

  cd product-is

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
      echo "Updating Multitenancy version in product-is..."
      echo "=========================================================="
      echo ""
      sed -i "s/<$MULTITENANCY_VERSION_PROPERTY_KEY>.*<\/$MULTITENANCY_VERSION_PROPERTY_KEY>/<$MULTITENANCY_VERSION_PROPERTY_KEY>$MULTITENANCY_DEPENDENCY_VERSION<\/$MULTITENANCY_VERSION_PROPERTY_KEY>/" pom.xml
      echo "Updating caron-kernel version in carbon.product..."
      echo "=========================================================="
      echo ""
      KERNEL_DEPENDENCY_VERSION=$(echo $DEPENDENCY_VERSION | sed -e "s/-/./g")
      echo "Dependency version for carbon.product : $KERNEL_DEPENDENCY_VERSION"
      sed -i "s/version=\"4.7.*\"/version=\"$KERNEL_DEPENDENCY_VERSION\"/g" modules/p2-profile-gen/carbon.product
    fi
  fi

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

  if [ "$PR_BUILD_STATUS" != "SUCCESS" ]; then
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