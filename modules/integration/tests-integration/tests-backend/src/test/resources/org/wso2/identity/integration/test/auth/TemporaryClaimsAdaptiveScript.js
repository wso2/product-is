/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

var temporaryClaim;

function onLoginRequest(context) {
    temporaryClaim = context.request.params.tempClaim;

    executeStep(1, {
        onSuccess: function (context) {
            if (temporaryClaim && temporaryClaim.length > 0) {
                // Assigning temporary claim to nickname, which is an already existing OIDC claim, for ease of
                // implementation.
                context.currentKnownSubject.claims['http://wso2.org/claims/nickname'] = temporaryClaim[0];
            }
        }
    });
}
