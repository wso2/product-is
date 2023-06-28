# THIS SHELL SCRIPT COMPRISES OF ENV CONFIGS RELATED TO POC - LINUX & MACOS
# ISOLD = version of WSO2 IS that is presently installed in your environment.
# ISNEW = version of WSO2 IS that you want to migrate.

###############################################################################################################################
#                                                    ENV CONFIGS RELATED TO LINUX                                            #
###############################################################################################################################

# Absolute path to root directory
export DIRECTORY_HOME="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing"

# Absolute path to data population script
export DATA_POPULATION="/home/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation"

# Absolute path to service provider
export SERVICE_PROVIDER_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation/4-service-provider-creation"

# Absolute path to home folder
export AUTOMATION_HOME="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation"

# Absolute path to ubuntu folder
export UBUNTU_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/ubuntu-os"

# Absolute path to the deployment file to replace
export DEPLOYMENT="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/repository/conf/deployment.toml"

# Absolute path to the oldIS deployment file
export DEPLOYMENT_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/repository/conf"

# Absolute path to the newIS deployment file
export DEPLOYMENT_PATH_NEW="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/repository/conf"

# Absolute path to the mysql deployment files in migration-tester/migration-automation folder
export DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_5_9_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.9/deployment-mysql-migration.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_5_10_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.10/deployment-mysql-migration.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_5_11_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.11/deployment-mysql-migration.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_6_0_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.0/deployment-mysql-migration.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_6_1_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.1/deployment-mysql-migration.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_6_2_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.2/deployment-mysql-migration.toml"

# Absolute path to the postgre deployment files in migration-tester/migration-automation folder
export DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_5_9_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.9/deployment-postgre-migration.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_5_10_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.10/deployment-postgre-migration.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_5_11_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.11/deployment-postgre-migration.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_6_0_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.0/deployment-postgre-migration.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_6_1_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.1/deployment-postgre-migration.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_6_2_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.2/deployment-postgre-migration.toml"

# Absolute path to the mssql deployment files in migration-tester/migration-automation folder
export DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_5_9_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.9/deployment-mssql-migration.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_5_10_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.10/deployment-mssql-migration.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_5_11_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.11/deployment-mssql-migration.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_6_0_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.0/deployment-mssql-migration.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_6_1_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.1/deployment-mssql-migration.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_6_2_MIGRATION="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.2/deployment-mssql-migration.toml"

# Absolute path to the mysql deployment files in migration-tester/migration-automation folder
export DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_5_9="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.9/deployment-mysql.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_5_10="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.10/deployment-mysql.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_5_11="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.11/deployment-mysql.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_6_0="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.0/deployment-mysql.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_6_1="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.1/deployment-mysql.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_6_2="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.2/deployment-mysql.toml"

# Absolute path to the postgre deployment files in migration-tester/migration-automation folder
export DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_5_9="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.9/deployment-postgre.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_5_10="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.10/deployment-postgre.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_5_11="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.11/deployment-postgre.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_6_0="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.0/deployment-postgre.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_6_1="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.1/deployment-postgre.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_6_2="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.2/deployment-postgre.toml"

# Absolute path to the mssql deployment files in migration-tester/migration-automation folder
export DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_5_9="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.9/deployment-mssql.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_5_10="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.10/deployment-mssql.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_5_11="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.11/deployment-mssql.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_6_0="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.0/deployment-mssql.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_6_1="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.1/deployment-mssql.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_6_2="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.2/deployment-mssql.toml"

# Absolute path to IS old deployment.toml
export PATH_IS_OLD="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/repository/conf/deployment.toml"

# Absolute path to IS old bin folder
export BIN_ISOLD="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/bin"

# Absolute path to IS New bin folder
export BIN_ISNEW="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/bin"

# Absolute path to IS Home New folder
export IS_HOME_NEW="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW"

# Absolute path for the relevant JDBC driver for the version you are using
export JDBC="/home/runner/work/product-is/product-is/.github/migration-tester/utils/migration-client"

# Absolute path for database creation mysql script
export DATABASE_CREATION_SCRIPT="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/database-create-scripts/mysql.sql"

# Absolute paths for DB scripts - IS 5.10 onwards
export DB_SCRIPT_MYSQL="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.11/mysql.sql"
export DB_SCRIPT_IDENTITY="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.11/identity/mysql.sql"
export DB_SCRIPT_UMA="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.11/identity/uma/mysql.sql"
export DB_SCRIPT_CONSENT="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.11/consent/mysql.sql"
export DB_SCRIPT_METRICS="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.11/metrics/mysql.sql"

# Absolute paths for DB scripts - IS 5.9
export DB_SCRIPT_MYSQL_5_9="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.9/mysql.sql"
export DB_SCRIPT_IDENTITY_5_9="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.9/identity/mysql.sql"
export DB_SCRIPT_UMA_5_9="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.9/identity/uma/mysql.sql"
export DB_SCRIPT_CONSENT_5_9="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.9/consent/mysql.sql"
export DB_SCRIPT_METRICS_5_9="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.9/metrics/mysql.sql"

# Absolute path for the <IS_HOME_OLD>/repository/components/lib folder
export LIB="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/repository/components/lib"

# Absolute path for the <IS_HOME_NEW>/repository/components/lib folder
export LIB_NEW="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/repository/components/"

# Absolute path for the <IS_HOME_OLD>/repository/components/bin folder
export IS_OLD_BIN="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/bin"

# Absolute path for the mysql jar connector
export JAR_MYSQL="/home/runner/work/product-is/product-is/.github/migration-tester/utils/jars/mysql/mysql-connector-java-8.0.29.jar"

# Absolute path for the mssql jar connector
export JAR_MSSQL="/home/runner/work/product-is/product-is/.github/migration-tester/utils/jars/mssql/mssql-jdbc-9.2.0.jre8.jar"

# Absolute path for the postgres jar connector
export JAR_POSTGRE="/home/runner/work/product-is/product-is/.github/migration-tester/utils/jars/postgresql/postgresql-42.5.3.jar"

# Absolute path for the <IS_HOME_OLD>/Tenants folder
export TENANT_OLD_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/repository"

# Absolute path for the <IS_HOME_NEW>/Tenants folder
export TENANT_NEW_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/repository/components/"

# Absolute path for the <IS_HOME_OLD>/Resources folder
export RESOURCES_OLD_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/repository/resources"

# Absolute path for the <IS_HOME_NEW>/Resources folder
export RESOURCES_NEW_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/repository/resources"

# Absolute path for the <IS_HOME_OLD>/Userstores folder
export USERSTORE_OLD_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/repository/deployment/server/userstores"

# Absolute path for the <IS_HOME_NEW>/Userstores folder
export USERSTORE_NEW_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/repository/deployment/server/"

# Link to download migration client
export LINK_TO_MIGRATION_CLIENT="https://github.com/wso2-enterprise/identity-migration-resources/releases/download/v1.0.231/wso2is-migration-1.0.225.zip"

# Absolute path to IS_MIGRATION_TOOL_HOME/dropins home folder
export DROPINS_PATH_HOME="/home/runner/work/product-is/product-is/.github/migration-tester/utils/migration-client/wso2is-migration-1.0.225"

# Absolute path to utils folder
export UTILS="/home/runner/work/product-is/product-is/.github/migration-tester/utils/migration-client"

# Absolute path to IS_MIGRATION_TOOL_HOME/dropins folder
export DROPINS_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/utils/migration-client/wso2is-migration-1.0.225/dropins"

# Absolute path to IS_HOME_NEW/repository/components
export COMPONENTS_PATH="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/repository/components"

# Absolute path to migration resources folder
export MIGRATION_RESOURCES="/home/runner/work/product-is/product-is/.github/migration-tester/utils/migration-client/wso2is-migration-1.0.225/migration-resources"

# Absolute path to latest wso2IS root folder
export IS_NEW_ROOT="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion"

# Absolute path to migration resources folder in latest IS
export MIGRATION_RESOURCES_NEW_IS_UBUNTU="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/migration-resources"

# Absolute path to migration config.yaml file in new IS
export MIGRATION_CONFIG_YAML_UBUNTU="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/migration-resources/migration-config.yaml"

# Absolute path to change migration config yaml file
export MIGRATION_YAML_UBUNTU="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/ubuntu-os/change-migration-config-yaml-ubuntu.sh"

# Absolute path to ubuntu folder
export UBUNTU_HOME="/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/ubuntu-os"

# Absolute path to ubuntu update tool
export UPDATE_TOOL_UBUNTU="/home/runner/work/product-is/product-is/.github/migration-tester/utils/update-tools/wso2update_linux "


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
export DATA_POPULATION_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation"

# Absolute path to service provider creation directory
export SERVICE_PROVIDER_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation/4-service-provider-creation"

# Absolute path to home folder
export AUTOMATION_HOME_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation"

# Absolute path to mac folder
export MAC_PATH="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/mac-os"

# Absolute path to the deployment file to replace
export DEPLOYMENT_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/repository/conf/deployment.toml"

# Absolute path to the oldIS conf folder
export DEPLOYMENT_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/repository/conf"

# Absolute path to the newIS conf folder
export DEPLOYMENT_PATH_NEW_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/repository/conf"

# Absolute paths to the mysql deployment files in migration-tester/migration-automation folder
export DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_5_9_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.9/deployment-mysql-migration.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_5_10_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.10/deployment-mysql-migration.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_5_11_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.11/deployment-mysql-migration.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_6_0_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.0/deployment-mysql-migration.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_6_1_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.1/deployment-mysql-migration.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_6_2_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.2/deployment-mysql-migration.toml"

# Absolute paths to the postgre deployment files in migration-tester/migration-automation folder
export DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_5_9_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.9/deployment-postgre-migration.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_5_10_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.10/deployment-postgre-migration.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_5_11_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.11/deployment-postgre-migration.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_6_0_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.0/deployment-postgre-migration.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_6_1_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.1/deployment-postgre-migration.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_6_2_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.2/deployment-postgre-migration.toml"

# Absolute paths to the mssql deployment files in migration-tester/migration-automation folder
export DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_5_9_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.9/deployment-mssql-migration.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_5_10_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.10/deployment-mssql-migration.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_5_11_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.11/deployment-mssql-migration.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_6_0_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.0/deployment-mssql-migration.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_6_1_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.1/deployment-mssql-migration.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_6_2_MIGRATION="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.2/deployment-mssql-migration.toml"

# Absolute paths to the mysql deployment files in migration-tester/migration-automation folder
export DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_5_9="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.9/deployment-mysql.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_5_10="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.10/deployment-mysql.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_5_11="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.11/deployment-mysql.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_6_0="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.0/deployment-mysql.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_6_1="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.1/deployment-mysql.toml"
export DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_6_2="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.2/deployment-mysql.toml"

# Absolute paths to the postgre deployment files in migration-tester/migration-automation folder
export DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_5_9="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.9/deployment-postgre.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_5_10="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.10/deployment-postgre.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_5_11="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.11/deployment-postgre.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_6_0="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.0/deployment-postgre.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_6_1="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.1/deployment-postgre.toml"
export DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_6_2="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.2/deployment-postgre.toml"

# Absolute paths to the mssql deployment files in migration-tester/migration-automation folder
export DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_5_9="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.9/deployment-mssql.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_5_10="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.10/deployment-mssql.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_5_11="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-5.11/deployment-mssql.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_6_0="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.0/deployment-mssql.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_6_1="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.1/deployment-mssql.toml"
export DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_6_2="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/deployment-tomls/IS-6.2/deployment-mssql.toml"

# Absolute path to IS old deployment.toml
export PATH_IS_OLD_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/repository/conf/deployment.toml"

# Absolute path to IS old bin folder
export BIN_ISOLD_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/bin"

# Absolute path to IS New bin folder
export BIN_ISNEW_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/bin"

# Absolute path to IS Home New folder
export IS_HOME_NEW_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW"

# Link to download wso2IS Old
export LINK_TO_IS_OLD_MAC="https://github.com/wso2/product-is/releases/download/v5.11.0/wso2is-5.11.0.zip"

# Absolute path for the utils folder to have relevant JDBC driver
export JDBC_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/migration-client"

# Absolute path for database creation mysql script
export DATABASE_CREATION_SCRIPT_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/database-create-scripts/mysql.sql"

# Absolute paths for DB scripts
export DB_SCRIPT_MYSQL_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.11/mysql.sql"
export DB_SCRIPT_IDENTITY_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.11/identity/mysql.sql"
export DB_SCRIPT_UMA_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.11/identity/uma/mysql.sql"
export DB_SCRIPT_CONSENT_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.11/consent/mysql.sql"
export DB_SCRIPT_METRICS_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.11/metrics/mysql.sql"

# Absolute paths for DB scripts - IS 5.9 Mac
export DB_SCRIPT_MYSQL_5_9_MAC="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.9/mysql.sql"
export DB_SCRIPT_IDENTITY_5_9_MAC="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.9/identity/mysql.sql"
export DB_SCRIPT_UMA_5_9_MAC="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.9/identity/uma/mysql.sql"
export DB_SCRIPT_CONSENT_5_9_MAC="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.9/consent/mysql.sql"
export DB_SCRIPT_METRICS_5_9_MAC="/home/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.9/metrics/mysql.sql"

# Absolute path for the <IS_HOME_OLD>/repository/components/lib folder
export LIB_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/repository/components/lib"

# Absolute path for the <IS_HOME_OLD>/repository/components/bin folder
export IS_OLD_BIN_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/bin"

# Absolute path for the <IS_HOME_NEW>/repository/components/lib folder
export LIB_NEW_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/repository/components/"

# Absolute path for the mysql jar connector
export JAR_MYSQL_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/jars/mysql/mysql-connector-java-8.0.29.jar"

# Absolute path for the mssql jar connector
export JAR_MSSQL_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/jars/mssql/mssql-jdbc-9.2.0.jre8.jar"

# Absolute path for the postgre jar connector
export JAR_POSTGRE_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/jars/postgresql/postgresql-42.5.3.jar"

# Absolute path for the <IS_HOME_OLD>/Tenants folder
export TENANT_OLD_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/repository"

# Absolute path for the <IS_HOME_NEW>/Tenants folder
export TENANT_NEW_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/repository/components/"

# Absolute path for the <IS_HOME_OLD>/Resources folder
export RESOURCES_OLD_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/repository/resources"

# Absolute path for the <IS_HOME_NEW>/Resources folder
export RESOURCES_NEW_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/repository/resources"

# Absolute path for the <IS_HOME_OLD>/Userstores folder
export USERSTORE_OLD_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_OLD/CurrentVersion/repository/deployment/server/userstores"

#Absolute path for the <IS_HOME_NEW>/Userstores folder
export USERSTORE_NEW_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/repository/deployment/server/"

# Link to download wso2IS New
export LINK_TO_IS_NEW_MAC="https://github.com/wso2/product-is/releases/download/v6.0.0-rc2/wso2is-6.0.0-rc2.zip"

# Link to download migration client
export LINK_TO_MIGRATION_CLIENT_MAC="https://github.com/wso2-extensions/identity-migration-resources/releases/download/v1.0.225/wso2is-migration-1.0.225.zip"

# Absolute path to IS_MIGRATION_TOOL_HOME/dropins home folder
export DROPINS_PATH_HOME_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/migration-client/wso2is-migration-1.0.225"

# Absolute path to db-scripts/IS-5.11 folder
export UTILS_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/db-scripts/IS-5.11"

# Absolute path to utils folder
export UTILS_MAC_PATH="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/migration-client"

# Absolute path to IS_MIGRATION_TOOL_HOME/dropins folder
export DROPINS_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/migration-client/wso2is-migration-1.0.225/dropins"

# Absolute path to IS_HOME_NEW/repository/components
export COMPONENTS_PATH_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/repository/components"

# Absolute path to migration resources folder
export MIGRATION_RESOURCES_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/migration-client/wso2is-migration-1.0.225/migration-resources"

# Absolute path to latest wso2IS root folder
export IS_NEW_ROOT_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion"

# Absolute path to migration resources folder in latest IS
export MIGRATION_RESOURCES_NEW_IS_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/migration-resources"

# Absolute path to migration config.yaml file in new IS
export MIGRATION_CONFIG_YAML_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/IS_HOME_NEW/MigratingVersion/migration-resources/migration-config.yaml"

# Absolute path to migration config yaml file
export MIGRATION_YAML_MAC="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/mac-os/change-migration-config-yaml-mac.sh"

# Absolute path to macos folder
export MAC_HOME="/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/mac-os"

# Absolute path to macos update tool
export UPDATE_TOOL_MACOS="/Users/runner/work/product-is/product-is/.github/migration-tester/utils/update-tools/wso2update_darwin"


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
export USER_CREATION=/home/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation/ubuntu-os/1-user-creation
export USER_CREATION_MAC=/Users/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation/mac-os/1-user-creation

# Absolute path to tenants creation shell files
export TENANT_CREATION=/home/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation/ubuntu-os/2-tenant-creation
export TENANT_CREATION_MAC=/Users/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation/mac-os/2-tenant-creation

# Absolute path to userstore creation shell files
export USERSTORE_CREATION=/home/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation/ubuntu-os/3-userstore-creation
export USERSTORE_CREATION_MAC=/Users/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation/mac-os/3-userstore-creation

# Given userstore name
export USERSTORE_NAME=NewUserStore1

# Given use name to add to userstore
export USERSTORE_USER_NAME=Jayana

# Given group name to add to userstore
export USERSTORE_GROUP_NAME=Engineering

# Given user's userstore password
export USERSTORE_USER_PASSWORD=Wso2@123

# Absolute path to create a group using shell files
export GROUP_CREATION=/home/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation/ubuntu-os/5-group-creation
export GROUP_CREATION_MAC=/Users/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation/mac-os/5-group-creation

# Given group name
export GROUP_NAME=ManagingGroup

# Given group name with users
export GROUP_DISPLAY_NAME=SalesGroup

# User's ID to add to the group
export GROUP_USER_ID=30ee7b3e-4b07-4931-81a7-830874f76aa2

# Absolute path to service provider creation shell files
export SP_CREATION=/home/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation/ubuntu-os/4-service-provider-creation
export SP_CREATION_MAC=/Users/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation/mac-os/4-service-provider-creation

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

# File IDs in IAM Updates User google Drive (Where GA Packs are hosted)
export FILE_ID_5_9=1pePZJM0gIFlPft8qSsu4613kiVzuLQHs
export FILE_ID_5_10=1WSQwh2aizd-nhrQQEc_PqnTJJxiRufUv
export FILE_ID_5_11=1tn6GYCzJtMBdwleQ2fVy1icUlItdm4w5
export FILE_ID_6_0=1eAcSLBD-UoYI84SefGQKokOhpmU9bO_a
export FILE_ID_6_1=1WQkO5xvN8RGNNSgF2Q-x9_dAnKs2MGN6

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
export MYSQL_VERSION=8
export DATABASE_NAME=testdb

###############################################################################################################################
#                             END OF ENV CONFIGS RELATED TO POC - LINUX & MACOS                                               #
###############################################################################################################################
