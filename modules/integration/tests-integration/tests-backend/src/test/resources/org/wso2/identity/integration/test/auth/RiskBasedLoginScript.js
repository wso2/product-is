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
    executeStep(1, {
        onSuccess: function (context) {
            var username = context.steps[1].subject.username;
            callAnalytics({'ReceiverUrl': '/RiskBasedLogin/InputStream'}, {"username": username}, {
                onSuccess: function (context, data) {
                    if (data.event.riskScore > 0) {
                        executeStep(2);
                    }
                }, onFail: function (context, data) {
                    Log.info('fail Called');
                    executeStep(2);
                }
            });
        }
    });
}
