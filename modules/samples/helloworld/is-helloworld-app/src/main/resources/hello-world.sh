#
# Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#

#!/usr/bin/env bash

# check JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
echo "You must set the JAVA_HOME variable before running the client."
exit 1
fi

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

[ -z "$APP_HOME" ] && APP_HOME=`cd "$PRGDIR/.." ; pwd`

APP_PORT="9090"

help_text() {
echo
echo Hello World is a simple OAuth app that can be used to quickly test single sign-on.
echo
echo Usage: sh hello-world.sh [OPTIONS]
echo
echo Available Options:
echo
echo "   -h, --help       Display this help and exit."
echo "   -H, --idphost    Hostname of the Identity Server. Default: localhost"
echo "   -P, --idpport    Port of the Identity Server. Default: 9443"
echo "   -a, --apphost    Hostname of the application. Set this if the application is"
echo "                    accessed through a hostname other than localhost. This will be used to"
echo "                    construct the redirect URI for the application"
echo "   -p, --appport    Port which the application should start in. Default: 9090"
echo "   -c, --appcontext URL context of the application. This will be use to construct app redirect URL. Default: /"
echo "                    e.g: If the app is running as http://localhost:9090/hello/, provide the context as /hello"
echo "   -u, --username   Admin username of Identity Server. This will be used to authenticate with APIs. Default: admin"
echo "   -p, --password   Password of admin user. This will be used to authenticate with APIs. Default: admin"
echo

return 0;
}


while [[ $# -gt 0 ]]
do
key="$1"

case $key in
-H|--idphost)
IDP_HOST="$2"
shift # past argument
shift # past value
;;
-P|--idpport)
IDP_PORT="$2"
shift # past argument
shift # past value
;;
-a|--apphost)
APP_HOST="$2"
shift # past argument
shift # past value
;;
-p|--appport)
APP_PORT="$2"
shift # past argument
shift # past value
;;
-c|--appcontext)
APP_CONTEXT="$2"
shift # past argument
shift # past value
;;
-u|--username)
USERNAME="$2"
shift # past argument
shift # past value
;;
-s|--password)
PASSWORD="$2"
shift # past argument
shift # past value
;;
-h|--help)
shift
help_text
exit 1
;;

*)    # unknown option
shift # past argument
;;
esac
done

java \
-Didp.hostname="$IDP_HOST" \
-Didp.port="$IDP_PORT" \
-Dapp.hostname="$APP_HOST" \
-Dapp.port="$APP_PORT" \
-Dapp.context="$APP_CONTEXT" \
-Dusername="$USERNAME" \
-Dpassword="$PASSWORD" \
-Dapp.home="$APP_HOME" \
-jar $APP_HOME/lib/jetty-runner.jar --port $APP_PORT $APP_HOME/webapp/hello-world.war
