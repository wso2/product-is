/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.is.migration.service.v530.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Claim  representation for a claim.
 */
public class Claim {
    private String claimURI;
    private String displayTag;
    private String description;
    private List<MappedAttribute> attributes;
    private String regEx;
    private boolean supportedByDefault;
    private boolean required;
    private int displayOrder;
    private boolean readOnly;
    private int tenantId;
    private String dialectURI;

    public Claim() {

    }

    public Claim(String claimURI, String displayTag, String description, String regEx, boolean supportedByDefault,
                 boolean required, int displayOrder, boolean readOnly, int tenantId, String dialectURI) {
        this.claimURI = claimURI;
        this.displayTag = displayTag;
        this.description = description;
        this.regEx = regEx;
        this.supportedByDefault = supportedByDefault;
        this.required = required;
        this.displayOrder = displayOrder;
        this.readOnly = readOnly;
        this.tenantId = tenantId;
        this.dialectURI = dialectURI;
    }

    public List<MappedAttribute> getAttributes() {
        if (attributes == null) {
            return new ArrayList<>();
        }
        return attributes;
    }

    public void setAttributes(List<MappedAttribute> attributes) {
        this.attributes = attributes;
    }

    public String getClaimURI() {
        return claimURI;
    }

    public void setClaimURI(String claimURI) {
        this.claimURI = claimURI;
    }

    public String getDisplayTag() {
        return displayTag;
    }

    public void setDisplayTag(String displayTag) {
        this.displayTag = displayTag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getDialectURI() {
        return dialectURI;
    }

    public void setDialectURI(String dialectURI) {
        this.dialectURI = dialectURI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Claim claim = (Claim) o;

        if (tenantId != claim.tenantId) return false;
        if (!claimURI.equals(claim.claimURI)) return false;
        return dialectURI.equals(claim.dialectURI);

    }

    @Override
    public int hashCode() {
        int result = claimURI.hashCode();
        result = 31 * result + tenantId;
        result = 31 * result + dialectURI.hashCode();
        return result;
    }
}
