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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;

public class InboundProtocolsListResponse {

    private List<InboundProtocolListItem> inboundProtocols = null;

    /**
    **/
    public InboundProtocolsListResponse inboundProtocols(List<InboundProtocolListItem> inboundProtocols) {

        this.inboundProtocols = inboundProtocols;
        return this;
    }
    
    @ApiModelProperty()
    @JsonProperty("inboundProtocols")
    @Valid
    public List<InboundProtocolListItem> getInboundProtocols() {
        return inboundProtocols;
    }
    public void setInboundProtocols(List<InboundProtocolListItem> inboundProtocols) {
        this.inboundProtocols = inboundProtocols;
    }

    public InboundProtocolsListResponse addInboundProtocols(InboundProtocolListItem inboundProtocolItem) {
        if (this.inboundProtocols == null) {
            this.inboundProtocols = new ArrayList<>();
        }
        this.inboundProtocols.add(inboundProtocolItem);
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
        InboundProtocolsListResponse inboundProtocolListItem = (InboundProtocolsListResponse) o;
        return Objects.equals(this.inboundProtocols, inboundProtocolListItem.inboundProtocols);
    }

    @Override
    public int hashCode() { return Objects.hash(inboundProtocols); }

    @Override
    public String toString() {

        return "class InboundProtocolsListResponse {\n" +
                    "    InboundProtocolListItem: " + toIndentedString(inboundProtocols) + "\n" +
                    "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString();
    }

}
