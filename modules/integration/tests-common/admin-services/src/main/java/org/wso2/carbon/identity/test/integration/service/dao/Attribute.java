package org.wso2.carbon.identity.test.integration.service.dao;

import java.io.Serializable;

public class Attribute implements Serializable {

    private String attributeName;
    private String attributeValue;

    public String getAttributeName() {

        return attributeName;
    }

    public void setAttributeName(String attributeName) {

        this.attributeName = attributeName;
    }

    public String getAttributeValue() {

        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {

        this.attributeValue = attributeValue;
    }
}
