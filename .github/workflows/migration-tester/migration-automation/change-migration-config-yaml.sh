#!/bin/bash

# Define color variables
GREEN='\033[0;32m\033[1m' # green color
RESET='\033[0m'           # reset color

# Get the value of the inputs
currentVersion="$1"
migratingVersion="$2"
os="$3"

# Setup file and path based on OS
if [ "$os" = "ubuntu-latest" ]; then
  cd "/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation"
  chmod +x env.sh
  . ./env.sh
  echo "${GREEN}==> Env file for Ubuntu sourced successfully${RESET}"
  cd "$MIGRATION_RESOURCES_NEW_IS_UBUNTU"
  chmod +x "$MIGRATION_CONFIG_YAML_UBUNTU"

  for file in $(find "$MIGRATION_RESOURCES_NEW_IS_UBUNTU" -type f -name 'migration-config.yaml'); do
    sed -i "s/\(.*migrationEnable:.*\)/migrationEnable: \"true\"/" "$file"
    sed -i "s/\(.*currentVersion: .*\)/currentVersion: \"$currentVersion\"/" "$file"
    sed -i "s/\(.*migrateVersion: .*\)/migrateVersion: \"$migratingVersion\"/" "$file"
    echo "${GREEN}==> Versions Changed.${RESET}"
  done

  # Define the search pattern for the block of text
  #if [ "$migratingVersion" = "6.0.0" ] || [ "$migratingVersion" = "6.1.0" ] || [ "$migratingVersion" = "6.2.0" ]; then
   # cd "$MIGRATION_RESOURCES_NEW_IS_UBUNTU"
   # chmod +x migration-config.yaml

   # for file in $(find "$MIGRATION_RESOURCES_NEW_IS_UBUNTU" -type f -name 'migration-config.yaml'); do
   #   search_pattern='version: "5.11.0"\n   migratorConfigs:\n   -\n     name: "EncryptionAdminFlowMigrator"\n     order: 1\n     parameters:\n       currentEncryptionAlgorithm: "RSA/ECB/OAEPwithSHA1andMGF1Padding"\n       migratedEncryptionAlgorithm: "AES/GCM/NoPadding"\n       schema: "identity"'

      # Define the replacement line
   #   replacement_line='       currentEncryptionAlgorithm: "RSA"'
   #   for file in $(find "$MIGRATION_RESOURCES_NEW_IS_UBUNTU" -type f -name 'migration-config.yaml'); do
        # Find and replace the line within the block of text
   #     sed -i "s~$search_pattern~$replacement_line~" "$file"
    #  done
   # done
  #  echo "${GREEN}==> CurrentEncryptionAlgorithm changed to \"RSA\" which is a special migration config change when migrating to versions above IS 5.11.0${RESET}"
  #fi

#if [ "$migratingVersion" = "6.0.0" ] || [ "$migratingVersion" = "6.1.0" ] || [ "$migratingVersion" = "6.2.0" ]; then
 # cd "$MIGRATION_RESOURCES_NEW_IS_UBUNTU"
#  migration_config_file="$MIGRATION_RESOURCES_NEW_IS_UBUNTU/migration-config.yaml"

 # if [ -f "$migration_config_file" ]; then
 #   # Find the line numbers of the occurrences of "UserStorePasswordMigrator"
 #   line_numbers=$(grep -n "UserStorePasswordMigrator" "$migration_config_file" | cut -d ":" -f 1)
#
#    if [ -n "$line_numbers" ]; then
#      # Loop through each line number and delete the line, as well as the lines below it until a line without any letter
#      IFS=$'\n' read -d '' -r -a line_number_array <<<"$line_numbers"
#      for line_number in "${line_number_array[@]}"; do
#        sed -i.bak "${line_number}s~^~#~" "$migration_config_file"
#        next_line=$((line_number + 1))
#        while IFS= read -r line; do
#          if [[ ! $line =~ [[:alpha:]] ]]; then
#            break
#          fi
#          sed -i.bak "${next_line}s~^~#~" "$migration_config_file"
#          next_line=$((next_line + 1))
 #       done < "$migration_config_file"
 #     done
 #     rm "$migration_config_file.bak"

#      echo "${GREEN}==> Commented all occurrences of UserStorePasswordMigrator and the lines below them until a line without any letter in the migration-config.yaml file.${RESET}"
#    else
#      echo "${RED}==> Failed to find any occurrences of UserStorePasswordMigrator in the migration-config.yaml file.${RESET}"
#    fi
#  else
#    echo "${RED}==> migration-config.yaml file not found.${RESET}"
#  fi
#fi


  # Check conditions to modify transformToSymmetric (This is a special migration config change when migrating to IS 5.11.0)
  if [ "$migratingVersion" = "5.11.0" ] || [ "$migratingVersion" = "6.0.0" ] || [ "$migratingVersion" = "6.1.0" ] || [ "$migratingVersion" = "6.2.0" ]; then
    cd "$MIGRATION_RESOURCES_NEW_IS_UBUNTU"
    chmod +x migration-config.yaml

    for file in $(find "$MIGRATION_RESOURCES_NEW_IS_UBUNTU" -type f -name 'migration-config.yaml'); do
      sed -i 's~transformToSymmetric:.*~transformToSymmetric: "true"~' "$file"
      echo "Content of migration-config-yaml file:"
      cat "migration-config.yaml"
      echo "${GREEN}==> Value of transformToSymmetric changed to true in migration-config.yaml which is a special migration config change when migrating to versions above IS 5.11.0${RESET}"
      echo "${GREEN}==> Did all the needed changes to migration-config.yaml  successfully.${RESET}"

    done

  fi
fi

if [ "$os" = "macos-latest" ]; then
  cd "/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation"
  chmod +x env.sh
  source ./env.sh
  echo "${GREEN}==> Env file for Mac sourced successfully${RESET}"
  cd "$MIGRATION_RESOURCES_NEW_IS_MAC"
  chmod +x "$MIGRATION_CONFIG_YAML_MAC"

  for file in $(find "$MIGRATION_RESOURCES_NEW_IS_MAC" -type f -name 'migration-config.yaml'); do
    sed -i "" "s/\(.*migrationEnable:.*\)/migrationEnable: \"true\"/" "$file"
    sed -i "" "s/\(.*currentVersion: .*\)/currentVersion: \"$currentVersion\"/" "$file"
    sed -i "" "s/\(.*migrateVersion: .*\)/migrateVersion: \"$migratingVersion\"/" "$file"
    echo "${GREEN}==> Versions Changed.${RESET}"
  done

  # Define the search pattern for the block of text
  if [ "$migratingVersion" = "6.0.0" ] || [ "$migratingVersion" = "6.1.0" ] || [ "$migratingVersion" = "6.2.0" ]; then
    cd "$MIGRATION_RESOURCES_NEW_IS_MAC"
    chmod +x migration-config.yaml

    for file in $(find "$MIGRATION_RESOURCES_NEW_IS_MAC" -type f -name 'migration-config.yaml'); do
      search_pattern='version: "5.11.0"\n   migratorConfigs:\n   -\n     name: "EncryptionAdminFlowMigrator"\n     order: 1\n     parameters:\n       currentEncryptionAlgorithm: "RSA/ECB/OAEPwithSHA1andMGF1Padding"\n       migratedEncryptionAlgorithm: "AES/GCM/NoPadding"\n       schema: "identity"'

      # Define the replacement line
      replacement_line='       currentEncryptionAlgorithm: "RSA"'
      for file in $(find "$MIGRATION_RESOURCES_NEW_IS_MAC" -type f -name 'migration-config.yaml'); do
        # Find and replace the line within the block of text
        sed -i "" "s~$search_pattern~$replacement_line~" "$file"
      done
    done
    echo "${GREEN}==> CurrentEncryptionAlgorithm changed to \"RSA\" which is a special migration config change when migrating to versions above IS 5.11.0${RESET}"
  fi

  # Check conditions to modify transformToSymmetric (This is a special migration config change when migrating to IS 5.11.0)
  if [ "$migratingVersion" = "5.11.0" ] || [ "$migratingVersion" = "6.0.0" ] || [ "$migratingVersion" = "6.1.0" ] || [ "$migratingVersion" = "6.2.0" ]; then
    cd "$MIGRATION_RESOURCES_NEW_IS_MAC"
    chmod +x migration-config.yaml
    for file in $(find "$MIGRATION_RESOURCES_NEW_IS_MAC" -type f -name 'migration-config.yaml'); do
      sed -i "" 's~transformToSymmetric:.*~transformToSymmetric: "true"~' "$file"
      echo "${GREEN}==> Value of transformToSymmetric changed to true in migration-config.yaml which is a special migration config change when migrating to versions above IS 5.11.0${RESET}"
      echo "Content of migration-config-yaml file:"
      cat "migration-config.yaml"
      echo "${GREEN}==> Did all the needed changes to migration-config.yaml  successfully.${RESET}"
    done

  fi
fi
