#!/bin/bash

# Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -o xtrace
DIR=$2
export DATA_BUCKET_LOCATION=$DIR

mvn clean install

echo "Copying surefire-reports to data bucket"

#mkdir -p ${DIR}/1-user-self-registration-to-an-application/1.1-user-registration-with-web-application-itself/1.1.1-provision-user-using-SCIM2
#cp -r 1.1-user-registration-with-web-application-itself/1.1.1-provision-user-using-SCIM2/target/surefire-reports ${DIR}/1-user-self-registration-to-an-application/1.1-user-registration-with-web-application-itself/1.1.1-provision-user-using-SCIM2

mkdir -p ${DIR}/1-user-self-registration-to-an-application/1.1-user-registration-with-web-application-itself/1.1.2-provision-user-using-SCIM1.1
cp -r 1.1-user-registration-with-web-application-itself/1.1.2-provision-user-using-SCIM1.1/target/surefire-reports ${DIR}/1-user-self-registration-to-an-application/1.1-user-registration-with-web-application-itself/1.1.2-provision-user-using-SCIM1.1

ls ${DIR}




