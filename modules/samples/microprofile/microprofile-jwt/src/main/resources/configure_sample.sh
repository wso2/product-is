#!/bin/sh

# ----------------------------------------------------------------------------
#  Copyright 2018 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

cleanup() {
  sp_name=$1
  delete_users
  delete_roles
  delete_sp "${sp_name}" urn:deleteApplication https://localhost:9443/services/IdentityApplicationManagementService.IdentityApplicationManagementServiceHttpsSoap11Endpoint/

  return 0;
}

add_users_and_roles() {
  IS_name=$1
  IS_pass=$2
  request_data1="configs/add-role-debtor.xml"
  request_data2="configs/add-role-creditor.xml"
  request_data3="configs/add-role-viewbalance.xml"

  if [ ! -d "configs" ]
    then
      echo "configs Directory does not exists."
      return 255
  fi

  if [ ! -f "$request_data1" ]
    then
      echo "$request_data1 File does not exists."
      return 255
  fi

  if [ ! -f "$request_data2" ]
   then
      echo "$request_data2 File does not exists."
      return 255
  fi

  if [ ! -f "$request_data3" ]
    then
      echo "$request_data2 File does not exists."
      return 255
  fi

  echo
  echo "Creating a user named cameron..."

  # The following command can be used to create a user.
  curl -s -k --user "${IS_name}":"${IS_pass}" --data '{"schemas":[],"name":{"familyName":"Smith","givenName":"Cameron"},"userName":"cameron","password":"cameron123","emails":"cameron@gmail.com","addresses":{"country":"United States"}}' --header "Content-Type:application/json" -o /dev/null https://localhost:9443/wso2/scim/Users
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while creating user cameron. !!"
    echo
    return 255
  fi
  echo "** The user cameron was successfully created. **"
  echo

  echo "Creating a user named alex..."

  curl -s -k --user "${IS_name}":"${IS_pass}" --data '{"schemas":[],"name":{"familyName":"Miller","givenName":"Alex"},"userName":"alex","password":"alex123","emails":"alex@gmail.com","addresses":{"country":"United States"}}' --header "Content-Type:application/json" -o /dev/null https://localhost:9443/wso2/scim/Users
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while creating user alex. !!"
    echo
    delete_users
    echo
    return 255
  fi
  echo "** The user alex was successfully created. **"
  echo

  echo "Creating a user named john..."

  curl -s -k --user "${IS_name}":"${IS_pass}" --data '{"schemas":[],"name":{"familyName":"Williams","givenName":"John"},"userName":"john","password":"john123","emails":"john@gmail.com","addresses":{"country":"United States"}}' --header "Content-Type:application/json" -o /dev/null https://localhost:9443/wso2/scim/Users
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while creating user john. !!"
    echo
    cleanup
    echo
    return 255
  fi
  echo "** The user john was successfully created. **"
  echo

  echo "Creating a role named Debtor..."

  #The following command will add a role to the user.
  curl -s -k --user "${IS_name}":"${IS_pass}" -d @${request_data1} -H "Content-Type: text/xml" -H "SOAPAction: urn:addRole" -o /dev/null https://localhost:9443/services/RemoteUserStoreManagerService.RemoteUserStoreManagerServiceHttpsSoap11Endpoint/
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while creating role Debtor. !!"
    echo
    cleanup
    echo
    return 255
  fi
  echo "** The role Debtor was successfully created. **"
  echo

  echo "Creating a role named Creditor..."

  curl -s -k --user "${IS_name}":"${IS_pass}" -d @${request_data2} -H "Content-Type: text/xml" -H "SOAPAction: urn:addRole" -o /dev/null https://localhost:9443/services/RemoteUserStoreManagerService.RemoteUserStoreManagerServiceHttpsSoap11Endpoint/
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while creating role Creditor. !!"
    echo
    cleanup
    echo
    return 255
  fi
  echo "** The role Creditor was successfully created. **"
  echo

  echo "Creating a role named ViewBalance..."

  curl -s -k --user "${IS_name}":"${IS_pass}" -d @${request_data3} -H "Content-Type: text/xml" -H "SOAPAction: urn:addRole" -o /dev/null https://localhost:9443/services/RemoteUserStoreManagerService.RemoteUserStoreManagerServiceHttpsSoap11Endpoint/
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while creating role ViewBalance. !!"
    echo
    cleanup
    echo
    return 255
  fi
  echo "** The role ViewBalance was successfully created. **"
  echo

  return 0;
}

create_service_provider() {
  sp_name=$1
  soap_action=$2
  endpoint=$3
  request_data="configs/create-sp.xml"
  auth=$(echo "admin:admin"|base64)

  if [ ! -d "configs" ]
    then
      echo "configs Directory not exists."
      return 255
  fi

  if [ ! -f "$request_data" ]
    then
      echo "$request_data File does not exists."
      return 255
  fi

  echo "Creating Service Provider ${sp_name}..."

  # Send the SOAP request to create the new SP.
  curl -s -k -d @${request_data} -H "Authorization: Basic ${auth}" -H "Content-Type: text/xml" -H "SOAPAction: ${soap_action}" -o /dev/null "${endpoint}"
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while creating the service provider. !!"
    echo
    cleanup "${sp_name}"
    echo
    return 255
  fi
  echo "** Service Provider ${sp_name} successfully created. **"

  return 0;
}

create_update_sp() {
  sp_name=$1
  cd configs || return

  request_data1="get-sp.xml"
  request_data2="oauth-app-registration.xml"
  auth=$(echo "admin:admin"|base64)

  if [ -f "update-sp_${sp_name}.xml" ]
    then
      rm -r update-sp_"${sp_name}".xml
  fi

  if [ -f "response_unformatted.xml" ]
    then
      rm -r response_unformatted.xml
  fi

  if [ ! -f "$request_data1" ]
    then
      echo "$request_data1 File does not exists."
      return 255
  fi

  if [ ! -f "$request_data2" ]
    then
      echo "$request_data2 File does not exists."
      return 255
  fi

  touch response_unformatted.xml
  # Send the SOAP request to Get the Application.
  curl -s -k -d @${request_data1} -H "Authorization: Basic ${auth}" -H "Content-Type: text/xml" -H "SOAPAction: urn:getApplication" https://localhost:9443/services/IdentityApplicationManagementService.IdentityApplicationManagementServiceHttpsSoap11Endpoint/ > response_unformatted.xml
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while getting application details for ${sp_name}.... !!"
    echo
    cleanup "${sp_name}"
    echo
    return 255
  fi

  xmllint --format response_unformatted.xml
  app_id=$(xmllint --xpath "//*[local-name()='applicationID']/text()" response_unformatted.xml)
  rm response_unformatted.xml

  # Send the SOAP request to register OAuth Application data
  curl -s -k -d @${request_data2} -H "Authorization: Basic ${auth}" -H "Content-Type: text/xml" -H "SOAPAction: urn:registerOAuthApplicationData" -o /dev/null https://localhost:9443/services/OAuthAdminService.OAuthAdminServiceHttpsSoap11Endpoint/
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while registering OAuth application.... !!"
    echo
    cleanup "${sp_name}"
    echo
    return 255
  fi

  touch update-sp_"${sp_name}".xml
  echo "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://org.apache.axis2/xsd\"
                  xmlns:xsd1=\"http://model.common.application.identity.carbon.wso2.org/xsd\">
    <soapenv:Header/>
    <soapenv:Body>
        <xsd:updateApplication>
            <!--Optional:-->
            <xsd:serviceProvider>
                <!--Optional:-->
                <xsd1:applicationID>${app_id}</xsd1:applicationID>
                <!--Optional:-->
                <xsd1:applicationName>${sp_name}</xsd1:applicationName>
                <!--Optional:-->
                <xsd1:claimConfig>
                    <!--Optional:-->
                    <xsd1:alwaysSendMappedLocalSubjectId>false</xsd1:alwaysSendMappedLocalSubjectId>
                    <!--Zero or more repetitions:-->
                    <xsd1:claimMappings>
                        <!--Optional:-->
                        <xsd1:localClaim>
                            <!--Optional:-->
                            <xsd1:claimUri>http://wso2.org/claims/role</xsd1:claimUri>
                        </xsd1:localClaim>
                        <!--Optional:-->
                        <xsd1:mandatory>true</xsd1:mandatory>
                        <!--Optional:-->
                        <xsd1:remoteClaim>
                            <!--Optional:-->
                            <xsd1:claimUri>http://wso2.org/claims/role</xsd1:claimUri>
                        </xsd1:remoteClaim>
                        <!--Optional:-->
                        <xsd1:requested>true</xsd1:requested>
                    </xsd1:claimMappings>
                    <xsd1:claimMappings>
                        <!--Optional:-->
                        <xsd1:localClaim>
                            <!--Optional:-->
                            <xsd1:claimUri>http://wso2.org/claims/userprincipal</xsd1:claimUri>
                        </xsd1:localClaim>
                        <!--Optional:-->
                        <xsd1:mandatory>true</xsd1:mandatory>
                        <!--Optional:-->
                        <xsd1:remoteClaim>
                            <!--Optional:-->
                            <xsd1:claimUri>http://wso2.org/claims/userprincipal</xsd1:claimUri>
                        </xsd1:remoteClaim>
                        <!--Optional:-->
                        <xsd1:requested>true</xsd1:requested>
                    </xsd1:claimMappings>
                    <!--Optional:-->
                    <xsd1:localClaimDialect>true</xsd1:localClaimDialect>
                </xsd1:claimConfig>
                <!--Optional:-->
                <xsd1:description>A sample service provider for microprofile jwt sample</xsd1:description>
                <!--Optional:-->
                 <xsd1:inboundAuthenticationConfig>
                     <!--Zero or more repetitions:-->
                     <xsd1:inboundAuthenticationRequestConfigs>
                        <!--Optional:-->
                        <xsd1:inboundAuthKey>li6JMbjW6WDMKTWsRnGcjp5zcGhi</xsd1:inboundAuthKey>
                        <!--Optional:-->
                        <xsd1:inboundAuthType>oauth2</xsd1:inboundAuthType>
                        <!--Zero or more repetitions:-->
                        <xsd1:properties>
                            <!--Optional:-->
                            <xsd1:advanced>false</xsd1:advanced>
                            <!--Optional:-->
                            <xsd1:confidential>false</xsd1:confidential>
                            <!--Optional:-->
                            <xsd1:defaultValue></xsd1:defaultValue>
                            <!--Optional:-->
                            <xsd1:description></xsd1:description>
                            <!--Optional:-->
                            <xsd1:displayName></xsd1:displayName>
                            <!--Optional:-->
                            <xsd1:name>oauthConsumerSecret</xsd1:name>
                            <!--Optional:-->
                            <xsd1:required>false</xsd1:required>
                            <!--Optional:-->
                            <xsd1:type></xsd1:type>
                            <!--Optional:-->
                            <xsd1:value>NMB3EAfxh4YvSTqbb3iMkongAHjW</xsd1:value>
                        </xsd1:properties>
                     </xsd1:inboundAuthenticationRequestConfigs>
                 </xsd1:inboundAuthenticationConfig>
                <xsd1:permissionAndRoleConfig/>
                <!--Optional:-->
                <xsd1:saasApp>false</xsd1:saasApp>
            </xsd:serviceProvider>
        </xsd:updateApplication>
    </soapenv:Body>
  </soapenv:Envelope>" >> update-sp_"${sp_name}".xml
  cd ../ || return

  return 0;
}

configure_service_provider() {
  sp_name=$1
  soap_action=$2
  endpoint=$3
  auth=$(echo "admin:admin"|base64)

  if [ ! -d "configs" ]
    then
      echo "configs Directory does not exists."
      return 255
  fi

  create_update_sp "${sp_name}"
  request_data="configs/update-sp_${sp_name}.xml"

  if [ ! -f "$request_data" ]
    then
      echo "$request_data File does not exists."
      return 255
  fi

  # Send the SOAP request to Update the Application.
  curl -s -k -d @"${request_data}" -H "Authorization: Basic ${auth}" -H "Content-Type: text/xml" -H "SOAPAction: ${soap_action}" -o /dev/null "${endpoint}"
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while updating application ${sp_name}.... !!"
    echo
    cleanup "${sp_name}"
    echo
    return 255
  fi
  echo "** Successfully updated the application ${sp_name}. **"

  rm configs/update-sp_"${sp_name}".xml

  return 0;
}

delete_sp() {
  sp_name=$1
  soap_action=$2
  endpoint=$3
  auth=$(echo "admin:admin"|base64)
  request_data="configs/cleanup/delete-sp.xml"

  if [ ! -d "configs/cleanup" ]
    then
      echo "configs/cleanup Directory not exists."
      return 255
  fi

  if [ ! -f "$request_data" ]
    then
      echo "$request_data File does not exists."
      return 255
  fi

  echo
  echo "Deleting Service Provider ${sp_name}..."

  # Send the SOAP request to delete a SP.
  curl -s -k -d @${request_data} -H "Authorization: Basic ${auth}" -H "Content-Type: text/xml" -H "SOAPAction: ${soap_action}" -o /dev/null "${endpoint}"
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while deleting the service provider. !!"
    echo
    return 255
  fi
  echo "** Service Provider ${sp_name} successfully deleted. **"

  return 0;
}

delete_users() {
  request_data1="configs/cleanup/delete-cameron.xml"
  request_data2="configs/cleanup/delete-alex.xml"
  request_data3="configs/cleanup/delete-john.xml"
  auth=$(echo "admin:admin"|base64)

  echo
  echo "Deleting the user named cameron..."

  # Send the SOAP request to delete the user.
  curl -s -k -d @${request_data1} -H "Authorization: Basic ${auth}" -H "Content-Type: text/xml" -H "SOAPAction: urn:deleteUser" -o /dev/null https://localhost:9443/services/RemoteUserStoreManagerService.RemoteUserStoreManagerServiceHttpsSoap11Endpoint/
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while deleting the user cameron. !!"
    echo
    return 255
  fi
  echo "** The user cameron was successfully deleted. **"
  echo
  echo "Deleting the user named alex..."

  # Send the SOAP request to delete the user.
  curl -s -k -d @${request_data2} -H "Authorization: Basic ${auth}" -H "Content-Type: text/xml" -H "SOAPAction: urn:deleteUser" -o /dev/null https://localhost:9443/services/RemoteUserStoreManagerService.RemoteUserStoreManagerServiceHttpsSoap11Endpoint/
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while deleting the user alex. !!"
    echo
    return 255
  fi
  echo "** The user alex was successfully deleted. **"
  echo
  echo "Deleting the user named john..."

  # Send the SOAP request to delete the user.
  curl -s -k -d @${request_data3} -H "Authorization: Basic ${auth}" -H "Content-Type: text/xml" -H "SOAPAction: urn:deleteUser" -o /dev/null https://localhost:9443/services/RemoteUserStoreManagerService.RemoteUserStoreManagerServiceHttpsSoap11Endpoint/
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while deleting the user john. !!"
    echo
    return 255
  fi
  echo "** The user john was successfully deleted. **"
  echo

  return 0;
}

delete_roles() {
  request_data1="configs/cleanup/delete-role-debtor.xml"
  request_data2="configs/cleanup/delete-role-creditor.xml"
  request_data3="configs/cleanup/delete-role-viewbalance.xml"
  auth=$(echo "admin:admin"|base64)

  echo "Deleting the role named Debtor..."
  # Send the SOAP request to delete the role.
  curl -s -k -d @${request_data1} -H "Authorization: Basic ${auth}" -H "Content-Type: text/xml" -H "SOAPAction: urn:deleteRole" -o /dev/null https://localhost:9443/services/RemoteUserStoreManagerService.RemoteUserStoreManagerServiceHttpsSoap11Endpoint/
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while deleting the role Debtor. !!"
    echo
    return 255
  fi
  echo "** The role Debtor was successfully deleted. **"
  echo

  echo "Deleting the role named Creditor..."
  # Send the SOAP request to delete the role.
  curl -s -k -d @${request_data2} -H "Authorization: Basic ${auth}" -H "Content-Type: text/xml" -H "SOAPAction: urn:deleteRole" -o /dev/null https://localhost:9443/services/RemoteUserStoreManagerService.RemoteUserStoreManagerServiceHttpsSoap11Endpoint/
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while deleting the role Creditor. !!"
    echo
    return 255
  fi
  echo "** The role Creditor was successfully deleted. **"
  echo

  echo "Deleting the role named ViewBalance..."

  # Send the SOAP request to delete the role.
  curl -s -k -d @${request_data3} -H "Authorization: Basic ${auth}" -H "Content-Type: text/xml" -H "SOAPAction: urn:deleteRole" -o /dev/null https://localhost:9443/services/RemoteUserStoreManagerService.RemoteUserStoreManagerServiceHttpsSoap11Endpoint/
  res=$?
  if test "${res}" != "0"; then
    echo "!! Problem occurred while deleting the role ViewBalance. !!"
    echo
    return 255
  fi
  echo "** The role ViewBalance was successfully deleted. **"
  echo

  return 0;
}

echo "CONFIGURING MICROPROFILE JWT SAMPLE"

add_users_and_roles admin admin
create_service_provider microprofile_jwt_sample urn:createApplication https://localhost:9443/services/IdentityApplicationManagementService.IdentityApplicationManagementServiceHttpsSoap11Endpoint/
configure_service_provider microprofile_jwt_sample urn:updateApplication https://localhost:9443/services/IdentityApplicationManagementService.IdentityApplicationManagementServiceHttpsSoap11Endpoint/

echo
echo "If you have finished trying out the sample, you can clean the resources now."
echo "Do you want to clean up the setup?"
echo
echo "Press y - YES"
echo "Press n - NO"
echo
read -r clean

  case ${clean} in
    [Yy]* )
      cleanup microprofile_jwt_sample;;
    [Nn]* )
      exit;;
    * ) echo "Please answer yes or no.";;
  esac
