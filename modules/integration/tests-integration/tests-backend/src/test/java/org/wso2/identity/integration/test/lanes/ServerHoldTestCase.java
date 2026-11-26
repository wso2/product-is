/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.lanes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Keeps a shared IS server running for the test lanes that attach to it. Run by TestLaneRunner in the JVM that owns
 * the server, after is-tests-initialize: writes {@code ready} (containing carbon.home) to the {@code lane.hold.dir}
 * directory, then waits until the runner writes {@code release} there, or {@code lane.hold.timeoutMinutes} pass.
 */
public class ServerHoldTestCase {

    private static final Log LOG = LogFactory.getLog(ServerHoldTestCase.class);

    @Test
    public void holdServerForAttachedLanes() throws Exception {

        Path dir = Paths.get(System.getProperty("lane.hold.dir"));
        Files.createDirectories(dir);
        Path ready = dir.resolve("ready");
        Files.write(dir.resolve("ready.tmp"), System.getProperty("carbon.home").getBytes(StandardCharsets.UTF_8));
        Files.move(dir.resolve("ready.tmp"), ready);
        LOG.info("Shared IS server is ready for attached lanes: " + System.getProperty("carbon.home"));

        long deadline = System.currentTimeMillis()
                + TimeUnit.MINUTES.toMillis(Long.getLong("lane.hold.timeoutMinutes", 240));
        while (!Files.exists(dir.resolve("release"))) {
            if (System.currentTimeMillis() > deadline) {
                throw new IllegalStateException("Attached lanes did not finish in time; stopping the shared server.");
            }
            Thread.sleep(2000);
        }
        LOG.info("Attached lanes are done; releasing the shared IS server.");
    }
}
