/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function onLoginRequest(context) {

    if (context.request.cookies.testcookie) {
        Log.info("--------------- cookie testcookie found in request.");
        Log.info("--------------- cookie testcookie.value: " + context.request.cookies.testcookie.value);
        Log.info("--------------- cookie testcookie.domain: " + context.request.cookies.testcookie.domain);
        Log.info("--------------- cookie testcookie.max-age: " + context.request.cookies.testcookie["max-age"]);
        Log.info("--------------- cookie testcookie.path: " + context.request.cookies.testcookie.path);
        Log.info("--------------- cookie testcookie.secure: " + context.request.cookies.testcookie.secure);
        Log.info("--------------- cookie testcookie.version: " + context.request.cookies.testcookie.version);
        Log.info("--------------- cookie testcookie.httpOnly: " + context.request.cookies.testcookie.httpOnly);
    } else {
        executeStep(1, {
            onSuccess: function (context) {
                Log.info("--------------- setting cookie : testcookie");
                context.response.headers["Set-Cookie"] = "testcookie=1FD36B269C61; Path=/; Secure;" +
                    " HttpOnly; Expires=Wed, 31 Jan 2018 07:28:00 GMT";
            }
        });
    }
}
