/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

var comprehensivePayload = %s;
function onLoginRequest(context) {
    httpPost('%s',
        comprehensivePayload,
        {
            "Accept": "application/json"
        },
        {
            "type": "apikey",
            "properties": {
                "headerName": "X-API-KEY",
                "apiKey": "a-p-i-k-e-y"
            }
        },
        {
            onSuccess: function(context, data) {
                Log.info('httpGet call success');
                if (data.responseObject.objectKey.objectStringKey == "objectStringValue") {
                    executeStep(1);
                } else {
                    fail();
                }
            },
            onFail: function(context, data) {
                Log.info('httpGet call failed');
                fail();
            }
        }
    );
}