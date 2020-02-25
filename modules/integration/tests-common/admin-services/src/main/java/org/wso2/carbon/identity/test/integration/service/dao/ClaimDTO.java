/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.test.integration.service.dao;

/**
 * This encapsulates the claim value data.
 */
public class ClaimDTO {

    private String claimUri;

    private String displayTag;

    private String description;

    private boolean supportedByDefault;

    private boolean required;

    private String regEx;

    private String dialectURI;

    private String value;

    private int displayOrder;

    public String getClaimUri() {
        return claimUri;
    }

    public void setClaimUri(String claimUri) {
        this.claimUri = claimUri;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getDialectURI() {
        return dialectURI;
    }

    public void setDialectURI(String dialectURI) {
        this.dialectURI = dialectURI;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getRegEx() {
        return regEx;
    }

    public void setRegEx(String regEx) {
        this.regEx = regEx;
    }

    public boolean isSupportedByDefault() {
        return supportedByDefault;
    }

    public void setSupportedByDefault(boolean supportedByDefault) {
        this.supportedByDefault = supportedByDefault;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayTag() {
        return displayTag;
    }

    public void setDisplayTag(String displayTag) {
        this.displayTag = displayTag;
    }
}
