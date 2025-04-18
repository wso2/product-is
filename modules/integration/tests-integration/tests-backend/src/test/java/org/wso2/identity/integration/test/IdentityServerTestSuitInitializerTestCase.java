/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testng.annotations.Test;
import org.wso2.identity.integration.test.util.Utils;

public class IdentityServerTestSuitInitializerTestCase {

    @Test(groups = "wso2.is.test")
    public void testInitialize() throws Exception {
        //save the carbon.home system property
        Utils.getResidentCarbonHome();
        //add BC provider
        BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
        if (java.security.Security.getProvider(bouncyCastleProvider.getName()) == null) {
            java.security.Security.addProvider(bouncyCastleProvider);
        }
    }
}
