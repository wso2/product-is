/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

public class SingleLogoutProfile {

    private Boolean enabled;
    private String logoutRequestUrl;
    private String logoutResponseUrl;

    @XmlType(name="LOGOUTMETHODEnum")
    @XmlEnum()
    public enum LOGOUTMETHODEnum {

        @XmlEnumValue("BACKCHANNEL") BACKCHANNEL("BACKCHANNEL"),
        @XmlEnumValue("FRONTCHANNEL_HTTP_REDIRECT") FRONTCHANNEL_HTTP_REDIRECT("FRONTCHANNEL_HTTP_REDIRECT"),
        @XmlEnumValue("FRONTCHANNEL_HTTP_POST") FRONTCHANNEL_HTTP_POST("FRONTCHANNEL_HTTP_POST");

        private final String value;

        LOGOUTMETHODEnum(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static LOGOUTMETHODEnum fromValue(String value) {
            for (LOGOUTMETHODEnum b : LOGOUTMETHODEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private LOGOUTMETHODEnum logoutMethod;
    private IdpInitiatedSingleLogout idpInitiatedSingleLogout;

    /**
     *
     **/
    public SingleLogoutProfile enabled(Boolean enabled) {

        this.enabled = enabled;
        return this;
    }

    @ApiModelProperty(example = "false")
    @JsonProperty("enabled")
    @Valid
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     *
     **/
    public SingleLogoutProfile logoutRequestUrl(String logoutRequestUrl) {

        this.logoutRequestUrl = logoutRequestUrl;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("logoutRequestUrl")
    @Valid
    public String getLogoutRequestUrl() {
        return logoutRequestUrl;
    }

    public void setLogoutRequestUrl(String logoutRequestUrl) {
        this.logoutRequestUrl = logoutRequestUrl;
    }

    /**
     *
     **/
    public SingleLogoutProfile logoutResponseUrl(String logoutResponseUrl) {

        this.logoutResponseUrl = logoutResponseUrl;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("logoutResponseUrl")
    @Valid
    public String getLogoutResponseUrl() {
        return logoutResponseUrl;
    }

    public void setLogoutResponseUrl(String logoutResponseUrl) {
        this.logoutResponseUrl = logoutResponseUrl;
    }

    /**
     *
     **/
    public SingleLogoutProfile logoutMethod(LOGOUTMETHODEnum logoutMethod) {

        this.logoutMethod = logoutMethod;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("logoutMethod")
    @Valid
    public LOGOUTMETHODEnum getLogoutMethod() {
        return logoutMethod;
    }

    public void setLogoutMethod(LOGOUTMETHODEnum logoutMethod) {
        this.logoutMethod = logoutMethod;
    }

    /**
     *
     **/
    public SingleLogoutProfile idpInitiatedSingleLogout(IdpInitiatedSingleLogout idpInitiatedSingleLogout) {

        this.idpInitiatedSingleLogout = idpInitiatedSingleLogout;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("idpInitiatedSingleLogout")
    @Valid
    public IdpInitiatedSingleLogout getIdpInitiatedSingleLogout() {
        return idpInitiatedSingleLogout;
    }

    public void setIdpInitiatedSingleLogout(IdpInitiatedSingleLogout idpInitiatedSingleLogout) {
        this.idpInitiatedSingleLogout = idpInitiatedSingleLogout;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SingleLogoutProfile singleLogoutProfile = (SingleLogoutProfile) o;
        return Objects.equals(this.enabled, singleLogoutProfile.enabled) &&
                Objects.equals(this.logoutRequestUrl, singleLogoutProfile.logoutRequestUrl) &&
                Objects.equals(this.logoutResponseUrl, singleLogoutProfile.logoutResponseUrl) &&
                Objects.equals(this.logoutMethod, singleLogoutProfile.logoutMethod) &&
                Objects.equals(this.idpInitiatedSingleLogout, singleLogoutProfile.idpInitiatedSingleLogout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, logoutRequestUrl, logoutResponseUrl, logoutMethod, idpInitiatedSingleLogout);
    }

    @Override
    public String toString() {

        return "class SingleLogoutProfile {\n" +
                "    enabled: " + toIndentedString(enabled) + "\n" +
                "    logoutRequestUrl: " + toIndentedString(logoutRequestUrl) + "\n" +
                "    logoutResponseUrl: " + toIndentedString(logoutResponseUrl) + "\n" +
                "    logoutMethod: " + toIndentedString(logoutMethod) + "\n" +
                "    idpInitiatedSingleLogout: " + toIndentedString(idpInitiatedSingleLogout) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString();

    }
}
