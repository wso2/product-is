#!/bin/bash

set -o xtrace
DIR=$2
export DATA_BUCKET_LOCATION=$DIR

mvn clean install

echo "Copying surefire-reports to data bucket"

cp -r 1.1-user-registration-with-web-application-itself/1.1.1-provision-user-using-SCIM2/target/surefire-reports ${DIR}
ls ${DIR}




