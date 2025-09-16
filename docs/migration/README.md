# WSO2 Identity Server Migration Documentation

This directory contains comprehensive migration documentation and configuration templates for WSO2 Identity Server upgrades.

## Directory Structure

```
docs/migration/
├── README.md                          # This file
└── is-7.2.0/                         # IS 7.2.0 specific migration resources
    ├── migration-execution.md         # Detailed migration guide
    ├── sample-migration-config.yaml   # Sample migration configuration
    └── migration-deployment-config.toml # Deployment configuration template
```

## Quick Start

For IS 7.2.0 migration, refer to the comprehensive guide:
- **[IS 7.2.0 Migration Guide](is-7.2.0/migration-execution.md)** - Complete step-by-step migration instructions

## Key Improvements Addressed

This documentation addresses several critical issues identified in the residual migration process:

### 1. OrganizationVersionMigrator Configuration
- ✅ **Fixed**: Unique order number (5) to avoid conflicts with SchemaMigrator
- ✅ **Fixed**: Clear version specification for IS 7.2.0 applicability
- ✅ **Improved**: Detailed explanation of when and how to use this migrator

### 2. Challenge Questions Connector References
- ✅ **Added**: Direct links to WSO2 Connector Store and GitHub repositories
- ✅ **Added**: Version compatibility information (3.0.3+)
- ✅ **Added**: Step-by-step installation instructions

### 3. Deployment.toml Configurations
- ✅ **Replaced**: TODO sections with actual tested configurations
- ✅ **Added**: Complete deployment.toml template for migration
- ✅ **Added**: Post-migration cleanup instructions
- ✅ **Added**: Performance optimization settings

## Files Overview

### migration-execution.md
The main migration guide containing:
- Complete step-by-step migration process
- Correct OrganizationVersionMigrator configuration (order: 5)
- Challenge Questions Connector setup with proper links
- Deployment.toml configurations (replacing TODO sections)
- Troubleshooting guide
- Verification checklist

### sample-migration-config.yaml
A complete sample migration configuration file featuring:
- Correct order numbers for all migrators
- Version-specific configurations
- Required connectors and their download links
- Performance optimization settings

### migration-deployment-config.toml
A comprehensive deployment.toml template including:
- Migration-specific database configurations
- Cache management during migration
- Logging configurations for troubleshooting
- Post-migration cleanup instructions

## Usage Instructions

1. **Before Migration:**
   - Review the [migration-execution.md](is-7.2.0/migration-execution.md) guide
   - Copy and customize [sample-migration-config.yaml](is-7.2.0/sample-migration-config.yaml)
   - Update your deployment.toml with settings from [migration-deployment-config.toml](is-7.2.0/migration-deployment-config.toml)

2. **During Migration:**
   - Follow the step-by-step process in the migration guide
   - Monitor logs for any issues
   - Use the troubleshooting section if problems arise

3. **After Migration:**
   - Complete the verification checklist
   - Perform post-migration cleanup as documented
   - Test all functionality thoroughly

## Common Issues Resolved

| Issue | Previous Problem | Solution Provided |
|-------|-----------------|-------------------|
| OrganizationVersionMigrator Order | Duplicate order: 4 causing conflicts | Use unique order: 5 |
| Challenge Questions Connector | No clear download links | Direct links to GitHub and WSO2 Store |
| Deployment.toml TODO | Placeholder configurations | Complete tested configurations |
| Version Confusion | Unclear which IS version applies | Explicit version specifications |
| Performance Issues | No optimization guidance | Comprehensive performance tuning |

## Version Support

This documentation supports migration to:
- **WSO2 Identity Server 7.2.0** from versions:
  - 7.0.0
  - 7.1.0

For other version migrations, please refer to the appropriate documentation in the WSO2 Identity Server documentation portal.

## Additional Resources

- [WSO2 Identity Server Documentation](https://is.docs.wso2.com/)
- [Identity Migration Resources](https://github.com/wso2-extensions/identity-migration-resources)
- [WSO2 Connector Store](https://store.wso2.com/store/)
- [Product IS GitHub Repository](https://github.com/wso2/product-is)

## Contributing

To contribute to this documentation:
1. Identify gaps or issues in the migration process
2. Create clear, tested documentation improvements
3. Ensure all configurations are validated
4. Submit pull requests with comprehensive descriptions

## Support

For migration support:
- Community: [WSO2 Identity Server Community](https://wso2.org/community/)
- Enterprise: [WSO2 Support Portal](https://support.wso2.com/)
- Issues: [GitHub Issues](https://github.com/wso2/product-is/issues)