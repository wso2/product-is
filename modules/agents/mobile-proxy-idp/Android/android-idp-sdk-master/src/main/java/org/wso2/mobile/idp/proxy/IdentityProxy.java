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
package org.wso2.mobile.idp.proxy;

import android.util.Log;
import org.wso2.mobile.idp.proxy.beans.Token;
import org.wso2.mobile.idp.proxy.callbacks.AccessTokenCallBack;
import org.wso2.mobile.idp.proxy.callbacks.CallBack;
import org.wso2.mobile.idp.proxy.handlers.RefreshTokenHandler;
import org.wso2.mobile.idp.proxy.utils.IDPConstants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * client application specific data
 */
public class IdentityProxy implements CallBack {
    private static String TAG = "IdentityProxy";
    private Token token;
    private static IdentityProxy identityProxy = new IdentityProxy();
    private String clientID;
    private String clientSecret;
    private String accessTokenURL;
    private AccessTokenCallBack accessTokenCallBack;

    private IdentityProxy() {

    }

    public String getAccessTokenURL() {
        return accessTokenURL;
    }

    public void setAccessTokenURL(String accessTokenURL) {
        this.accessTokenURL = accessTokenURL;
    }

    public void receiveAccessToken(String status, String message, Token token) {
        Log.d(TAG, token.getAccessToken());
        Log.d(TAG, token.getIdToken());
        Log.d(TAG, token.getRefreshToken());
        this.token = token;
        accessTokenCallBack.onTokenReceived();
    }

    public void receiveNewAccessToken(String status, String message, Token token) {
        this.token = token;
    }

    public static IdentityProxy getInstance() {
        return identityProxy;
    }

    public void init(String clientID, String clientSecret, AccessTokenCallBack accessTokenCallBack) {
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.accessTokenCallBack = accessTokenCallBack;
    }

    public Token getToken() throws Exception, InterruptedException, ExecutionException, TimeoutException {
        boolean decision = dateComparison(token.getDate());
        if (decision) {
            return token;
        }
        RefreshTokenHandler refreshTokenHandler = new RefreshTokenHandler(clientID, clientSecret, token);
        refreshTokenHandler.obtainNewAccessToken();
        return token;
    }

    /** check the age of access token, if it is more than 3000secs, will get a new access token
     *
     * @param date
     * @return
     */
    public boolean dateComparison(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date currentDate = new Date();
        String strDate = dateFormat.format(currentDate);
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        try {
            currentDate = format.parse(strDate);
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        long diff = (currentDate.getTime() - date.getTime());
        long diffSeconds = diff / 1000 % 60;
        if (diffSeconds < IDPConstants.ACCESS_TOKEN_AGE) {
            return true;
        }
        return false;
    }
}
