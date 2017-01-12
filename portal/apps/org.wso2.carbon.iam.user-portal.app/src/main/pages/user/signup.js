/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

function onRequest(env) {

  /*  if (env.request.method == "POST") {
        var map = {};
        /!*for (var i = 0; i < document.getElementById("signupClaims").length; ++i) {
         map[signupClaims[i].claimURI] = document.getElementById(signupClaims[i].displayLabel).value;
         }*!/
        map["http://wso2.org/claims/username"] = "indunil";
        map["http://wso2.org/claims/employeeNumber"] = "1289";

        var userRegistration = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "addUser", [map]);
    }

    if (env.request.method == "GET") {
        var claimProfile = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
            "getProfile", ["self-signUp"]);
        var claimForProfile = claimProfile.claims;
        var claimArray = [];
        for (var i = 0; i < claimForProfile.length; i++) {
            var ProfileMgtDAO = Java.type("org.wso2.is.portal.user.client.api.dao.ProfileMgtDAO");
            var profileMgt = new ProfileMgtDAO();
            claimArray[i] = profileMgt.getClaimProfile(claimForProfile[i]);
        }
        sendToClient("signupClaims", claimArray);
        return {"signupClaims": claimArray};
    }*/
}