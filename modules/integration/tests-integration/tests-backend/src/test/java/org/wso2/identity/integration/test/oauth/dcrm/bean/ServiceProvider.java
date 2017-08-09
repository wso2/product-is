package org.wso2.identity.integration.test.oauth.dcrm.bean;

import java.util.ArrayList;
import java.util.List;

public class ServiceProvider {
    private String clientName;
    private String clientID;
    private String clientSecret;

    List<String> grantTypes = new ArrayList<>();
    List<String> redirectURIs = new ArrayList<>();

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public List getGrantTypes() {
        return grantTypes;
    }

    public void addGrantType(String grantType) {
        grantTypes.add(grantType);
    }

    public List getRedirectUris() {
        return redirectURIs;
    }

    public void addRedirectUri(String redirectURI) {
        redirectURIs.add(redirectURI);
    }
}
