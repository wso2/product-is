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
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.is.portal.user.client.api.bean.UUFUser;

import java.util.UUID;

/**
 * test for UUFUser
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class UUFUserTest {
    private String username;
    private String userID;
    private String domain;

    @BeforeMethod
    public void init() {
        username = "Ayesha";
        userID = UUID.randomUUID().toString();
        domain = "PRIMARY";
    }

    @Test
    public void testUUFUserGetters() {
        UUFUser user = new UUFUser(username, userID, domain);
        Assert.assertEquals(user.getUsername(), username, "Incorrect Username");
        Assert.assertEquals(user.getDomainName(), domain, "Incorrect Domain");
        Assert.assertEquals(user.getUserId(), userID, "Incorrect ID");
    }

    @Test
    public void testUUFHasPermission() {
        UUFUser user = new UUFUser(username, userID, domain);
        Assert.assertFalse(user.hasPermission("s1", "s2"), "Default permission state is not returned");
    }

    @Test
    public void testUUFUseSetters() {
        UUFUser user = new UUFUser(username, userID, domain);
        String usernameNew = "NewName";
        String userIDNew = UUID.randomUUID().toString();
        String domainNew = "NewDomain";
        user.setDomainName(domainNew);
        user.setUserId(userIDNew);
        user.setUsername(usernameNew);
        Assert.assertEquals(user.getUsername(), usernameNew, "Incorrect Username is set.");
        Assert.assertEquals(user.getDomainName(), domainNew, "Incorrect Domain is set.");
        Assert.assertEquals(user.getUserId(), userIDNew, "Incorrect ID is set.");
    }
}
