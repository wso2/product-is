#!/bin/bash

# Define colors
RED='\033[0;31m'
GREEN='\033[1;38;5;206m'
PURPLE='\033[1;35m'
BOLD='\033[1m'
NC='\033[0m' # No Color

os=$1

# Set deployment file and path based on OS
if [ "$os" = "ubuntu-latest" ]; then

  chmod +x env.sh
  . "/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/env.sh"
  echo "${GREEN}==> Env file for Ubuntu sourced successfully"
fi
if [ "$os" = "macos-latest" ]; then

  chmod +x env.sh
  source "/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/env.sh"
  echo "${GREEN}==> Env file for Mac sourced successfully${RESET}"

fi
  
# WSO2 Identity Server admin credentials
admin_username="admin"
admin_password="admin"

# User Store details
userstore_domain="myuserstore"
userstore_class="org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager"
userstore_properties="
<Property name=\"driverName\">com.mysql.jdbc.Driver</Property>
<Property name=\"url\">jdbc:mysql://localhost:3306/myuserstoredb</Property>
<Property name=\"userName\">dbuser</Property>
<Property name=\"password\">dbpassword</Property>
<Property name=\"Disabled\">false</Property>
<Property name=\"ReadOnly\">false</Property>
<Property name=\"MaxUserNameListLength\">100</Property>
<Property name=\"IsEmailUserName\">false</Property>
<Property name=\"DomainCalculation\">default</Property>
<Property name=\"StoreSaltedPassword\">true</Property>
<Property name=\"ReadGroups\">true</Property>
<Property name=\"WriteGroups\">true</Property>
<Property name=\"UsernameJavaRegEx\">^[\S]{5,30}$</Property>
<Property name=\"PasswordJavaRegEx\">^[\S]{5,30}$</Property>
<Property name=\"RolenameJavaRegEx\">^[\S]{5,30}$</Property>
<Property name=\"ReadOnly\">false</Property>
<Property name=\"UserRolesCacheEnabled\">true</Property>
<Property name=\"PasswordDigest\">SHA-256</Property>
<Property name=\"MultiAttributeSeparator\">,</Property>
"

# Create the SOAP request XML
soap_request=$(
   cat <<EOF
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://services.mgt.user.carbon.wso2.org" xmlns:xsd="http://beans.common.stratos.carbon.wso2.org/xsd">
   <soapenv:Header/>
   <soapenv:Body>
      <ser:addUserStore>
         <ser:userStoreInfo>
            <xsd:domain>${userstore_domain}</xsd:domain>
            <xsd:className>${userstore_class}</xsd:className>
            <xsd:properties>${userstore_properties}</xsd:properties>
         </ser:userStoreInfo>
      </ser:addUserStore>
   </soapenv:Body>
</soapenv:Envelope>
EOF
)

# WSO2 Identity Server SOAP admin service endpoint
soap_endpoint="https://localhost:9443/services/UserStoreManager"

# Send the SOAP request and capture the response
soap_response=$(curl -k -s -u "${admin_username}:${admin_password}" -H "Content-Type: text/xml" -d "${soap_request}" "${soap_endpoint}")

# Check the SOAP response for errors
if echo "${soap_response}" | grep -q "<ns:addUserStoreResponse>"; then
   echo "User store created successfully."
else
   echo "Failed to create user store. Error response:"
   echo "${soap_response}"
fi
