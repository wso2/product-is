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

public class InboundProtocols  {
  
    private SAML2Configuration saml;
    private OpenIDConnectConfiguration oidc;
    private PassiveStsConfiguration passiveSts;
    private WSTrustConfiguration wsTrust;
    private List<CustomInboundProtocolConfiguration> custom = null;


    /**
    **/
    public InboundProtocols saml(SAML2Configuration saml) {

        this.saml = saml;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("saml")
    @Valid
    public SAML2Configuration getSaml() {
        return saml;
    }
    public void setSaml(SAML2Configuration saml) {
        this.saml = saml;
    }

    /**
    **/
    public InboundProtocols oidc(OpenIDConnectConfiguration oidc) {

        this.oidc = oidc;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("oidc")
    @Valid
    public OpenIDConnectConfiguration getOidc() {
        return oidc;
    }
    public void setOidc(OpenIDConnectConfiguration oidc) {
        this.oidc = oidc;
    }

    /**
    **/
    public InboundProtocols passiveSts(PassiveStsConfiguration passiveSts) {

        this.passiveSts = passiveSts;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("passiveSts")
    @Valid
    public PassiveStsConfiguration getPassiveSts() {
        return passiveSts;
    }
    public void setPassiveSts(PassiveStsConfiguration passiveSts) {
        this.passiveSts = passiveSts;
    }

    /**
    **/
    public InboundProtocols wsTrust(WSTrustConfiguration wsTrust) {

        this.wsTrust = wsTrust;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("wsTrust")
    @Valid
    public WSTrustConfiguration getWsTrust() {
        return wsTrust;
    }
    public void setWsTrust(WSTrustConfiguration wsTrust) {
        this.wsTrust = wsTrust;
    }

    /**
    **/
    public InboundProtocols custom(List<CustomInboundProtocolConfiguration> custom) {

        this.custom = custom;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("custom")
    @Valid
    public List<CustomInboundProtocolConfiguration> getCustom() {
        return custom;
    }
    public void setCustom(List<CustomInboundProtocolConfiguration> custom) {
        this.custom = custom;
    }

    public InboundProtocols addCustomItem(CustomInboundProtocolConfiguration customItem) {
        if (this.custom == null) {
            this.custom = new ArrayList<>();
        }
        this.custom.add(customItem);
        return this;
    }

    

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InboundProtocols inboundProtocols = (InboundProtocols) o;
        return Objects.equals(this.saml, inboundProtocols.saml) &&
            Objects.equals(this.oidc, inboundProtocols.oidc) &&
            Objects.equals(this.passiveSts, inboundProtocols.passiveSts) &&
            Objects.equals(this.wsTrust, inboundProtocols.wsTrust) &&
            Objects.equals(this.custom, inboundProtocols.custom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saml, oidc, passiveSts, wsTrust, custom);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class InboundProtocols {\n");

        sb.append("    saml: ").append(toIndentedString(saml)).append("\n");
        sb.append("    oidc: ").append(toIndentedString(oidc)).append("\n");
        sb.append("    passiveSts: ").append(toIndentedString(passiveSts)).append("\n");
        sb.append("    wsTrust: ").append(toIndentedString(wsTrust)).append("\n");
        sb.append("    custom: ").append(toIndentedString(custom)).append("\n");
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

