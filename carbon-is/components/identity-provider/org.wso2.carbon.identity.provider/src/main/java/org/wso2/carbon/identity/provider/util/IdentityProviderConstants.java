/*
 * Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.provider.util;

/**
 * This class is used to keep the identity provider related constants.
 */
public class IdentityProviderConstants {

    public static final String SHARED_IDP_PREFIX = "SHARED_";
    public static final String MULTI_VALUED_PROPERTY_CHARACTER = ".";
    public static final String IS_TRUE_VALUE = "1";
    public static final String IS_FALSE_VALUE = "0";
    public static final String MULTI_VALUED_PROPERT_IDENTIFIER_PATTERN = ".*\\" + MULTI_VALUED_PROPERTY_CHARACTER +
            "[0-9]+";

}
