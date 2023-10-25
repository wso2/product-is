/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;

public class IdTokenConfiguration  {
  
    private Long expiryInSeconds;
    private List<String> audience = null;
    private String idTokenSignedResponseAlg;


    private IdTokenEncryptionConfiguration encryption;

    /**
    **/
    public IdTokenConfiguration expiryInSeconds(Long expiryInSeconds) {

        this.expiryInSeconds = expiryInSeconds;
        return this;
    }
    
    @ApiModelProperty(example = "3600", value = "")
    @JsonProperty("expiryInSeconds")
    @Valid
    public Long getExpiryInSeconds() {
        return expiryInSeconds;
    }
    public void setExpiryInSeconds(Long expiryInSeconds) {
        this.expiryInSeconds = expiryInSeconds;
    }

    /**
    **/
    public IdTokenConfiguration audience(List<String> audience) {

        this.audience = audience;
        return this;
    }
    
    @ApiModelProperty(example = "[\"http://idp.xyz.com\",\"http://idp.abc.com\"]", value = "")
    @JsonProperty("audience")
    @Valid
    public List<String> getAudience() {
        return audience;
    }
    public void setAudience(List<String> audience) {
        this.audience = audience;
    }

    public IdTokenConfiguration addAudienceItem(String audienceItem) {
        if (this.audience == null) {
            this.audience = new ArrayList<>();
        }
        this.audience.add(audienceItem);
        return this;
    }

    /**
     **/
    public IdTokenConfiguration idTokenSignedResponseAlg(String idTokenSignedResponseAlg) {

        this.idTokenSignedResponseAlg = idTokenSignedResponseAlg;
        return this;
    }

    @ApiModelProperty(example = "PS256", value = "")
    @JsonProperty("idTokenSignedResponseAlg")
    @Valid
    public String getIdTokenSignedResponseAlg() {
        return idTokenSignedResponseAlg;
    }
    public void setIdTokenSignedResponseAlg(String idTokenSignedResponseAlg) {
        this.idTokenSignedResponseAlg = idTokenSignedResponseAlg;
    }


    /**
    **/
    public IdTokenConfiguration encryption(IdTokenEncryptionConfiguration encryption) {

        this.encryption = encryption;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("encryption")
    @Valid
    public IdTokenEncryptionConfiguration getEncryption() {
        return encryption;
    }
    public void setEncryption(IdTokenEncryptionConfiguration encryption) {
        this.encryption = encryption;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdTokenConfiguration idTokenConfiguration = (IdTokenConfiguration) o;
        return Objects.equals(this.expiryInSeconds, idTokenConfiguration.expiryInSeconds) &&
                Objects.equals(this.audience, idTokenConfiguration.audience) &&
                Objects.equals(this.idTokenSignedResponseAlg, idTokenConfiguration.idTokenSignedResponseAlg) &&
                Objects.equals(this.encryption, idTokenConfiguration.encryption);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expiryInSeconds, audience,idTokenSignedResponseAlg, encryption);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class IdTokenConfiguration {\n");

        sb.append("    expiryInSeconds: ").append(toIndentedString(expiryInSeconds)).append("\n");
        sb.append("    audience: ").append(toIndentedString(audience)).append("\n");
        sb.append("    encryption: ").append(toIndentedString(encryption)).append("\n");
        sb.append("    idTokenSignedResponseAlg: ").append(toIndentedString(idTokenSignedResponseAlg)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
    * Convert the given object to string with each line indented by 4 spaces
    * (except the first line).
    */
    private String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

