#!/bin/bash

# WINDOWS OS MYSQL DB - CODEBLOCK TO RUN WITH GITHUB ACTIONS - POC

#tail -n 10000 /home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/logs/wso2carbon.log | tee logs.txt

Write-Host "WELCOME TO AUTOMATING PRODUCT MIGRATION TESTING!!" -ForegroundColor Green -BackgroundColor Black

Write-Host "
`n1.|* Before proceeding make sure you have installed`033[0;31m wget and jq`033[0;34m *|
2.|* Set up `033[0;31mjava open JDK 11`033[0;34m *|
3.|* Set up `033[0;31mdocker`033[0;34m and `033[0;31mmysql 8.3`033[0;34m *|
4 and stop mysql if its locally installed.|* Place relevant JDBC driver for the version you are using in the `033[0;31mutils`033[0;34m folder  *|
5.|* Clean the database if there's any revoked, inactive, and expired tokens accumulate in the IDN_OAUTH2_ACCESS_TOKEN table|
" -ForegroundColor Cyan

Write-Host "`e[0;32m`e[1mTIME TO Automating PRODUCT MIGRATION TESTING!`e[0m"

# Source env file
cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation"
. ./env.sh
Write-Host "`e[0;32m`e[1mEnv file sourced successfully`e[0m"

chmod +x create-new-database.sh
chmod +x copy-jar-file.sh
chmod +x server-start.sh
chmod +x enter-login-credentials.sh
chmod +x copy-data-to-new-IS.sh
chmod +x change-migration-configyaml.sh 
chmod +x copy-data-to-new-IS.sh
chmod +x change-deployment-toml.sh
chmod +x backup-database.sh
chmod +x create-new-database.sh                                                               #executes from bash
chmod +x check-cpu-health.sh
chmod +x server-start-windows.sh
#chmod +x data-population-script.sh
chmod +x  start-is-windows.sh
chmod +x change-migration-configs-windows.sh

# Process start
Write-Host "`e[0;32m`e[1mProcess started!`e[0m"


# Setup Java
choco install -y openjdk11
Write-Host -ForegroundColor Green "Installed Java successfully!"

# Set the JAVA_HOME environment variable
$javaPath = (Get-Command java).Source
$javaHome = Split-Path -Parent $javaPath
[Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, "Machine")
Write-Host -ForegroundColor Green "Set JAVA_HOME to: $javaHome"

# Create directory for placing wso2IS 
mkdir IS_HOME_OLD
Write-Host "`e[0;32m`e[1mCreated a directory to place wso2IS`e[0m"

pwd
echo "pwd"

# Navigate to folder
#cd "./IS_HOME_OLD"

cd D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_OLD

# Download needed wso2IS zip
#wget -qq "$LINK_TO_IS_OLD" &
#wait $!

curl -LJO https://github.com/wso2/product-is/releases/download/v5.11.0/wso2is-5.11.0.zip

Write-Host "`e[0;32m`e[1mDownloaded needed wso2IS zip`e[0m"

# Unzip IS archive
unzip -qq wso2is-5.11.0.zip
sleep 10
Write-Host "`e[0;32m`e[1mUnzipped downloaded Identity Server zip`e[0m"

cd "$AUTOMATION_HOME"

# Given read write access to deployment.toml
chmod +x /d/a/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation/IS_HOME_OLD/wso2is-5.11.0/repository/conf/deployment.toml
Write-Host "`e[0;32m`e[1mGiven read write access to deployment.toml`e[0m"

#cd "$AUTOMATION_HOME"

# Needed changes in deployment.toml

#for file in $(find  D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_OLD\wso2is-5.11.0\repository\conf -type f -name 'deployment.toml');
#do
#cat  D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\deploymentwindowsmysql.toml > $file;

#done

$files = Get-ChildItem -Path "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_OLD\wso2is-5.11.0\repository\conf" -Recurse -Filter "deployment.toml"

foreach ($file in $files) {
  $content = Get-Content "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\deploymentwindowsmysql.toml"
  Set-Content $file.FullName $content
}

Write-Host "`e[0;32m`e[1mDeployment.toml changed successfully`e[0m"
      
#cd "$AUTOMATION_HOME"
cd D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation

# Stop mysql running inside github actions and wait for the MySQL container to start
#sudo systemctl stop mysql &
#sleep 20

# Start running docker container
#docker run --name amazing_feynman -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -d mysql:5.7.38
#sleep 30

# Check status
#docker ps

# Find the ID of the running MySQL container
#MYSQL_CONTAINER_ID=$(docker ps | grep mysql | awk '{print $1}')

# Start the MySQL container
#if [ -n "$MYSQL_CONTAINER_ID" ]; then
#  docker start $MYSQL_CONTAINER_ID
#  echo "\033[0;32m\033[1mMySQL container started successfully\033[0;m"
#else
#  echo "\033[0;32m\033[1mNo running MySQL container found\033[0;m"
#fi

# Check if MySQL is listening on the default MySQL port (3306)
#if netstat -ln | grep ':3306'; then
#  echo "\033[0;32m\033[1mMySQL is listening on port 3306\033[0;m"
#else
#  echo "\033[0;32m\033[1mMySQL is not listening on port 3306\033[0;m"
#fi

# Create database
#chmod +x /home/runner/work/product-is/product-is/.github/migration-tester/utils/mysql.sql
#docker exec -i amazing_feynman sh -c 'exec mysql -uroot -proot' < /home/runner/work/product-is/product-is/.github/migration-tester/utils/mysql.sql
#echo "\033[0;32m\033[1mDatabase created successfully!\033[0;m"

#chmod +x ~/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/mysql.sql
#docker exec -i amazing_feynman sh -c 'exec mysql -uroot -proot -D testdb' < ~/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/mysql.sql
#docker exec -i amazing_feynman sh -c 'exec mysql -uroot -proot -D testdb' < ~/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/identity/mysql.sql
#docker exec -i amazing_feynman sh -c 'exec mysql -uroot -proot -D testdb' < ~/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/identity/uma/mysql.sql
#docker exec -i amazing_feynman sh -c 'exec mysql -uroot -proot -D testdb' < ~/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/consent/mysql.sql
#docker exec -i amazing_feynman sh -c 'exec mysql -uroot -proot -D testdb' < ~/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/metrics/mysql.sql
#echo "\033[0;32m\033[1mDatabase scripts executed and created tables successfully!\033[0;m"

#bash create-new-database\.sh &
#wait $!
#echo "\033[0;32m\033[1mCreated database and run needed sql scripts against it - for current IS"   

# Copy the JDBC driver to the target directory
#cp -r D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\utils\mysql-connector-java-8.0.29.jar D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_OLD\wso2is-5.11.0\repository\components\lib\
Copy-Item -Path "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\utils\mysql-connector-java-8.0.29.jar" -Destination "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_OLD\wso2is-5.11.0\repository\components\lib\"

Write-Host "`e[0;32m`e[1mPlaced JDBC driver successfully`e[0m"
# Wait for the JDBC driver to be copied to the lib folder
while(!(Test-Path "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_OLD\wso2is-5.11.0\repository\components\lib\mysql-connector-java-8.0.29.jar")) {
    Write-Host "JDBC driver not found in lib folder, waiting..."
    Start-Sleep -Seconds 5
}
Write-Host "JDBC driver found in lib folder, continuing..."

# Start wso2IS
Write-Host "`e[0;32m`e[1mIs old started running!`e[0m"

# Start WSO2 Identity Server with Windows OS
$runPath = "D:\wso2\run"
New-Item -ItemType Directory -Path $runPath | Out-Null
Set-Content -Path "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_OLD\wso2is-5.11.0\repository\conf\tomcat\catalina-server.xml" -Value ('<?xml version="1.0" encoding="UTF-8"?>
<Server port="8005" shutdown="SHUTDOWN">
    <Service name="Catalina">
        <Connector port="9443" protocol="org.apache.coyote.http11.Http11NioProtocol"
                   maxThreads="150" SSLEnabled="true">
            <SSLHostConfig>
                <Certificate certificateKeystoreFile="D:\wso2\run\resources\security\wso2carbon.jks"
                             certificateKeystorePassword="wso2carbon"
                             type="RSA"/>
            </SSLHostConfig>
        </Connector>
        <Engine name="Catalina" defaultHost="localhost">
            <Host name="localhost" appBase="webapps"
                  unpackWARs="true" autoDeploy="true">
                <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
                       prefix="localhost_access_log" suffix=".txt"
                       pattern="%h %l %u %t &quot;%r&quot; %s %b" />
            </Host>
        </Engine>
    </Service>
</Server>') | Out-Null

# Create a start.bat file
$startBatPath = Join-Path $PSScriptRoot "start.bat"
Set-Content -Path $startBatPath -Value "set RUNTIME_PATH=$runPath`nD:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_OLD\wso2is-5.11.0\bin\wso2server.bat -Dcarbon.bootstrap.timeout=300" | Out-Null

# Set the execution policy to unrestricted
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy Unrestricted -Force

# Output stack trace before starting the server
Write-Host "Starting server..."

# Start the server in the background
Start-Process -FilePath $startBatPath -NoNewWindow

# Wait until server is up
function IsServerUp {
    try {
        $status = (Invoke-WebRequest -Uri "https://localhost:9443/" -Method GET -UseBasicParsing -SkipCertificateCheck).StatusCode
        if ($status -eq 200) {
            return $true
        }
    } catch {
        Write-Host "Error accessing server: $($_.Exception.Message)"
    }
    return $false
}

function WaitUntilServerIsUp {
    $timeout = 600
    $waitTime = 0
    while (-not (IsServerUp)) {
        Write-Host "Waiting until server starts..."
        Start-Sleep -Seconds 10
        $waitTime += 10
        if ($waitTime -ge $timeout) {
            Write-Error "Timeout: server did not start within $timeout seconds"
            exit 1
        }
    }
}

WaitUntilServerIsUp

#cd "$BIN_ISOLD"
#bash server-start.sh&
 
#cd /home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/wso2is-5.11.0/bin
Write-Host "Diverted to bin"

#Set-Location -Path "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\02-POC\macos\data-population-and-validation"
#Set-Location -Path "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\data-population-and-validation\1-user-creation"
Write-Host "Entered to data population directory"

$port = 9443
$server = "localhost"
$timeout = 5

$body = '{
    "schemas": [],
    "name": {
        "givenName": "Dimithri",
        "familyName": "Gunaweera"
    },
    "userName": "Dimithri",
    "password": "Dimithri123456789",
    "emails": [
        {
            "type": "home",
            "value": "dimithri@iit.ac.lk",
            "primary": true
        },
        {
            "type": "work",
            "value": "dimithri@iit2.ac.lk"
        }
    ],
    "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User": {
        "employeeNumber": "1234A",
        "manager": {
            "value": "Taylor"
        }
    }
}'
$headers = @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
    "Content-Type" = "application/json"
}
$uri = "https://${server}:${port}/scim2/Users"
Invoke-RestMethod -Uri $uri -Method POST -Headers $headers -Body $body -SkipCertificateCheck -UseBasicParsing
Write-Host "`e[0;32m`e[1mCreated a user`e[0m"

$SCIM_BULK_EP = "https://localhost:9443/scim2/Bulk"
$headers = @{
    "Content-Type" = "application/scim+json"
    "Authorization" = "Basic YWRtaW46YWRtaW4="
}
$body = '{
    "failOnErrors": 1,
    "schemas": ["urn:ietf:params:scim:api:messages:2.0:BulkRequest"],
    "Operations": [
        {
            "method": "POST",
            "path": "/Users",
            "bulkId": "qwerty",
            "data": {
                "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
                "userName": "Jayana",
                "password": "jayanapass"
            }
        },
        {
            "method": "POST",
            "path": "/Users",
            "bulkId": "qwerty",
            "data": {
                "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
                "userName": "Randul",
                "password": "Randulpass"
            }
        },
        {
            "method": "POST",
            "path": "/Users",
            "bulkId": "qwerty",
            "data": {
                "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
                "userName": "Rukshan",
                "password": "rukshanpass"
            }
        },
        {
            "method": "POST",
            "path": "/Users",
            "bulkId": "qwerty",
            "data": {
                "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
                "userName": "Chithara",
                "password": "chitharapass"
            }
        },
        {
            "method": "POST",
            "path": "/Users",
            "bulkId": "ytrewq",
            "data": {
                "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User","urn:ietf:params:scim:schemas:extension:enterprise:2.0:User"],
                "userName": "Chamath",
                "password": "chamathpass",
                "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User": {
                    "employeeNumber": "11250",
                    "mentor": { "value": "bulkId:qwerty" }
                }
            }
        },
        {
            "method": "POST",
            "path": "/Users",
            "bulkId": "ytrewq",
            "data": {
                "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User","urn:ietf:params:scim:schemas:extension:enterprise:2.0:User"],
                "userName": "Ashen",
                "password": "ashenpass",
                "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User": {
                    "employeeNumber": "11251",
                    "": { "value": "bulkId:qwerty" }
                }
            }
        }
    ]
}'

Invoke-RestMethod -Uri $SCIM_BULK_EP -Method POST -Headers $headers -Body $body -SkipCertificateCheck -UseBasicParsing
Write-Host "`e[0;32m`e[1mThis is a bulk import of users`e[0m"

$TENANT_EP = "https://localhost:9443/t/carbon.super/api/server/v1/tenants"
$headers = @{
    "Accept" = "*/*"
    "Content-Type" = "application/json"
    "Authorization" = "Basic YWRtaW46YWRtaW4="
}
$body = '{
    "domain": "iit.com",
    "owners": [
        {
            "username": "iit",
            "password": "iit123",
            "email": "iit@iit.com",
            "firstname": "iit",
            "lastname": "iit",
            "provisioningMethod": "inline-password",
            "additionalClaims": [
                {
                    "claim": "http://wso2.org/claims/telephone",
                    "value": "+94 76 318 6705"
                }
            ]
        }
    ]
}'

Invoke-RestMethod -Uri $TENANT_EP -Method POST -Headers $headers -Body $body -SkipCertificateCheck -UseBasicParsing
Write-Host "`e[0;32m`e[1mCreated a tenant`e[0m"


$USERSTORE_EP = "https://localhost:9443/t/carbon.super/api/server/v1/userstores"
$USERSTORE_NAME = "NewUserStore1"

$userstore_body = @{
    "typeId" = "SkRCQ1VzZXJTdG9yZU1hbmFnZXI"
    "description" = "Sample JDBC user store to add."
    "name" = $USERSTORE_NAME
    "properties" = @(
        @{
            "name" = "userName"
            "value" = "testdb"
        },
        @{
            "name" = "password"
            "value" = "testdb"
        },
        @{
            "name" = "driverName"
            "value" = "com.mysql.jdbc.Driver"
        },
        @{
            "name" = "url"
            "value" = "jdbc:mysql://localhost:3306/testdb?useSSL=false"
        },
        @{
            "name" = "disabled"
            "value" = "false"
        }
    )
} | ConvertTo-Json

Invoke-RestMethod -Uri $USERSTORE_EP -Method POST -Headers @{
    "Content-Type" = "application/json"
    "Authorization" = "Basic YWRtaW46YWRtaW4="
} -Body $userstore_body -SkipCertificateCheck -UseBasicParsing
Write-Host "`e[0;32m`e[1mRegistered a userstore`e[0m"

$SCIM_USER_EP_USERSTORE = "https://localhost:9443/scim2/Users"
$USERSTORE_USER_NAME = "Prabhanu"
$USERSTORE_USER_PASSWORD = "Demo@12345"
$USERSTORE_GROUP_NAME = "Engineering"

$scim_user_body = @{
    "schemas" = @()
    "userName" = $USERSTORE_USER_NAME
    "password" = $USERSTORE_USER_PASSWORD
    "wso2Extension" = @{
        "employeeNumber" = "000111"
        "costCenter" = "111111"
        "organization" = "WSO2Org"
        "division" = $USERSTORE_GROUP_NAME
        "department" = "Integration"
        "manager" = @{
            "managerId" = "111000"
            "displayName" = $USERSTORE_USER_NAME
        }
    }
} | ConvertTo-Json

Invoke-RestMethod -Uri $SCIM_USER_EP_USERSTORE -Method POST -Headers @{
    "Content-Type" = "application/json"
    "Authorization" = "Basic YWRtaW46YWRtaW4="
} -Body $scim_user_body -SkipCertificateCheck -UseBasicParsing
Write-Host "`e[0;32m`e[1mCreated a user in a userstore`e[0m"

$SCIM2_GROUP_EP = "https://localhost:9443/scim2/Groups"
$GROUP_NAME = "Engineering"

$scim_group_body = @{
    "displayName" = $GROUP_NAME
    "schemas" = @(
        "urn:ietf:params:scim:schemas:core:2.0:Group"
    )
} | ConvertTo-Json

Invoke-RestMethod -Uri $SCIM2_GROUP_EP -Method POST -Headers @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
    "Content-Type" = "application/json"
} -Body $scim_group_body -SkipCertificateCheck -UseBasicParsing
Write-Host "`e[0;32m`e[1mGenerated a group`e[0m"

#$interns_group_body = @{
#    "displayName" = "interns"
#    "members" = @(
#        @{
#            "display" = "Jayana"
 #           "value" = "Jayana"
 #           "ref" = "User"
#            "operation" = "add"
#        },
#        @{
#            "display" = "Randul"
#            "value" = "Randul"
#            "ref" = "User"
#            "operation" = "add"
#        },
#        @{
#            "display" = "Chithara"
#            "value" = "Chithara"
#            "ref" = "User"
 #           "operation" = "add"
 #       },
 #       @{
#            "display" = "Rukshan"
#            "value" = "Rukshan"
#            "ref" = "User"
#            "operation" = "add"
#        }
#    )
#} | ConvertTo-Json

#$mentors_group_body = @{
#    "displayName" = "mentors"
#    "members" = @(
#        @{
#            "display" = "Ashen"
#            "value" = "Ashen"
#            "ref" = "User"
#            "operation" = "add"
#        },
#        @{
#            "display" = "Chamath"
#            "value" = "Chamath"
#            "ref" = "User"
#            "operation" = "add"
#        }
#    )
#} | ConvertTo-Json

#$group_endpoint = "https://localhost:9443/api/identity/group/v1.0/groups"

#Invoke-RestMethod -Uri $group_endpoint -Method POST -Headers @{
#    "Authorization" = "Basic YWRtaW46YWRtaW4="
#    "Content-Type" = "application/json"
#} -Body $interns_group_body -SkipCertificateCheck -UseBasicParsing

#Invoke-RestMethod -Uri $group_endpoint -Method POST -Headers @{
#    "Authorization" = "Basic YWRtaW46YWRtaW4="
#    "Content-Type" = "application/json"
#} -Body $mentors_group_body -SkipCertificateCheck -UseBasicParsing
#Write-Host "`e[0;32m`e[1mCreated users in the group`e[0m"


$register_endpoint = "https://localhost:9443/api/identity/oauth2/dcr/v1.1/register"

$register_body = @{
    "client_name" = "test migration app"
    "grant_types" = @("authorization_code","implicit","password","client_credentials","refresh_token")
    "redirect_uris" = @("http://localhost:8080/playground2")
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri $register_endpoint -Method POST -Headers @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
    "Content-Type" = "application/json"
} -Body $register_body -SkipCertificateCheck -UseBasicParsing

Write-Host "`e[0;32m`e[1mRegistered a service provider successfully!`e[0m"

$endpoint = "https://localhost:9443/t/carbon.super/scim2/Users"

$body = @{
    "schemas" = @()
    "name" = @{
        "givenName" = "Lanka"
        "familyName" = "Hewapathirana"
    }
    "userName" = "lanka"
    "password" = "Test123"
    "emails" = @(
        @{
            "type" = "home"
            "value" = "lanka@gmail.com"
            "primary" = $true
        },
        @{
            "type" = "work"
            "value" = "lankawork@gmail.com"
        }
    )
    "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User" = @{
        "employeeNumber" = "1234A"
        "manager" = @{
            "value" = "Taylor"
        }
    }
} | ConvertTo-Json

Invoke-RestMethod -Uri $endpoint -Method POST -Headers @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
    "Content-Type" = "application/json"
} -Body $body -SkipCertificateCheck -UseBasicParsing
Write-Host "`e[0;32m`e[1mRegistered user in a service provider successfully!`e[0m"

$endpoint = 'https://localhost:9443/api/identity/oauth2/dcr/v1.1/register'

$body = @{
    "client_name" = "Migration Application"
    "grant_types" = @("authorization_code", "implicit", "password", "client_credentials", "refresh_token")
    "redirect_uris" = @("http://localhost:8080/playground2")
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri $endpoint -Method POST -Headers @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
    "Content-Type" = "application/json"
} -Body $body -SkipCertificateCheck -UseBasicParsing

$client_name = $response.client_name

# Define colors
$RED = [ConsoleColor]::Red
$GREEN = [ConsoleColor]::Green
$YELLOW = [ConsoleColor]::Yellow
$NC = [ConsoleColor]::Gray

Write-Host "Response: $response" -ForegroundColor $YELLOW
Write-Host "Service provider '$client_name' registered successfully" -ForegroundColor $GREEN
Write-Host

$client_id = $response.client_id
$client_secret = $response.client_secret

# Store client_id and client_secret in a file
if (Test-Path "client_credentials") {
    Add-Content "client_credentials" "client_id=$client_id"
    Add-Content "client_credentials" "client_secret=$client_secret"
}
else {
    Set-Content "client_credentials" "client_id=$client_id"
    Add-Content "client_credentials" "client_secret=$client_secret"
}

# Print client_id and client_secret
Write-Host "Client ID: $client_id" -ForegroundColor $YELLOW
Write-Host "Client Secret: $client_secret" -ForegroundColor $YELLOW

# Encode client_id:client_secret as base64
$base64_encoded = [System.Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes("${client_id}:${client_secret}"))

# Get access token
$access_token_response = Invoke-RestMethod -Uri 'https://localhost:9443/oauth2/token' -Method POST -Headers @{
    "Authorization" = "Basic ${base64_encoded}"
    "Content-Type" = "application/x-www-form-urlencoded"
} -Body 'grant_type=password&username=admin&password=admin&scope=somescope_password' -SkipCertificateCheck -UseBasicParsing

$access_token = $access_token_response.access_token

# Store access token in a file
Add-Content "client_credentials" "access_token=$access_token"

# Print client credentials and access token in file
Write-Host "Client Credentials and Access Token:" -ForegroundColor $YELLOW
Get-Content "client_credentials"

# Print access token
Write-Host "An access token generated successfully from the registered service provider: $access_token" -ForegroundColor $GREEN

Write-Host "An oAuth token has been generated." -ForegroundColor $GREEN

Write-Host "Created users, user stores, service providers, tenants, generated oAuth tokens and executed the script successfully"

# cd "$BIN_ISOLD"
cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_OLD\wso2is-5.11.0\bin"
Write-Host "`e[0;32m`e[1mEntered bin successfully`e[0m"

#D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_OLD\wso2is-5.11.0\bin\wso2server.bat stop
#$IS_HOME_OLD = "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_OLD\wso2is-5.11.0"
#$wso2serverPath = Join-Path -Path $IS_HOME_OLD -ChildPath "bin\wso2server.bat"

# Stop the WSO2 Identity Server
#& $wso2serverPath --stop

#Start-Sleep -s 30
#Write-Host "`e[0;32m`e[1mStopped identity server successfully.`e[0m"

# Stop the WSO2 Identity Server
$shutdownUri = "https://localhost:9443/carbon/admin/shutdown"
$shutdownResponse = Invoke-WebRequest -Uri $shutdownUri -Method POST -UseBasicParsing -SkipCertificateCheck

if ($shutdownResponse.StatusCode -eq 200) {
    Write-Host "WSO2 Identity Server stopped successfully."
} else {
    Write-Host "Failed to stop WSO2 Identity Server."
}

# Stop WSO2 IS
# D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_OLD\wso2is-5.11.0\bin\wso2server.bat stop

$maxAttempts = 30  # Maximum number of attempts to check server status
$attempt = 0      # Current attempt

do {
    # Wait for 1 second
    Start-Sleep -Seconds 1

    # Check if the server is running
    $serverRunning = Get-Process | Where-Object {$_.Name -eq 'wso2carbon'}

    $attempt++

    # Check if the server is still running
    if ($serverRunning) {
        # Print the current attempt
        Write-Host "Attempt ${attempt}: WSO2 Identity Server is still running..."

        # Check if maximum attempts reached
        if ($attempt -eq $maxAttempts) {
            Write-Host "Maximum attempts reached. Unable to stop WSO2 Identity Server." -ForegroundColor Red
            break
        }
    }
    else {
        # Server has stopped
        Write-Host "`e[0;32m`e[1mWSO2 Identity Server has stopped successfully`e[0m"
        break
    }

} while ($true)

Write-Host "`e[0;32m`e[1mStopped identity server successfully.`e[0m"

# cd "$AUTOMATION_HOME"
cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation"
Write-Host "`e[0;32m`e[1mDirected to home successfully`e[0m"

# Create directory for placing latest wso2IS (IS to migrate)
mkdir IS_HOME_NEW
Write-Host "`e[0;32m`e[1mCreated a directory for placing latest wso2IS`e[0m"

# Navigate to folder 
# cd IS_HOME_NEW
cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_NEW"
pwd

# Download and unzip IS archive
curl -LJO https://github.com/wso2/product-is/releases/download/v6.0.0-rc2/wso2is-6.0.0-rc2.zip
sleep 30
unzip -qq wso2is-6.0.0-rc2.zip
pwd
sleep 15

# Find the extracted directory and store its name in a variable
$extractedDir = Get-ChildItem -Directory -Name wso2is*

# Check if the extracted directory was found
if (!$extractedDir) {
  Write-Error "Could not find extracted directory"
  exit 1
}

# Use the extracted directory name to create a path to the new IS_HOME
$newISHOME = "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_NEW\$extractedDir\repository\components"

# Verify that the new IS_HOME directory exists
if (!(Test-Path $newISHOME)) {
  Write-Error "New IS_HOME directory not found: $newISHOME"
  exit 1
}

Write-Host "`e[0;32m`e[1mUnzipped latest wso2IS zip and found extracted directory $extractedDir`e[0m"


#cd "$AUTOMATION_HOME"
cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation"

# Download migration client
#wget -qq "$LINK_TO_MIGRATION_CLIENT" &
#sleep 30
#echo "\033[0;32m\033[1mDownloaded migration client successfully!\033[0;m"

#pwd
#ls -a

#bash download-migration-client.sh
# Wait for the Migration client to be copied 
#while [ ! -f "/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/wso2is-migration-1.0.225" ]
#do
  #echo "\033[0;32m\033[1mMigration client not found in  folder, waiting...\033[0;m"
  #sleep 5
#done
#echo "\033[0;32m\033[1mMigration client found in folder, continuing...\033[0;m"

#cd /home/runner/work/product-is/product-is/.github/migration-tester/utils
cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\utils"

# Unzip migration client archive
unzip -qq wso2is-migration-1.0.225.zip &
sleep 10
Write-Host "`e[0;32m`e[1mUnzipped migration client archive`e[0m"

# Navigate to dropins folder 
#cd "$DROPINS_PATH_HOME"
#cd /home/runner/work/product-is/product-is/.github/migration-tester/utils/wso2is-migration-1.0.225
cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\utils\wso2is-migration-1.0.225"


# Copy droipns folder to wso2IS (latest) dropins folder                                               
#cp -r "$DROPINS_PATH" "$COMPONENTS_PATH" &
#sleep 10
#echo "\033[0;32m\033[1mJar files from migration client have been copied to IS_HOME_NEW/repository/components/dropins folder successfully!\033[0;m"
                                             
#cp -r /home/runner/work/product-is/product-is/.github/migration-tester/utils/wso2is-migration-1.0.225 "$COMPONENTS_PATH" &
#sleep 10
#echo "\033[0;32m\033[1mJar files from migration client have been copied to IS_HOME_NEW/repository/components/dropins folder successfully!\033[0;m"

cp -r "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\utils\wso2is-migration-1.0.225" "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_NEW\wso2is-6.0.0\repository\components" 
sleep 10
Write-Host "`e[0;32m`e[1mJar files from migration client have been copied to IS_HOME_NEW/repository/components/dropins folder successfully!`e[0m"

# Copy migration resources folder to wso2IS (latest) root folder

cp -r "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\utils\wso2is-migration-1.0.225\migration-resources"  "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_NEW\wso2is-6.0.0"
sleep 10
Write-Host "`e[0;32m`e[1mMigration-resources from migration client have been copied to IS_HOME_NEW root folder successfully!`e[0m"

#cd "$AUTOMATION_HOME"
cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\02-POC\windows\data-population-and-validation"
Write-Host "`e[0;32m`e[1mDiverted to data-population-and-validation directoy successfully!`e[0m"

# Needed changes in migration-config.yaml                                                                        
sh change-migration-configs-windows.sh
sleep 10 
                  
# Copy userstores, tenants,jar files,.jks files from oldIS to newIS
sh copy-data-to-new-IS-windows.sh 
sleep 10
Write-Host "`e[0;32m`e[1mCopied userstores, tenants,jar files,.jks files from oldIS to newIS successfully`e[0m"
 
# Get a backup of existing database
#sh backup-database.sh &
#sleep 30
#echo "\033[0;32m\033[1mData backedup successfully\033[0;m" 

# Create a new database for newIS
#sh create-new-database-NewIS.sh &
#sleep 10
#echo "\033[0;32m\033[1mCreated a new database and cloned the data in the backup in it.\033[0;m" 

#cd "$AUTOMATION_HOME"
cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation"

$files = Get-ChildItem -Path "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_NEW\wso2is-6.0.0\repository\conf" -Recurse -Filter "deployment.toml"

foreach ($file in $files) {
  $content = Get-Content "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\deploymentwindowsmysql.toml"
  Set-Content $file.FullName $content
}

#Divert to bin folder
#cd "$BIN_ISNEW"
cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_NEW\wso2is-6.0.0\bin"
Write-Host "`e[0;32m`e[1mDiverted to bin folder successfully`e[0m"

#changes in wso2server.sh file                                                                            Check here

Write-Host "`e[0;32m`e[1mStarted running migration client`e[0m"

# Run migration client
# Set up a new run directory location
mkdir D:\wso2\runmigration
echo set RUNTIME_PATH=D:\wso2\runmigration > D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_NEW\wso2is-6.0.0\bin\wso2server.bat

# Start wso2IS with windows OS
chmod +x wso2server.bat
echo "./wso2server.bat -Dcomponent=identity -Dcarbon.bootstrap.timeout=300"  > start.sh
chmod +x start.sh && chmod 777 start.sh
nohup ./start.sh &
sleep 250

Write-Host "`e[0;32m`e[1mYay!Migration executed successfully`e[0m"

# wso2server.sh stop
#D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_NEW\wso2is-6.0.0\bin\wso2server.bat stop
#sleep 120

cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_NEW\wso2is-6.0.0\bin"
Write-Host "`e[0;32m`e[1mEntered bin successfully`e[0m"

# Stop wso2IS
D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_NEW\wso2is-6.0.0\bin\wso2server.bat stop
Start-Sleep -s 30
Write-Host "`e[0;32m`e[1mStopped migration terminal successfully.`e[0m"

# Kill any background processes that may interfere with WSO2 IS
#taskkill /F /IM java.exe
#taskkill /F /IM javaw.exe

mkdir D:\wso2\runmigrated
echo set RUNTIME_PATH=D:\wso2\runmigrated > D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_NEW\wso2is-6.0.0\bin\wso2server.bat

cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_NEW\wso2is-6.0.0\bin"
chmod +x wso2server.bat
echo "./wso2server.bat -Dcarbon.bootstrap.timeout=300"  > start.sh
chmod +x start.sh && chmod 777 start.sh
nohup ./start.sh &
sleep 100

Write-Host "`e[0;32m`e[1m Migrated WSO2 Identity Server has started successfully`e[0m"

# Send a token and verify database
cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation"

# Run the .sh script to enter login credentials(admin) and divert to management console home page
sh enter-login-credentials.sh 
Write-Host "`e[0;32m`e[1mEntered to Management console home page successfully`e[0m"
 
#cd "$DATA_POPULATION" 
cd D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\data-population-and-validation
Write-Host "`e[0;32m`e[1mEntered to data population directory`e[0m"

# Run data-population-script.sh which is capable of populating data to create users,tenants,userstores,generate tokens etc.
sh data-population-script.sh &
sleep 10
Write-Host "`e[0;32m`e[1mCreated users, user stores, service providers, tenants,generated oAuth tokens and executed the script successfully`e[0m"

# Stop wso2IS
#D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_NEW\wso2is-6.0.0\bin\wso2server.bat stop
#Start-Sleep -s 30
#Write-Host "`e[0;32m`e[1mStopped migrated wso2IS successfully.`e[0m"

set RUNTIME_PATH=D:\wso2\runmigrated 

cd "D:\a\Automating-Product-Migration-Testing\Automating-Product-Migration-Testing\migration-tester/migration-automation\IS_HOME_NEW\wso2is-6.0.0\bin"
Write-Host "`e[0;32m`e[1mEntered bin successfully`e[0m"

Write-Host "`e[0;32m`e[1mValidated database successfully!`e[0m"

Write-Host "End of the process"


