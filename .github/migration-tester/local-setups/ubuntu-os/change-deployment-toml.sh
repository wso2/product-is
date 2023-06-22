#!/bin/bash

for file in $(find $DEPLOYMENT_PATH -type f -name 'deployment.toml');
do
cat $DEPLOYMENT_AUTOMATION_MYSQL > $file;

done
