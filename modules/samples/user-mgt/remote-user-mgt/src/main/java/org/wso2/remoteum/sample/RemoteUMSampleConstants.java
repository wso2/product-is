/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.remoteum.sample;

import java.io.File;

public class RemoteUMSampleConstants {

    public static final String PROPERTIES_FILE_NAME = "client.properties";
    public static final String REMOTE_SERVER_URL = "remote.server.url";
    public static final String USER_NAME = "user.name";
    public static final String PASSWORD = "user.password";

    public static final String IS_HOME = ".." + File.separator + ".." + File.separator;

    public static final String TRUST_STORE_PATH = IS_HOME + "repository" + File.separator + "resources" +
                                                  File.separator + "security" + File.separator + "wso2carbon.jks";
    
    public static final String TRUST_STORE_PASSWORD = "wso2carbon";
}
