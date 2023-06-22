#!/bin/bash

# Define color variables
GREEN='\033[0;32m\033[1m' # green color
RESET='\033[0m'           # reset color

# Get the value of the inputs
currentVersion=$1

# Source env file
cd /home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation
chmod +x env.sh
. ./env.sh

# Stop mysql running inside github actions and wait for the MySQL container to start
sudo systemctl stop mysql &
sleep 10
echo "${GREEN}==> Local mysql stopped successfully${RESET}"

# Start running docker container
docker run --name "$CONTAINER_NAME" -p "$HOST_PORT":"$CONTAINER_PORT" -e MYSQL_ROOT_PASSWORD="$ROOT_PASSWORD" -d mysql:"$MYSQL_VERSION"

# Wait for container to start up
while [ "$(docker inspect -f '{{.State.Status}}' "$CONTAINER_NAME")" != "running" ]; do
  printf "${GREEN}==> Waiting for container to start up...${RESET}\n"
  sleep 1
done
echo "${GREEN}==> Container is up and running.${RESET}"

# Get container IP address
CONTAINER_ID=$(docker ps -aqf "name=$CONTAINER_NAME")
DB_HOST=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' "$CONTAINER_ID")

while ! mysqladmin ping -h"$DB_HOST" --silent; do
  printf "${GREEN}==> Waiting for mysql server to be healthy...${RESET}\n"
  sleep 1
done

# add a timeout for the loop
# Modify the comment  

# Connect to MySQL server
echo "${GREEN}==> MySQL server is available on $DB_HOST${RESET}"

# MySQL is available
echo "${GREEN}==> MySQL is now available!${RESET}"

# Check docker status
docker ps

# Find the ID of the running MySQL container
MYSQL_CONTAINER_ID=$(docker ps | grep mysql | awk '{print $1}')

# Start the MySQL container
if [ -n "$MYSQL_CONTAINER_ID" ]; then
  docker start $MYSQL_CONTAINER_ID
  echo "${GREEN}==> MySQL container started successfully${RESET}"
else
  echo "${GREEN}==> No running MySQL container found${RESET}"
fi

# Check if MySQL is listening on the default MySQL port (3306)
if netstat -ln | grep ':3306'; then
  echo "${GREEN}==> MySQL is listening on port 3306${RESET}"

else
  echo "${GREEN}==> MySQL is not listening on port 3306${RESET}"
fi

# Create database
chmod +x "$DATABASE_CREATION_SCRIPT"
docker exec -i "$CONTAINER_NAME" sh -c 'exec mysql -uroot -p'$ROOT_PASSWORD'' <"$DATABASE_CREATION_SCRIPT"
echo "${GREEN}==> Database created successfully!${RESET}"

# Execute SQL scripts
chmod +x ~/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/utils/db-scripts/database-create-scripts/mysql.sql

if [ "$currentVersion" = "5.9.0" ]; then
docker exec -i "$CONTAINER_NAME" sh -c 'exec mysql -uroot -p'$ROOT_PASSWORD' -D '$DATABASE_NAME'' <"$DB_SCRIPT_MYSQL_5_9"
docker exec -i "$CONTAINER_NAME" sh -c 'exec mysql -uroot -p'$ROOT_PASSWORD' -D '$DATABASE_NAME'' <"$DB_SCRIPT_IDENTITY_5_9"
docker exec -i "$CONTAINER_NAME" sh -c 'exec mysql -uroot -p'$ROOT_PASSWORD' -D '$DATABASE_NAME'' <"$DB_SCRIPT_UMA_5_9"
docker exec -i "$CONTAINER_NAME" sh -c 'exec mysql -uroot -p'$ROOT_PASSWORD' -D '$DATABASE_NAME'' <"$DB_SCRIPT_CONSENT_5_9"
docker exec -i "$CONTAINER_NAME" sh -c 'exec mysql -uroot -p'$ROOT_PASSWORD' -D '$DATABASE_NAME'' <"$DB_SCRIPT_METRICS_5_9"
echo "${GREEN}==> Database scripts for IS 5.9 executed and created tables successfully!${RESET}"

else
docker exec -i "$CONTAINER_NAME" sh -c 'exec mysql -uroot -p'$ROOT_PASSWORD' -D '$DATABASE_NAME'' <"$DB_SCRIPT_MYSQL"
docker exec -i "$CONTAINER_NAME" sh -c 'exec mysql -uroot -p'$ROOT_PASSWORD' -D '$DATABASE_NAME'' <"$DB_SCRIPT_IDENTITY"
docker exec -i "$CONTAINER_NAME" sh -c 'exec mysql -uroot -p'$ROOT_PASSWORD' -D '$DATABASE_NAME'' <"$DB_SCRIPT_UMA"
docker exec -i "$CONTAINER_NAME" sh -c 'exec mysql -uroot -p'$ROOT_PASSWORD' -D '$DATABASE_NAME'' <"$DB_SCRIPT_CONSENT"
docker exec -i "$CONTAINER_NAME" sh -c 'exec mysql -uroot -p'$ROOT_PASSWORD' -D '$DATABASE_NAME'' <"$DB_SCRIPT_METRICS"
echo "${GREEN}==> Database scripts executed and created tables successfully!${RESET}"
fi


