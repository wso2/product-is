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

package org.wso2.identity.integration.test.rest.api.server.email.template.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class EmailTemplateType {
  
    private String displayName;
    private List<EmailTemplateWithID> templates = new ArrayList<>();


    /**
    * Display name of the email template type.
    **/
    public EmailTemplateType displayName(String displayName) {

        this.displayName = displayName;
        return this;
    }
    
    @ApiModelProperty(example = "Account Confirmation", required = true, value = "Display name of the email template type.")
    @JsonProperty("displayName")
    @Valid
    @NotNull(message = "Property displayName cannot be null.")

    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
    * Email templates for the template type.
    **/
    public EmailTemplateType templates(List<EmailTemplateWithID> templates) {

        this.templates = templates;
        return this;
    }
    
    @ApiModelProperty(required = true, value = "Email templates for the template type.")
    @JsonProperty("templates")
    @Valid
    @NotNull(message = "Property templates cannot be null.")

    public List<EmailTemplateWithID> getTemplates() {
        return templates;
    }
    public void setTemplates(List<EmailTemplateWithID> templates) {
        this.templates = templates;
    }

    public EmailTemplateType addTemplatesItem(EmailTemplateWithID templatesItem) {
        this.templates.add(templatesItem);
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
        EmailTemplateType emailTemplateType = (EmailTemplateType) o;
        return Objects.equals(this.displayName, emailTemplateType.displayName) &&
            Objects.equals(this.templates, emailTemplateType.templates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, templates);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class EmailTemplateType {\n");

        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    templates: ").append(toIndentedString(templates)).append("\n");
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

