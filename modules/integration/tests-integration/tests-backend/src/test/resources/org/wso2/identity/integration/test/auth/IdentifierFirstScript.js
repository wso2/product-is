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

var username;
function onLoginRequest(context) {
    promptUsername ();
}

function promptUsername () {
    promptIdentifierForStep(1, {
        onSuccess: function(context, data) {
            if (context.request.params.username) {
                username = context.request.params.username[0];
            }
            promptBasic(username);
        },
        onSkip: function(context, data) {
            executeStep(1);

        },
        onFail: function(context, data) {
            Log.info('================================= onFail prompt');
        }
    });
}

function promptBasic(username) {
    executeStep(1, {
        authenticatorParams: {
            "common": {
                "username": username,"inputType":"identifierFirst"
            }
        }
    }, {
        onSuccess: function(context) {
            Log.info("================================= onSuccess basic auth");
        }, onUserAbort: function(context) {
            Log.info("================================= onUserAbort basic auth");
            promptUsername ();
        }, onFail: function(context) {
            promptBasic(username);
        }
    });
}
