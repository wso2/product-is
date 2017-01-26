/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
<<<<<<< HEAD
<<<<<<< HEAD
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
=======
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
>>>>>>> f61b1d5... Add initial test case for profile downloader microservice
=======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
>>>>>>> b8255cb... Added license headers
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> b8255cb... Added license headers
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
<<<<<<< HEAD
 */

package org.wso2.is.portal.user.client.api.unit.test;

=======
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
=======
>>>>>>> b8255cb... Added license headers
 */

package org.wso2.is.portal.user.client.api.unit.test;

<<<<<<< HEAD
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
>>>>>>> f61b1d5... Add initial test case for profile downloader microservice
=======
>>>>>>> b3abc2e... Resolved checkstyle issues
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
<<<<<<< HEAD
<<<<<<< HEAD
import org.wso2.is.portal.user.client.api.ProfileImageDownloaderService;
import org.wso2.msf4j.MicroservicesRunner;

=======
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
=======
>>>>>>> b3abc2e... Resolved checkstyle issues
import org.wso2.is.portal.user.client.api.ProfileImageDownloaderService;
import org.wso2.msf4j.MicroservicesRunner;

<<<<<<< HEAD
import javax.inject.Inject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
>>>>>>> f61b1d5... Add initial test case for profile downloader microservice
=======
>>>>>>> b3abc2e... Resolved checkstyle issues
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
<<<<<<< HEAD
<<<<<<< HEAD
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

import static org.testng.AssertJUnit.assertEquals;
=======
import java.nio.file.Paths;
import java.util.List;
=======
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
>>>>>>> b3abc2e... Resolved checkstyle issues

import static org.testng.AssertJUnit.assertEquals;
<<<<<<< HEAD
import static org.testng.AssertJUnit.assertTrue;
>>>>>>> f61b1d5... Add initial test case for profile downloader microservice
=======
>>>>>>> b3abc2e... Resolved checkstyle issues

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class ProfileImageDownloaderServiceTest {

    protected static URI baseURI;
    private MicroservicesRunner microservicesRunner;
    private final ProfileImageDownloaderService testMicroservice = new ProfileImageDownloaderService();
    public static final String HEADER_KEY_CONNECTION = "CONNECTION";
    public static final String HEADER_VAL_CLOSE = "CLOSE";

    @BeforeClass
    public void setup() throws Exception {
        microservicesRunner = new MicroservicesRunner(8129);
        microservicesRunner
                .deploy("/DynamicPath", testMicroservice)
                .start();
    }

    @AfterClass
    public void teardown() throws Exception {
        microservicesRunner.stop();
    }


    @Test(groups = "getProfileImage")
    public void testGetProfileImage() throws IOException {
<<<<<<< HEAD
<<<<<<< HEAD
        HttpURLConnection urlConn = request("/DynamicPath/image?userid=1.0a8faaa2-4091-4000-bdd4-9c417798e47c",
                HttpMethod.GET);
=======
        HttpURLConnection urlConn = request("/DynamicPath/image?userid=1.0a8faaa2-4091-4000-bdd4-9c417798e47c", HttpMethod.GET);
>>>>>>> f61b1d5... Add initial test case for profile downloader microservice
=======
        HttpURLConnection urlConn = request("/DynamicPath/image?userid=1.0a8faaa2-4091-4000-bdd4-9c417798e47c",
                HttpMethod.GET);
>>>>>>> b3abc2e... Resolved checkstyle issues
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
    }

    private HttpURLConnection request(String path, String method) throws IOException {
        return request(path, method, false);
    }

    private HttpURLConnection request(String path, String method, boolean keepAlive) throws IOException {
        baseURI = URI.create(String.format("http://%s:%d", "localhost", 8129));
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
