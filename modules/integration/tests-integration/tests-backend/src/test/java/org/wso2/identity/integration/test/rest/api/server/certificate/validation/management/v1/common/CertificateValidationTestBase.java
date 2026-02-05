/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.certificate.validation.management.v1.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.RestAssured;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;

public class CertificateValidationTestBase extends RESTAPIServerTestBase {

    private static final String API_DEFINITION_NAME = "certificate-validation-management.yaml";
    protected static final String API_VERSION = "v1";
    protected static final String CERTIFICATE_VALIDATION_API_BASE_PATH = "/certificate-validation";
    protected static final String TEST_CERTIFICATE = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNMRENDQWRLZ0F3SUJBZ0lCQURBS0JnZ3Foa2pPUFFRREFqQjlNUXN3Q1FZRFZRUUdFd0pDUlRFUE1BMEcKQTFVRUNoTUdSMjUxVkV4VE1TVXdJd1lEVlFRTEV4eEhiblZVVEZNZ1kyVnlkR2xtYVdOaGRHVWdZWFYwYUc5eQphWFI1TVE4d0RRWURWUVFJRXdaTVpYVjJaVzR4SlRBakJnTlZCQU1USEVkdWRWUk1VeUJqWlhKMGFXWnBZMkYwClpTQmhkWFJvYjNKcGRIa3dIaGNOTVRFd05USXpNakF6T0RJeFdoY05NVEl4TWpJeU1EYzBNVFV4V2pCOU1Rc3cKQ1FZRFZRUUdFd0pDUlRFUE1BMEdBMVVFQ2hNR1IyNTFWRXhUTVNVd0l3WURWUVFMRXh4SGJuVlVURk1nWTJWeQpkR2xtYVdOaGRHVWdZWFYwYUc5eWFYUjVNUTh3RFFZRFZRUUlFd1pNWlhWMlpXNHhKVEFqQmdOVkJBTVRIRWR1CmRWUk1VeUJqWlhKMGFXWnBZMkYwWlNCaGRYUm9iM0pwZEhrd1dUQVRCZ2NxaGtqT1BRSUJCZ2dxaGtqT1BRTUIKQndOQ0FBUlMySTBqaXVObjE0WTJzU0FMQ1gzSXlicWlJSlV2eFVwaitvTmZ6bmd2ai9OaXl2MjM5NEJXblc0WAp1UTRSVEVpeXdLODdXUmNXTUdnSkI1a1gvdDJubzBNd1FUQVBCZ05WSFJNQkFmOEVCVEFEQVFIL01BOEdBMVVkCkR3RUIvd1FGQXdNSEJnQXdIUVlEVlIwT0JCWUVGUEMwZ2Y2WUVyKzFLTGxrUUFQTHpCOW1UaWdETUFvR0NDcUcKU000OUJBTUNBMGdBTUVVQ0lER3V3RDFLUHlHK2hSZjg4TWV5TVFjcU9GWkQwVGJWbGVGK1VzQUdRNGVuQWlFQQpsNHdPdUR3S1FhK3VwYzhHZnRYRTJDLy80bUtBTkJDNkl0MDFnVWFUSXBvPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0t";
    protected static final String TEST_SERIAL_NUMBER = "0";
    protected static final String TEST_ISSUER_DN = "cn=gnutlscertificateauthority,st=leuven,ou=gnutlscertificateauthority,o=gnutls,c=be";
    protected static final String TEST_INVALID_CERTIFICATE = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURRakNDQWdvZ0F3SUJBZ0lJWVZBbjV1cHlvZ0F3RFFZSktvWklodmNOQVFFTEJRQXdHakVTTUJBR0ExVUUKQ2hNSFZtbHVaWEowYVc5dUlFTkJNQjRYRFRJek1ETXhOakUwTWpFMU5Wb1hEVE16TURNeE1UQXhNakUxTlZvdwpHakVTTUJBR0ExVUVBd3dHVm1sdVpYSjBhVzl1SUVObGNuUnBabWxqWVhScGIyNXpNUmN3RlFZRFZRUUxFd0pCCmJXOTFiblFnUTBFd0hoY05NakF3T0RJMk1USTNNalV4V2hjTk1qQXdPREkyTVRJek1qVXhXakJTTUJBR0ExVUUKQ2hNSFZtbHVaWEowYVc5dUlFTkJNQjRYRFRJek1ETXhOakUwTWpFMU5Wb1hEVE16TURNeE1UQXhNakUxTlZvdwpNUXN3Q1FZRFZRUUdFd0pDU1RFUE1BMEdBMVVFQ2hNSFZtbHVaWEowYVc5dUlFTkJNQjRYRFRJek1ETXhOakUwCk1qRTFOVm93R0pFUk1BOEdBMVVkRXdFQi93UUZNQU1CQWY4d0RRWUpLb1pJaHZjTkFRRUxCUUFEZ2dFQkFJRHUKOGdTYzY3TW9PNUhHRVUyUUJ2b3gycWdxQ3doeTh5emNaYXpTMGZqY2dKbFRhVkxyb3JvVUdZQ2N4SlFoa0prVQpsZ05PTlMrRzUwM2I3M2NrcmhSZUJzZzZ4Q0lnL2FQUGFHa3Z2U1puaUZYd2tMcnpPbGpISmF3V0o5alZkbCtjCnRrcmM3ejVYaExwbnE1UjhMY3F1blZzazlzVjd2VG82cW1CVWhTSXhoTktsRjVPRVlaVEZLbHllbFNhTUVtQmQKRXphUFRka0VKeThJVGxWNklUZ2xDYmUrM1hxajFRPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0t";
    protected static final String TEST_INVALID_CERTIFICATE_ID = "invalid_certificate_id";

    protected static String swaggerDefinition;

    static {
        String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.certificate.validation.management.v1";
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    @BeforeClass(alwaysRun = true)
    @Override
    public void init() throws Exception {

        super.init();
        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    /**
     * To convert object to a json string.
     *
     * @param object Respective java object.
     * @return Relevant json string.
     */
    protected String toJSONString(Object object) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }
}
