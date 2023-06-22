#!/bin/bash

log_file=/home/wso2/Downloads/Automating-Product-Migration-Testing/local-setups/migration.log
carbon_log_file=/home/wso2/Downloads/Automating-Product-Migration-Testing/local-setups/IS_HOME_OLD/wso2is-6.0.0/repository/logs/wso2carbon.log
wso2_is_home=/home/wso2/Downloads/Automating-Product-Migration-Testing/local-setups/IS_HOME_NEW/wso2is-6.0.0

# Start WSO2 Identity Server and redirect all output to log file
${wso2_is_home}/bin/wso2server.sh -Dmigrate -Dcomponent=identity >> "${log_file}" 2>&1 &

# Wait for server to start
is_server_up() {
  local status
  status=$(curl -k -L -s \
    -o /dev/null \
    -w "%{http_code}" \
    --request GET \
    "https://localhost:9443/")
}

wait_until_server_is_up() {
  while ! is_server_up; do
    echo "Waiting until server starts..." >> "${log_file}"
    sleep 10
  done
}

is_server_up 
wait_until_server_is_up 

# Print carbon logs to log file in real-time
tail -f "${carbon_log_file}" >> "${log_file}" 2>&1 &
