/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.is.portal.user.client.api.unit.test;

import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.is.portal.user.client.api.micro.IdentityStoreClientMicroService;
import org.wso2.is.portal.user.client.api.micro.ProfileImageDownloaderService;
import org.wso2.msf4j.MicroservicesRunner;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

import static org.testng.AssertJUnit.assertEquals;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)

/**
 * Test Class For Forced Password Reset
 */
public class AdminForcedPasswordResetTest {
    public static final String HEADER_KEY_CONNECTION = "CONNECTION";
    public static final String HEADER_VAL_CLOSE = "CLOSE";
    private final IdentityStoreClientMicroService testMicroservice = new IdentityStoreClientMicroService();
    private final ProfileImageDownloaderService testMicroservice1 = new ProfileImageDownloaderService();
    private static final int port = 8129;
    protected static URI baseURI;
    private MicroservicesRunner microservicesRunner;

    @BeforeClass
    public void setup() throws Exception {

        microservicesRunner = new MicroservicesRunner(port);
        microservicesRunner.deploy("/DynamicPath", testMicroservice).start();

    }

    @AfterClass
    public void teardown() throws Exception {
        microservicesRunner.stop();
    }

    @Test
    public void testvalidateGenerateOTPEndPoint() throws IOException {
        HttpURLConnection urlConn = request
                ("/DynamicPath/generateOTP", HttpMethod.GET);
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());

    }

    private HttpURLConnection request(String path, String method) throws IOException {
        return request(path, method, false);
    }

    private HttpURLConnection request(String path, String method, boolean keepAlive) throws IOException {
        baseURI = URI.create(String.format("http://%s:%d", "localhost", port));
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        if (!keepAlive) {
            urlConn.setRequestProperty(HEADER_KEY_CONNECTION, HEADER_VAL_CLOSE);
        }

        return urlConn;
    }

}
