/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.email.template.v2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SimpleEmailTemplate  {
  
    private String locale;
    private String self;

    /**
    * Unique ID of the email template. This id represents the locale of the email template.
    **/
    public SimpleEmailTemplate locale(String locale) {

        this.locale = locale;
        return this;
    }
    
    @ApiModelProperty(example = "en_US", required = true, value = "Unique ID of the email template. This id represents the locale of the email template.")
    @JsonProperty("locale")
    @Valid
    @NotNull(message = "Property id cannot be null.")

    public String getLocale() {
        return locale;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
    * Location of the created/updated resource.
    **/
    public SimpleEmailTemplate self(String self) {

        this.self = self;
        return this;
    }
    
    @ApiModelProperty(example = "/t/{tenant-domain}/api/server/v2/email/template-types/YWNjb3VudGNvbmZpcm1hdGlvbg/org-templates/en_US", required = true, value = "Location of the created/updated resource.")
    @JsonProperty("self")
    @Valid
    @NotNull(message = "Property self cannot be null.")

    public String getSelf() {
        return self;
    }
    public void setSelf(String self) {
        this.self = self;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleEmailTemplate simpleEmailTemplate = (SimpleEmailTemplate) o;
        return Objects.equals(this.locale, simpleEmailTemplate.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale, self);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class SimpleEmailTemplate {\n");

        sb.append("    locale: ").append(toIndentedString(locale)).append("\n");
        sb.append("    self: ").append(toIndentedString(self)).append("\n");
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

