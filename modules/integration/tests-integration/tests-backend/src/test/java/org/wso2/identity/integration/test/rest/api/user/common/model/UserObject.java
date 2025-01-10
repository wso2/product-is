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

package org.wso2.identity.integration.test.rest.api.user.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserObject {

    private List<String> schemas = null;
    private Name name;
    private String userName;
    private String password;
    private List<Email> emails = null;
    private List<PhoneNumbers> phoneNumbers = null;
    private String locale;
    private ScimSchemaExtensionEnterprise scimSchemaExtensionEnterprise;

    /**
     *
     **/
    public UserObject schemas(List<String> schemas) {

        this.schemas = schemas;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("schemas")
    @Valid
    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    /**
     *
     **/
    public UserObject name(Name name) {

        this.name = name;
        return this;
    }

    @ApiModelProperty(example = "ashan")
    @JsonProperty("name")
    @Valid
    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    /**
     *
     **/
    public UserObject userName(String userName) {

        this.userName = userName;
        return this;
    }

    @ApiModelProperty(example = "abc@wso2.com")
    @JsonProperty("userName")
    @Valid
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     *
     **/
    public UserObject password(String password) {

        this.password = password;
        return this;
    }

    @ApiModelProperty(example = "abc123")
    @JsonProperty("password")
    @Valid
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     **/
    public UserObject emails(List<Email> emails) {

        this.emails = emails;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("emails")
    @Valid
    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }

    public UserObject addEmail(Email email) {
        if (this.emails == null) {
            this.emails = new ArrayList<>();
        }
        this.emails.add(email);
        return this;
    }

    public UserObject phoneNumbers(List<PhoneNumbers> phoneNumbers) {

        this.phoneNumbers = phoneNumbers;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("phoneNumbers")
    @Valid
    public List<PhoneNumbers> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<PhoneNumbers> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public UserObject addPhoneNumbers(PhoneNumbers phoneNumbers) {
        if (this.phoneNumbers == null) {
            this.phoneNumbers = new ArrayList<>();
        }
        this.phoneNumbers.add(phoneNumbers);
        return this;
    }

    /**
     *
     **/
    public UserObject locale(String locale) {

        this.locale = locale;
        return this;
    }

    @ApiModelProperty(example = "en_US")
    @JsonProperty("locale")
    @Valid
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     *
     **/
    public UserObject scimSchemaExtensionEnterprise(ScimSchemaExtensionEnterprise scimSchemaExtensionEnterprise) {

        this.scimSchemaExtensionEnterprise = scimSchemaExtensionEnterprise;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User")
    @Valid
    public ScimSchemaExtensionEnterprise getScimSchemaExtensionEnterprise() {
        return scimSchemaExtensionEnterprise;
    }

    public void setScimSchemaExtensionEnterprise(ScimSchemaExtensionEnterprise scimSchemaExtensionEnterprise) {
        this.scimSchemaExtensionEnterprise = scimSchemaExtensionEnterprise;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserObject user = (UserObject) o;
        return Objects.equals(this.schemas, user.schemas) &&
                Objects.equals(this.name, user.name) &&
                Objects.equals(this.userName, user.userName) &&
                Objects.equals(this.password, user.password) &&
                Objects.equals(this.emails, user.emails) &&
                Objects.equals(this.locale, user.locale) &&
                Objects.equals(this.scimSchemaExtensionEnterprise, user.scimSchemaExtensionEnterprise);

    }

    @Override
    public int hashCode() {
        return Objects.hash(schemas, name, userName, password, emails, locale, scimSchemaExtensionEnterprise);
    }

    @Override
    public String toString() {

        return "class UserObject {\n" +
                "    schemas: " + toIndentedString(schemas) + "\n" +
                "    name: " + toIndentedString(name) + "\n" +
                "    userName: " + toIndentedString(userName) + "\n" +
                "    password: " + toIndentedString(password) + "\n" +
                "    emails: " + toIndentedString(emails) + "\n" +
                "    locale: " + toIndentedString(locale) + "\n" +
                "    urn:ietf:params:scim:schemas:extension:enterprise:2.0:User: " + toIndentedString(scimSchemaExtensionEnterprise) + "\n" +
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
