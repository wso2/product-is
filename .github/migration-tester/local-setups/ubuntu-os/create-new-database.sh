#!/bin/bash

chmod +x $Home/Downloads/Automating-Product-Migration-Testing/utils/mysql.sql
docker exec -i mysql_eight sh -c 'exec mysql -uroot -proot' < $Home/Downloads/Automating-Product-Migration-Testing/utils/mysql.sql
echo "Database created successfully!!"

chmod +x  $Home/Downloads/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/mysql.sql
docker exec -i mysql_eight sh -c 'exec mysql -uroot -proot -D '$DATABASE'' < $Home/Downloads/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/mysql.sql
docker exec -i mysql_eight sh -c 'exec mysql -uroot -proot -D '$DATABASE'' < $Home/Downloads/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/identity/mysql.sql
docker exec -i mysql_eight sh -c 'exec mysql -uroot -proot -D '$DATABASE'' < $Home/Downloads/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/identity/uma/mysql.sql
docker exec -i mysql_eight sh -c 'exec mysql -uroot -proot -D '$DATABASE'' < $Home/Downloads/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/consent/mysql.sql
docker exec -i mysql_eight sh -c 'exec mysql -uroot -proot -D '$DATABASE'' < $Home/Downloads/Automating-Product-Migration-Testing/utils/db-scripts/IS-5.11/metrics/mysql.sql
