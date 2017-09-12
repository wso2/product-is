package org.wso2.carbon.is.migration.service.v540.bean;


public class OAuth2ScopeBinding {
    private String scopeId;
    private String scopeBinding;

    public OAuth2ScopeBinding(String scopeId, String scopeBinding) {
        this.scopeId = scopeId;
        this.scopeBinding = scopeBinding;
    }

    public String getScopeBinding() {
        return scopeBinding;
    }

    public void setScopeBinding(String scopeBinding) {
        this.scopeBinding = scopeBinding;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }
}
