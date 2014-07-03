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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import org.wso2.mobile.idp.proxy.callbacks.AccessTokenCallBack;
import org.wso2.mobile.idp.proxy.handlers.AccessTokenHandler;
import org.wso2.mobile.idp.proxy.utils.IDPConstants;

/**
 * This is the entry point to IDP proxy SDK any client application should extends this Activity
 *
 */
public class IDPProxyActivity extends Activity {

    private static String TAG = "IdentityProxyActivity";
    private String clientID = null;
    private String clientSecret = null;

    /**
     * initialize attributes required for IDP proxy SDK
     * @param clientID
     * @param clientSecret
     * @param accessTokenCallBack callback to acknowledge client application once access token is received
     */
	public void init(String clientID, String clientSecret, AccessTokenCallBack accessTokenCallBack){
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        
		IdentityProxy identityProxy = IdentityProxy.getInstance();
        identityProxy.init(clientID,clientSecret,accessTokenCallBack);
        
        Log.d(TAG, "starting IDP Proxy App");
        final Intent loginIntent = new Intent("android.intent.action.MAIN");
        loginIntent.setComponent(ComponentName.unflattenFromString(IDPConstants.IDP_PROXY_PACKAGE + "/" + IDPConstants.IDP_PROXY_ACTIVITY));
        loginIntent.putExtra(IDPConstants.CLIENT_ID, clientID);
        startActivityForResult(loginIntent, 0);//start main activity of IDP proxy application
	}

    /** Get authorization code, token endpoint from IDP proxy application as a response. After receiving authorization code and token endpoint, it sends the second request of authorization grant to obtain access token, refresh token and id token as a response  
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "starting IdentityProxyActivity onActivityResult");
        if (data != null) {
            String code = data.getStringExtra("code");
            Log.v(TAG, code);
            String accessTokenURL = data.getStringExtra("access_token_url");
            IdentityProxy.getInstance().setAccessTokenURL(accessTokenURL);
            super.onActivityResult(requestCode, resultCode, data);
            
            try {
                AccessTokenHandler accessTokenHandler = new AccessTokenHandler(clientID, clientSecret);
                accessTokenHandler.obtainAccessToken(code);
            } catch (Exception e) {
                Log.d("ERROR", e.toString());
            }
        }
    }
}
