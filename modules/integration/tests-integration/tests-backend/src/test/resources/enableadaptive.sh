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
#   ASM_VERSION   	ASM Util, Commons Version.
#
# -----------------------------------------------------------------------------

CARBON_HOME=$1

NASHORN_VERSION=15.3;
ASM_VERSION=9.2;

LIB_REPO=$CARBON_HOME/repository/components/lib

if compgen -G "$CARBON_HOME/repository/components/lib/nashorn-core-*.jar" > /dev/null; then
    location=$(find "$CARBON_HOME/repository/components/lib/" ~+ -type f -name "nashorn-core-*.jar" | head -1)
    full_artifact_name=$(basename ${location})
    artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $3}')
    LOCAL_NASHORN_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
    if [ "$NASHORN_VERSION" = "$LOCAL_NASHORN_VERSION" ]; then
    	echo "Updated nashorn exists. No need to download."
    else
    	echo "Updated nashorn not found. Updating OpenJDK Nashorn."
    	rm $location
    	mvn dependency:get -Dartifact=org.openjdk.nashorn:nashorn-core:$NASHORN_VERSION -Ddest=$LIB_REPO
    fi
else
   echo "OpenJDK Nashorn not found. Downloading OpenJDK Nashorn."
   mvn dependency:get -Dartifact=org.openjdk.nashorn:nashorn-core:$NASHORN_VERSION -Ddest=$LIB_REPO
fi

if compgen -G "$CARBON_HOME/repository/components/lib/asm-util-*.jar" > /dev/null; then
    location=$(find "$CARBON_HOME/repository/components/lib/" ~+ -type f -name "asm-util-*.jar" | head -1)
    full_artifact_name=$(basename ${location})
    artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $3}')
    LOCAL_ASM_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
    if [ "$ASM_VERSION" = "$LOCAL_ASM_VERSION" ]; then
    	echo "Updated asm util exists. No need to download."
    else
    	echo "Updated asm util not found. Updating asm."
    	rm $location
    	mvn dependency:get -Dartifact=org.ow2.asm:asm-util:$ASM_VERSION -Ddest=$LIB_REPO
    fi
else
   echo "asm util not found. Downloading asm."
  mvn dependency:get -Dartifact=org.ow2.asm:asm-util:$ASM_VERSION -Ddest=$LIB_REPO
fi

echo "Updating Adaptive Authentication Dependencies finished."
