#!/bin/sh

if [ -z "$JAVA_HOME" ]; then
  echo "You must set the JAVA_HOME variable before running the client."
  exit 1
fi


export CLASSPATH=$CLASSPATH:target/*:target/classes/lib/*
$JAVA_HOME/bin/java org.wso2.remoteum.sample.RemoteUMClient $@

