#!/bin/bash

# Define color variables
GREEN='\033[0;32m\033[1m' # green color
RESET='\033[0m'           # reset color

# Get the value of the inputs
database=$1
os=$2

# Setup file and path based on OS
if [ "$os" = "ubuntu-latest" ]; then
  cd "/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation"
  chmod +x env.sh
  . ./env.sh
  echo "${GREEN}==> Env file for Ubuntu sourced successfully${RESET}"

elif [ "$os" = "macos-latest" ]; then
  cd "/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation" 1
  chmod +x env.sh
  source ./env.sh
  echo "${GREEN}==> Env file for Mac sourced successfully${RESET}"
fi

# Modify the JDBC driver path based on the database and OS
if [ "$database" = "mssql" ]; then
  if [ "$os" = "ubuntu-latest" ]; then
    jdbc_driver="$JAR_MSSQL"
  elif [ "$os" = "macos-latest" ]; then
    jdbc_driver="$JAR_MSSQL_MAC"
  fi
elif [ "$database" = "postgres" ]; then
  if [ "$os" = "ubuntu-latest" ]; then
    jdbc_driver="$JAR_POSTGRE"
  elif [ "$os" = "macos-latest" ]; then
    jdbc_driver="$JAR_POSTGRE_MAC"
  fi
else
  if [ "$os" = "ubuntu-latest" ]; then
    jdbc_driver="$JAR_MYSQL"
  elif [ "$os" = "macos-latest" ]; then
    jdbc_driver="$JAR_MYSQL_MAC"
  fi
fi

# Copy the JDBC driver to the target directory based on OS
if [ "$os" = "ubuntu-latest" ]; then
  cp -r "$jdbc_driver" "$LIB"
  lib_folder="$LIB"
elif [ "$os" = "macos-latest" ]; then
  cp -r "$jdbc_driver" "$LIB_MAC"
  lib_folder="$LIB_MAC"
fi

# Wait for the JDBC driver to be copied to the lib folder
while [ ! -f "$jdbc_driver" ]; do
  echo "${GREEN}==> JDBC driver in $jdbc_driver not found in lib folder, waiting...${RESET}"
  sleep 5
done

if [ "$os" = "ubuntu-latest" ]; then
  echo "${GREEN}==> Validating the JDBC drivers inside $LIB...${RESET}"
  cd "$LIB"
  ls -a
elif [ "$os" = "macos-latest" ]; then
  echo "${GREEN}==> Validating the JDBC drivers inside $LIB_MAC...${RESET}"
  cd "$LIB_MAC"
  ls -a
fi

echo "${GREEN}==> JDBC driver in $jdbc_driver found in lib folder, continuing...${RESET}"
