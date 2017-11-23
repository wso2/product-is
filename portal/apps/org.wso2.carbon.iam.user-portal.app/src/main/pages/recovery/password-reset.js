/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/*
 Page sent to a user in order to recover password
 */
module("recovery-manager");

function onGet(env) {
    if (!env.request.queryParams['confirmation']) {
        sendRedirect(env.contextPath + '/login');
    }
}

function onPost(env) {
    var confirmationCode = env.request.queryParams['confirmation'];
    var password = env.request.formParams['confirmPassword'];

    if (confirmationCode && password) {
        recoveryManager.updatePassword(confirmationCode, password, env);
    }
 }