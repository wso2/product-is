#!/bin/bash

# Define color variables
ORANGE='\033[0;33m\033[1m'
RESET='\033[0m'
GREEN='\033[0;32m\033[1m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
PURPLE='\033[1;35m'
BOLD='\033[1m'
NC='\033[0m' # No Color

currentVersion=$1

data_population_dir="/home/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation"

echo -e "${GREEN}==> Running data population scripts${NC}"
if [ "$currentVersion" = "5.9" ]; then
  # execute scripts in order
  for script in \
    "1-user-creation/create-user.sh" \
    "1-user-creation/create-bulk-users.sh" \
    "2-tenant-creation/create-tenant-soapAPI.sh" \
    "3-userstore-creation/create-userstore.sh" \
    "3-userstore-creation/create-user-in-userstore.sh" \
    "4-service-provider-creation/register-a-service-provider.sh" \
    "4-service-provider-creation/create-user-in-a-service-provider.sh" \
    "4-service-provider-creation/register-a-service-provider-get-access-token-ubuntu.sh" \
    "5-group-creation/create-group.sh" \
    "5-group-creation/create-groups-with-users.sh"
  do
    # construct the full path of the script
    script_path="$data_population_dir/$script"

    # check if script exists and is executable
    if [ -f "$script_path" ]; then
      chmod +x "$script_path" # make the script executable
      printf "${ORANGE}Running script: %s${RESET}\n" "$script_path"
      # execute script
      "$script_path"
    else
      echo "${GREEN}==> Script '$script_path' does not exist.${RESET}"
    fi
  done
else
  if [ -d "$data_population_dir" ]; then
    cd "$data_population_dir" || {
      echo "${RED}${BOLD}Failed to change directory.${NC}"
      exit 1
    }

    # execute scripts in alternative order
    for script in \
      "1-user-creation/create-user.sh" \
      "1-user-creation/create-bulk-users.sh" \
      "2-tenant-creation/create-tenant.sh" \
      "2-tenant-creation/create-tenant-soapAPI.sh" \
      "3-userstore-creation/create-userstore.sh" \
      "3-userstore-creation/create-user-in-userstore.sh" \
      "4-service-provider-creation/register-a-service-provider.sh" \
      "4-service-provider-creation/create-user-in-a-service-provider.sh" \
      "4-service-provider-creation/register-a-service-provider-get-access-token-ubuntu.sh" \
      "5-group-creation/create-group.sh" \
      "5-group-creation/create-groups-with-users.sh"
    do
      # construct the full path of the script
      script_path="$data_population_dir/$script"

      # check if script exists and is executable
      if [ -f "$script_path" ]; then
        chmod +x "$script_path" # make the script executable
        printf "${ORANGE}Running script: %s${RESET}\n" "$script_path"
        # execute script
        "$script_path"
      else
        echo "${GREEN}==> Script '$script_path' does not exist.${RESET}"
      fi
    done
  else
    echo "${GREEN}==> Directory '$data_population_dir' does not exist.${RESET}"
  fi
fi

# execute scripts in any other subdirectories
for dir in */; do
  # check if directory is not one of the specified ones and exists
  if [ "$dir" != "1-user-creation/" ] && [ "$dir" != "2-tenant-creation/" ] && [ "$dir" != "3-userstore-creation/" ] && [ "$dir" != "4-service-provider-creation/" ] && [ "$dir" != "5-group-creation/" ] && [ "$dir" != "windows-os/" ] && [ -d "$dir" ]; then
    # execute scripts in subdirectory
    cd "$dir" || exit
    for script in *.sh; do
      # check if script exists and is executable
      if [ -f "$script" ]; then
        chmod +x "$script" # make the script executable
        # execute script and redirect output to stdout
        printf "${ORANGE}Running script: %s${RESET}\n" "$script"
        "./$script" >&1
      fi
    done
    cd ..
  fi
done

