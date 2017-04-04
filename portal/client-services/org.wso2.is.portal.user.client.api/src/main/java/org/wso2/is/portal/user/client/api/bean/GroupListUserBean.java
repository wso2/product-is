package org.wso2.is.portal.user.client.api.bean;

/**
 * Bean class used with group listing
 */
public class GroupListUserBean {
    private String username;
    private String uid;
    private String picture;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
