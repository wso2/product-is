#!/bin/bash

# Starts wso2is in a new terminal window.

  port=9443
  npx ttab -w -q -t "WSO2 Identity Server" \
    -d "/home/wso2/Downloads/Automating-Product-Migration-Testing/local-setups/IS_HOME_OLD/wso2is-5.11.0/bin/" \
    "./wso2server.sh"
#./wso2server.sh -DportOffset=2

is_server_up() {
  local status
  status=$(curl -k -L -s \
    -o /dev/null \
    -w "%{http_code}" \
    --request GET \
    "https://localhost:9443/")
  if [[ "$status" == 200 ]]; then
    return 0
  fi
  return 1
}

wait_until_server_is_up() {
  while ! is_server_up; do
    echo "Waiting until server starts..." &&
      sleep 10
  done
}

is_server_up
wait_until_server_is_up

