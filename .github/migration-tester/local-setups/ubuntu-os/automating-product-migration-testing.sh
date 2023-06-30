#!/bin/bash

chmod +x $Home/Downloads/Automating-Product-Migration-Testing/local-setups/migration-automation-script-linux.sh
chmod +x $Home/Downloads/Automating-Product-Migration-Testing/local-setups/migration-automation-script-macos.sh
chmod +x $Home/Downloads/Automating-Product-Migration-Testing/local-setups/migration-automation-script-windows.ps1

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Prompt user for database type
while true; do
    read -p "$(echo -e "${BOLD}${YELLOW}Enter database type (1 for mysql, 2 for postgresql, 3 for mssql): ${NC}")" db_type

    case $db_type in
        1)
            db="mysql"
            break
            ;;
        2)
            db="postgresql"
            break
            ;;
        3)
            db="mssql"
            break
            ;;
        *)
            echo -e "${BOLD}${RED}Invalid input. Please try again.${NC}"
            ;;
    esac
done

# Prompt user for operating system
while true; do
    read -p "$(echo -e "${BOLD}${YELLOW}Enter operating system (1 for ubuntu, 2 for macos, 3 for windows): ${NC}")" os_type

    case $os_type in
        1)
            os="ubuntu"
            break
            ;;
        2)
            os="macos"
            break
            ;;
        3)
            os="windows"
            break
            ;;
        *)
            echo -e "${BOLD}${RED}Invalid input. Please try again.${NC}"
            ;;
    esac
done

if [ "$db" = "mysql" ] && [ "$os" = "ubuntu" ]; then
    echo -e "${BOLD}${GREEN}Executing migration-automation-script-linux.sh for MySQL on Ubuntu${NC}"
    sh $Home/Downloads/Automating-Product-Migration-Testing/local-setups/migration-automation-script-linux.sh
elif [ "$db" = "mssql" ] && [ "$os" = "macos" ]; then
    echo -e "${BOLD}${GREEN}Executing migration-automation-script-macos.sh for MSSQL on macOS${NC}"
    sh $Home/Downloads/Automating-Product-Migration-Testing/local-setups/migration-automation-script-macos.sh
elif [ "$db" = "postgresql" ] && [ "$os" = "windows" ]; then
    echo -e "${BOLD}${GREEN}Executing migration-tester/migration-automation-script-windows.ps1 for PostgreSQL on Windows${NC}"
    sh $Home/Downloads/Automating-Product-Migration-Testing/local-setups/migration-automation-script-windows.ps1
else
    echo -e "${BOLD}${RED}Invalid database or operating system type. Please try again.${NC}"
fi

