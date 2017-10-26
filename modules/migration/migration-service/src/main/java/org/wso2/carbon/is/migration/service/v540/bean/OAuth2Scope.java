package org.wso2.carbon.is.migration.service.v540.bean;

public class OAuth2Scope {

    private String scopeId;
    private String roleString;

    private String scopeKey;
    private String name;
    private String description;

    public OAuth2Scope(String scopeId, String roleString, String scopeKey, String name, String description) {
        this.scopeId = scopeId;
        this.roleString = roleString;
        this.scopeKey = scopeKey;
        this.name = name;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoleString() {
        return roleString;
    }

    public void setRoleString(String roleString) {
        this.roleString = roleString;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public String getScopeKey() {
        return scopeKey;
    }

    public void setScopeKey(String scopeKey) {
        this.scopeKey = scopeKey;
    }
}
