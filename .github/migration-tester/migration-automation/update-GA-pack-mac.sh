#!/bin/bash

# Define colours
RED='\033[0;31m'
GREEN='\033[0;32m\033[1m'
PURPLE='\033[1;35m'
BOLD='\033[1m'
RESET='\033[0m' # No Color

email=$1
password=$2
startServer=$3


# Source env file
. "/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/env.sh"
echo -e "${GREEN}==> Env file for Macos sourced successfully${RESET}"

# Copy update tool from utils to bin folder
cd "/Users/runner/work/product-is/product-is/.github/migration-tester/utils/update-tools"

if [ "$startServer" = "current" ]; then
  cp -r $UPDATE_TOOL_MACOS $BIN_ISOLD_MAC
elif [ "$startServer" = "migrating" ]; then
  cp -r $UPDATE_TOOL_MACOS $BIN_ISNEW_MAC
fi
copy_exit_code=$?
if [ $copy_exit_code -eq 0 ]; then
    echo "${GREEN}==> Update tool successfully copied to "$startServer"${RESET}"
else
    echo "${RED}==> Failed to copy the update tool.${RESET}"
fi

if [ "$startServer" = "current" ]; then
  cd "$BIN_ISOLD_MAC"
elif [ "$startServer" = "migrating" ]; then
  cd "$BIN_ISNEW_MAC"
fi

# Install expect if not already installed
if ! command -v expect &> /dev/null; then
    echo "Installing expect..."
    brew install expect
fi

# Create an expect script file
cat >wso2update_script.expect <<EOF
#!/usr/bin/expect -f
spawn ./wso2update_darwin
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

# Run the expect script in the background
./wso2update_script.expect &
wait $!
echo "${GREEN}==> Updated the Client Tool successfully${RESET}"

# Update Product Pack
./wso2update_darwin 
echo "${GREEN}==> Updated the Product Pack successfully${RESET}"
wait $!
