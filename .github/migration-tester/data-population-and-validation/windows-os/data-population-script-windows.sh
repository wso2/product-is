#!/bin/bash

# Edit the env file to change parameters.

cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\02-POC\macos\data-population-and-validation\1-user-creation"
sh create-user.sh
echo "\033[0;34mA user has been created in Identity Server.User name="$GIVEN_USER_NAME"\033[0;34m"

sh create-bulk-users.sh
echo "\033[0;34mThis is a bulk import of users to IS.User roles also have been assigned to some users as well.\033[0;34m"

cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\02-POC\macos\data-population-and-validation\2-tenant-creation"

sh create-tenant.sh
echo "\033[0;34mA tenant has been created with a user. Tenant name="$TENANT_USER_NAME".User name="$TENANT_USER_NAME"\033[0;34m"
echo "\033[0;34mA tenant has been created with a user. Tenant name=iit.User name=iit@iit.com\033[0;34m"

sh register-an-app-in-a-tenant.sh
echo "\033[0;34mA Service provider has been generated in tenant="$TENANT_NAME". Application name ="$APP_NAME"\033[0;34m"

sh get-access-token-tenantwise.sh
echo "\033[0;34mSome tokens have been generated from a registered application in the tenant="$TENANT_NAME"\033[0;34m"

cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\02-POC\macos\data-population-and-validation\3-userstore-creation"

sh create-userstore.sh
echo "\033[0;34mA userstore has been created in Identity Server. Userstore name="$USERSTORE_NAME"\033[0;34m"

sh create-user-in-userstore.sh
echo "\033[0;34mA user has been created in userstore. User name="$USERSTORE_USER_NAME",Group name="$USERSTORE_GROUP_NAME"\033[0;34m"


cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\02-POC\macos\data-population-and-validation\5-group-creation"

sh create-group.sh
echo "\033[0;34mA group has been created.Group name="$GROUP_NAME"\033[0;34m"

sh create-groups-with-users.sh
echo "\033[0;34mA group called "$GROUP_DISPLAY_NAME" has been created with a user-ID="$GROUP_USER_ID"\033[0;34m"

cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\02-POC\macos\data-population-and-validation\4-service-provider-creation"

sh register-a-service-provider.sh
echo "\033[0;34mA new service provider has been registered. Service provider name="$CLIENT_NAME"\033[0;34m"

sh create-user-in-a-service-provider.sh
echo "\033[0;34mA user has been created in service provider. Service provider name= Jayana. Name of user created in SP="$SP_USER_NAME"\033[0;34m"

sh register-a-service-provider-get-access-token.sh
echo "\033[0;34mAn oAuth token has been generated.Relevant Service provider=sampleapp\033[0;34m"
