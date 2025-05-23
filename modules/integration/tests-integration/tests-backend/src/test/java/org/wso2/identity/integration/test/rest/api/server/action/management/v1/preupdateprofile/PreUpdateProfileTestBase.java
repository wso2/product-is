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

package org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdateprofile;

import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.ActionTestBase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PreUpdateProfileTestBase extends ActionTestBase {

    protected static final String PRE_UPDATE_PROFILE_PATH = "/preUpdateProfile";

    protected static final String PRE_UPDATE_PROFILE_ACTION_TYPE = "PRE_UPDATE_PROFILE";
    protected static final String ATTRIBUTES = "attributes";
    protected static final int MAX_ATTRIBUTES_COUNT = 10;
    protected static final List<String> TEST_ATTRIBUTES = Arrays.asList("http://wso2.org/claims/active",
            "http://wso2.org/claims/dob");

    protected static final List<String> TEST_UPDATED_ATTRIBUTES = Arrays.asList("http://wso2.org/claims/country",
            "http://wso2.org/claims/created");

    protected static final List<String> INVALID_TEST_ATTRIBUTES_COUNT =  Collections.nCopies(11,
            "http://wso2.org/claims/active");

    protected static final List<String> TEST_DUPLICATED_ATTRIBUTES =  Collections.nCopies(2,
            "http://wso2.org/claims/active");

    protected static final List<String> ROLES_CLAIM_ATTRIBUTE =
            Collections.singletonList("http://wso2.org/claims/roles");

    public static final List<String> INVALID_TEST_ATTRIBUTES = Arrays.asList("invalidattribute");;
}
