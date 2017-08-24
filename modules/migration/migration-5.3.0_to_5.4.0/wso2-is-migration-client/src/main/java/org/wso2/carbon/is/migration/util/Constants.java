/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.is.migration.util;

public class Constants {



    public static final String CARBON_HOME = "carbon.home" ;



    public static final String MIGRATION_LOG = "Identity Server 5.3.0 to 5.4.0 Migration Process : ";
    public static final String MIGRATION_CONTINUE_ON_ERROR_WARN = "IS530:540 MIGRATION >> Continue even with errors "
                                                                  + "because of "
                                                                  + "continueOnError=true";

    public static final String VERSION_5_3_0 = "5.3.0";
    public static final String VERSION_5_4_0 = "5.4.0";

    public static final int SUPER_TENANT_ID = -1234;

    public class MigrationScriptConstants {

        public static final String MIGRATION_HOME = "migration-resource" ;
        public static final String MIGRATION_DB_SCRIPTS = "dbscripts" ;
        public static final String MIGRATION_DATA = "data" ;
        public static final String SQL_SCRIPT_FILE_EXTENSION = ".sql" ;

    }

    public class MigrationParameterConstants{

        public static final String MIGRATE_IDENTITY_SCHEMA = "migrateIdentitySchema";
        public static final String MIGRATE_IDENTITY_DATA = "migrateIdentityData";

        public static final String MIGRATE_UM_SCHEMA = "migrateUMSchema";
        public static final String MIGRATE_UM_DATA = "migrateUMData";

        public static final String MIGRATION_CONTINUE_ON_ERROR = "continueOnError";
        public static final String MIGRATION_BATCH_EXECUTION = "batchExecution";
    }
}
