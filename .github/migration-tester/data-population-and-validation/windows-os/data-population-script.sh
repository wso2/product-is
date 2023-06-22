#!/bin/bash
                                                            # relpace this file with      find /path/to/folder -name "*.sh" -type f -exec sh {} \;
# Edit the env file to change parameters.
# Source env file
. ./env.sh
echo "\033[1;33mEnv file sourced successfully\033[0m"

cd "$USER_CREATION"
sh create-user.sh
sh create-bulk-users.sh

cd "$TENANT_CREATION"
sh create-tenant.sh
sh register-an-app-in-a-tenant.sh
sh get-access-token-tenantwise.sh

#chmod +x tenant-soap.sh
#sh tenant-soap.sh
#chmod +x create-tenant-soapAPI.sh
#sh create-tenant-soapAPI.sh
#chmod +x get-access-token-tenantwise-soapAPI.sh
#sh get-access-token-tenantwise-soapAPI.sh

cd "$USERSTORE_CREATION"
sh create-userstore.sh
sh create-user-in-userstore.sh
#chmod +x create-userstore-soapAPI.sh
#sh create-userstore-soapAPI.sh

cd "$GROUP_CREATION"
sh create-group.sh
sh create-groups-with-users.sh

cd "$SP_CREATION"
sh register-a-service-provider.sh
sh create-user-in-a-service-provider.sh
sh register-a-service-provider-get-access-token.sh

