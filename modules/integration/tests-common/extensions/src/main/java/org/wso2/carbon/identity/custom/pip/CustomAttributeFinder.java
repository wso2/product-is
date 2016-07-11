/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.custom.pip;


import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.wso2.carbon.identity.entitlement.pip.AbstractPIPAttributeFinder;

/**
 * This class is used by EntitlementPIPAttributeCacheTestCase to simulate the PIP attribute caching scenario.
 */
public class CustomAttributeFinder extends AbstractPIPAttributeFinder {


    private static final String EMAIL_ID = "http://wso2.org/claims/emailaddress";


    /**
     * List of attribute finders supported by the this PIP attribute finder
     */
    private Set<String> supportedAttributes = new HashSet<String>();

    @Override
    public void init(Properties properties) throws Exception {
        supportedAttributes.add(EMAIL_ID);
    }

    @Override
    public String getModuleName() {
        return "Custom Attribute Finder";
    }

    @Override
    public boolean overrideDefaultCache() {
        return false;
    }

    @Override
    public Set<String> getAttributeValues(String subjectId, String resourceId, String actionId,
                                          String environmentId, String attributeId, String issuer) throws Exception {


        Set<String> values = new HashSet<String>();
        if ("admin@wso2.com".equals(subjectId)) {
            values.add(subjectId);
        } else {
            values.add("notexist");
        }
        return values;
    }

    @Override
    public Set<String> getSupportedAttributes() {
        return supportedAttributes;
    }
}
