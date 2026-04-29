# ✅ FIXED: External Server Testing Now Works

The issue was that the `automation.config.file` system property was **NOT being passed to the test framework** by Maven's surefire plugin.

## 🔧 What Was Fixed

### Problem
- The `-Dautomation.config.file=automation-external.xml` parameter was being passed to Maven but NOT to the test framework
- The pom.xml surefire configuration didn't include this property in `<systemProperties>`
- Result: Framework always used default `automation.xml` which tries to auto-start the server

### Solution
1. **Updated pom.xml** - Added `automation.config.file` system property to surefire plugin in BOTH profiles:
   - `integration` profile (default)
   - `testgrid` profile
   - Property defaults to `automation.xml` for backward compatibility
   - Can be overridden at runtime

2. **Created NoOpServerExtension** - New server extension that disables lifecycle management

3. **Updated automation-external.xml** - Uses `NoOpServerExtension` instead of `IdentityServerExtension`

## 🚀 How to Run Tests Against External IS

### Step 1: Ensure External IS is Running
```bash
# On your external machine or locally on different port
cd /path/to/wso2is-7.3.0-rc1/bin
./wso2server.sh
# Or on custom port:
./wso2server.sh -Dcom.sun.jndi.ldap.connect.pool=false -Dcom.sun.jndi.ldap.connect.timeout=5000
```

### Step 2: Run Tests with External Configuration

#### Option A: External IS on Default Port (localhost:9853)
```bash
cd /Users/hasanthi/Applications/Source/Product_IS/product-is
mvn -pl modules/integration/tests-integration/tests-backend test \
    -Dautomation.config.file=automation-external.xml \
    -Dintegration.test.is.host=localhost \
    -Dintegration.test.is.https.port=9853
```

#### Option B: External IS on Different Host/Port
```bash
mvn -pl modules/integration/tests-integration/tests-backend test \
    -Dautomation.config.file=automation-external.xml \
    -Dintegration.test.is.host=192.168.1.100 \
    -Dintegration.test.is.https.port=9445 \
    -Dintegration.test.sample.host=192.168.1.100 \
    -Dintegration.test.sample.http.port=8490
```

#### Option C: Run Single Test Suite
```bash
mvn -pl modules/integration/tests-integration/tests-backend test \
    -Dautomation.config.file=automation-external.xml \
    -Dtest=SAMLSSOTestCase \
    -Dintegration.test.is.host=external-is.example.com \
    -Dintegration.test.is.https.port=9853
```

## 🎯 Key Points

### ✅ DO THIS
```bash
mvn test -Dautomation.config.file=automation-external.xml ...
```
- ✅ `mvn test` - Runs tests only
- ✅ `mvn clean test` - Clean + run tests
- ✅ `-Dautomation.config.file=automation-external.xml` - Use external config

### ❌ DON'T DO THIS
```bash
mvn clean install -Dautomation.config.file=automation-external.xml
```
- ❌ `mvn clean install` - Does NOT run tests (install goal doesn't execute tests)
- ❌ Tests won't run, so the parameter is ignored

## 📋 System Properties Reference

| Parameter | Default | Purpose |
|-----------|---------|---------|
| `automation.config.file` | `automation.xml` | Which automation config to use |
| `integration.test.is.host` | `localhost` | External IS hostname |
| `integration.test.is.https.port` | `9853` | External IS HTTPS port |
| `integration.test.sample.host` | `localhost` | Sample app hostname |
| `integration.test.sample.http.port` | `8490` | Sample app HTTP port |

## 📝 Complete Example

**Running all backend integration tests against external IS on 192.168.1.10:9445**

```bash
cd /Users/hasanthi/Applications/Source/Product_IS/product-is

mvn -pl modules/integration/tests-integration/tests-backend test \
    -Dautomation.config.file=automation-external.xml \
    -Dintegration.test.is.host=192.168.1.10 \
    -Dintegration.test.is.https.port=9445 \
    -Dintegration.test.sample.host=192.168.1.10 \
    -Dintegration.test.sample.http.port=8490
```

## 🔍 How to Verify It's Working

When tests start, you should see:
```
[INFO] NoOpServerExtension: Initiated. No server lifecycle management will be performed.
[INFO] NoOpServerExtension: onExecutionStart() - No action taken. Expecting external IS to be running.
```

Not this (which means it's trying to auto-start):
```
ERROR [org.wso2.carbon.automation.extensions.servers.utils.ArchiveExtractor] - Error on archive extraction
```

## 📦 Files Modified

1. **pom.xml** (lines 76-114, 179-217)
   - Added `automation.config.file` system property to surefire
   - Applied to both `integration` and `testgrid` profiles

2. **automation-external.xml** (existing)
   - Uses `NoOpServerExtension` instead of `IdentityServerExtension`
   - Keeps `UserPopulateExtension` for user creation

3. **README-EXTERNAL-TESTING.md** (existing)
   - Usage examples and troubleshooting guide
   - Clarification on Maven goals

## 🐛 If Still Having Issues

1. **Verify automation-external.xml exists**
   ```bash
   ls -la modules/integration/tests-integration/tests-backend/src/test/resources/automation-external.xml
   ```

2. **Check IS is accessible**
   ```bash
   curl -k https://localhost:9853/carbon/ 2>&1 | grep -i "carbon\|error"
   ```

3. **Ensure using 'mvn test' goal** (not 'mvn clean install')
   ```bash
   mvn test -Dautomation.config.file=automation-external.xml ...
   ```

4. **Check pom.xml was updated correctly**
   ```bash
   grep -A 3 "automation.config.file" modules/integration/tests-integration/tests-backend/pom.xml
   ```

## 🎓 Technical Details

### How It Works

1. **Maven Surefire Plugin** passes system properties to test JVM
2. **`automation.config.file` property** tells the automation framework which config file to load
3. **automation-external.xml** specifies to use `NoOpServerExtension`
4. **NoOpServerExtension** overrides lifecycle methods to do nothing
5. **Tests run** against your pre-running IS instance

### Why This Approach

- ✅ Backward compatible - defaults to `automation.xml`
- ✅ No code changes to test files
- ✅ Runtime configuration via system properties
- ✅ Supports both internal and external testing
- ✅ Clear separation of concerns (DefaultServerExtension vs NoOpServerExtension)

## 🚀 Ready to Test!

You can now run integration tests against ANY external IS instance by:
1. Starting your IS instance
2. Running tests with `-Dautomation.config.file=automation-external.xml` parameter
3. Configuring host/port with system properties

No code changes needed! 🎉

