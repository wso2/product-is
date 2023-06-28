#!/bin/bash

# THIS SCRIPT RUNS IN LOCAL LINUX OS - NEED TO MANUALLY SETUP MYSQL8 AND JAVA11 IN YOUR LOCAL ENVIRONMENT BEFORE RUNNING THIS SCRIPT.

# Update the system before downloading packages
sudo apt-get -qq update

# Install fonts for terminal usage
sudo apt-get -qq install toilet figlet
sudo apt-get -qq install toilet toilet-fonts
echo 

toilet -f future --filter border:metal -w 105 'AUTOMATING PRODUCT MIGRATION TESTING!'
# Define color variables
GREEN='\033[0;32m\033[1m' # green color
RESET='\033[0m' # reset color

# Print instructions with different colors and formatting using echo command
echo "${GREEN}
1.|* Before proceeding make sure you have installed ${RESET}\033[0;31mwget and jq${RESET}${GREEN} *|
2.|* Set up ${RESET}\033[0;31mjava open JDK 11${RESET}${GREEN} *|
3.|* Set up ${RESET}\033[0;31mdocker${RESET}${GREEN} and ${RESET}\033[0;31mmysql 8.3${RESET}${GREEN} *|
4 and stop mysql if its locally installed.|* Place relevant JDBC driver for the version you are using in the  ${RESET}\033[0;31mutils${RESET}${GREEN} folder  *|
5.|* Clean the database if there's any revoked, inactive, and expired tokens accumulate in the IDN_OAUTH2_ACCESS_TOKEN table${RESET}${GREEN}|
${RESET}"

toilet -f term -F border --gay 'TIME TO Automating PRODUCT MIGRATION TESTING!'
sudo apt-get install jp2a
jp2a --colors  --flipx --term-height "$Home/Downloads/Automating-Product-Migration-Testing/migration-tester/migration-automation/humanoid.jpg" 

# Source env file
. $Home/Downloads/Automating-Product-Migration-Testing/local-setups/env.sh
echo "\033[0;32m\033[1mEnv file sourced successfully\033[0;m"

chmod +x $Home/Downloads/Automating-Product-Migration-Testing/migration-tester/migration-automation/create-new-database.sh
chmod +x $Home/Downloads/Automating-Product-Migration-Testing/migration-tester/migration-automation/copy-jar-file.sh
chmod +x $Home/Downloads/Automating-Product-Migration-Testing/migration-tester/migration-automation/server-start.sh
chmod +x $Home/Downloads/Automating-Product-Migration-Testing/migration-tester/migration-automation/enter-login-credentials.sh
chmod +x $Home/Downloads/Automating-Product-Migration-Testing/migration-tester/migration-automation/copy-data-to-new-IS.sh
chmod +x $Home/Downloads/Automating-Product-Migration-Testing/migration-tester/migration-automation/change-migration-configyaml.sh 
chmod +x $Home/Downloads/Automating-Product-Migration-Testing/migration-tester/migration-automation/copy-data-to-new-IS.sh
chmod +x $Home/Downloads/Automating-Product-Migration-Testing/migration-tester/migration-automation/change-deployment-toml.sh
chmod +x $Home/Downloads/Automating-Product-Migration-Testing/migration-tester/migration-automation/backup-database.sh
chmod +x $Home/Downloads/Automating-Product-Migration-Testing/migration-tester/migration-automation/create-new-database.sh                                                               #executes from bash
chmod +x $Home/Downloads/Automating-Product-Migration-Testing/migration-tester/migration-automation/check-cpu-health.sh
chmod +x $Home/Downloads/Automating-Product-Migration-Testing/migration-tester/migration-automation/migration-terminal.sh 
chmod +x $Home/Downloads/Automating-Product-Migration-Testing/data-population-and-validation/data-population-script.sh

# Process start
toilet -f term -F border --gay 'PROCESS STARTED!'

# Create directory for placing wso2IS 
mkdir IS_HOME_OLD
echo "\033[0;32m\033[1mCreated a directory to place wso2IS\033[0;m"

# Navigate to folder
cd "./IS_HOME_OLD"

# Download needed wso2IS zip
#wget -qq "$LINK_TO_IS_OLD" &
#wait $!
echo "\033[0;32m\033[1mDownloaded needed wso2IS zip\033[0;m"

# Unzip IS archive
unzip -qq wso2is-5.11.0.zip &
wait $!
echo "\033[0;32m\033[1mUnzipped downloaded Identity Server zip\033[0;m"


# Giving read write access to deployment.toml
chmod +x "$DEPLOYMENT"
echo "\033[0;32m\033[1mGiven read write access to deployment.toml\033[0;m"

cd "$LOCAL_SETUP"

# Needed changes in deployment.toml
sh change-deployment-toml.sh
echo "\033[0;32m\033[1mDeployment.toml changed successfully\033[0;m"           


# Create database
bash create-new-database\.sh
sleep 10
echo "\033[0;32m\033[1mDatabase scripts executed and created tables, DB created and done all the needed configurations successfully!\033[0;m"

cd "$LOCAL_SETUP"

# Copy the JDBC driver to the target directory
sh copy-jar-file-mysql.sh
echo "\033[0;32m\033[1mPlaced JDBC driver successfully\033[0;m"
sleep 5

# Start wso2IS
toilet -f term -F border --gay 'IS Old started running!'

cd "$AUTOMATION_HOME"

#Install jq
sudo apt-get install jq

bash server-start.sh
sleep 30
echo "\033[0;32m\033[1mWSO2 Identity Server has started successfully\033[0;m"

cd "$AUTOMATION_HOME_01_Migration_Automation"

# Run the .sh script to enter login credentials(admin) and divert to management console home page
sh enter-login-credentials.sh 
sleep 5
echo "\033[0;32m\033[1mEntered to Management console home page successfully\033[0;m"
 
cd "$DATA_POPULATION" 
echo "\033[0;32m\033[1mEntered to data population directory\033[0;m"
echo

chmod +x automated-data-population-and-validation-script-ubuntu-local-setup.sh

# Run data-population-script.sh which is capable of populating data to create users,tenants,userstores,generate tokens etc.
sh automated-data-population-and-validation-script-ubuntu-local-setup.sh
sleep 10
echo "\033[0;32m\033[1mCreated users, user stores, service providers, tenants,generated oAuth tokens and executed the script successfully\033[0;m"

cd "$BIN_ISOLD"
echo "\033[0;32m\033[1mEntered bin successfully\033[0;m"

# Stop IS
sh wso2server.sh stop	
echo "\033[0;32m\033[1mWSO2 Identity Server has stopped successfully\033[0;m"
sleep 10
 
cd "$AUTOMATION_HOME"
echo "\033[0;32m\033[1mDirected to home successfully\033[0;m" 

# Create directory for placing latest wso2IS (IS to migrate)
mkdir IS_HOME_NEW
echo "\033[0;32m\033[1mCreated a directory for placing latest wso2IS\033[0;m"

# Navigate to folder 
cd IS_HOME_NEW

# Download needed (latest) wso2IS zip                                                            
wget -qq "$LINK_TO_IS_NEW" 
sleep 30
echo "\033[0;32m\033[1mDownloaded latest wso2IS zip\033[0;m"

# Unzip IS archive
unzip -qq wso2is-6.0.0-rc2.zip
sleep 30
echo "\033[0;32m\033[1mUnzipped latest wso2IS zip\033[0;m"

# Download migration client                                                                      
#wget -qq "$LINK_TO_MIGRATION_CLIENT" &
#sleep 30
#echo "\033[0;32m\033[1mDownloaded migration client successfully!\033[0;m"

# Download and place migration client
cd "$utils_PATH"

# Unzip migration client archive
unzip -qq wso2is-migration-1.0.225.zip 
sleep 10
echo "\033[0;32m\033[1mUnzipped migration client archive\033[0;m"

# Navigate to dropins folder 
cd "$DROPINS_PATH_HOME"

# Copy droipns folder to wso2IS (latest) dropins folder                                               
cp -r "$DROPINS_PATH" "$COMPONENTS_PATH"
sleep 10
echo "\033[0;32m\033[1mJar files from migration client have been copied to IS_HOME_NEW/repository/components/dropins folder successfully!\033[0;m"
                                             
# Copy migration resources folder to wso2IS (latest) root folder
cp -r "$MIGRATION_RESOURCES" "$IS_NEW_ROOT" 
sleep 10 
echo "\033[0;32m\033[1mMigration-resources from migration client have been copied to IS_HOME_NEW root folder successfully!\033[0;m"

cd "$AUTOMATION_HOME_01_Migration_Automation"
echo "\033[0;32m\033[1mDiverted to home successfully\033[0;m"

# Needed changes in migration-config.yaml                                                                        
sh change-migration-configyaml.sh
sleep 10 
echo "\033[0;32m\033[1mDid needed changes in migration-config.yaml file successfully\033[0;m" 
                     
# Copy userstores, tenants,jar files,.jks files from oldIS to newIS
sh copy-data-to-new-IS.sh 
sleep 10
echo "\033[0;32m\033[1mCopied userstores, tenants,jar files,.jks files from oldIS to newIS successfully\033[0;m"
 
# Get a backup of existing database
#sh backup-database.sh &
#sleep 30
echo "\033[0;32m\033[1mData backedup successfully\033[0;m" 

cd "$AUTOMATION_HOME_01_Migration_Automation"

for file in $(find $Home/Downloads/Automating-Product-Migration-Testing/local-setups/IS_HOME_NEW/wso2is-6.0.0/repository/conf -type f -name 'deployment.toml');
do
cat $Home/Downloads/Automating-Product-Migration-Testing/migration-automation/deployment.toml > $file;
sleep 10
done

cd "$AUTOMATION_HOME"

# Divert to bin folder
cd "$BIN_ISNEW"
echo "\033[0;32m\033[1mDiverted to bin folder successfully\033[0;m"

cd "$AUTOMATION_HOME"

# Run the migration client
toilet -f term -F border --gay 'Started running migration client'
sh migration-terminal.sh
sleep 100
toilet -f term -F border --gay 'Yay!Migration executed successfully.'

# Stop wso2IS migration server
cd "$BIN_ISNEW"                                                                         
./wso2server.sh stop
ps -ef | grep migration-terminal.sh| grep -v grep | awk '{print $2}' | xargs #kill	
sleep 60
toilet -f term -F border --gay 'Stopped migration terminal successfully.'
echo

toilet -f term -F border --gay 'Starting Migrated Identity Server'

cd "$AUTOMATION_HOME"

#Restart wso2IS (latest) server
sh server-start-newIS.sh
echo "\033[0;32m\033[1mWSO2 Identity Server has started successfully\033[0;m"

cd "$AUTOMATION_HOME_01_Migration_Automation"

# Run the .sh script to enter login credentials(admin) and divert to management console home page
sh enter-login-credentials.sh 
echo "\033[0;32m\033[1mEntered to Management console home page successfully\033[0;m"
 
cd "$GENERATE_TOKEN" 
echo "\033[0;32m\033[1mEntered to data population directory\033[0;m"

# Validate database.
sh validate-database-ubuntu-local-setup.sh

toilet -f term -F border --gay 'Migration executed and database validated successfully!'

toilet --filter metal -w 140 'BYE!!'




