/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * A message surfaced to the user during a flow step (e.g. validation or status messages).
 **/
public class Message  {

    @XmlType(name="TypeEnum")
    @XmlEnum(String.class)
    public enum TypeEnum {

        @XmlEnumValue("ERROR") ERROR(String.valueOf("ERROR")),
        @XmlEnumValue("WARNING") WARNING(String.valueOf("WARNING")),
        @XmlEnumValue("INFO") INFO(String.valueOf("INFO"));


        private String value;

        TypeEnum(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static TypeEnum fromValue(String value) {
            for (TypeEnum b : TypeEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private TypeEnum type;
    private String message;
    private String i18nKey;

    /**
     **/
    public Message type(TypeEnum type) {

        this.type = type;
        return this;
    }

    @ApiModelProperty(example = "ERROR", value = "")
    @JsonProperty("type")
    @Valid
    public TypeEnum getType() {
        return type;
    }
    public void setType(TypeEnum type) {
        this.type = type;
    }

    /**
     **/
    public Message message(String message) {

        this.message = message;
        return this;
    }

    @ApiModelProperty(example = "The provided identifier is invalid.", value = "")
    @JsonProperty("message")
    @Valid
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     **/
    public Message i18nKey(String i18nKey) {

        this.i18nKey = i18nKey;
        return this;
    }

    @ApiModelProperty(example = "invalid.identifier", value = "")
    @JsonProperty("i18nKey")
    @Valid
    public String getI18nKey() {
        return i18nKey;
    }
    public void setI18nKey(String i18nKey) {
        this.i18nKey = i18nKey;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Message message = (Message) o;
        return Objects.equals(this.type, message.type) &&
                Objects.equals(this.message, message.message) &&
                Objects.equals(this.i18nKey, message.i18nKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, message, i18nKey);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class Message {\n");

        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
        sb.append("    i18nKey: ").append(toIndentedString(i18nKey)).append("\n");
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
