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

package org.wso2.identity.integration.test.rest.api.server.notification.template.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class EmailTemplate  {

    private String contentType;
    private String subject;
    private String body;
    private String footer;

    /**
     * Content type of the email template.
     **/
    public EmailTemplate contentType(String contentType) {

        this.contentType = contentType;
        return this;
    }

    @ApiModelProperty(example = "text/html", required = true, value = "Content type of the email template.")
    @JsonProperty("contentType")
    @Valid
    @NotNull(message = "Property contentType cannot be null.")

    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * The subject of the email.
     **/
    public EmailTemplate subject(String subject) {

        this.subject = subject;
        return this;
    }

    @ApiModelProperty(example = "WSO2 - Account Confirmation", required = true, value = "The subject of the email.")
    @JsonProperty("subject")
    @Valid
    @NotNull(message = "Property subject cannot be null.")

    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * The body of the email.
     **/
    public EmailTemplate body(String body) {

        this.body = body;
        return this;
    }

    @ApiModelProperty(example = "HTML Body", required = true, value = "The body of the email.")
    @JsonProperty("body")
    @Valid
    @NotNull(message = "Property body cannot be null.")

    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * The footer of the email.
     **/
    public EmailTemplate footer(String footer) {

        this.footer = footer;
        return this;
    }

    @ApiModelProperty(example = "WSO2 Identity Server Team", value = "The footer of the email.")
    @JsonProperty("footer")
    @Valid
    public String getFooter() {
        return footer;
    }
    public void setFooter(String footer) {
        this.footer = footer;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmailTemplate emailTemplate = (EmailTemplate) o;
        return Objects.equals(this.contentType, emailTemplate.contentType) &&
                Objects.equals(this.subject, emailTemplate.subject) &&
                Objects.equals(this.body, emailTemplate.body) &&
                Objects.equals(this.footer, emailTemplate.footer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentType, subject, body, footer);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class EmailTemplate {\n");

        sb.append("    contentType: ").append(toIndentedString(contentType)).append("\n");
        sb.append("    subject: ").append(toIndentedString(subject)).append("\n");
        sb.append("    body: ").append(toIndentedString(body)).append("\n");
        sb.append("    footer: ").append(toIndentedString(footer)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}
