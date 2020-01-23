package org.wso2.identity.integration.test.user.mgt.uuid;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

public class JDBCUUIDUMTestCase extends UUIDUserManagerTestBase {

    @BeforeTest(alwaysRun = true)
    public void initTest() throws Exception {
        super.initTest();
    }

    @AfterTest
    public void deInit() throws Exception {
        super.deInitTest();
    }
}
