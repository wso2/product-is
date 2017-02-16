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

 module("recovery-manager");

  function getClaims(){
        /**
         * Get the username-recovery Profile
         */

      var claimProfile;

      try {
          claimProfile = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
                "getProfile", ["username-recovery"]);
      } catch (e) {
      Log.error("Error", e);
                    return {errorMessage: profile + '.error.retrieve.claim'};
      }
      var claimForProfileEntry = claimProfile.claims;

      var claimProfileArray = [];

      for (var i = 0; i < claimForProfileEntry.length; i++) {

         claimProfileArray[i] = generateClaimProfileMap(claimForProfileEntry[i]);

      }

      return{
         "usernameRecoveryClaims":claimProfileArray
      };
  }

    function generateClaimProfileMap(claimProfileEntry) {
        var claimProfileMap = {};
        claimProfileMap["displayName"] = claimProfileEntry.getDisplayName();
        claimProfileMap["claimURI"] = claimProfileEntry.getClaimURI();
        claimProfileMap["required"] = claimProfileEntry.getRequired();
        claimProfileMap["regex"] = claimProfileEntry.getRegex();
        claimProfileMap["readonly"] = claimProfileEntry.getReadonly();
        claimProfileMap["dataType"] = claimProfileEntry.getDataType();
        claimProfileMap["claimLabel"] = claimProfileEntry.getClaimURI().replace("http://wso2.org/claims/", "");
        return claimProfileMap;
    }



 function onGet(env) {

     var domainSeparator = env.config['domainSeparator'];
         //check whether password recovery options are enabled
         try {
             var result = recoveryManager.isUsernameRecoveryPortalEnabled();
             if (result) {
                return getClaims();
             }else {
                return {isUsernameRecoveryDisabled: true};
             }

         } catch (e) {
             sendError(500, "something.went.wrong");
         }
 }


 function onPost(env) {
     Log.info(" on post starts ..")
     var claimMap =  env.request.formParams;
     Log.info(" got form params to a claim map");

    for (var i in claimMap) {
        Log.info(i +"------------" + claimMap[i])
    }
    //TODO check formparams and send only not null ones
     var userRecovered = callOSGiService("org.wso2.is.portal.user.client.api.RecoveryMgtService",
                                                            "verifyUsername", [claimMap]);
    Log.info("User recovered :" + userRecovered );

    if (userRecovered) {
        sendRedirect(env.contextPath + '/recovery/complete?username=true');
    }else {
        return {errorMessage: 'username.error.invalid.username'};
    }
 }

