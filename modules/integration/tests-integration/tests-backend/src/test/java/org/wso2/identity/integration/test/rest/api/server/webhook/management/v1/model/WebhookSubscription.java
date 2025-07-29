/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.webhook.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

public class WebhookSubscription  {
  
    private String channelUri;

@XmlType(name="StatusEnum")
@XmlEnum(String.class)
public enum StatusEnum {

    @XmlEnumValue("SUBSCRIPTION_ACCEPTED") SUBSCRIPTION_ACCEPTED(String.valueOf("SUBSCRIPTION_ACCEPTED")), @XmlEnumValue("SUBSCRIPTION_PENDING") SUBSCRIPTION_PENDING(String.valueOf("SUBSCRIPTION_PENDING")), @XmlEnumValue("SUBSCRIPTION_ERROR") SUBSCRIPTION_ERROR(String.valueOf("SUBSCRIPTION_ERROR")), @XmlEnumValue("UNSUBSCRIPTION_ACCEPTED") UNSUBSCRIPTION_ACCEPTED(String.valueOf("UNSUBSCRIPTION_ACCEPTED")), @XmlEnumValue("UNSUBSCRIPTION_PENDING") UNSUBSCRIPTION_PENDING(String.valueOf("UNSUBSCRIPTION_PENDING")), @XmlEnumValue("UNSUBSCRIPTION_ERROR") UNSUBSCRIPTION_ERROR(String.valueOf("UNSUBSCRIPTION_ERROR"));


    private String value;

    StatusEnum(String v) {
        value = v;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static StatusEnum fromValue(String value) {
        for (StatusEnum b : StatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

    private StatusEnum status;

    /**
    * Channel URI to subscribe to.
    **/
    public WebhookSubscription channelUri(String channelUri) {

        this.channelUri = channelUri;
        return this;
    }
    
    @ApiModelProperty(example = "https://schemas.identity.wso2.org/events/login", value = "Channel URI to subscribe to.")
    @JsonProperty("channelUri")
    @Valid
    public String getChannelUri() {
        return channelUri;
    }
    public void setChannelUri(String channelUri) {
        this.channelUri = channelUri;
    }

    /**
    * Status of the subscription.
    **/
    public WebhookSubscription status(StatusEnum status) {

        this.status = status;
        return this;
    }
    
    @ApiModelProperty(example = "SUBSCRIPTION_PENDING", value = "Status of the subscription.")
    @JsonProperty("status")
    @Valid
    public StatusEnum getStatus() {
        return status;
    }
    public void setStatus(StatusEnum status) {
        this.status = status;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WebhookSubscription webhookSubscription = (WebhookSubscription) o;
        return Objects.equals(this.channelUri, webhookSubscription.channelUri) &&
            Objects.equals(this.status, webhookSubscription.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelUri, status);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class WebhookSubscription {\n");
        
        sb.append("    channelUri: ").append(toIndentedString(channelUri)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

