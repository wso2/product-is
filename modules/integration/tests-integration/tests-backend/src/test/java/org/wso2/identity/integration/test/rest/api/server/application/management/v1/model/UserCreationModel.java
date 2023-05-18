package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

public class UserCreationModel {

    private String[] schemas = new String[1];
    private String givenName;
    private String familyName;
    private String userName;
    private String password;
    private String homeEmail;
    private String workEmail;

    public String[] getSchemas() {
        return schemas;
    }
    public void setSchemas(String[] schemas) {
        this.schemas = schemas;
    }

    public String getGivenName() {
        return givenName;
    }
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getHomeEmail() {
        return homeEmail;
    }
    public void setHomeEmail(String homeEmail) {
        this.homeEmail = homeEmail;
    }

    public String getWorkEmail() {
        return workEmail;
    }
    public void setWorkEmail(String workEmail) {
        this.workEmail = workEmail;
    }
}

