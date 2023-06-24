# THIS SHELL SCRIPT COMPRISES OF ENV CONFIGS RELATED TO POC - LINUX & MACOS 
# ISOLD = version of WSO2 IS that is presently installed in your environment.
# ISNEW = version of WSO2 IS that you want to migrate.


###############################################################################################################################
#                                                    ENV CONFIGS RELATED TO LINUX                                            #                                               
###############################################################################################################################


# Absolute path to root directory
export DIRECTORY_HOME="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing"

# Absolute path to data population script
export DATA_POPULATION="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/data-population-and-validation"

# Absolute path to service provider
export SERVICE_PROVIDER_PATH="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/data-population-and-validation/4-service-provider-creation"

# Absolute path to home folder
export AUTOMATION_HOME="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation"

# Absolute path to the deployment file to replace
export DEPLOYMENT="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/repository/conf/deployment.toml"

# Absolute path to the oldIS deployment file 
export DEPLOYMENT_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/repository/conf"

# Absolute path to the newIS deployment file 
export DEPLOYMENT_PATH_NEW="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/repository/conf"

# Absolute path to the mysql-ubuntu deployment file in migration-tester/migration-automation folder
exportDEPLOYMENT_AUTOMATION_MYSQL="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment.toml"

# Absolute path to the postgre-ubuntu deployment file in migration-tester/migration-automation folder
exportDEPLOYMENT_AUTOMATION_MYSQL_POSTGRE_UBUNTU="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deploymentpostgres.toml" 

# Absolute path to the mssql-ubuntu deployment file in migration-tester/migration-automation folder
exportDEPLOYMENT_AUTOMATION_MYSQL_MSSQL_UBUNTU="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deploymentmssql.toml" 

# Absolute path to IS old deployment.toml
export PATH_IS_OLD="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/repository/conf/deployment.toml"

# Absolute path to IS old bin folder
export BIN_ISOLD="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/bin"

# Absolute path to IS New bin folder
export BIN_ISNEW="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/bin"

# Absolute path to IS Home New folder
export IS_HOME_NEW="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW"

# Absolute path for the relevant JDBC driver for the version you are using
export JDBC="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils"

# Absolute path for database creation mysql script
export DATABASE_CREATION_SCRIPT="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/mysql.sql"

# Absolute paths for DB scripts
export DB_SCRIPT_MYSQL="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/mysql.sql"
export DB_SCRIPT_IDENTITY="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/identity/mysql.sql"
export DB_SCRIPT_UMA="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/identity/uma/mysql.sql"
export DB_SCRIPT_CONSENT="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/consent/mysql.sql"
export DB_SCRIPT_METRICS="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/metrics/mysql.sql"
     
# Absolute path for the <IS_HOME_OLD>/repository/components/lib folder
export LIB="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/repository/components/lib"

# Absolute path for the <IS_HOME_OLD>/repository/components/bin folder
export IS_OLD_BIN="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/bin"

# Absolute path for the mysql jar connector
export JAR_MYSQL="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/mysql-connector-java-8.0.29.jar"

# Absolute path for the <IS_HOME_NEW>/repository/components/lib folder
export LIB_NEW="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/repository/components/"

# Absolute path for the mssql jar connector
export JAR_MSSQL="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/mssql-jdbc-9.2.0.jre8.jar"

# Absolute path for the postgres jar connector
export JAR_POSTGRE="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/postgresql-42.5.3.jar"

# Absolute path for the <IS_HOME_OLD>/Tenants folder
export TENANT_OLD_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/repository"

# Absolute path for the <IS_HOME_NEW>/Tenants folder
export TENANT_NEW_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/repository/components/"

# Absolute path for the <IS_HOME_OLD>/Resources folder
export RESOURCES_OLD_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/repository/resources"

# Absolute path for the <IS_HOME_NEW>/Resources folder
export RESOURCES_NEW_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/repository/resources"

# Absolute path for the <IS_HOME_OLD>/Userstores folder
export USERSTORE_OLD_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/repository/deployment/server/userstores"

# Absolute path for the <IS_HOME_NEW>/Userstores folder
export USERSTORE_NEW_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/repository/deployment/server/"

# Link to download migration client
export LINK_TO_MIGRATION_CLIENT="https://github.com/wso2-enterprise/identity-migration-resources/releases/download/v1.0.230/wso2is-migration-1.0.230.zip"

# Absolute path to IS_MIGRATION_TOOL_HOME/dropins home folder
export DROPINS_PATH_HOME="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/wso2is-migration-1.0.225"

# Absolute path to utils folder
export utils="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils"

# Absolute path to IS_MIGRATION_TOOL_HOME/dropins folder
export DROPINS_PATH="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/wso2is-migration-1.0.225/dropins"

# Absolute path to IS_HOME_NEW/repository/components
export COMPONENTS_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/repository/components"

# Absolute path to migration resources folder
export MIGRATION_RESOURCES="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/wso2is-migration-1.0.225/migration-resources"

# Absolute path to latest wso2IS root folder
export IS_NEW_ROOT="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion"

# Absolute path to migration resources folder in latest IS
export MIGRATION_RESOURCES_NEW_IS="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/migration-resources"

# Absolute path to migration config.yaml file in new IS
export MIGRATION_CONFIG_YAML="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/migration-resources/migration-config.yaml"

# Absolute path to POC folder
export MIGRATION_YAML_UBUNTU="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/02-POC"



###############################################################################################################################
#                                                      ENV CONFIGS RELATED TO MACOS                                           #                                               
###############################################################################################################################

# ISOLD = version of WSO2 IS that is presently installed in your environment.
# ISNEW = version of WSO2 IS that you want to migrate.

# Absolute path to root directory
export DIRECTORY_HOME_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing"

#Absolute path to IS_HOME_OLD
export IS_HOME_OLD_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD"

# Absolute path to data population script
export DATA_POPULATION_MAC_="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/02-POC/macos/data-population-and-validation"

# Absolute path to service provider creation directory
export SERVICE_PROVIDER_PATH_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/data-population-and-validation/4-service-provider-creation"

# Absolute path to home folder
export AUTOMATION_HOME_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation"

# Absolute path to the deployment file to replace
export DEPLOYMENT_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/repository/conf/deployment.toml"

# Absolute path to the oldIS conf folder
export DEPLOYMENT_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/repository/conf"

# Absolute path to the newIS conf folder
export DEPLOYMENT_PATH_NEW_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/repository/conf"

# Absolute path to the mysql-ubuntu deployment file in migration-tester/migration-automation folder
exportDEPLOYMENT_AUTOMATION_MYSQL_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-toml-mac-with-mysql.toml"

# Absolute path to the postgre-ubuntu deployment file in migration-tester/migration-automation folder
exportDEPLOYMENT_AUTOMATION_MYSQL_POSTGRE_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deploymentpostgres.toml" 

# Absolute path to the mssql-ubuntu deployment file in migration-tester/migration-automation folder
exportDEPLOYMENT_AUTOMATION_MYSQL_MSSQL_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deploymentmssql.toml" 

# Absolute path to IS old deployment.toml
export PATH_IS_OLD_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/repository/conf/deployment.toml"

# Absolute path to IS old bin folder
export BIN_ISOLD_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/bin"

# Absolute path to IS New bin folder
export BIN_ISNEW_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/bin"

# Absolute path to IS Home New folder
export IS_HOME_NEW_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW"

# Link to download wso2IS Old
export LINK_TO_IS_OLD_MAC="https://github.com/wso2/product-is/releases/download/v5.11.0/wso2is-5.11.0.zip"

# Absolute path for the utils folder to have relevant JDBC driver
export JDBC_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils"

# Absolute path for database creation mysql script
export DATABASE_CREATION_SCRIPT_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/mysql.sql"

# Absolute paths for DB scripts
export DB_SCRIPT_MYSQL_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/mysql.sql"
export DB_SCRIPT_IDENTITY_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/identity/mysql.sql"
export DB_SCRIPT_UMA_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/identity/uma/mysql.sql"
export DB_SCRIPT_CONSENT_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/consent/mysql.sql"
export DB_SCRIPT_METRICS_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/metrics/mysql.sql"
     
# Absolute path for the <IS_HOME_OLD>/repository/components/lib folder
export LIB_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/repository/components/lib"

# Absolute path for the <IS_HOME_OLD>/repository/components/bin folder
export IS_OLD_BIN_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/bin"

# Absolute path for the <IS_HOME_NEW>/repository/components/lib folder
export LIB_NEW_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/repository/components/"

# Absolute path for the mysql jar connector
export JAR_MYSQL_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/mysql-connector-java-8.0.29.jar"

# Absolute path for the mssql jar connector
export JAR_MSSQL_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/mssql-jdbc-9.2.0.jre8.jar"

# Absolute path for the postgre jar connector
export JAR_POSTGRE_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/postgresql-42.5.3.jar"

# Absolute path for the <IS_HOME_OLD>/Tenants folder
export TENANT_OLD_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/repository"

# Absolute path for the <IS_HOME_NEW>/Tenants folder
export TENANT_NEW_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/repository/components/"

# Absolute path for the <IS_HOME_OLD>/Resources folder
export RESOURCES_OLD_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/repository/resources"

# Absolute path for the <IS_HOME_NEW>/Resources folder
export RESOURCES_NEW_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/repository/resources"

# Absolute path for the <IS_HOME_OLD>/Userstores folder
export USERSTORE_OLD_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/currentVersion/repository/deployment/server/userstores"

#Absolute path for the <IS_HOME_NEW>/Userstores folder
export USERSTORE_NEW_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/repository/deployment/server/"

# Link to download wso2IS New
export LINK_TO_IS_NEW_MAC="https://github.com/wso2/product-is/releases/download/v6.0.0-rc2/wso2is-6.0.0-rc2.zip"

# Link to download migration client
export LINK_TO_MIGRATION_CLIENT_MAC="https://github.com/wso2-extensions/identity-migration-resources/releases/download/v1.0.225/wso2is-migration-1.0.225.zip"

# Absolute path to IS_MIGRATION_TOOL_HOME/dropins home folder
export DROPINS_PATH_HOME_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/wso2is-migration-1.0.225"

# Absolute path to db-scripts/IS-5.11 folder
export UTILS_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11"

# Absolute path to utils folder
export UTILS_MAC_PATH="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils"

# Absolute path to IS_MIGRATION_TOOL_HOME/dropins folder
export DROPINS_PATH_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/wso2is-migration-1.0.225/dropins"

# Absolute path to IS_HOME_NEW/repository/components
export COMPONENTS_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/repository/components"

# Absolute path to migration resources folder
export MIGRATION_RESOURCES_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/wso2is-migration-1.0.225/migration-resources"

# Absolute path to latest wso2IS root folder
export IS_NEW_ROOT_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion"

# Absolute path to migration resources folder in latest IS
export MIGRATION_RESOURCES_NEW_IS_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/migration-resources"

# Absolute path to migration config.yaml file in new IS
export MIGRATION_CONFIG_YAML_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/migratingVersion/migration-resources/migration-config.yaml"

# Absolute path to POC folder
export MIGRATION_YAML_MAC="/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/02-POC"



###############################################################################################################################
#                   ENV CONFIGS RELATED TO data-population-and-validation FOLDER - COMMON TO LINUX & MACOS                                #                                               
###############################################################################################################################


export TOKEN_EP=https://localhost:9443/oauth2/token

export TENANT_TOKEN_EP=https://localhost:9443/t/iit.com/oauth2/token

export APPLICATION_EP=https://localhost:9443/t/iit.com/api/server/v1/applications

export AUTHZ_EP=https://localhost:9443/oauth2/authorize

export INTROSPECT_EP=https://localhost:9443/oauth2/introspect

export REVOKE_EP=https://localhost:9443/oauth2/revoke

export PDP_SERVER_URL=https://localhost:9443/api/identity/entitlement/decision/pdp

export SCIM_GROUP_EP=https://localhost:9443/wso2/scim/Groups

export SCIM2_GROUP_EP=https://localhost:9443/scim2/Groups

export SCIM_USER_EP=https://localhost:9443/scim2/Users

export SCIM_USER_EP_USERSTORE=https://localhost:9443/scim/Users

export SCIM_BULK_EP=https://localhost:9443/scim2/Bulk

export SP_REGISTER_EP=https://localhost:9443/api/identity/oauth2/dcr/v1.1/register

export USERSTORE_EP=https://localhost:9443/t/carbon.super/api/server/v1/userstores

export SP_USER_REGISTER_EP=https://localhost:9443/t/carbon.super/scim2/Users

export TENANT_EP=https://localhost:9443/t/carbon.super/api/server/v1/tenants



# Client id
export CLIENTID=sVgtdpO1c5UL83TFtfLJfkkdMmka

# Client secret
export CLIENTSECRET=2fzWw3xcfvGS4qCXqBu_pvyfe8Ia

# Name of the service provider
export CLIENT_NAME=service_provider1

# Callback URI of the service provider
export CALLBACK_URI=http://localhost:8080/playground2

# Username used for both resource owner password grant type and to authenticate to the XACML PDP
export USERNAME=admin

# Password used for both resource owner password grant type and to authenticate to the XACML PDP
export PASSWORD=admin

# Scope used in requesting an access token
export SCOPE=read

# First name for newly creating user in SCIM
export GIVEN_NAME=Testname1

# Given user name for newly creating user in SCIM
export GIVEN_USER_NAME=Testname1

# Family name for newly creating user in SCIM
export GIVEN_FAMILY_NAME=testfamilyname1

# Password for newly creating a user in SCIM
export GIVEN_PASSWORD=testpassword1

# Given user email for newly creating user in SCIM
export GIVEN_USER_EMAIL_HOME=testemail1home@gmail.com

# Given user email for newly creating user in SCIM
export GIVEN_USER_EMAIL_WORK=testemailwork@gmail.com

# Absolute path to user creation shell files
export USER_CREATION=/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/data-population-and-validation/1-user-creation

# Absolute path to tenants creation shell files
export TENANT_CREATION=/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/data-population-and-validation/2-tenant-creation

# Absolute path to userstore creation shell files
export USERSTORE_CREATION=/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/data-population-and-validation/3-userstore-creation

# Given userstore name 
export USERSTORE_NAME=NewUserStore1

# Given use name to add to userstore
export USERSTORE_USER_NAME=Jayana

# Given group name to add to userstore
export USERSTORE_GROUP_NAME=Engineering

# Given user's userstore password
export USERSTORE_USER_PASSWORD=Wso2@123
 
# Absolute path to create a group using shell files
export GROUP_CREATION=/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/data-population-and-validation/5-group-creation

# Given group name 
export GROUP_NAME=ManagingGroup

# Given group name with users 
export GROUP_DISPLAY_NAME=SalesGroup

# User's ID to add to the group 
export GROUP_USER_ID=30ee7b3e-4b07-4931-81a7-830874f76aa2

# Absolute path to service provider creation shell files
export SP_CREATION=/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/data-population-and-validation/4-service-provider-creation

# User's First name to register in a Service Provider 
export SP_USER_NAME=Lanka

# User's Family name to register in a Service Provider 
export SP_USER_FAMILY_NAME=Hewapathirana

# User's password to register in a Service Provider 
export SP_USER_PASSWORD=Test123

# User's home email to register in a Service Provider 
export SP_USER_HOME_EMAIL=lanka@gmail.com

# User's work email to register in a Service Provider 
export SP_USER_WORK_EMAIL=lankawork@gmail.com

# Specific tenant url to create the service provider
export TENANT_URL=https://localhost:9443/t/iit.com

# Specific user's name in a tenant
export TENANT_USER_NAME=natsumehuga

# Specific tenant name
export TENANT_NAME=iit@iit.com

# Specific tenant password
export TENANT_PASSWORD=iit123

# Specific application name to be created in tenant
export APP_NAME=Demoapp4

# Oauth 2.0 redirect_uri used in authorization code and implicit grant types.
export REDIRECTURI=https://localhost:8080/callback





###############################################################################################################################
#                             ENV CONFIGS RELATED TO DATABASE BACKUP - COMMON TO LINUX & MACOS                                #                                               
###############################################################################################################################

export BACKUP_PATH=/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/backup_db.sql



###############################################################################################################################
#                             DOCKER MYSQL CONFIGS - COMMON TO LINUX & MACOS                                                  #                                               
###############################################################################################################################

export CONTAINER_NAME=amazing_feynman
export HOST_PORT=3306
export CONTAINER_PORT=3306
export ROOT_PASSWORD=root
export MYSQL_VERSION=5.7.38
export DATABASE_NAME=testdb


###############################################################################################################################
#                             END OF ENV CONFIGS RELATED TO POC - LINUX & MACOS                                               #                                               
###############################################################################################################################



