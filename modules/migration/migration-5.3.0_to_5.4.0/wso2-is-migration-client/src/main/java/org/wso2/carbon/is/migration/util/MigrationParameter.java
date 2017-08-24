package org.wso2.carbon.is.migration.util;

/**
 * Runtime Migration Parameter Holder.
 *
 */
public class MigrationParameter {

    private static MigrationParameter  migrationParameter = null;

    private boolean isMigrateIdentitySchema ;
    private boolean isMigrateIdentityData ;
    private boolean isMigrateUMSchema ;
    private boolean isMigrateUMData ;

    private boolean isContinueOnError ;
    private boolean isBatchExecution ;

    private MigrationParameter() {
        //Read all the runtime parameter and parse to the boolean value.
        isMigrateIdentitySchema = getSystemPropertyValue(Constants.MigrationParameterConstants.MIGRATE_IDENTITY_SCHEMA);
        isMigrateIdentityData = getSystemPropertyValue(Constants.MigrationParameterConstants.MIGRATE_IDENTITY_DATA);
        isMigrateUMSchema = getSystemPropertyValue(Constants.MigrationParameterConstants.MIGRATE_UM_SCHEMA);
        isMigrateUMData = getSystemPropertyValue(Constants.MigrationParameterConstants.MIGRATE_UM_DATA);

        isContinueOnError = getSystemPropertyValue(Constants.MigrationParameterConstants.MIGRATION_CONTINUE_ON_ERROR);
        isBatchExecution = getSystemPropertyValue(Constants.MigrationParameterConstants.MIGRATION_BATCH_EXECUTION);
    }

    public static MigrationParameter getInstance(){
        if(MigrationParameter.migrationParameter == null){
            MigrationParameter.migrationParameter = new MigrationParameter();
        }
        return MigrationParameter.migrationParameter;
    }

    private boolean getSystemPropertyValue(String migrationParameterName){
        return Boolean.parseBoolean(System.getProperty(migrationParameterName));
    }

    public boolean isMigrateIdentitySchema() {
        return isMigrateIdentitySchema;
    }

    public boolean isMigrateIdentityData() {
        return isMigrateIdentityData;
    }

    public boolean isMigrateUMSchema() {
        return isMigrateUMSchema;
    }

    public boolean isMigrateUMData() {
        return isMigrateUMData;
    }

    public boolean isContinueOnError() {
        return isContinueOnError;
    }

    public boolean isBatchExecution() {
        return isBatchExecution;
    }
}
