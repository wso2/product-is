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

function onGet(env) {
    /*TODO
     * confirmation code has to be sent in the URL (a query param) and need to validate it here
     * If the confirmation code is expired notify in the UI
     * Proceed to page otherwise
     */
}

function onPost(env) {
    /*TODO
     * perform password reset with newly provided password and verification code
     */
 }