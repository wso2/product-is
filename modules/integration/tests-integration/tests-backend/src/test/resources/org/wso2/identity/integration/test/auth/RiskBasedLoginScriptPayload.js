/*
 * Copyright (c) 2014, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

function onLoginRequest(context) {
    executeStep(1, {
        onSuccess: function (context) {
            var username = context.steps[1].subject.username;
            callAnalytics({'ReceiverUrl': '/risk-based-login-endpoint'},
            {
              "stringKey": "stringValue",
              "numberKey": 123,
              "booleanKey": true,
              "arrayKey": [
                "arrayString",
                456,
                false,
                {
                  "nestedObjectInArrayKey": "nestedObjectValue",
                  "nestedArrayInArrayKey": ["nestedArrayValue1", "nestedArrayValue2"]
                }
              ],
              "objectKey": {
                "objectStringKey": "objectStringValue",
                "objectNumberKey": 789,
                "objectBooleanKey": false,
                "nestedObjectKey": {
                  "nestedObjectStringKey": "nestedObjectStringValue",
                  "nestedObjectArrayKey": [
                    "nestedArrayValue1",
                    101112,
                    true
                  ]
                },
                "arrayOfObjectsKey": [
                  {
                    "arrayOfObjectsStringKey": "arrayOfObjectsStringValue1",
                    "arrayOfObjectsNumberKey": 131415,
                    "arrayOfObjectsBooleanKey": true
                  },
                  {
                    "arrayOfObjectsStringKey": "arrayOfObjectsStringValue2",
                    "arrayOfObjectsNumberKey": 161718,
                    "arrayOfObjectsBooleanKey": false
                  }
                ]
              }
            }, {
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
