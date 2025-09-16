# WSO2 Identity Server 7.2.0 Residual Migration Guide

This guide provides detailed instructions for performing residual migration to WSO2 Identity Server 7.2.0 from previous versions (7.0.0 or earlier).

## Overview

Residual migration allows you to migrate data and configurations that were not migrated during the initial upgrade process. This includes:

- Organization configurations and inheritance settings
- Challenge questions and security configurations
- Custom database schemas and configurations

## Prerequisites

Before starting the migration process, ensure you have:

1. WSO2 Identity Server 7.2.0 installed and configured
2. Access to the previous version's database
3. Backup of your current deployment.toml and other configuration files
4. Required connectors and extensions installed

## Migration Configuration

### Step 1: Configure migration-config.yaml

Create or update your `migration-config.yaml` file with the following structure:

```yaml
migrationEnable: true
currentVersion: "7.2.0"
migrateVersion: "7.0.0"  # or your source version

datasource:
  name: "WSO2_IDENTITY_DB"
  
migrationSteps:
  - name: "SchemaMigrator"
    order: 1
    parameters:
      enable: "true"
      
  - name: "IdentityDataMigrator" 
    order: 2
    parameters:
      enable: "true"
      
  - name: "UMDataMigrator"
    order: 3
    parameters:
      enable: "true"
      
  - name: "SchemaMigrator"
    order: 4
    parameters:
      enable: "true"
      location: "step2"
      schema: "identity"
      
  # Organization Version Migrator for IS 7.2.0
  # This enables login and registration configuration inheritance for existing organizations
  - name: "OrganizationVersionMigrator"
    order: 5  # Use unique order number to avoid conflicts
    parameters:
      enable: "true"
    # This migrator applies to IS 7.2.0 upgrades from 7.0.0 or earlier
    applicableVersions: ["7.0.0", "7.1.0"]
    targetVersion: "7.2.0"
```

### Step 2: Enable Optional Migrators

#### OrganizationVersionMigrator Configuration

The `OrganizationVersionMigrator` is specifically designed for IS 7.2.0 to apply login and registration configuration inheritance for existing organizations.

**Important Notes:**
- This migrator only applies when upgrading **TO** IS 7.2.0 **FROM** versions 7.0.0 or 7.1.0
- Use `order: 5` to avoid conflicts with the existing SchemaMigrator (step2/identity) which uses `order: 4`
- Ensure this migrator is only enabled for the target version 7.2.0

**Configuration:**
```yaml
- name: "OrganizationVersionMigrator"
  order: 5
  parameters:
    enable: "true"
  applicableVersions: ["7.0.0", "7.1.0"]
  targetVersion: "7.2.0"
```

### Step 3: Challenge Questions Connector Setup

For challenge questions functionality, you need the WSO2 Challenge Questions Connector (version 3.0.3 or higher).

**Download and Installation:**

1. Download the latest Challenge Questions Connector from the [WSO2 Connector Store](https://store.wso2.com/store/assets/esbconnector/details/challenge-questions)
2. Alternatively, get it from the [WSO2 Extensions Repository](https://github.com/wso2-extensions/esb-connector-challengequestions)
3. For version 3.0.3+, visit: [Challenge Questions Connector Releases](https://github.com/wso2-extensions/esb-connector-challengequestions/releases)

**Installation Steps:**
1. Download the connector JAR file (e.g., `org.wso2.carbon.extension.challenge.questions-3.0.3.jar`)
2. Copy it to `<IS_HOME>/repository/components/dropins/`
3. Restart the WSO2 Identity Server

### Step 4: Deployment.toml Configuration Updates

During the migration process, you may need to temporarily adjust certain configurations in your `deployment.toml` file.

#### Temporary Migration Settings

Add the following configurations to your `deployment.toml` to optimize the migration process:

```toml
# Migration-specific database configurations
[database.identity_db.pool_options]
maxActive = "200"
maxWait = "60000"
testOnBorrow = true
validationQuery = "SELECT 1"
validationInterval = "30000"

# Disable certain features during migration to improve performance
[migration]
# Temporarily disable organization management improvements during migration
disable_organization_improvements = true

# Disable event listeners during bulk data migration
disable_event_listeners = true

# Increase timeout for long-running migration operations
operation_timeout = "300000"

# Cache configurations for migration performance
[cache.manager]
# Disable certain caches during migration to prevent memory issues
disable_authorization_cache = true
disable_authentication_cache = true

# Logging configuration for migration
[logs]
# Enable detailed migration logging
migration_log_level = "DEBUG"

# Migration batch processing
[migration.batch_processing]
# Optimize batch sizes for your environment
batch_size = "1000"
thread_pool_size = "10"
```

#### Post-Migration Configuration Cleanup

**Important:** After migration is complete, remove or comment out the migration-specific configurations:

```toml
# Remove or comment out these settings after migration completion:
# [migration]
# disable_organization_improvements = true
# disable_event_listeners = true
# operation_timeout = "300000"

# Re-enable caches
# [cache.manager]
# disable_authorization_cache = false
# disable_authentication_cache = false

# Reset logging to production levels
[logs]
migration_log_level = "INFO"
```

### Step 5: Verification and Testing

#### Pre-Migration Verification

1. **Database Connectivity:**
   ```bash
   # Test database connection
   cd <IS_HOME>/bin
   ./wso2server.sh -Dmigration.dryrun=true
   ```

2. **Configuration Validation:**
   ```bash
   # Validate migration configuration
   ./migration-client.sh -validateConfig
   ```

#### Post-Migration Verification

1. **Organization Settings:**
   - Login to the Management Console
   - Navigate to Organizations → Settings
   - Verify that login and registration configurations are inherited properly

2. **Challenge Questions:**
   - Test user registration with challenge questions
   - Verify that existing challenge questions are preserved
   - Test password recovery using challenge questions

3. **Database Integrity:**
   ```sql
   -- Verify organization data migration
   SELECT COUNT(*) FROM UM_ORG WHERE VERSION='7.2.0';
   
   -- Check challenge questions data
   SELECT COUNT(*) FROM IDN_CHALLENGE_QUESTION;
   ```

## Troubleshooting

### Common Issues and Solutions

#### Issue 1: OrganizationVersionMigrator Order Conflict
**Error:** `Duplicate order number 4 in migration configuration`

**Solution:** 
Ensure OrganizationVersionMigrator uses `order: 5` instead of `order: 4`:
```yaml
- name: "OrganizationVersionMigrator"
  order: 5  # Not 4
  parameters:
    enable: "true"
```

#### Issue 2: Challenge Questions Connector Not Found
**Error:** `Challenge Questions Connector not loaded`

**Solution:**
1. Verify connector is in `<IS_HOME>/repository/components/dropins/`
2. Check version compatibility (requires 3.0.3+)
3. Restart the server after copying the connector

#### Issue 3: Migration Timeout
**Error:** `Migration operation timed out`

**Solution:**
Increase timeout in deployment.toml:
```toml
[migration]
operation_timeout = "600000"  # 10 minutes
```

#### Issue 4: Memory Issues During Migration
**Error:** `OutOfMemoryError during migration`

**Solution:**
1. Increase JVM heap size:
   ```bash
   export JAVA_OPTS="-Xms2048m -Xmx4096m"
   ```
2. Reduce batch size in migration config
3. Temporarily disable caches as shown in deployment.toml section

## Migration Validation Checklist

- [ ] Database backup completed
- [ ] Migration configuration file validated
- [ ] Required connectors downloaded and installed
- [ ] Deployment.toml updated with migration settings
- [ ] Migration dry-run executed successfully
- [ ] Actual migration completed without errors
- [ ] Organization inheritance settings verified
- [ ] Challenge questions functionality tested
- [ ] Post-migration configuration cleanup completed
- [ ] Application functionality verified
- [ ] Performance testing completed

## Additional Resources

- [WSO2 Identity Server 7.2.0 Documentation](https://is.docs.wso2.com/en/7.2.0/)
- [Migration Client Documentation](https://github.com/wso2-extensions/identity-migration-resources)
- [Challenge Questions Connector](https://github.com/wso2-extensions/esb-connector-challengequestions)
- [WSO2 Connector Store](https://store.wso2.com/store/)

## Support

For additional support and troubleshooting:
- [WSO2 Identity Server Community](https://wso2.org/community/)
- [WSO2 Support Portal](https://support.wso2.com/) (for enterprise customers)
- [GitHub Issues](https://github.com/wso2/product-is/issues)