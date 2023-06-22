#!/bin/bash

# Define color variables
GREEN='\033[0;32m\033[1m' # green color
RESET='\033[0m'           # reset color

# Get the value of the inputs
currentVersion=$1
migratingVersion=$2
database=$3
os=$4
startServer=$5

# Set deployment file and path based on OS
if [ "$os" = "ubuntu-latest" ]; then
    if [ "$startServer" = "current" ]; then
        deployment_file="$DEPLOYMENT_PATH/deployment.toml"
        deployment_path="$DEPLOYMENT_PATH"
        cd "/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation"
        chmod +x env.sh
        . ./env.sh
        echo "${GREEN}==> Env file for Ubuntu sourced successfully${RESET}"
    elif [ "$startServer" = "migrated" ]; then
        deployment_file="$DEPLOYMENT_PATH_NEW/deployment.toml"
        deployment_path="$DEPLOYMENT_PATH_NEW"
        cd "/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation"
        chmod +x env.sh
        . ./env.sh
        echo "${GREEN}==> Env file for Migrating Identity server in Ubuntu os sourced successfully${RESET}"
    fi
elif [ "$os" = "macos-latest" ]; then
    if [ "$startServer" = "current" ]; then
        deployment_file="$DEPLOYMENT_PATH_MAC/deployment.toml"
        deployment_path="$DEPLOYMENT_PATH_MAC"
        cd "/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation"
        chmod +x env.sh
        source ./env.sh
        echo "${GREEN}==> Env file for Mac sourced successfully${RESET}"
    elif [ "$startServer" = "migrated" ]; then
        deployment_file="$DEPLOYMENT_PATH_NEW_MAC/deployment.toml"
        deployment_path="$DEPLOYMENT_PATH_NEW_MAC"
        cd "/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation"
        chmod +x env.sh
        source ./env.sh
        echo "${GREEN}==> Env file for migrating Identity server in Macos sourced successfully${RESET}"
    fi
fi

# Set deployment automation file based on database and OS - Current Version
if [ "$startServer" = "current" ]; then
    if [ "$database" = "mysql" ]; then
        if [ "$os" = "ubuntu-latest" ]; then
            case "$currentVersion" in
            "5.9.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_5_9"
                ;;
            "5.10.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_5_10"
                ;;
            "5.11.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_5_11"
                ;;
            "6.0.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_6_0"
                ;;
            "6.1.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_6_1"
                ;;
            "6.2.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_6_2"
                ;;
            esac
        elif [ "$os" = "macos-latest" ]; then
            case "$currentVersion" in
            "5.9.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_5_9"
                ;;
            "5.10.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_5_10"
                ;;
            "5.11.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_5_11"
                ;;
            "6.0.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_6_0"
                ;;
            "6.1.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_6_1"
                ;;
            "6.2.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_6_2"
                ;;
            esac
        fi
    elif [ "$database" = "postgres" ]; then
        if [ "$os" = "ubuntu-latest" ]; then
            case "$currentVersion" in
            "5.9.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_5_9"
                ;;
            "5.10.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_5_10"
                ;;
            "5.11.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_5_11"
                ;;
            "6.0.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_6_0"
                ;;
            "6.1.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_6_1"
                ;;
            "6.2.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_6_2"
                ;;
            esac
        elif [ "$os" = "macos-latest" ]; then
            case "$currentVersion" in
            "5.9.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_5_9"
                ;;
            "5.10.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_5_10"
                ;;
            "5.11.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_5_11"
                ;;
            "6.0.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_6_0"
                ;;
            "6.1.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_6_1"
                ;;
            "6.2.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_6_2"
                ;;
            esac
        fi
    elif [ "$database" = "mssql" ]; then
        if [ "$os" = "ubuntu-latest" ]; then
            case "$currentVersion" in
            "5.9.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_5_9"
                ;;
            "5.10.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_5_10"
                ;;
            "5.11.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_5_11"
                ;;
            "6.0.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_6_0"
                ;;
            "6.1.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_6_1"
                ;;
            "6.2.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_6_2"
                ;;
            esac
        elif [ "$os" = "macos-latest" ]; then
            case "$currentVersion" in
            "5.9.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_5_9"
                ;;
            "5.10.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_5_10"
                ;;
            "5.11.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_5_11"
                ;;
            "6.0.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_6_0"
                ;;
            "6.1.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_6_1"
                ;;
            "6.2.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_6_2"
                ;;
            esac
        fi
    fi
fi

# Set deployment automation file based on database and OS - Migrating Version
if [ "$startServer" = "migrated" ]; then
    if [ "$database" = "mysql" ]; then
        if [ "$os" = "ubuntu-latest" ]; then
            case "$migratingVersion" in
            "5.9.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_5_9_MIGRATION"
                ;;
            "5.10.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_5_10_MIGRATION"
                ;;
            "5.11.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_5_11_MIGRATION"
                ;;
            "6.0.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_6_0_MIGRATION"
                ;;
            "6.1.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_6_1_MIGRATION"
                ;;
            "6.2.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_UBUNTU_IS_6_2_MIGRATION"
                ;;
            esac
        elif [ "$os" = "macos-latest" ]; then
            case "$migratingVersion" in
            "5.9.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_5_9_MIGRATION"
                ;;
            "5.10.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_5_10_MIGRATION"
                ;;
            "5.11.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_5_11_MIGRATION"
                ;;
            "6.0.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_6_0_MIGRATION"
                ;;
            "6.1.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_6_1_MIGRATION"
                ;;
            "6.2.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_6_2_MIGRATION"
                ;;
            esac
        fi
    elif [ "$database" = "postgres" ]; then
        if [ "$os" = "ubuntu-latest" ]; then
            case "$migratingVersion" in
            "5.9.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_5_9_MIGRATION"
                ;;
            "5.10.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_5_10_MIGRATION"
                ;;
            "5.11.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_5_11_MIGRATION"
                ;;
            "6.0.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_6_0_MIGRATION"
                ;;
            "6.1.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_6_1_MIGRATION"
                ;;
            "6.2.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_UBUNTU_IS_6_2_MIGRATION"
                ;;
            esac
        elif [ "$os" = "macos-latest" ]; then
            case "$migratingVersion" in
            "5.9.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_5_9_MIGRATION"
                ;;
            "5.10.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_5_10_MIGRATION"
                ;;
            "5.11.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_5_11_MIGRATION"
                ;;
            "6.0.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_6_0_MIGRATION"
                ;;
            "6.1.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_6_1_MIGRATION"
                ;;
            "6.2.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_6_2_MIGRATION"
                ;;
            esac
        fi
    elif [ "$database" = "mssql" ]; then
        if [ "$os" = "ubuntu-latest" ]; then
            case "$migratingVersion" in
            "5.9.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_5_9_MIGRATION"
                ;;
            "5.10.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_5_10_MIGRATION"
                ;;
            "5.11.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_5_11_MIGRATION"
                ;;
            "6.0.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_6_0_MIGRATION"
                ;;
            "6.1.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_6_1_MIGRATION"
                ;;
            "6.2.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_UBUNTU_IS_6_2_MIGRATION"
                ;;
            esac
        elif [ "$os" = "macos-latest" ]; then
            case "$migratingVersion" in
            "5.9.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_5_9_MIGRATION"
                ;;
            "5.10.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_5_10_MIGRATION"
                ;;
            "5.11.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_5_11_MIGRATION"
                ;;
            "6.0.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_6_0_MIGRATION"
                ;;
            "6.1.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_6_1_MIGRATION"
                ;;
            "6.2.0")
                deployment_automation_file="$DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_6_2_MIGRATION"
                ;;
            esac
        fi
    fi
fi

# Special config change when migrating from IS 5.9: doing migration with database type as "database" and starting migrated server as "database-unique-id"
if [ -n "$deployment_automation_file" ]; then
    if [ "$currentVersion" = "5.9.0" ]; then
        if [ "$startServer" = "migrated" ]; then
            deployment_directory=$(dirname "$deployment_automation_file")
            chmod +x "$deployment_automation_file"
            # Search for the code block in deployment_automation_file
            for file in "$deployment_automation_file"; do
                sed -i '/\[user_store\]/,/^$/ s/#type = "database"/type = "database"/' "$file"
                sed -i '/\[user_store\]/,/^$/ s/type = "database_unique_id"/#type = "database_unique_id"/' "$file"
            done
            echo "${GREEN}==> Done special config change when migrating from IS 5.9: doing migration with database type as database${RESET}"
        fi
    fi
fi

# Replace deployment file if deployment automation file exists
if [ -n "$deployment_automation_file" ]; then
    chmod +x "$deployment_automation_file"

    find "$deployment_path" -type f -name 'deployment.toml' -exec sh -c "cat '$deployment_automation_file' > '{}'" \;
    wait $!
fi

# Replace secret key in deployment.toml
if [ "$startServer" = "migrated" ]; then
    cd "$deployment_path"
    chmod +x deployment.toml
    # Generate the secret key
    secret_key=$(openssl rand -hex 32)
    wait $!
    echo "${GREEN}==> Secret key is $secret_key${RESET}"
    if [ "$currentVersion" = "5.9.0" ] || [ "$currentVersion" = "5.10.0" ] && [ "$migratingVersion" = "6.0.0" ] || [ "$migratingVersion" = "6.1.0" ] || [ "$migratingVersion" = "6.2.0" ]; then
        if [ "$os" = "ubuntu-latest" ]; then
            for file in $(find "$deployment_path" -type f -name 'deployment.toml'); do
                # Replace the placeholder with the generated secret key
                sed -i "s/<provide-your-key-here>/$secret_key/g" "$file"
                echo "${GREEN}==> Secret key generated and replaced in deployment.toml${RESET}"
                echo "Content of deployment automation file before migration:"
                cat "$file"
                echo "${GREEN}==> Did needed changes of deployment toml file to configure \"$database\" database successfully.${RESET}"
            done
        elif [ "$os" = "macos-latest" ]; then
            for file in $(find "$deployment_path" -type f -name 'deployment.toml'); do
                # Replace the placeholder with the generated secret key
                sed -i "" "s~<provide-your-key-here>~$secret_key~g" "$file"
                echo "${GREEN}==> Secret key generated and replaced in deployment.toml${RESET}"
                echo "Content of deployment automation file before migration:"
                cat "$file"
                echo "${GREEN}==> Did needed changes of deployment toml file to configure \"$database\" database successfully.${RESET}"
            done
        fi
    else
        if [ "$os" = "ubuntu-latest" ]; then
            for file in $(find "$deployment_path" -type f -name 'deployment.toml'); do
                # Comment out the lines if present
                sed -i 's/^\[encryption\]/#&/' "$file"
                sed -i 's/^key = "<provide-your-key-here>"/#&/' "$file"
                sed -i 's/^internal_crypto_provider = "org.wso2.carbon.crypto.provider.KeyStoreBasedInternalCryptoProvider"/#&/' "$file"
                echo "Content of deployment automation file before migration:"
                cat "$file"
                echo "${GREEN}==> Did needed changes of deployment toml file to configure \"$database\" database successfully.${RESET}"
            done
        elif [ "$os" = "macos-latest" ]; then
            for file in $(find "$deployment_path" -type f -name 'deployment.toml'); do
                # Comment out the lines if present
                sed -i '' 's/^\[encryption\]/#&/' "$file"
                sed -i '' 's/^key = "<provide-your-key-here>"/#&/' "$file"
                sed -i '' 's/^internal_crypto_provider = "org.wso2.carbon.crypto.provider.KeyStoreBasedInternalCryptoProvider"/#&/' "$file"
                echo "Content of deployment automation file before migration:"
                cat "$file"
                echo "${GREEN}==> Did needed changes of deployment toml file to configure \"$database\" database successfully.${RESET}"
            done
        fi
    fi
fi
