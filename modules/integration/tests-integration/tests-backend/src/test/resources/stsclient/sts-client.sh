#!/bin/sh

if [ -z "$JAVA_HOME" ]; then
  echo "You must set the JAVA_HOME variable before running the client."
  exit 1
fi

export CLASSPATH=$CLASSPATH:../../../../../../samples/sts/sts-client/lib/*:../../../../../../samples/sts/sts-client/target/org.wso2.carbon.identity.samples.sts-4.2.0-SNAPSHOT.jar
$JAVA_HOME/bin/java org.wso2.carbon.identity.samples.sts.Client
