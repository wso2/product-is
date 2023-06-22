#!/bin/bash

# WSO2 Identity Server OAuth token endpoint
token_endpoint="https://localhost:9443/oauth2/token"

# WSO2 Identity Server admin credentials
admin_username="admin"
admin_password="admin"

# New tenant details
new_tenant_domain="example.com"
new_tenant_admin_username="newadmin"
new_tenant_admin_password="newadmin123"
new_tenant_admin_firstname="New"
new_tenant_admin_lastname="Admin"
new_tenant_admin_email="admin@example.com"

# Service provider details
service_provider_client_id="your_client_id"
service_provider_client_secret="your_client_secret"

# Create the SOAP request XML
soap_request=$(cat <<EOF
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://services.mgt.tenant.carbon.wso2.org" xmlns:xsd="http://beans.common.stratos.carbon.wso2.org/xsd">
   <soapenv:Header/>
   <soapenv:Body>
      <ser:addTenant>
         <ser:tenantInfoBean>
            <xsd:active>true</xsd:active>
            <xsd:admin>${new_tenant_admin_username}</xsd:admin>
            <xsd:adminPassword>${new_tenant_admin_password}</xsd:adminPassword>
            <xsd:email>${new_tenant_admin_email}</xsd:email>
            <xsd:first>${new_tenant_admin_firstname}</xsd:first>
            <xsd:last>${new_tenant_admin_lastname}</xsd:last>
            <xsd:tenantDomain>${new_tenant_domain}</xsd:tenantDomain>
         </ser:tenantInfoBean>
      </ser:addTenant>
   </soapenv:Body>
</soapenv:Envelope>
EOF
)

# Send the SOAP request and capture the response
soap_response=$(curl -k -s -u "${admin_username}:${admin_password}" -H "Content-Type: text/xml" -d "${soap_request}" "${soap_api_url}")

# Check the SOAP response for errors
if echo "${soap_response}" | grep -q "<ns:addTenantResponse>"; then
    echo "Tenant created successfully."
    
    # Retrieve an access token for the service provider
    token_response=$(curl -k -s -u "${service_provider_client_id}:${service_provider_client_secret}" -d "grant_type=password&username=${new_tenant_admin_username}&password=${new_tenant_admin_password}" "${token_endpoint}")
    
    # Extract the access token from the token response
    access_token=$(echo "${token_response}" | jq -r '.access_token')
    
    if [[ -n "${access_token}" && "${access_token}" != "null" ]]; then
        echo "Access token obtained successfully:"
        echo "${access_token}"
    else
        echo "Failed to obtain access token. Token response:"
        echo "${token_response}"
    fi
else
    echo "Failed to create tenant. Error response:"
    echo "${soap_response}"
fi
