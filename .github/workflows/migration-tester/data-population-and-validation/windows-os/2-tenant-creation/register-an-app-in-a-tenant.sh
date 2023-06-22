#!/bin/bash

#curl -k --location --request POST "$APPLICATION_EP" \
#--header 'Access-Control-Allow-Origin: '$TENANT_URL'' \
#--header 'Accept: application/json' \
#--header 'Referer;' \
#--header 'Authorization: Basic aWl0QGlpdC5jb206aWl0MTIz' \
#--header 'User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36' \
#--header 'Content-Type: application/json' \
#--data-raw '{"name":"'$APP_NAME'","description":"Manually configure the inbound authentication protocol, authentication flow, etc.","templateId":"custom-application"}'



# set the server url
server_url=https://localhost:9443

# set the tenant domain
tenant_domain=iit@iit.com

# set the application name
application_name=MigrationApp

# Get the tenant Id 
tenant_id=$(curl -k -X GET -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" ${server_url}/api/identity/user/v0.9/tenants?tenantDomain=${tenant_domain} | jq -r '.tenantId')

# create the service provider
curl -k -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -d "{
    \"applicationName\":\"${application_name}\",
    \"description\":\"Application for ${application_name}\",
    \"saasApp\":true,
    \"inboundProvisioningConfig\": {
    \"provisioningEnabled\": true,
    \"deprovisioningEnabled\": true,
    \"userstore\": \"PRIMARY\"
    },
    \"owner\": \"admin\",
    \"appOwner\":\"admin\",
    \"tenantId\":\"${tenant_id}\",
    \"permissionAndRoleConfig\":{
    \"roleMappings\":[]
    }
}" ${server_url}/api/identity/application/v1.0/service-providers

echo "\033[0;36mA Service provider has been generated in tenant=$TENANT_NAME. Application name=$APP_NAME\033[0m"
echo
