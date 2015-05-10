/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.openid;


import org.wso2.carbon.identity.provider.openid.stub.dto.OpenIDParameterDTO;

public class Util {

    public static String openidUserIDBase = "https://localhost:9443/openid/";

    /**
     * Return the OpenID Identifier of the username
     * 
     * @param username
     * @return
     */
    public static String getDefaultOpenIDIdentifier(String username) {
        return openidUserIDBase.concat(username);
    }

    /**
     * Returns a dummy request with four claims requests : firstname, lastname,
     * country and email.
     * 
     * @return
     */
    public static OpenIDParameterDTO[] getDummyOpenIDParameterDTOArray() {

        OpenIDParameterDTO[] openidParams = new OpenIDParameterDTO[14];

        OpenIDParameterDTO ext1 = new OpenIDParameterDTO();
        ext1.setName("openid.ns.ext1");
        ext1.setValue("http://openid.net/srv/ax/1.0");
        openidParams[0] = ext1;

        OpenIDParameterDTO claimedID = new OpenIDParameterDTO();
        claimedID.setName("openid.claimed_id");
        claimedID.setValue("https://localhost:9443/openid/suresh");
        openidParams[1] = claimedID;

        OpenIDParameterDTO required = new OpenIDParameterDTO();
        required.setName("openid.ext1.required");
        required.setValue("email,firstname,lastname,country");
        openidParams[2] = required;

        OpenIDParameterDTO handle = new OpenIDParameterDTO();
        handle.setName("openid.assoc_handle");
        handle.setValue("32471379494934315-1");
        openidParams[3] = handle;

        OpenIDParameterDTO lastname = new OpenIDParameterDTO();
        lastname.setName("openid.ext1.type.lastname");
        lastname.setValue("http://axschema.org/namePerson/last");
        openidParams[4] = lastname;

        OpenIDParameterDTO ns = new OpenIDParameterDTO();
        ns.setName("openid.ns");
        ns.setValue("http://specs.openid.net/auth/2.0");
        openidParams[5] = ns;

        OpenIDParameterDTO firstname = new OpenIDParameterDTO();
        firstname.setName("openid.ext1.type.firstname");
        firstname.setValue("http://axschema.org/namePerson/first");
        openidParams[6] = firstname;

        OpenIDParameterDTO identity = new OpenIDParameterDTO();
        identity.setName("openid.identity");
        identity.setValue("https://localhost:9443/openid/suresh");
        openidParams[7] = identity;

        OpenIDParameterDTO email = new OpenIDParameterDTO();
        email.setName("openid.ext1.type.email");
        email.setValue("http://axschema.org/contact/email");
        openidParams[8] = email;

        OpenIDParameterDTO mode = new OpenIDParameterDTO();
        mode.setName("openid.mode");
        mode.setValue("checkid_setup");
        openidParams[9] = mode;

        OpenIDParameterDTO extMode = new OpenIDParameterDTO();
        extMode.setName("openid.ext1.mode");
        extMode.setValue("fetch_request");
        openidParams[10] = extMode;

        OpenIDParameterDTO realm = new OpenIDParameterDTO();
        realm.setName("openid.realm");
        realm.setValue("http://localhost:8090/openid-client");
        openidParams[11] = realm;

        OpenIDParameterDTO country = new OpenIDParameterDTO();
        country.setName("openid.ext1.type.country");
        country.setValue("http://axschema.org/contact/country/home");
        openidParams[12] = country;

        OpenIDParameterDTO returnto = new OpenIDParameterDTO();
        returnto.setName("openid.return_to");
        returnto.setValue("http://localhost:8090/openid-client/");
        openidParams[13] = returnto;

        return openidParams;

    }

}
