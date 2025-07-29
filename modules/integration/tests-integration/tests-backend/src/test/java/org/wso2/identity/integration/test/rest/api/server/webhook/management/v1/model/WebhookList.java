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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

public class WebhookList  {
  
    private List<WebhookSummary> webhooks = null;


    /**
    **/
    public WebhookList webhooks(List<WebhookSummary> webhooks) {

        this.webhooks = webhooks;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("webhooks")
    @Valid
    public List<WebhookSummary> getWebhooks() {
        return webhooks;
    }
    public void setWebhooks(List<WebhookSummary> webhooks) {
        this.webhooks = webhooks;
    }

    public WebhookList addWebhooksItem(WebhookSummary webhooksItem) {
        if (this.webhooks == null) {
            this.webhooks = new ArrayList<WebhookSummary>();
        }
        this.webhooks.add(webhooksItem);
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
        WebhookList webhookList = (WebhookList) o;
        return Objects.equals(this.webhooks, webhookList.webhooks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(webhooks);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class WebhookList {\n");
        
        sb.append("    webhooks: ").append(toIndentedString(webhooks)).append("\n");
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

