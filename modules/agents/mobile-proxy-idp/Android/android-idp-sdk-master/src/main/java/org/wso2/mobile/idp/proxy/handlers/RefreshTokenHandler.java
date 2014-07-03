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
package org.wso2.mobile.idp.proxy.handlers;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.mobile.idp.proxy.IdentityProxy;
import org.wso2.mobile.idp.proxy.utils.IDPConstants;
import org.wso2.mobile.idp.proxy.utils.ServerUtilities;
import org.wso2.mobile.idp.proxy.beans.Token;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Getting new access token and refresh token  from refresh grant after access token is expired
 */
public class RefreshTokenHandler extends Activity {
    private Token tokens = null;
    private static final String TAG = "RefreshTokenHandler";
    private String clientID = null;
    private String clientSecret = null;
    private Token token;

    /**
     * @param clientID
     * @param clientSecret
     * @param token
     */
    public RefreshTokenHandler(String clientID, String clientSecret, Token token) {
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.token = token;
    }

    public void obtainNewAccessToken() throws InterruptedException, ExecutionException, TimeoutException {
        new NetworkCallTask().execute().get(1000, TimeUnit.MILLISECONDS);
    }

    private class NetworkCallTask extends AsyncTask<Void, Void, Map<String,String>> {

        private String responseCode = null;

        public NetworkCallTask() {

        }

        @Override
        protected Map<String,String> doInBackground(Void... params) {
            Map<String, String> requestParams = new HashMap<String, String>();
            requestParams.put(IDPConstants.GRANT_TYPE, IDPConstants.REFRESH_TOKEN);
            requestParams.put(IDPConstants.REFRESH_TOKEN, tokens.getRefreshToken());
            Map<String, String> responseResult = ServerUtilities.postData(IdentityProxy.getInstance().getAccessTokenURL(), requestParams, clientID, clientSecret);
            return responseResult;
        }

        /** Get new access token and refresh token from result
         *
         * @param responseResult
         */
        @Override
        protected void onPostExecute(Map<String, String> responseParams) {
        	if(responseParams==null){
        		return;
        	}
            String response = responseParams.get("response");
            String responseCode = responseParams.get("status");
            try {
                JSONObject responseJsonObj = new JSONObject(response);
                IdentityProxy identityProxy = IdentityProxy.getInstance();
                if (responseCode != null && responseCode.equals(String.valueOf(HttpStatus.SC_OK))) {
                    String refreshToken = responseJsonObj.getString(IDPConstants.REFRESH_TOKEN);
                    String accessToken = responseJsonObj.getString(IDPConstants.ACCESS_TOKEN);
                    Log.d(TAG, refreshToken);
                    Log.d(TAG, accessToken);
                    token.setRefreshToken(refreshToken);
                    token.setAccessToken(accessToken);
                    identityProxy.receiveNewAccessToken(responseCode, "success", token);
                } else if (responseCode != null) {
                    String error = responseJsonObj.getString("error");
                    String errorDescription = responseJsonObj.getString("error_description");
                    Log.d(TAG, error);
                    Log.d(TAG, errorDescription);
                    identityProxy.receiveNewAccessToken(responseCode, errorDescription, null);
                }
            } catch (JSONException e) {
                Log.d(TAG,e.toString());
            }
        }
    }
}
