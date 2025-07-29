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
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class WebhookRequestEventProfile  {
  
    private String name;
    private String uri;

    /**
    * Webhook Event Profile.
    **/
    public WebhookRequestEventProfile name(String name) {

        this.name = name;
        return this;
    }
    
    @ApiModelProperty(example = "WSO2", required = true, value = "Webhook Event Profile.")
    @JsonProperty("name")
    @Valid
    @NotNull(message = "Property name cannot be null.")

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
    * Webhook Event Profile URI.
    **/
    public WebhookRequestEventProfile uri(String uri) {

        this.uri = uri;
        return this;
    }
    
    @ApiModelProperty(example = "https://schemas.identity.wso2.org/events", required = true, value = "Webhook Event Profile URI.")
    @JsonProperty("uri")
    @Valid
    @NotNull(message = "Property uri cannot be null.")

    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WebhookRequestEventProfile webhookRequestEventProfile = (WebhookRequestEventProfile) o;
        return Objects.equals(this.name, webhookRequestEventProfile.name) &&
            Objects.equals(this.uri, webhookRequestEventProfile.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uri);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class WebhookRequestEventProfile {\n");
        
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    uri: ").append(toIndentedString(uri)).append("\n");
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

