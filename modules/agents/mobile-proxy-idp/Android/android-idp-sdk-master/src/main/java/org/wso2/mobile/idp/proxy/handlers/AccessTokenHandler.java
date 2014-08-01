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
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.mobile.idp.proxy.utils.IDPConstants;
import org.wso2.mobile.idp.proxy.IdentityProxy;
import org.wso2.mobile.idp.proxy.utils.ServerUtilities;
import org.wso2.mobile.idp.proxy.beans.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * After receiving authorization code, this class can be used to obtain access token
 */
public class AccessTokenHandler extends Activity {
    private static final String TAG = "AccessTokenHandler";
    private String clientSecret = null;
    private String clientID = null;

    /**
     *
     * @param clientID
     * @param clientSecret
     */
    public AccessTokenHandler(String clientID, String clientSecret) {
        this.clientID = clientID;
        this.clientSecret = clientSecret;
    }

    /**
     *
     * @param code
     */
    public void obtainAccessToken(String code) {
        new NetworkCallTask().execute(code);
    }

    /**
     * AsyncTask to send authorization code and get access token and refresh token from authorization server
     */
    private class NetworkCallTask extends AsyncTask<String, Void, Map<String,String>> {

        public NetworkCallTask() {

        }

        /**
         *
         * @param params array of Strings first element as authorization code
         * @return
         */
        @Override
        protected Map<String,String> doInBackground(String... params) {
            if(params == null || params[0] == null){
                return null;
            }
            String code = params[0];
            Log.d(TAG, code);
            Map<String, String> requestParams = new HashMap<String, String>();
            // TODO : consider extract these as constants
            requestParams.put(IDPConstants.GRANT_TYPE, IDPConstants.GRANT_TYPE_AUTHORIZATION_CODE);
            requestParams.put(IDPConstants.AUTHORIZATION_CODE, code);
            requestParams.put(IDPConstants.REDIRECT_URL, IDPConstants.CALL_BACK_URL);
            requestParams.put(IDPConstants.SCOPE, IDPConstants.OPENID);
            Map<String, String> responseParams = ServerUtilities.postData(IdentityProxy.getInstance().getAccessTokenURL(), requestParams, clientID, clientSecret);
            return responseParams;
        }

        /**
         * access token, refresh token and id token will be received from response, After receiving tokens invoke receiveAccessToken method in CallBack
         * @param responseParams HashMap with two elements response and status
         */
        @Override
        protected void onPostExecute(Map<String,String> responseParams) {

        	if(responseParams==null){
        		return;
        	}

        	String response = responseParams.get("response");
        	String responseCode = responseParams.get("status");
            Log.d(TAG,response);

            try {
                JSONObject responseJSONObj = new JSONObject(response);
                IdentityProxy identityProxy = IdentityProxy.getInstance();

                if (responseCode != null && responseCode.equals(String.valueOf(HttpStatus.SC_OK))) {
                    String refreshToken = responseJSONObj.getString(IDPConstants.REFRESH_TOKEN);
                    String accessToken = responseJSONObj.getString(IDPConstants.ACCESS_TOKEN);
                    String idToken = responseJSONObj.getString(IDPConstants.ID_TOKEN);

                    idToken = new String(Base64.decodeBase64(idToken.getBytes()));

                    Token token = new Token();
                    token.setRefreshToken(refreshToken);
                    token.setIdToken(idToken);
                    token.setAccessToken(accessToken);
                    token.setDate();

                    identityProxy.receiveAccessToken(responseCode, "success", token);
                } else if (responseCode != null) {
                    String error = responseJSONObj.getString("error");
                    String errorDescription = responseJSONObj.getString("error_description");
                    Log.d(TAG, error);
                    Log.d(TAG, errorDescription);

                    identityProxy.receiveAccessToken(responseCode, errorDescription, null);
                }
            }catch (JSONException e) {
                Log.d(TAG,e.toString());
            }
            
        }
    }
}
