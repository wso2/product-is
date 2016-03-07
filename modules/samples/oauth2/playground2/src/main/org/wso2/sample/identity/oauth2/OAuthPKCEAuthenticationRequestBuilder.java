package org.wso2.sample.identity.oauth2;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;

/**
 * Created by yasiru on 2/23/16.
 */
public class OAuthPKCEAuthenticationRequestBuilder extends OAuthClientRequest.AuthenticationRequestBuilder   {
    public OAuthPKCEAuthenticationRequestBuilder(String url) {
        super(url);
    }
    public OAuthPKCEAuthenticationRequestBuilder setPKCECodeChallenge(String codeChallenge, String method) {
        this.parameters.put(OAuth2Constants.OAUATH2_PKCE_CODE_CHALLENGE, codeChallenge);
        this.parameters.put(OAuth2Constants.OAUATH2_PKCE_CODE_CHALLENGE_METHOD, method);
        return this;
    }
}
