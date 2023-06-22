#!/bin/bash

# Define color variables
GREEN='\033[0;32m\033[1m' # green color
RESET='\033[0m'           # reset color

# Get the value of the inputs
os=$1
startServer=$2
currentVersion=$3
migratingVersion=$4

# Setup file and path based on OS and server number
if [ "$os" = "ubuntu-latest" ]; then
  cd "/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation"
  chmod +x env.sh
  . ./env.sh
  echo "${GREEN}==> Env file for Ubuntu sourced successfully${RESET}"
  
  if [ "$startServer" = "current" ]; then
    cd "$IS_OLD_BIN"
    echo "${GREEN}Diverted to bin${RESET}"
    echo "${GREEN}Starting current Identity Server in Ubuntu OS${RESET}"
    chmod +x ./wso2server.sh
    echo "./wso2server.sh -Dcarbon.bootstrap.timeout=300" > start.sh
  elif [ "$startServer" = "migrated" ]; then
    cd "$BIN_ISNEW"
    echo "${GREEN}Diverted to bin${RESET}"
    echo "${GREEN}Starting Migrated Identity Server in Ubuntu OS${RESET}"
    chmod +x ./wso2server.sh
    echo "./wso2server.sh -Dcarbon.bootstrap.timeout=300" > start.sh
  elif [ "$startServer" = "migration" ]; then
    cd "$BIN_ISNEW"
    echo "${GREEN}Starting Migration terminal${RESET}"
    chmod +x ./wso2server.sh
    echo "./wso2server.sh -Dmigrate -Dcomponent=identity -Dcarbon.bootstrap.timeout=300" > start.sh
  fi
elif [ "$os" = "macos-latest" ]; then
  cd "/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation"
  chmod +x env.sh
  source ./env.sh
  echo "${GREEN}==> Env file for Mac sourced successfully${RESET}"
  
  if [ "$startServer" = "current" ]; then
    cd "$IS_OLD_BIN_MAC"
    echo "${GREEN}Diverted to bin${RESET}"
    echo "${GREEN}Starting current Identity Server in macOS${RESET}"
    chmod +x ./wso2server.sh
    echo "./wso2server.sh -Dcarbon.bootstrap.timeout=300" > start.sh
  elif [ "$startServer" = "migrated" ]; then
    cd "$BIN_ISNEW_MAC"
    echo "${GREEN}Diverted to bin${RESET}"
    echo "${GREEN}Starting Migrated Identity Server in macOS${RESET}"
    chmod +x ./wso2server.sh
    echo "./wso2server.sh -Dcarbon.bootstrap.timeout=300" > start.sh
  elif [ "$startServer" = "migration" ]; then
    cd "$BIN_ISNEW_MAC"
    echo "${GREEN}Starting Migration terminal${RESET}"
    chmod +x ./wso2server.sh
    echo "./wso2server.sh -Dmigrate -Dcomponent=identity -Dcarbon.bootstrap.timeout=300" > start.sh
  fi
fi

chmod +x start.sh && chmod 777 start.sh
nohup ./start.sh &

# Wait until server is up
is_server_up() {
  local status
  status=$(curl -k -L -s \
    -o /dev/null \
    -w "%{http_code}" \
    --request GET \
    "https://localhost:9443/")
  if [ "$status" -eq 200 ]; then
    return 0
  fi
  return 1
}

wait_until_server_is_up() {
  local timeout=600
  local wait_time=0
  while ! is_server_up; do
    echo "Waiting until server starts..." &&
      sleep 10
    wait_time=$((wait_time + 10))
    if [ "$wait_time" -ge "$timeout" ]; then
      echo "Timeout: server did not start within $timeout seconds"
      exit 1
    fi
  done
}

wait_until_server_is_up
echo "${GREEN}WSO2 Identity Server has started successfully${RESET}"
