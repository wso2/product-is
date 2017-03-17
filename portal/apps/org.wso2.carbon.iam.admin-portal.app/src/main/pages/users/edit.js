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


function getProfileNames() {

    try {
        var profiles = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
            "getProfileNames", []);
        return {success: true, profiles: profiles};
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause !== null) {
            //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                message = cause.getTargetException().message;
            }
        }
    }
    return {success: false, message: message};
}



function onGet(env) {
    var session = getSession();
    if (!session || !session.getUser()) {
        sendRedirect(env.contextPath + env.config['loginPageUri']);
    }

    var formId = "";
    var result = getProfileNames();
    var selectedProfile = env.request.queryParams.profile;

    var profilesList = result.profiles.toArray();
    var profiles = [];

    for(var i=0; i < profilesList.length; i++){
       var isSelected = false;
        if (profilesList[i] === selectedProfile){
            isSelected = true;
        }
        profiles.push({"name" : profilesList[i], "isSelected" : isSelected});
    }

    if (result.success) {
        return {profiles: profiles, actionId: formId};
    } else {
        return {errorMessage: result.message};
    }
}


function onPost(env) {
    var session = getSession();
    if (!session || !session.getUser()) {
        sendRedirect(env.contextPath + env.config['loginPageUri']);
    }

    var formId = "";
    formId = env.request.queryString;
    Log.info("Form Id"+ formId);
    var result = getProfileNames();
    var selectedProfile = env.request.queryParams.profile;


    var profilesList = result.profiles.toArray();
    var profiles = [];

    for(var i=0; i < profilesList.length; i++){
      var  isSelected = false;
        if (profilesList[i] === selectedProfile){
            isSelected = true;
        }
        profiles.push({"name" : profilesList[i], "isSelected" : isSelected});
    }



    if (result.success) {
        return {profiles: profiles, actionId: formId};
    } else {
        return {errorMessage: result.message};
    }
}
