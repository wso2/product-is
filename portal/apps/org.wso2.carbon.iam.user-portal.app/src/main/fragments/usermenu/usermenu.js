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

function isProfileImageAvailbale(session) {
    var Paths = Java.type('java.nio.file.Paths');
    var System = Java.type('java.lang.System');
    var Files = Java.type('java.nio.file.Files');
    var File = Java.type('java.io.File');
    var imageDirPath = Paths.get(System.getProperty('user.dir'), "images");
    if (!Files.exists(imageDirPath)) {
        return {profileImage: false};
    }
    else {
        var file = new File(imageDirPath.toString());
        var names = file.list();
        if (names) {
            for (var i = 0; i < names.length; i++) {
                var imageName = names[i].toString();
                if (imageName.indexOf(session.getUser().getUserId()) !== -1) {
                    return {profileImage: true, userId: session.getUser().getUserId()};
                }
            }
        }

    }
    return {profileImage: false};
}

function onGet(env) {
    var session = getSession();
    if (!session) {
        sendRedirect(env.contextPath + env.config.loginPageUri);
    }
    var profileImageResult = isProfileImageAvailbale(session);
    return {
        username: session.getUser().getUsername(), profileImage: profileImageResult.profileImage,
        userId: profileImageResult.userId
    };
}

function onPost(env) {
    var session = getSession();
    if (!session) {
        sendRedirect(env.contextPath + env.config.loginPageUri);
    }
    var profileImageResult = isProfileImageAvailbale(session);
    return {
        username: session.getUser().getUsername(), profileImage: profileImageResult.profileImage,
        userId: profileImageResult.userId
    };
}