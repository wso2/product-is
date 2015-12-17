/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.identity.integration.test.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;

public class Utils {

    private static String RESIDENT_CARBON_HOME;

    public static  boolean nameExists(FlaggedName[] allNames, String inputName) {
        boolean exists = false;

        for (FlaggedName flaggedName : allNames) {
            String name = flaggedName.getItemName();

            if (name.equals(inputName)) {
                exists = true;
                break;
            } else {
                exists = false;
            }
        }

        return exists;
    }

    public static String getResidentCarbonHome() {
        if(StringUtils.isEmpty(RESIDENT_CARBON_HOME)){
            RESIDENT_CARBON_HOME = System.getProperty("carbon.home");;
        }
        return RESIDENT_CARBON_HOME;
    }

}
