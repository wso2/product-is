# External Server Testing Configuration Guide

This guide explains how to run integration tests against an external (pre-running) IS instance using the `automation-external.xml` configuration.

## Overview

By default, the integration tests use the `automation.xml` configuration which:
- Automatically extracts the IS pack from `modules/distribution/target/`
- Starts the server before running tests
- Stops the server after tests complete

The `automation-external.xml` configuration is designed for testing against:
- A pre-running IS instance on a different machine
- A pre-running IS instance on a different port
- An external IS deployment that you want to keep running

## Key Modifications

### 1. **NoOpServerExtension**
- Replaces `IdentityServerExtension` 
- Does NOT manage server lifecycle (no auto-start/stop)
- Allows tests to run against any pre-running IS instance

### 2. **Configurable Endpoints**
All endpoint URLs are configurable via system properties:
- `integration.test.is.host` - IS hostname (default: `localhost`)
- `integration.test.is.https.port` - IS HTTPS port (default: `9853`)
- `integration.test.sample.host` - Sample app hostname (default: `localhost`)
- `integration.test.sample.http.port` - Sample app HTTP port (default: `8490`)
- `integration.test.host` - Global default hostname

## Prerequisites

1. **External IS Instance Running**
   ```bash
   # Example: Start IS on custom port
   cd /path/to/wso2is/bin
   ./wso2server.sh -Dcom.sun.jndi.ldap.connect.pool=false
   ```

2. **Verify Connectivity**
   ```bash
   curl -k https://localhost:9853/carbon/
   ```

## Usage Examples

### Scenario 1: Local IS on Default Port (localhost:9853)
```bash
mvn -pl modules/integration/tests-integration/tests-backend test \
    -Dautomation.config.file=automation-external.xml
```

### Scenario 2: External IS on Different Host/Port
```bash
mvn -pl modules/integration/tests-integration/tests-backend test \
    -Dautomation.config.file=automation-external.xml \
    -Dintegration.test.is.host=192.168.1.100 \
    -Dintegration.test.is.https.port=8443 \
    -Dintegration.test.sample.host=192.168.1.101 \
    -Dintegration.test.sample.http.port=8080
```

### Scenario 3: Run Single Test Suite
```bash
mvn -pl modules/integration/tests-integration/tests-backend test \
    -Dautomation.config.file=automation-external.xml \
    -Dtest=SAMLSSOTestCase \
    -Dintegration.test.is.host=external-is.example.com \
    -Dintegration.test.is.https.port=9443
```

**IMPORTANT: Use `test` goal NOT `clean install`**
- ❌ `mvn clean install` - This compiles but does NOT run tests
- ✅ `mvn test` - This compiles and runs tests
- ✅ `mvn clean test` - This cleans, compiles, and runs tests

## Configuration Details

### System Properties Precedence

The application resolves system properties in this order (highest to lowest priority):
1. Specific property (e.g., `integration.test.is.host`)
2. Global default property (e.g., `integration.test.host`)
3. Hardcoded default value

### Example Property Resolution
```
IS_HOST resolution:
  1. Check: System.getProperty("integration.test.is.host")
     ✓ If found, use this value
  
  2. If not found, check: System.getProperty("integration.test.host")
     ✓ If found, use this value
  
  3. If not found, use default: "localhost"
```

## Modified Java Constant Class

The `CommonConstants` class now includes these configurable properties:

```java
public static final String DEFAULT_HOST = getSystemProperty("integration.test.host", "localhost");
public static final String IS_HOST = getSystemProperty("integration.test.is.host", DEFAULT_HOST);
public static final int IS_DEFAULT_HTTPS_PORT = getSystemPropertyAsInt("integration.test.is.https.port", 9853);
public static final String IS_HTTPS_BASE_URL = "https://" + IS_HOST + ":" + IS_DEFAULT_HTTPS_PORT;
public static final String SAMPLE_APP_BASE_URL = "http://" + SAMPLE_APP_HOST + ":" + DEFAULT_TOMCAT_PORT;
```

## Troubleshooting

### Error: Tests not running / pom.xml system properties not recognized
**Problem**: You run `mvn clean install` and tests don't execute

**Solution**: 
- Use `mvn test` instead of `mvn clean install`
- The `install` goal only compiles; it doesn't run tests
- Always use one of these:
  ```bash
  mvn test                    # Run tests only
  mvn clean test             # Clean + run tests
  mvn clean install -DskipTests test  # Build all → Install → Run tests
  ```

### Error: Server Extension Still Attempting Auto-Start
**Problem**: Tests fail with connection refused errors
```
java.net.ConnectException: Connection refused
```

**Solution**: 
1. Verify external IS is running on configured host/port
2. Check firewall allows connection to IS port
3. Verify ports are not changed in `-D` parameters

### Error: Server Extension Still Attempting Auto-Start
**Problem**: Tests still try to extract pack from `modules/distribution/target/`

**Solution**:
1. Ensure using correct config file: `-Dautomation.config.file=automation-external.xml`
2. Verify the file path is relative to `src/test/resources/`
3. Confirm Maven sees the parameter by checking build output

### Error: User Population Fails
**Problem**: UserPopulateExtension fails to create test users

**Solution**:
- Option 1: Comment out `UserPopulateExtension` in `automation-external.xml` if users already exist
- Option 2: Ensure external IS admin credentials are correct (admin/admin by default)
- Option 3: Check external IS has proper user store configured

## Advanced Configuration

### Disable User Population
If your external IS already has all test users configured, disable user creation:

Edit `automation-external.xml`:
```xml
<!--
<class>
    <name>org.wso2.carbon.integration.common.extensions.usermgt.UserPopulateExtension</name>
</class>
-->
```

### Increase Deployment Timeout
If your IS takes longer to initialize, increase deployment delay in `automation-external.xml`:
```xml
<deploymentDelay>60000</deploymentDelay>  <!-- 60 seconds instead of 30 -->
```

### Custom Test User Creation
Modify `automation-external.xml` directly to specify different test users:
```xml
<user key="myuser">
    <userName>customuser</userName>
    <password>CustomPassword123</password>
</user>
```

## Integration with CI/CD

### Jenkins/GitLab CI Example
```bash
#!/bin/bash
set -e

# Start external IS in background
./start-external-is.sh &
IS_PID=$!

# Wait for IS to initialize
sleep 60

# Run tests
mvn -pl modules/integration/tests-integration/tests-backend test \
    -Dautomation.config.file=automation-external.xml \
    -Dintegration.test.is.host=${IS_HOST} \
    -Dintegration.test.is.https.port=${IS_PORT} \
    || TESTS_FAILED=1

# Cleanup
kill $IS_PID

exit ${TESTS_FAILED}
```

## Related Files

- **automation.xml** - Default configuration (auto-manages server lifecycle)
- **CommonConstants.java** - Contains all configurable endpoint constants
- **NoOpServerExtension.java** - Extension that disables server lifecycle management

## Notes

- The `automation-external.xml` keeps `UserPopulateExtension` enabled by default to create test users. This is usually safe and recommended unless users are pre-created.
- All test URLs are now configurable through system properties, allowing tests to run against any IS instance without code changes.
- The configuration maintains backward compatibility - if no system properties are set, it defaults to `localhost` and standard ports.

