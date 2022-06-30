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

NASHORN_VERSION=15.3;
ASM_VERSION=9.2;

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set CARBON_HOME if not already set
[ -z "$CARBON_HOME" ] && CARBON_HOME=`cd "$PRGDIR/.." ; pwd`

if compgen -G "$CARBON_HOME/lib/nashorn-core-*.jar" > /dev/null; then
    location=$(find "$CARBON_HOME/lib/" ~+ -type f -name "nashorn-core-*.jar" | head -1)
    full_artifact_name=$(basename ${location})
    artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $3}')
    LOCAL_NASHORN_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
    if [ "$NASHORN_VERSION" = "$LOCAL_NASHORN_VERSION" ]; then
    	echo "Updated nashorn exists. No need to download."
    else 
    	echo "Updated nashorn not found. Updating OpenJDK Nashorn."
    	rm $location
    	wget -P $CARBON_HOME/lib https://repo1.maven.org/maven2/org/openjdk/nashorn/nashorn-core/$NASHORN_VERSION/nashorn-core-$NASHORN_VERSION.jar
    fi
else
   echo "OpenJDK Nashorn not found. Downloading OpenJDK Nashorn."
   wget -P $CARBON_HOME/lib https://repo1.maven.org/maven2/org/openjdk/nashorn/nashorn-core/$NASHORN_VERSION/nashorn-core-$NASHORN_VERSION.jar
fi

if compgen -G "$CARBON_HOME/lib/asm-*.jar" > /dev/null; then
    location=$(find "$CARBON_HOME/lib/" ~+ -type f -name "asm-*.jar" | head -1)
    full_artifact_name=$(basename ${location})
    artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $2}')
    LOCAL_ASM_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
    if [ "$ASM_VERSION" = "$LOCAL_ASM_VERSION" ]; then
    	echo "Updated asm exists. No need to download."
    else 
    	echo "Updated asm not found. Updating asm."
    	rm $location
        wget -P $CARBON_HOME/lib https://repo1.maven.org/maven2/org/ow2/asm/asm/$ASM_VERSION/asm-$ASM_VERSION.jar
    fi
else
   echo "asm not found. Downloading asm."
   wget -P $CARBON_HOME/lib https://repo1.maven.org/maven2/org/ow2/asm/asm/$ASM_VERSION/asm-$ASM_VERSION.jar
fi

if compgen -G "$CARBON_HOME/lib/asm-commons-*.jar" > /dev/null; then
    location=$(find "$CARBON_HOME/lib/" ~+ -type f -name "asm-commons-*.jar" | head -1)
    full_artifact_name=$(basename ${location})
    artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $3}')
    LOCAL_ASM_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
    if [ "$ASM_VERSION" = "$LOCAL_ASM_VERSION" ]; then
    	echo "Updated asm commons exists. No need to download."
    else  
    	echo "Updated asm commons not found. Updating asm."
    	rm $location
        wget -P $CARBON_HOME/lib https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/$ASM_VERSION/asm-commons-$ASM_VERSION.jar
    fi
else
   echo "asm commons not found. Downloading asm."
   wget -P $CARBON_HOME/lib https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/$ASM_VERSION/asm-commons-$ASM_VERSION.jar
fi

if compgen -G "$CARBON_HOME/lib/asm-util-*.jar" > /dev/null; then
    location=$(find "$CARBON_HOME/lib/" ~+ -type f -name "asm-util-*.jar" | head -1)
    full_artifact_name=$(basename ${location})
    artifact_name=$(echo "$full_artifact_name" | awk -F'-' '{print $3}')
    LOCAL_ASM_VERSION=$(echo "$artifact_name" | awk -F'.' '{print $1 "." $2}')
    if [ "$ASM_VERSION" = "$LOCAL_ASM_VERSION" ]; then
    	echo "Updated asm util exists. No need to download."
    else  
    	echo "Updated asm util not found. Updating asm."
    	rm $location
        wget -P $CARBON_HOME/lib https://repo1.maven.org/maven2/org/ow2/asm/asm-util/$ASM_VERSION/asm-util-$ASM_VERSION.jar
    fi
else
   echo "asm util not found. Downloading asm."
   wget -P $CARBON_HOME/lib https://repo1.maven.org/maven2/org/ow2/asm/asm-util/$ASM_VERSION/asm-util-$ASM_VERSION.jar
fi

