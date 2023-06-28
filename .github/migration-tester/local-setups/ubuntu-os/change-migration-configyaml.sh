#!/bin/bash

cd "$MIGRATION_RESOURCES_NEW_IS"

for file in $(find "$MIGRATION_RESOURCES_NEW_IS" -type f -name 'migration-config.yaml');
		
do
#sudo
chmod +x "$MIGRATION_CONFIG_YAML"

sed -i 's/\(.*migrationEnable:.*\)/migrationEnable: "true"/' migration-config.yaml

sed -i 's/\(.*currentVersion: .*\)/currentVersion: "5.11.0"/' migration-config.yaml

sed -i 's/\(.*migrateVersion: .*\)/migrateVersion: "6.0.0"/' migration-config.yaml

#sed '1 c\
#> migrationEnable: "true"' migration-config.yaml

#sed '4 c\
#> currentVersion: "5.11.0"' migration-config.yaml

#sed '6 c\
#> migrateVersion: "6.0.0"' migration-config.yaml

done
