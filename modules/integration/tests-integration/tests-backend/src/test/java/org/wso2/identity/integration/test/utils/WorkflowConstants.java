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

package org.wso2.identity.integration.test.utils;

public class WorkflowConstants {

    private WorkflowConstants(){
    }

    public static final String IMMEDIATE_DENY_TEMPLATE_IMPL_NAME = "Default";
    public static final String IMMEDIATE_DENY_TEMPLATE_ID = "ImmediateDeny";


    public static final String ADD_USER_EVENT = "ADD_USER";
    public static final String ADD_ROLE_EVENT = "ADD_ROLE";
    public static final String DELETE_USER_EVENT = "DELETE_USER";
    public static final String DELETE_ROLE_EVENT = "DELETE_ROLE";
    public static final String UPDATE_ROLE_NAME_EVENT = "UPDATE_ROLE_NAME";
    public static final String CHANGE_USER_CREDENTIAL_EVENT = "CHANGE_CREDENTIAL";
    public static final String SET_USER_CLAIM_EVENT = "SET_USER_CLAIM";
    public static final String DELETE_USER_CLAIM_EVENT = "DELETE_MULTIPLE_USER_CLAIMS";
    public static final String SET_MULTIPLE_USER_CLAIMS_EVENT = "SET_MULTIPLE_USER_CLAIMS";
    public static final String DELETE_MULTIPLE_USER_CLAIMS_EVENT = "DELETE_MULTIPLE_USER_CLAIMS";
    public static final String UPDATE_USER_ROLES_EVENT = "UPDATE_USER_ROLES";
    public static final String UPDATE_ROLE_USERS_EVENT = "UPDATE_ROLE_USERS";


}
