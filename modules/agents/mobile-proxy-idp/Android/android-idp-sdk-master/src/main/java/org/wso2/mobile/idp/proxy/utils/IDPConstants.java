/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.mobile.idp.proxy.utils;

import android.util.StringBuilderPrinter;

/**
 * package names of IDP proxy application
 */
public class IDPConstants {
    public final static String IDP_PROXY_PACKAGE = "org.wso2.mobile.idp";
    public final static String IDP_PROXY_ACTIVITY = "org.wso2.mobile.idp.WebViewActivity";
    public final static String CALL_BACK_URL = "http://wso2.com";
    public final static int ACCESS_TOKEN_AGE =3000;
    public final static String GRANT_TYPE = "grant_type";
    public final static String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    public final static String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    public final static String AUTHORIZATION_CODE = "code";
    public final static String REDIRECT_URL = "redirect_uri";
    public final static String SCOPE = "scope";
    public final static String OPENID = "openid";
    public final static String REFRESH_TOKEN = "refresh_token";
    public final static String ACCESS_TOKEN = "access_token";
    public final static String ID_TOKEN = "id_token";
    public final static String CLIENT_ID = "client_id";
}