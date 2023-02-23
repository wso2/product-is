#! /bin/bash
# ----------------------------------------------------------------------------
#  Copyright 2023 WSO2, LLC. http://www.wso2.org
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

BC_FIPS_VERSION=1.0.2.3;
BCPKIX_FIPS_VERSION=1.0.7;
BCPROV_JDK15ON_VERSION=1.70.0.wso2v1;
BCPKIX_JDK15ON_VERSION=1.70.0.wso2v1;

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set CARBON_HOME if not already set
[ -z "$CARBON_HOME" ] && CARBON_HOME=`cd "$PRGDIR/.." ; pwd`
echo $CARBON_HOME

DISABLE=$1;
bundles_info="$CARBON_HOME/repository/components/default/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info";

if [ "$DISABLE" = "DISABLE" ] || [ "$DISABLE" = "disable" ]; then
	if [ ! -e $CARBON_HOME/repository/components/plugins/bcprov-jdk15on*.jar ]; then
		echo "Downloading required bcprov-jdk15on jar : bcprov-jdk15on-$BCPROV_JDK15ON_VERSION"
		curl https://maven.wso2.org/nexus/content/repositories/releases/org/wso2/orbit/org/bouncycastle/bcprov-jdk15on/$BCPROV_JDK15ON_VERSION/bcprov-jdk15on-$BCPROV_JDK15ON_VERSION.jar -o $CARBON_HOME/repository/components/plugins/bcprov-jdk15on_$BCPROV_JDK15ON_VERSION.jar
	fi
	if [ ! -e $CARBON_HOME/repository/components/plugins/bcpkix-jdk15on*.jar ]; then
		echo "Downloading required bcpkix-jdk15on jar : bcprov-jdk15on-$BCPKIX_JDK15ON_VERSION"
		curl https://maven.wso2.org/nexus/content/repositories/releases/org/wso2/orbit/org/bouncycastle/bcpkix-jdk15on/$BCPKIX_JDK15ON_VERSION/bcpkix-jdk15on-$BCPKIX_JDK15ON_VERSION.jar -o $CARBON_HOME/repository/components/plugins/bcpkix-jdk15on_$BCPKIX_JDK15ON_VERSION.jar
	fi
	bcprov_text="bcprov-jdk15on1,$BCPKIX_JDK15ON_VERSION,../plugins/bcprov-jdk15on_$BCPKIX_JDK15ON_VERSION.jar,4,true";
	bcpkix_text="bcpkix-jdk15on1,$BCPROV_JDK15ON_VERSION,../plugins/bcpkix-jdk15on_$BCPROV_JDK15ON_VERSION.jar,4,true";
	if ! grep -q "$bcprov_text" "$bundles_info" ; then
		echo  $bcprov_text >> $bundles_info;
	fi
	if ! grep -q "$bcpkix_text" "$bundles_info" ; then
		echo  $bcpkix_text >> $bundles_info;
	fi
	if [ -f $CARBON_HOME/repository/components/lib/bc-fips*.jar ]; then
   		echo "Remove existing bc-fips_$BC_FIPS_VERSION jar from lib folder."
   		rm rm $CARBON_HOME/repository/components/lib/bc-fips*.jar 2> /dev/null
		echo "bc-fips_$BC_FIPS_VERSION Removed from component/plugin."
   	fi
   	if [ -f $CARBON_HOME/repository/components/lib/bcpkix-fips*.jar ]; then
   		echo "Remove existing bcpkix-fips_$BCPKIX_JDK15ON_VERSION jar from lib folder."
   		rm rm $CARBON_HOME/repository/components/lib/bcpkix-fips*.jar 2> /dev/null
   		echo "bcpkix-fips_$BCPKIX_JDK15ON_VERSION Removed from component/lib."
   	fi
   	if [ -f $CARBON_HOME/repository/components/dropins/bc_fips*.jar ]; then
   		echo "Remove existing bc-fips_$BC_FIPS_VERSION jar from dropins folder."
   		rm rm $CARBON_HOME/repository/components/dropins/bc_fips*.jar 2> /dev/null
   		echo "bc-fips_$BC_FIPS_VERSION Removed from component/plugin."
   	fi
   	if [ -f $CARBON_HOME/repository/components/dropins/bcpkix_fips*.jar ]; then
   		echo "Remove existing bcpkix_fips_$BCPKIX_JDK15ON_VERSION jar from dropins folder."
   		rm rm $CARBON_HOME/repository/components/dropins/bcpkix_fips*.jar 2> /dev/null
		echo "bcpkix_fips_$BCPKIX_JDK15ON_VERSION Removed from component/dropins."
   	fi

else
	if [ -f $CARBON_HOME/repository/components/plugins/bcprov-jdk15on*.jar ]; then
   		echo "Remove existing bcprov-jdk15on_$BCPROV_JDK15ON_VERSION jar from plugins folder."
   		rm rm $CARBON_HOME/repository/components/plugins/bcprov-jdk15on*.jar 2> /dev/null
   		echo "bcprov-jdk15on Removed from component/plugin."
	fi
	if [ -f $CARBON_HOME/repository/components/plugins/bcpkix-jdk15on*.jar ]; then
   		echo "Remove existing bcpkix-jdk15on_BCPKIX_JDK15ON_VERSION jar from plugins folder."
   		rm rm $CARBON_HOME/repository/components/plugins/bcpkix-jdk15on*.jar 2> /dev/null
   		echo "bcpkix-jdk15on Removed from component/plugin."
	fi

	sed -i '/bcprov-jdk15on/d' $CARBON_HOME/repository/components/default/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info
	sed -i '/bcpkix-jdk15on/d' $CARBON_HOME/repository/components/default/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info

	if [ ! -e $CARBON_HOME/repository/components/lib/bc-fips*.jar ]; then
		echo "Downloading required bc-fips jar : bc-fips-$BC_FIPS_VERSION"
		curl https://repo1.maven.org/maven2/org/bouncycastle/bc-fips/$BC_FIPS_VERSION/bc-fips-$BC_FIPS_VERSION.jar -o $CARBON_HOME/repository/components/lib/bc-fips-$BC_FIPS_VERSION.jar
	fi

	if [ ! -e $CARBON_HOME/repository/components/lib/bcpkix-fips*.jar ]; then
		echo "Downloading required bcpkix-fips jar : bcpkix-fips-$BCPKIX_FIPS_VERSION"
		curl https://repo1.maven.org/maven2/org/bouncycastle/bcpkix-fips/$BCPKIX_FIPS_VERSION/bcpkix-fips-$BCPKIX_FIPS_VERSION.jar -o $CARBON_HOME/repository/components/lib/bcpkix-fips-$BCPKIX_FIPS_VERSION.jar
	fi

fi

echo "Please restart the server."
