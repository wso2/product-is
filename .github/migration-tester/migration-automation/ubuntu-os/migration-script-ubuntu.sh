#!/bin/bash

# THIS IS THE SHELL SCRIPT FOR LINUX (UBUNTU) - MYSQL/MSSQL/POSTGRE MIGRATIONS INSIDE GITHUB ACTIONS - [POC]

# Define color variables
CYAN='\033[0;36m\033[1m'   # cyan color
GREEN='\033[0;32m\033[1m'  # green color
BLUE='\033[0;34m\033[1m'   # blue color
YELLOW='\033[0;33m\033[1m' # yellow color
ORANGE='\033[0;91m\033[1m' # orange color
RED='\033[0;31m\033[1m'    # red color
RESET='\033[0m'            # reset color

# Update the system before downloading packages
sudo apt-get -qq update

cd "$AUTOMATION_HOME"
cd ./.github/migration-tester/migration-automation

# Get the value of the inputs from workflow dispatch

urlOld=$1
urlNew=$2
currentVersion=$3
migratingVersion=$4
database=$5
os=$6
email=$7
password=$8
migrationClient=$9
gcpClientId=${10}
gcpClientSecret=${11}
gcpRefreshToken=${12}
gcpClientIddummy=${13}

# Remove spaces from the beginning and end of the currentVersion variable
currentVersion=$(echo $currentVersion | xargs)

# Combine "wso2is-" and the value of the "currentVersion" input, without spaces
combinedCurrentVersion="wso2is-${currentVersion}"

# Replace all instances of "CurrentVersion" with the combined version value
sed -i "s/CurrentVersion/${combinedCurrentVersion}/g" /home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/env.sh

# Remove spaces from the beginning and end of the migratingVersion variable
migratingVersion=$(echo $migratingVersion | xargs)

# Combine "wso2is-" and the value of the "currentVersion" input, without spaces
combinedMigratingVersion="wso2is-${migratingVersion}"

# Replace all instances of "MigratingVersion" with the value of the "migratingVersion" input, without spaces
sed -i "s/MigratingVersion/${combinedMigratingVersion}/g" /home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/env.sh

# Define the message in a variable for easier modification
echo
echo "${ORANGE}WELCOME TO AUTOMATING PRODUCT MIGRATION TESTING! THIS TIME WE WILL PERFORM A MIGRATION TESTING FROM IS VERSION ${RESET}${YELLOW}"$currentVersion"${RESET}${ORANGE} TO IS VERSION ${RESET}${YELLOW}"$migratingVersion"${RESET}${ORANGE} IN THE ${RESET}${YELLOW}"$database"${RESET}${ORANGE} DATABASE, RUNNING ON THE ${RESET}${YELLOW}"$os"${RESET}${ORANGE} OPERATING SYSTEM.${RESET}"

# Print instructions with different colors and formatting using echo command
echo "${ORANGE}${RESET} ${CYAN}1. PRIOR TO PROCEEDING, ENSURE THAT YOU HAVE MADE THE NECESSARY MODIFICATIONS IN THE env.sh FILE TO ALIGN WITH YOUR REQUIREMENTS.${RESET} ${ORANGE}${RESET}"
echo "${ORANGE}${RESET} ${CYAN}2. IF YOU REQUIRE THE INCLUSION OF ADDITIONAL FEATURES, SUCH AS DATA POPULATION OR VERIFICATION, PLEASE INCORPORATE THEM WITHIN THE APPROPRIATE DIRECTORY NAMED data-population-and-validation.${RESET} ${ORANGE}${RESET}"
echo "${ORANGE}${RESET} ${CYAN}3. DOUBLE-CHECK THE CONTENTS OF THE deployment.toml FILES IN THE deployment-tomls FOLDER TO ENSURE THAT IT CORRESPONDS TO YOUR SPECIFIC DATABASE CONFIGURATIONS.${RESET} ${ORANGE}${RESET}"
echo

# Print welcome message in terminal
echo "${ORANGE}"PROCESS STARTED!"${RESET}"

# Source env file
cd "/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation"
chmod +x env.sh
. ./env.sh
echo "${GREEN}==> Env file sourced successfully${RESET}"

# Setup Java
sudo apt-get install -y openjdk-11-jdk &
wait $!
echo "${GREEN}==> Installed Java successfully!${RESET}"

# Set the JAVA_HOME environment variable
export JAVA_HOME=$(readlink -f $(which java)) &
wait $!

cd "$AUTOMATION_HOME"

# Create directory for placing wso2IS
mkdir IS_HOME_OLD
echo "${GREEN}==> Created a directory to place wso2IS${RESET}"

# Navigate to folder
cd IS_HOME_OLD
echo "${GREEN}==> Navigated to home folder successfully${RESET}"

# Download needed wso2IS zip
# Generate access token
access_token=$(curl --location --request POST 'https://oauth2.googleapis.com/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id='$gcpClientId'' \
--data-urlencode 'client_secret='$gcpClientSecret'' \
--data-urlencode 'refresh_token='$gcpRefreshToken'' \
--data-urlencode 'grant_type=refresh_token' | jq -r '.access_token')

# Download the file using the access token from the "access_token" file
response=$(curl "https://www.googleapis.com/drive/v3/files/1WSQwh2aizd-nhrQQEc_PqnTJJxiRufUv?alt=media" \
  --header "Authorization: Bearer $access_token" \
  --header "Accept: application/json" \
  --compressed -O wso2is.zip)

# Check if the response contains any error message
if echo "$response" | grep -q '"error":'; then
  # If there is an error, print the failure message with the error description
  error_description=$(echo "$response" | jq -r '.error_description')
  echo -e "${RED}${BOLD}Failure in downloading Identity Server $error_description${NC}"

else
  # If there is no error, print the success message
  echo -e "${PURPLE}${BOLD}Success: IS downloaded successfully .${NC}"
fi

# Check if the file was downloaded successfully
if [ $? -eq 0 ]; then
  echo "File downloaded successfully."
else
  echo "File download failed."
fi
ls -a

# Unzip the downloaded zip 
unzip -qq *.zip &
wait $!
ls -a
echo "${GREEN}==> Unzipped downloaded Identity Server zip${RESET}"

cd "$AUTOMATION_HOME"

# Update IS packs
if [ "$currentVersion" = "5.9.0" ]; then
    echo "The current version is 5.9.0."
    # Update IS packs
    chmod +x update-pack.sh 
    sh update-pack.sh "$email" "$password" "current"
    wait $!
else
    # Update IS packs
    chmod +x update-pack-5-9.sh 
    sh update-pack.sh "$email" "$password" "current"
    wait $! 
       
fi

cd "$AUTOMATION_HOME"

# Given read write access to deployment.toml
chmod +x "$DEPLOYMENT"
echo "${GREEN}==> Given read write access to deployment.toml${RESET}"

cd "$AUTOMATION_HOME"

# Needed changes in deployment.toml
chmod +x change-deployment-toml.sh
sh change-deployment-toml.sh "$currentVersion" "$migratingVersion" "$database" "$os" "current"
echo "${GREEN}==> Deployment.toml changed successfully${RESET}"

# Check if database is set to mysql
if [ "$database" = "mysql" ]; then
    # Setup mysql
    cd "$UBUNTU_HOME"
    chmod +x setup-mysql-ubuntu.sh
    sh setup-mysql-ubuntu.sh "$currentVersion"

else
    echo "${GREEN}==> Skipping the MySQL setup process since the selected database is "$database" ${RESET}"
fi

cd "$AUTOMATION_HOME"

# Copy Jars
chmod +x copy-jar-file.sh
sh copy-jar-file.sh "$database" "$os"

cd "$AUTOMATION_HOME"

# Start wso2IS
echo "${GREEN}==> Identity server "$currentVersion" started running!${RESET}"

# Starting downloaded identity server
chmod +x start-server.sh
sh start-server.sh "$os" "current" "$currentVersion" "$migratingVersion"

cd "$AUTOMATION_HOME"

# Run the .sh script to enter login credentials(admin) and divert to management console home page
sh enter-login-credentials.sh &
wait $!
echo "${GREEN}==> Entered to Management console home page successfully${RESET}"

cd "$DATA_POPULATION"
echo "${GREEN}==> Entered the data population directory successfully.${RESET}"

# Run data-population-script.sh which is capable of populating data to create users, tenants, userstores, generate tokens, etc.
chmod +x automated-data-population-and-validation-script-ubuntu.sh
sh automated-data-population-and-validation-script-ubuntu.sh "$currentVersion"
echo "${GREEN}==> Created users, user stores, service providers, tenants, generated oAuth tokens and executed the script successfully${RESET}"

cd "$AUTOMATION_HOME"
echo "${GREEN}==> Directed to home successfully${RESET}"

# Stop wso2IS
chmod +x stop-server.sh
sh stop-server.sh "$os" "current"
echo

cd "$AUTOMATION_HOME"
echo "${GREEN}==> Directed to home successfully${RESET}"

# Create directory for placing latest wso2IS (IS to migrate)
mkdir IS_HOME_NEW
echo "${GREEN}==> Created a directory for placing latest wso2IS${RESET}"

# Navigate to folder
cd "$IS_HOME_NEW"

# Download needed (latest) wso2IS zip
#wget -qq --waitretry=5 --retry-connrefused "$urlNew"
#wait $!


#curl -L -o wso2is.zip "https://drive.google.com/uc?export=download&id=1ik0CJM5V9CXzBwl7DQpeBDBTT4t_cWlL"
#response=$(curl -k -L -o wso2is.zip "https://drive.google.com/uc?export=download&id=1ik0CJM5V9CXzBwl7DQpeBDBTT4t_cWlL")
#echo "$response"
#curl -k -L -o wso2is.zip "https://drive.google.com/u/0/uc?id=1ik0CJM5V9CXzBwl7DQpeBDBTT4t_cWlL&export=download"

# Download needed wso2IS zip
if [ "$currentVersion" = "5.9.0" ]; then

    response=$(curl -k -L -o wso2is.zip "https://drive.google.com/u/0/uc?id=1GU32FtPGvvB2WsmQoPnHr5yn1M6ddL-h&amp;amp;export=download&amp;amp;confirm=t&amp;amp;uuid=712b27d8-ea10-4e0b-bbbd-3cde24b1d92e&amp;amp;at=AKKF8vzpFDL5XdIrNRv6KFY0ZvPr:1687251333945&amp;confirm=t&amp;uuid=efe88210-d059-4968-84c6-7a1236bb6ef9&amp;at=AKKF8vxFWIDReSULenUtKrASKULT:1687251490306&confirm=t&uuid=7017f976-902b-4050-a3a6-22b26bb46d88&at=AKKF8vyia4DpEk742C_FTMCmJDE9:1687251582718")
    wait $!
    echo "$response"
else
    
    # Specify the Google Drive file URL
    file_url="$urlOld"
    
    # Specify your Google Drive API key
    api_key="$migrationApiKey"
    
    # Extract the file ID from the URL
    file_id=$(echo "$file_url" | awk -F'/' '{print $NF}' | awk -F'=' '{print $2}')
    
    # Generate the download link with the API key
    download_link="https://www.googleapis.com/drive/v3/files/$file_id?alt=media&key=$api_key"
    
    # Download the ZIP file using curl
    curl -L -o wso2is.zip "$download_link"
    wait $!
fi
echo "${GREEN}==> Downloaded "$migratingVersion" zip${RESET}"

# Unzip IS archive
unzip -qq *.zip &
wait $!
echo "${GREEN}==> Unzipped "$migratingVersion" zip${RESET}"

cd "$AUTOMATION_HOME"
chmod +x download-migration-client.sh
sh download-migration-client.sh "$migrationClient" &
wait $!
unzip -qq wso2is-migration-1.0.225.zip  &
wait $!
ls -a
echo "${GREEN}==> Unzipped migration client successfully${RESET}"

# Copy migration client from home to migration client folder
cp -r "/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/wso2is-migration-1.0.225" "/home/runner/work/product-is/product-is/.github/migration-tester/utils/migration-client/" &
cp_pid=$!

wait $cp_pid
echo "${GREEN}==> Copied migration client from home to migration client folder${RESET}"

# Navigate to dropins folder
cd "$DROPINS_PATH_HOME"

# Copy droipns folder to wso2IS (latest) dropins folder
cp -r "$DROPINS_PATH" "$COMPONENTS_PATH" &
cp_pid=$!

wait $cp_pid
echo "${GREEN}==> Jar files from migration client have been copied to IS_HOME_NEW/repository/components/dropins folder successfully!${RESET}"

cd "$COMPONENTS_PATH"

# Copy migration resources folder to wso2IS (latest) root folder
cp -r "$MIGRATION_RESOURCES" "$IS_NEW_ROOT" &
cp_pid=$!

wait $cp_pid
echo "${GREEN}==> Migration-resources from migration client have been copied to IS_HOME_NEW root folder successfully!${RESET}"

cd "$AUTOMATION_HOME"
echo "${GREEN}==> Diverted to home successfully${RESET}"

# Needed changes in migration-config.yaml
chmod +x change-migration-config-yaml.sh
sh change-migration-config-yaml.sh "$currentVersion" "$migratingVersion" "$os"
wait $!
echo "${GREEN}==> Did needed changes in migration-config.yaml file successfully${RESET}"

# Copy userstores, tenants,jar files,.jks files from oldIS to newIS
cp -r "$LIB" "$LIB_NEW"
echo "${BLUE}==> Jar files from IS "$currentVersion" to IS "$migratingVersion" copied successfully!${RESET}"

cp -r "$TENANT_OLD_PATH" "$TENANT_NEW_PATH"
echo "${BLUE}==> Tenants from from IS "$currentVersion" to IS "$migratingVersion" copied successfully!${RESET}"

cp -r "$RESOURCES_OLD_PATH" "$RESOURCES_NEW_PATH"
echo "${BLUE}==> .jks files from from IS "$currentVersion" to IS "$migratingVersion" copied successfully!${RESET}"

cp -r "$USERSTORE_OLD_PATH" "$USERSTORE_NEW_PATH"
echo "${BLUE}==> Userstores from IS "$currentVersion" to IS "$migratingVersion" copied successfully!${RESET}"

# Check if all files are copied successfully
if [ $? -eq 0 ]; then
    echo "${BLUE}==> All files are copied successfully!${RESET}"
else
    echo "${BLUE}==> Error: Some files could not be copied.${RESET}"
fi
echo "${BLUE}==> Copied userstores, tenants,jar files,.jks files from oldIS to newIS successfully${RESET}"

# Deployment toml changes in new is version
chmod +x change-deployment-toml.sh
sh change-deployment-toml.sh "$currentVersion" "$migratingVersion" "$database" "$os" "migrated"
echo "${GREEN}==> Deployment.toml changed successfully${RESET}"
echo "${BLUE}==> Copied deployment toml of "$currentVersion" to "$migratingVersion" successfully!${RESET}"
wait $!

# Execute consent management db scripts for IS 5.11.0 - MySQL
if [ "$migratingVersion" = "5.11.0" && "$database" = "mysql" ]; then
    docker exec -i amazing_feynman sh -c 'exec mysql -uroot -proot -D testdb' </home/runner/work/product-is/product-is/.github/migration-tester/utils/other-db-scripts/config-management-is-5-11.sql
    echo "${GREEN}==> Executing consent management db scripts for IS 5.11.0 - MySQL${RESET}"
else
    echo "${GREEN}==> Skipping executing consent management db scripts since the migrating version is not IS 5.11.0"$database" ${RESET}"

fi
wait $!

# Execute consent management db scripts for IS 5.11.0 - MSSQL
if [ "$migratingVersion" = "5.11.0" && "$database" = "mssql" ]; then
    # Add the command for executing MSSQL script here
    echo "${GREEN}==> Executing consent management db scripts for IS 5.11.0 - MSSQL${RESET}"
else
    echo "${GREEN}==> Skipping executing consent management db scripts since the migrating version is not IS 5.11.0"$database" ${RESET}"
fi
wait $!

# Execute consent management db scripts for IS 5.11.0 - PostgreSQL
if [ "$migratingVersion" = "5.11.0" && "$database" = "postgres" ]; then
    # Add the command for executing PostgreSQL script on Ubuntu here
    echo "${GREEN}==> Executing consent management db scripts for IS 5.11.0 - PostgreSQL-Ubuntu${RESET}"
else
    echo "${GREEN}==> Skipping executing consent management db scripts since the migrating version is not IS 5.11.0"$database" ${RESET}"
fi
wait $!

# Run the migration client
echo "${GREEN}==> Started running migration client${RESET}"

# Get the existing time and date
time_and_date=$(date +"%Y-%m-%d %H:%M:%S")

# Display message with migration details, currentVersion and migrateVersion values, and time and date
STAR='*'
SPACE=' '

# Define box width
box_width=50

# Function to print a line with stars
print_star_line() {
    printf "%s\n" "$(printf "%${box_width}s" | tr ' ' "$STAR")"
}

# Print the box with migration details
print_star_line
echo "${YELLOW}${STAR}${SPACE}Migration details:${SPACE}${RESET}"
echo "${YELLOW}${STAR}${SPACE}Migrating from IS: "$currentVersion" to IS: "$migratingVersion"${SPACE}${RESET}"
echo "${YELLOW}${STAR}${SPACE}Database: "$database"${SPACE}${RESET}"
echo "${YELLOW}${STAR}${SPACE}Operating System: "$os"${SPACE}${RESET}"
echo "${YELLOW}${STAR}${SPACE}Time and date: $time_and_date${SPACE}${RESET}"
print_star_line

# Start the migration server
chmod +x start-server.sh
sh start-server.sh "$os" "migration" "$currentVersion" "$migratingVersion"
echo "${GREEN}==> Yay! Migration process completed!🎉 Check artifacts after completing workflow run to check whether there are any errors${RESET}"

cd "$AUTOMATION_HOME"
echo "${GREEN}==> Directed to home successfully${RESET}"

# Stop wso2IS
chmod +x stop-server.sh
sh stop-server.sh "$os" "migration"
echo

# Special config change when migrating from IS 5.9 changing userstore type to database unique id
if [ "$currentVersion" = "5.9.0" ]; then
    cd "$DEPLOYMENT_PATH_NEW"
    chmod +x deployment.toml
    for file in $(find "$DEPLOYMENT_PATH_NEW" -type f -name 'deployment.toml'); do
        sed -i 's/type = "database"/#type = "database"/' "$file"
        sed -i 's/#type = "database_unique_id"/type = "database_unique_id"/' "$file"
        echo "Content of $file:"
        cat "$file"
        wait $!
    done
    echo "${GREEN}==> Changes made to deployment toml file - special config change when migrating from IS 5.9 changing userstore type to database unique id${RESET}"

else
    echo "${GREEN}==> Skipping this step since the current version was not IS 5.9.0"$database" ${RESET}"
fi

cd "$AUTOMATION_HOME"
echo "${GREEN}==> Migrated WSO2 Identity Server - IS "$migratingVersion" is starting....${RESET}"

# Starting migrated identity server
chmod +x start-server.sh
sh start-server.sh "$os" "migrated" "$currentVersion" "$migratingVersion"

cd "$AUTOMATION_HOME"

# Run the .sh script to enter login credentials(admin) and divert to management console home page
sh enter-login-credentials.sh &
wait $!
echo "${GREEN}==> Entered to Management console home page successfully${RESET}"

cd "$SERVICE_PROVIDER_PATH"
echo "${GREEN}==> Entered to data population directory-service provider creation${RESET}"

# Run data-population-script.sh which is capable of populating data to create users,tenants,userstores,generate tokens etc.
chmod +x validate-database-ubuntu.sh
sh validate-database-ubuntu.sh
wait $!
echo "${GREEN}==> Validated database successfully${RESET}"

cd "$AUTOMATION_HOME"
echo "${GREEN}==> Directed to home successfully${RESET}"

# Stop wso2IS
chmod +x stop-server.sh
sh stop-server.sh "$os" "migrated"
echo

echo "${CYAN}-END OF AUTOMATING PRODUCT MIGRATION TESTING-${CYAN}"
