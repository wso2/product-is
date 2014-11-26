/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.sample.is.sso.agent;

import org.wso2.carbon.identity.sso.agent.openid.AttributesRequestor;

import java.util.*;

public class SampleAttributesRequestor implements AttributesRequestor {

    List<String> requestedAttributes = new ArrayList<String>();
    Map<String, Boolean> requiredMap = new HashMap<String, Boolean>();
    Map<String, String> typeURIMap = new HashMap<String, String>();
    Map<String, Integer> countMap = new HashMap<String, Integer>();

    public void init() {
        requestedAttributes.add("nickname");
        requiredMap.put("nickname", true);
        typeURIMap.put("nickname","http://axschema.org/namePerson/first");
        countMap.put("nickname",1);
        requestedAttributes.add("lastname");
        requiredMap.put("lastname", true);
        typeURIMap.put("lastname","http://axschema.org/namePerson/last");
        countMap.put("lastname",1);
        requestedAttributes.add("email");
        requiredMap.put("email", true);
        typeURIMap.put("email","http://axschema.org/contact/email");
        countMap.put("email",0);
        requestedAttributes.add("country");
        requiredMap.put("country", true);
        typeURIMap.put("country","http://axschema.org/contact/country/home");
        countMap.put("country",1);
        requestedAttributes.add("dob");
        requiredMap.put("dob", true);
        typeURIMap.put("dob","http://axschema.org/birthDate");
        countMap.put("dob",1);
    }

    public String[] getRequestedAttributes(String s) {
        String[] attrArray = new String[requestedAttributes.size()];
        return requestedAttributes.toArray(attrArray);
    }

    public boolean isRequired(String s, String s2) {
        return requiredMap.get(s2);
    }

    public String getTypeURI(String s, String s2) {
        return typeURIMap.get(s2);
    }

    public int getCount(String s, String s2) {
        return countMap.get(s2);
    }
}
