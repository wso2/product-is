/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.vc.template.management.v1.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Response model of a VC template.
 */
public class VCTemplate {

    private String id;
    private String identifier;
    private String displayName;
    private String format;
    private List<Claim> claims = new ArrayList<>();
    private Integer expiresIn;
    private String description;
    private String offerId;

    public String getId() {

        return id;
    }

    public VCTemplate id(String id) {

        this.id = id;
        return this;
    }

    public String getIdentifier() {

        return identifier;
    }

    public VCTemplate identifier(String identifier) {

        this.identifier = identifier;
        return this;
    }

    public String getDisplayName() {

        return displayName;
    }

    public VCTemplate displayName(String displayName) {

        this.displayName = displayName;
        return this;
    }

    public String getFormat() {

        return format;
    }

    public VCTemplate format(String format) {

        this.format = format;
        return this;
    }

    public List<Claim> getClaims() {

        return claims;
    }

    public VCTemplate claims(List<Claim> claims) {

        this.claims = claims;
        return this;
    }

    public Integer getExpiresIn() {

        return expiresIn;
    }

    public VCTemplate expiresIn(Integer expiresIn) {

        this.expiresIn = expiresIn;
        return this;
    }

    public String getDescription() {

        return description;
    }

    public VCTemplate description(String description) {

        this.description = description;
        return this;
    }

    public String getOfferId() {

        return offerId;
    }

    public VCTemplate offerId(String offerId) {

        this.offerId = offerId;
        return this;
    }
}
