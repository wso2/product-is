#!/bin/bash

# WSO2 Identity Server admin credentials
admin_username="admin"
admin_password="admin"

# Tenant details
tenant_domain="sample.com"
admin_username="admin"
admin_password="admin"

# Create the SOAP request XML
soap_request=$(cat <<EOF
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://services.mgt.tenant.carbon.wso2.org" xmlns:xsd="http://beans.common.stratos.carbon.wso2.org/xsd">
   <soapenv:Header/>
   <soapenv:Body>
      <ser:addTenant>
         <!--Optional:-->
         <ser:tenantInfoBean>
            <!--Optional:-->
            <xsd:active>true</xsd:active>
            <!--Optional:-->
            <xsd:admin>jayanag</xsd:admin>
            <!--Optional:-->
            <xsd:adminPassword>jayanagpw</xsd:adminPassword>
            <!--Optional:-->
            <xsd:email>jayanag@examplestest.com</xsd:email>
            <!--Optional:-->
            <xsd:firstname>First</xsd:firstname>
            <!--Optional:-->
            <xsd:lastname>Last</xsd:lastname>
            <!--Optional:-->
            <xsd:tenantDomain>examplestest.com</xsd:tenantDomain>
         </ser:tenantInfoBean>
      </ser:addTenant>
   </soapenv:Body>
</soapenv:Envelope>
EOF
)

# WSO2 Identity Server SOAP admin service endpoint
soap_endpoint="https://localhost:9443/services/TenantMgtAdminService?wsdl"

# Send the SOAP request and capture the response
soap_response=$(curl -k -s -u "${admin_username}:${admin_password}" -H "Content-Type: text/xml" -d "${soap_request}" "${soap_endpoint}")

# Check the SOAP response for errors
if echo "${soap_response}" | grep -q "<ns:addTenantResponse>"; then
    echo "Tenant created successfully."
else
    echo "Failed to create tenant. Error response:"
    echo "${soap_response}"
fi

sleep 15
