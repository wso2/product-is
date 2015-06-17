/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var opIFrame = document.getElementById('opIFrame').contentWindow;

/**
 * periodically invoking the Endpoint at OP for every six seconds
 */
setInterval(function () {
    console.log('Sending polling Request From RP Client ... ' + mes + ',' + sessionState);
    document.all.opIFrame.src = endPoint;
    opIFrame.postMessage(mes, endPoint);
}, 6000);


window.addEventListener("message", OnMessage, false);

/**
 * Fires when a message arrives to the RP
 */
function OnMessage(event) {
    console.log('Receiving the Session Status From OP=========' + event.data);
    if (event.origin == targetOrigin) {
        if (event.data == "changed") {
            var prompt = "none";
            console.log("The state has been changed....");
            document.all.opIFrame.src = endPoint;
            opIFrame.postMessage(prompt, endPoint);
        }
        else if (event.data == "unchanged") {
            console.log("The state not has been changed....");
        }
        else {
            //after comparing the id token hints, logout the RP
        }
    }
    else {
        return;
    }
}

