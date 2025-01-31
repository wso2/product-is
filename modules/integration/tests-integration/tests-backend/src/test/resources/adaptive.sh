#!/bin/sh
# ----------------------------------------------------------------------------
#  Copyright 2022 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# ----------------------------------------------------------------------------
# Main Script for the WSO2 Carbon Server
#
# Environment Variable Prequisites
#
#   CARBON_HOME   	Home of WSO2 Carbon installation. If not set I will  try
#                   	to figure it out.
#   NASHORN_VERSION   	OpenJDK Nashorn Version
#
#   ASM_VERSION   	ASM Util, Commons, Tree Version.
#
#   JMS_VERSION   	Geronimo Spec Jms Version.
#
# -----------------------------------------------------------------------------

CARBON_HOME=$1

NASHORN_VERSION=15.3;
ASM_VERSION=9.2;
JMS_VERSION=1.1.0.rc4-wso2v1;
DISABLE=$2;

LIB_REPO=$CARBON_HOME/repository/components/lib

if [[ "$DISABLE" == "DISABLE" || "$DISABLE" == "disable" ]]; then
    LOCAL_NASHORN_VERSION=""
    LOCAL_ASM_VERSION=""
    if compgen -G "$CARBON_HOME/repository/components/lib/nashorn-core-*.jar" > /dev/null; then
      location=$(find "$CARBON_HOME/repository/components/lib/" ~+ -type f -name "nashorn-core-*.jar" | head -1)
      full_artifact_name=$(basename ${location})
      artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $3}')
      LOCAL_NASHORN_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
      echo "Remove existing Nashorn library from lib folder: ${full_artifact_name}"
      rm $location
      echo "Nashorn library Removed from component/lib."
    fi
    if compgen -G "$CARBON_HOME/repository/components/dropins/nashorn_core_*.jar" > /dev/null; then
      location=$(find "$CARBON_HOME/repository/components/dropins/" ~+ -type f -name "nashorn_core_$LOCAL_NASHORN_VERSION*.jar" | head -1)
      full_artifact_name=$(basename ${location})
      echo "Remove existing Nashorn library from dropins: ${full_artifact_name}"
      rm $location
      echo "Nashorn library Removed from component/dropins."
    fi
    if compgen -G "$CARBON_HOME/repository/components/lib/asm-util-*.jar" > /dev/null; then
      location=$(find "$CARBON_HOME/repository/components/lib/" ~+ -type f -name "asm-util-*.jar" | head -1)
      full_artifact_name=$(basename ${location})
      artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $3}')
      LOCAL_ASM_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
      echo "Remove existing ASM Util library from lib folder: ${full_artifact_name}"
      rm $location
      echo "ASM Util library Removed from component/lib."
    fi
    if compgen -G "$CARBON_HOME/repository/components/dropins/asm_util_*.jar" > /dev/null; then
      location=$(find "$CARBON_HOME/repository/components/dropins/" ~+ -type f -name "asm_util_$LOCAL_ASM_VERSION*.jar" | head -1)
      full_artifact_name=$(basename ${location})
      echo "Remove existing ASM Util library from dropins: ${full_artifact_name}"
      rm $location
      echo "ASM Util library Removed from component/dropins."
    fi
    if compgen -G "$CARBON_HOME/repository/components/lib/asm-commons-*.jar" > /dev/null; then
      location=$(find "$CARBON_HOME/repository/components/lib/" ~+ -type f -name "asm-commons-*.jar" | head -1)
      full_artifact_name=$(basename ${location})
      artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $3}')
      LOCAL_ASM_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
      echo "Remove existing ASM Commons library from lib folder: ${full_artifact_name}"
      rm $location
      echo "ASM Commons library Removed from component/lib."
    fi
    if compgen -G "$CARBON_HOME/repository/components/dropins/asm_commons_*.jar" > /dev/null; then
      location=$(find "$CARBON_HOME/repository/components/dropins/" ~+ -type f -name "asm_commons_$LOCAL_ASM_VERSION*.jar" | head -1)
      full_artifact_name=$(basename ${location})
      echo "Remove existing ASM Commons library from dropins: ${full_artifact_name}"
      rm $location
      echo "ASM Commons library Removed from component/dropins."
    fi
    if compgen -G "$CARBON_HOME/repository/components/lib/asm-tree-*.jar" > /dev/null; then
      location=$(find "$CARBON_HOME/repository/components/lib/" ~+ -type f -name "asm-tree-*.jar" | head -1)
      full_artifact_name=$(basename ${location})
      artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $3}')
      LOCAL_ASM_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
      echo "Remove existing ASM Tree library from lib folder: ${full_artifact_name}"
      rm $location
      echo "ASM Tree library Removed from component/lib."
    fi
    if compgen -G "$CARBON_HOME/repository/components/dropins/asm_tree_*.jar" > /dev/null; then
      location=$(find "$CARBON_HOME/repository/components/dropins/" ~+ -type f -name "asm_tree_$LOCAL_ASM_VERSION*.jar" | head -1)
      full_artifact_name=$(basename ${location})
      echo "Remove existing ASM Tree library from dropins: ${full_artifact_name}"
      rm $location
      echo "ASM Tree library Removed from component/dropins."
    fi
    if compgen -G "$CARBON_HOME/repository/components/lib/geronimo-spec-jms-*.jar" > /dev/null; then
      location=$(find "$CARBON_HOME/repository/components/lib/" ~+ -type f -name "geronimo-spec-jms-*.jar" | head -1)
      full_artifact_name=$(basename ${location})
      artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $3}')
      LOCAL_JMS_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
      echo "Remove existing Geronimo Spec Jms library from lib folder: ${full_artifact_name}"
      rm $location
      echo "Geronimo Spec Jms library Removed from component/lib."
    fi
    if compgen -G "$CARBON_HOME/repository/components/dropins/geronimo_spec_jms_*.jar" > /dev/null; then
      location=$(find "$CARBON_HOME/repository/components/dropins/" ~+ -type f -name "geronimo_spec_jms_$LOCAL_JMS_VERSION*.jar" | head -1)
      full_artifact_name=$(basename ${location})
      echo "Remove existing Geronimo Spec Jms library from dropins: ${full_artifact_name}"
      rm $location
      echo "Geronimo Spec Jms library Removed from component/dropins."
    fi
    echo "Adaptive authentication successfully disabled."
else
  if compgen -G "$CARBON_HOME/repository/components/lib/nashorn-core-*.jar" > /dev/null; then
      location=$(find "$CARBON_HOME/repository/components/lib/" ~+ -type f -name "nashorn-core-*.jar" | head -1)
      full_artifact_name=$(basename ${location})
      artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $3}')
      LOCAL_NASHORN_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
      if [ "$NASHORN_VERSION" = "$LOCAL_NASHORN_VERSION" ]; then
        echo "Nashorn library exists. No need to download."
      else
        echo "Required Nashorn library not found. Remove existing library : ${full_artifact_name}"
        rm $location
        echo "Downloading required Nashorn library : nashorn-core-${NASHORN_VERSION}"
        mvn dependency:get --batch-mode -Dartifact=org.openjdk.nashorn:nashorn-core:$NASHORN_VERSION
        mvn dependency:copy --batch-mode -Dartifact=org.openjdk.nashorn:nashorn-core:$NASHORN_VERSION -DoutputDirectory=$LIB_REPO
        echo "Nashorn library updated."
      fi
  else
     echo "Nashorn library not found. Starting to download....."
     mvn dependency:get --batch-mode -Dartifact=org.openjdk.nashorn:nashorn-core:$NASHORN_VERSION
     mvn dependency:copy --batch-mode -Dartifact=org.openjdk.nashorn:nashorn-core:$NASHORN_VERSION -DoutputDirectory=$LIB_REPO
     echo "Nashorn download completed. Downloaded version : nashorn-core-${NASHORN_VERSION}"
  fi

  if compgen -G "$CARBON_HOME/repository/components/lib/asm-util-*.jar" > /dev/null; then
      location=$(find "$CARBON_HOME/repository/components/lib/" ~+ -type f -name "asm-util-*.jar" | head -1)
      full_artifact_name=$(basename ${location})
      artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $3}')
      LOCAL_ASM_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
      if [ "$ASM_VERSION" = "$LOCAL_ASM_VERSION" ]; then
        echo "ASM-Util library exists. No need to download."
      else
        echo "Required ASM-Util library not found. Remove existing library : ${full_artifact_name}"
        rm $location
        echo "Downloading required ASM-Util library : asm-util-${ASM_VERSION}"
        mvn dependency:get --batch-mode -Dartifact=org.ow2.asm:asm-util:$ASM_VERSION
        mvn dependency:copy --batch-mode -Dartifact=org.ow2.asm:asm-util:$ASM_VERSION -DoutputDirectory=$LIB_REPO
        echo "ASM-Util library updated."
      fi
  else
     echo "ASM-Util library not found. Starting to download....."
     mvn dependency:get --batch-mode -Dartifact=org.ow2.asm:asm-util:$ASM_VERSION
     mvn dependency:copy --batch-mode -Dartifact=org.ow2.asm:asm-util:$ASM_VERSION -DoutputDirectory=$LIB_REPO
     echo "ASM-Util download completed. Downloaded version : asm-util-${ASM_VERSION}"
  fi

  if compgen -G "$CARBON_HOME/repository/components/lib/asm-commons-*.jar" > /dev/null; then
      location=$(find "$CARBON_HOME/repository/components/lib/" ~+ -type f -name "asm-commons-*.jar" | head -1)
      full_artifact_name=$(basename ${location})
      artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $3}')
      LOCAL_ASM_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
      if [ "$ASM_VERSION" = "$LOCAL_ASM_VERSION" ]; then
        echo "ASM-Commons library exists. No need to download."
      else
        echo "Required ASM-Commons library not found. Remove existing library : ${full_artifact_name}"
        rm $location
        echo "Downloading required ASM-Commons library : asm-commons-${ASM_VERSION}"
        mvn dependency:get --batch-mode -Dartifact=org.ow2.asm:asm-commons:$ASM_VERSION
        mvn dependency:copy --batch-mode -Dartifact=org.ow2.asm:asm-commons:$ASM_VERSION -DoutputDirectory=$LIB_REPO
        echo "ASM-Commons library updated."
      fi
  else
     echo "ASM-Commons library not found. Starting to download....."
     mvn dependency:get --batch-mode -Dartifact=org.ow2.asm:asm-commons:$ASM_VERSION
     mvn dependency:copy --batch-mode -Dartifact=org.ow2.asm:asm-commons:$ASM_VERSION -DoutputDirectory=$LIB_REPO
     echo "ASM-Commons download completed. Downloaded version : asm-commons-${ASM_VERSION}"
  fi

  if compgen -G "$CARBON_HOME/repository/components/lib/asm-tree-*.jar" > /dev/null; then
      location=$(find "$CARBON_HOME/repository/components/lib/" ~+ -type f -name "asm-tree-*.jar" | head -1)
      full_artifact_name=$(basename ${location})
      artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $3}')
      LOCAL_ASM_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
      if [ "$ASM_VERSION" = "$LOCAL_ASM_VERSION" ]; then
        echo "ASM-Tree library exists. No need to download."
      else
        echo "Required ASM-Tree library not found. Remove existing library : ${full_artifact_name}"
        rm $location
        echo "Downloading required ASM-Tree library : asm-tree-${ASM_VERSION}"
        mvn dependency:get --batch-mode -Dartifact=org.ow2.asm:asm-tree:$ASM_VERSION
        mvn dependency:copy --batch-mode -Dartifact=org.ow2.asm:asm-tree:$ASM_VERSION -DoutputDirectory=$LIB_REPO
        echo "ASM-Tree library updated."
      fi
  else
     echo "ASM-Tree library not found. Starting to download....."
     mvn dependency:get --batch-mode -Dartifact=org.ow2.asm:asm-tree:$ASM_VERSION
     mvn dependency:copy --batch-mode -Dartifact=org.ow2.asm:asm-tree:$ASM_VERSION -DoutputDirectory=$LIB_REPO
     echo "ASM-Tree download completed. Downloaded version : asm-tree-${ASM_VERSION}"
  fi

  if compgen -G "$CARBON_HOME/repository/components/lib/geronimo-spec-jms-*.jar" > /dev/null; then
      location=$(find "$CARBON_HOME/repository/components/lib/" ~+ -type f -name "geronimo-spec-jms-*.jar" | head -1)
      full_artifact_name=$(basename ${location})
      artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $3}')
      LOCAL_JMS_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
      if [ "$JMS_VERSION" = "$LOCAL_JMS_VERSION" ]; then
        echo "Geronimo-Spec-Jms library exists. No need to download."
      else
        echo "Required Geronimo-Spec-Jms library not found. Remove existing library : ${full_artifact_name}"
        rm $location
        echo "Downloading required Geronimo-Spec-Jms library : geronimo-spec-jms-${JMS_VERSION}"
        mvn dependency:get --batch-mode -DremoteRepositories=https://dist.wso2.org/maven2/ -Dartifact=geronimo-spec/wso2:geronimo-spec-jms:${JMS_VERSION}
        mvn dependency:copy --batch-mode -Dartifact=geronimo-spec/wso2:geronimo-spec-jms:${JMS_VERSION} -DoutputDirectory=$LIB_REPO
        echo "Geronimo-Spec-Jms library updated."
      fi
  else
     echo "Geronimo-Spec-Jms library not found. Starting to download....."
     mvn dependency:get --batch-mode -DremoteRepositories=https://dist.wso2.org/maven2/ -Dartifact=geronimo-spec/wso2:geronimo-spec-jms:${JMS_VERSION}
     mvn dependency:copy --batch-mode -Dartifact=geronimo-spec/wso2:geronimo-spec-jms:${JMS_VERSION} -DoutputDirectory=$LIB_REPO
     echo "Geronimo-Spec-Jms download completed. Downloaded version : geronimo-spec-jms-${JMS_VERSION}"
  fi
  echo "Adaptive authentication successfully enabled."
fi
