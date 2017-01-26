/*
<<<<<<< HEAD
<<<<<<< HEAD
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

function onRequest(env) {
    if (getSession()) {
        if (destroySession()) {
            sendRedirect(env.contextPath + env.config['loginPageUri']);
        } else {
            Log.debug("Error while logging out.");
        }
    } else {
=======
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
=======
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
>>>>>>> 1b5ffcb... Changed year to 2017
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

function onRequest(env) {
<<<<<<< HEAD
    if (destroySession()) {
>>>>>>> 43350c9... Adding Login/Logout functionality.
=======
    var session = getSession();
    if (session) {
        if (destroySession()) {
            sendRedirect(env.contextPath + env.config['loginPageUri']);
        } else {
            LOG.info("Error while logging out.");
        }
    } else if (!session || !session.getUser()) {
>>>>>>> cbdd792... Fixed issue in logout page when there is no session
        sendRedirect(env.contextPath + env.config['loginPageUri']);
    }
}
