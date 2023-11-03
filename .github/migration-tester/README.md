
#  Automating-Product-Migration-Testing

Welcome to Automating-Product-Migration-Testing! This project aims to automate the testing process of the migration client for WSO2 Identity Server when migrating between different product versions. By automating the migration client test execution, we can significantly reduce the overhead and effort involved in manual testing.

## Workflow Status

[![Workflow Status](https://github.com/JayanaGunaweera01/Automating-Product-Migration-Testing/actions/workflows/.github/workflows/MainMigrationWorkflow.yml/badge.svg)](https://github.com/JayanaGunaweera01/Automating-Product-Migration-Testing/actions)

Click on the badge above to view the current status of the main migration workflow. The workflow ensures seamless execution of the migration client tests across various infrastructure combinations, including different databases and operating systems.

## Notion User Guide 
https://www.notion.so/User-Guide-Of-Product-Migration-Testing-Automation-Feature-17676cd5205a49a898d6527a03347199?pvs=4

## Directory Structure

This repository contains automation scripts and tools for automating product migration testing.

```
.
├── product-is
│   ├── .github
│   │   └── workflows
│   │       └── migration-automation.yml
│   ├── data-population-and-validation
│   │   ├── 1-user-creation
│   │   │   │   ├── create-bulk-users.sh
│   │   │   │   ├── create-user.sh
│   │   ├── 2-tenant-creation
│   │   │   │   ├── create-tenant-soapAPI.sh
│   │   │   │   ├── create-tenant.sh
│   │   ├── 3-userstore-creation
│   │   │   │   ├── create-user-in-userstore.sh
│   │   │   │   ├── create-userstore-soapAPI.sh
│   │   │   │   └── create-userstore.sh
│   │   ├── 4-service-provider-creation
│   │   │   │   ├── create-user-in-a-service-provider.sh
│   │   │   │   ├── register-a-service-provider-get-access-token-mac.sh
│   │   │   │   └── register-a-service-provider-get-access-token-ubuntu.sh
│   │   │   │   ├── register-a-service-provider.sh
│   │   │   │   ├── validate-database-mac.sh
│   │   │   │   └── validate-database-ubuntu.sh
│   │   ├── 5-group-creation
│   │   │   │   ├── create-group.sh
│   │   │   │   ├── create-groups-with-users.sh
│   │   └── automated-data-poputation-and-validation-script-mac.sh
│   │   └── automated-data-poputation-and-validation-script-ubuntu.sh
│   ├── documents
│   │   └── Automating Product Migration Testing.word
│   ├── local-setups
│   │   ├── mac-os
│   │   │   ├── migration-automation-script-macos.sh
│   │   ├── ubuntu-os
│   │   │   │   ├──automated-data-population-and-validation-script-ubuntu-local-setup.sh
│   │   │   │   ├──automating-product-migration-testing.sh
│   │   │   │   └── backup_db.sql
│   │   │   │   ├──change-deployment-toml.sh
│   │   │   │   ├── change-migration-configyaml.sh
│   │   │   │   └── copy-jar-file-mysql.sh
│   │   │   │   ├──create-new-database.sh
│   │   │   │   ├── deployment.toml
│   │   │   │   └── enter-login-credentials.sh
│   │   │   │   ├── env.sh
│   │   │   │   ├── humanoid.jpg
│   │   │   │   └── migration-automation-script-linux.sh
│   │   │   │   ├── migration-terminal.sh
│   │   │   │   ├── migration.log
│   │   │   │   └── server-start-newIS.sh
│   │   │   │   ├── server-start.sh
│   │   │   │   ├── validate-database-ubuntu-local-setup.sh
│   ├── migration-automation
│   │   ├── deployment-tomls
│   │   │   ├── IS-5.9
│   │   │   │   ├── deployment-mssql.toml
│   │   │   │   ├── deployment-mysql.toml
│   │   │   │   └── deployment-postgre.toml
│   │   │   │   ├── deployment-mssql-migration.toml
│   │   │   │   ├── deployment-mysql-migration.toml
│   │   │   │   └── deployment-postgre-migration.toml
│   │   │   ├── IS-5.10
│   │   │   │   ├── deployment-mssql.toml
│   │   │   │   ├── deployment-mysql.toml
│   │   │   │   └── deployment-postgre.toml
│   │   │   │   ├── deployment-mssql-migration.toml
│   │   │   │   ├── deployment-mysql-migration.toml
│   │   │   │   └── deployment-postgre-migration.toml
│   │   │   ├── IS-5.11
│   │   │   │   ├── deployment-mssql.toml
│   │   │   │   ├── deployment-mysql.toml
│   │   │   │   └── deployment-postgre.toml
│   │   │   │   ├── deployment-mssql-migration.toml
│   │   │   │   ├── deployment-mysql-migration.toml
│   │   │   │   └── deployment-postgre-migration.toml
│   │   │   ├── IS-6.0
│   │   │   │   ├── deployment-mssql.toml
│   │   │   │   ├── deployment-mysql.toml
│   │   │   │   └── deployment-postgre.toml
│   │   │   │   ├── deployment-mssql-migration.toml
│   │   │   │   ├── deployment-mysql-migration.toml
│   │   │   │   └── deployment-postgre-migration.toml
│   │   │   ├── IS-6.1
│   │   │   │   ├── deployment-mssql.toml
│   │   │   │   ├── deployment-mysql.toml
│   │   │   │   └── deployment-postgre.toml
│   │   │   │   ├── deployment-mssql-migration.toml
│   │   │   │   ├── deployment-mysql-migration.toml
│   │   │   │   └── deployment-postgre-migration.toml
│   │   │   └── IS-6.2
│   │   │       ├── deployment-mssql.toml
│   │   │       ├── deployment-mysql.toml
│   │   │       └── deployment-postgre.toml
│   │   │       ├── deployment-mssql-migration.toml
│   │   │       ├── deployment-mysql-migration.toml
│   │   │       └── deployment-postgre-migration.toml
│   │   ├── mac-os
│   │   │   ├── migration-script-mac.sh
│   │   │   └── setup-mysql-mac.sh
│   │   ├── ubuntu-os
│   │   │   ├── migration-script-ubuntu.sh
│   │   │   └── setup-mysql-ubuntu.sh
│   │   ├── enter-login-credentials.sh
│   │   ├── env.sh
│   │   ├── logs.txt
│   │   ├── change-deployment-toml.sh
│   │   └── change-migration-config-yaml.sh
│   │   └──download-migration-client.sh
│   │   └──update-pack.sh
│   │   ├── copy-jar-file.sh
│   │   └── start-server.sh
│   │   └── stop-server.sh
│   ├── utils
│   |   ├── db-scripts
│   │   |    ├── IS-5.11
│   │   │    ├── Bps
│   │   │    ├── consent
│   │   │    │   ├── mysql.sql
│   │   │    │   ├── mssql.sql
│   │   │    │   ├── postgresfour.sql
│   │   │    ├── identity
│   │   │    │   ├── uma
│   │   │    │   │   ├── mysql.sql
│   │   │    │   │   ├── mssql.sql
│   │   │    │   │   ├── postgresthree.sql   
│   │   │    │   ├── mysql.sql
│   │   │    │   ├── mssql.sql
│   │   │    │   ├── postgrestwo.sql
│   │   │    ├── metrics
│   │   │    │   ├── mysql.sql
│   │   │    │   ├── mssql.sql
│   │   │    │   ├── postgresfive.sql
│   │   │    ├── mssql.sql
│   │   │    ├── mysql.sql
│   │   │    ├── postgresone.sql
│   │   |    ├── IS-5.9
│   │   │    ├── Bps
│   │   │    ├── consent
│   │   │    │   ├── mysql.sql
│   │   │    │   ├── mssql.sql
│   │   │    │   ├── postgresfour.sql
│   │   │    ├── identity
│   │   │    │   ├── uma
│   │   │    │   │   ├── mysql.sql
│   │   │    │   │   ├── mssql.sql
│   │   │    │   │   ├── postgresthree.sql   
│   │   │    │   ├── mysql.sql
│   │   │    │   ├── mssql.sql
│   │   │    │   ├── postgrestwo.sql
│   │   │    ├── metrics
│   │   │    │   ├── mysql.sql
│   │   │    │   ├── mssql.sql
│   │   │    │   ├── postgresfive.sql
│   │   │    ├── mssql.sql
│   │   │    ├── mysql.sql
│   │   │    ├── postgresone.sql
│   │   ├── database-create-scripts
│   │   │   ├── mysql.sql
│   │   │   ├── mssql.sql
│   │   │   ├── postgressql.sql
│   |   ├── jars
│   │   |   ├── mssql
│   │   │   |   ├── mssql-jdbc-12.2.0.jre11.jar
│   │   │   |   ├── mssql-jdbc-12.2.0.jre8.jar
│   │   │   |   └── mssql-jdbc-9.2.0.jre8.jar
│   │   |   ├── mysql
│   │   │   |   └── mysql-connector-java-8.0.29.jar
│   │   |   └── postgresql
│   │   |       └── postgresql-42.5.3.jar
│   |   ├── update-tools
│   │   |   ├── wso2update_darwin
│   │   |   ├── wso2update_linux
│   │   |   ├── wso2update_windows.exe
│   |   ├── migration-client
│   |   
│   └── other-db-scripts
│       └── config-management-is-5-11.sql
└── README.md

```

- `.github/workflows`:
  - Contains the workflow file `MainMigrationWorkflow.yml`, which defines the main migration workflow for the repository.

- `data-population-and-validation`:
  - Contains subdirectories for different operating systems: `mac-os`, `ubuntu-os`, and `windows-os`.
  - Each OS directory includes scripts for data population and validation, such as user creation, tenant creation, user store creation, service provider creation, and group creation.
  - Additionally, the directory includes a common script named `data-population-script.sh` for data population.

- `documents`:
  - Contains the document file `Automating Product Migration Testing.word`, which likely provides documentation or instructions related to automating product migration testing.

- `local-setups`:
  - Contains subdirectories for different operating systems: `mac-os`, `ubuntu-os`, and `windows-os`.
  - Each OS directory includes setup scripts specific to that operating system, such as changing deployment toml files, migration configuration YAML files, copying jar files, migration scripts, and MySQL setup scripts.

- `migration-automation`:
  - Contains subdirectories for different operating systems: `mac-os`, `ubuntu-os`, and `windows-os`.
  - Each OS directory includes scripts specific to that operating system for migration automation, such as changing deployment toml files, changing migration configuration YAML files, copying jar files, migration scripts, and MySQL setup scripts.
  - The `deployment-tomls` directory includes subdirectories for different versions of the migration target (e.g., IS-5.10, IS-5.11) and respective deployment toml files for MSSQL, MySQL, and Postgre databases.

 `utils`:
 - Contains utility scripts and helper tools.
- `db-scripts`:
  - Contains subdirectories for different versions of the migration target (e.g., IS-5.11, IS-5.9) and respective subdirectories for database create scripts.
  - Additionally, the directory includes deployment toml files for MSSQL databases.

- `jars`:
  - Contains subdirectories for different database types: `mssql`, `mysql`, and `postgresql`.
  - The `mssql` directory includes `mssql-jdbc-12.2.0.jre11.jar` file for MSSQL database connectivity.
  - The `mysql` directory includes the `mysql-connector-java-8.0.29.jar` file for MySQL database connectivity.
  - The `postgresql` directory includes the `postgresql-42.5.3.jar` file for PostgreSQL database connectivity.

- `migration-client`:
  - Contains the `wso2is-migration-1.0.225.zip` file, which represents a migration client for performing specific migration tasks.

- `LICENSE`:
  - Represents the license file (`LICENSE`) for the repository, which is Apache License 2.0.

- `README.md`:
  - Represents the readme file (`README.md`) for the repository, which provides information about the project, its purpose, and instructions for usage or contribution.

Feel free to explore each directory to find more details about the specific components and scripts.

## Getting Started

To get started with the migration process, follow the steps below:

### 1. Configure Environment Variables

- Open the `.env` file located in the root directory of the project.

- Update the necessary environment variables based on your specific setup and requirements.

### 2. Execute Migration Automation Scripts

- Obtain the URLs of the WSO2 Identity Server releases from the [WSO2 Identity Server Releases](https://github.com/wso2/product-is/releases) page.

- Navigate to the [MainMigrationWorkflow](https://github.com/JayanaGunaweera01/Automating-Product-Migration-Testing/actions/workflows/MainMigrationWorkflow.yml) workflow on GitHub.

- Execute the main migration workflow by providing your inputs and following the on-screen prompts.

- Once the workflow starts, the migration automation scripts will be triggered to automate the migration process.

### 3. Test Migration and Evaluate Artifacts

- After the migration process completes, you will receive the necessary artifacts.

- Use these artifacts to test the migrated setup and validate the migration.



## Technologies and Tools Used

This project utilizes the following technologies and tools:

- **wso2 Identity Server Versions**:
  - 5.9.0
  - 5.10.0
  - 5.11.0
  - 6.0.0
  - 6.1.0
  - 6.2.0

- **wso2 Migration Client**:
  - Version: wso2is-migration-1.0.225.zip

- **wso2 REST APIs**

- **wso2 SOAP APIs** 

- **Google Drive APIs** - API V3

- **Bash Scripting**

- **Docker**

- **Git**

- **Github Actions**

- **Curl**

- **Powershell**

- **Home Brew**

- **Java 11 Temurin**

- **Dbeaver**

- **Meld**

- **VSCode**

- **SoapUI 5.7.0.desktop**

- **Keystore Explorer**

- **Postman**

- **MySQL Version**:
  - 8
  - JAR: mysql-connector-java-8.0.29.jar

- **MSSQL Version**:
  - 12
  - JAR: mssql-jdbc-12.2.0.jre11.jar

- **PostgreSQL Version**:
  - 42
  - JAR: postgresql-42.5.3.jar

Feel free to explore the repository and leverage these technologies and tools for the project.














