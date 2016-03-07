package org.wso2.sample.identity.oauth2;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;

/**
 * Created by yasiru on 2/23/16.
 */
public class OAuthTokenPKCERequestBuilder extends OAuthClientRequest.TokenRequestBuilder{
    public OAuthTokenPKCERequestBuilder(String url) {
        super(url);
    }

    public OAuthTokenPKCERequestBuilder setPKCECodeVerifier(String codeVerifier) {
        this.parameters.put(OAuth2Constants.OAUATH2_PKCE_CODE_VERIFIER, codeVerifier);
        return this;
    }
}
