#!/bin/bash

# Define colours
RED='\033[0;31m'
GREEN='\033[0;32m\033[1m'
PURPLE='\033[1;35m'
BOLD='\033[1m'
NC='\033[0m' # No Color

email=$1
password=$2
startServer=$3


# Source env file
. "/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation/env.sh"
echo -e "${GREEN}==> Env file for Ubuntu sourced successfully${NC}"

# Copy update tool from utils to bin folder
cd "/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/update-tools"


if [ "$startServer" = "currentVersion" ]; then
  cp -r $UPDATE_TOOL_UBUNTU $BIN_ISOLD
elif [ "$startServer" = "migratingVersion" ]; then
  cp -r $UPDATE_TOOL_UBUNTU $BIN_ISNEW
fi
copy_exit_code=$?
if [ $copy_exit_code -eq 0 ]; then
    echo "${GREEN}==> Update tool successfully copied to "$startServer"${RESET}"
else
    echo "${RED}==> Failed to copy the update tool.${RESET}"
fi

if [ "$startServer" = "currentVersion" ]; then
  cd "$BIN_ISOLD"
elif [ "$startServer" = "migratingVersion" ]; then
  cd "$BIN_ISNEW"
fi


sudo apt-get install expect -y

# Create an expect script file
cat >wso2update_script.expect <<EOF
#!/usr/bin/expect -f
email=$1
password=$2

spawn ./wso2update_linux
expect "Please enter your credentials to continue."
sleep 5
send -- "$email\r"
expect "Email:"
sleep 5
send -- "$password\r"
expect {
    "wso2update: Error while authenticating user: Error while authenticating user credentials: Invalid email address '*'" {
        puts "Invalid email address. Please check the MIGRATION_EMAIL environment variable."
        exit 1
    }
    "wso2update: Error while authenticating user: Error while authenticating user credentials: Unable to read input: EOF" {
        puts "Error while authenticating user credentials. Please check the MIGRATION_PASSWORD environment variable."
        exit 1
    }
    eof {
        puts "Updated the Client Tool successfully"
        exit 0
    }
}
EOF
# Set executable permissions for the expect script
chmod +x wso2update_script.expect
# Run the expect script
./wso2update_script.expect "$email" "$password"

echo "${GREEN}==> Updated the Client Tool successfully${RESET}" &
wait $!

# Update Product Pack
./wso2update_linux 
echo "${GREEN}==> Updated the Product Pack successfully${RESET}" &
wait $!


