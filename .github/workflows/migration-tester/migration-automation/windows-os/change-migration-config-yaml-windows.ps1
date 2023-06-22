#!/bin/bash

cd "$MIGRATION_RESOURCES_NEW_IS_MAC"
chmod +x "$MIGRATION_CONFIG_YAML_MAC"

currentVersion="$3"
migratingVersion="$4"

for file in $(find "$MIGRATION_RESOURCES_NEW_IS_MAC" -type f -name 'migration-config.yaml');
do
    sed "s/\(.*migrationEnable:.*\)/migrationEnable: \"true\"/" "$file" > tmpfile && mv tmpfile "$file"
    sed "s/\(.*currentVersion: .*\)/currentVersion: \"$currentVersion\"/" "$file" > tmpfile && mv tmpfile "$file"
    sed "s/\(.*migrateVersion: .*\)/migrateVersion: \"$migratingVersion\"/" "$file" > tmpfile && mv tmpfile "$file"
done

# use $currentVersion and $migratingVersion to get URLs
