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

package org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.model;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SMS Sender
 */
public class SMSSender {
    @SerializedName("name")
    private String name = null;

    @SerializedName("provider")
    private String provider = null;

    @SerializedName("providerURL")
    private String providerURL = null;

    @SerializedName("key")
    private String key = null;

    @SerializedName("secret")
    private String secret = null;

    @SerializedName("sender")
    private String sender = null;

    @SerializedName("authentication")
    private Authentication authentication = null;

    /**
     * Gets or Sets contentType
     */
    @JsonAdapter(ContentTypeEnum.Adapter.class)
    public enum ContentTypeEnum {
        @SerializedName("JSON")
        JSON("JSON"),
        @SerializedName("FORM")
        FORM("FORM");

        private String value;

        ContentTypeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static ContentTypeEnum fromValue(String input) {
            for (ContentTypeEnum b : ContentTypeEnum.values()) {
                if (b.value.equals(input)) {
                    return b;
                }
            }
            return null;
        }

        public static class Adapter extends TypeAdapter<ContentTypeEnum> {
            @Override
            public void write(final JsonWriter jsonWriter, final ContentTypeEnum enumeration) throws IOException {
                jsonWriter.value(String.valueOf(enumeration.getValue()));
            }

            @Override
            public ContentTypeEnum read(final JsonReader jsonReader) throws IOException {
                Object value = jsonReader.nextString();
                return ContentTypeEnum.fromValue((String) (value));
            }
        }
    }

    @SerializedName("contentType")
    private ContentTypeEnum contentType = null;

    @SerializedName("properties")
    private List<Properties> properties = null;

    public SMSSender name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
     *
     * @return name
     **/
    @Schema(example = "SMSPublisher", required = true, description = "")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SMSSender provider(String provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Get provider
     *
     * @return provider
     **/
    @Schema(example = "NEXMO", required = true, description = "")
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public SMSSender providerURL(String providerURL) {
        this.providerURL = providerURL;
        return this;
    }

    /**
     * Get providerURL
     *
     * @return providerURL
     **/
    @Schema(example = "https://rest.nexmo.com/sms/json", required = true, description = "")
    public String getProviderURL() {
        return providerURL;
    }

    public void setProviderURL(String providerURL) {
        this.providerURL = providerURL;
    }

    public SMSSender key(String key) {
        this.key = key;
        return this;
    }

    /**
     * Get key
     *
     * @return key
     **/
    @Schema(example = "123**45", description = "")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public SMSSender secret(String secret) {
        this.secret = secret;
        return this;
    }

    /**
     * Get secret
     *
     * @return secret
     **/
    @Schema(example = "5tg**ssd", description = "")
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public SMSSender sender(String sender) {
        this.sender = sender;
        return this;
    }

    /**
     * Get sender
     *
     * @return sender
     **/
    @Schema(example = "+94 775563324", description = "")
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public SMSSender authentication(Authentication authentication) {
        this.authentication = authentication;
        return this;
    }

    /**
     * Get authentication
     *
     * @return authentication
     **/
    @Schema(description = "")
    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public SMSSender contentType(ContentTypeEnum contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Get contentType
     *
     * @return contentType
     **/
    @Schema(required = true, description = "")
    public ContentTypeEnum getContentType() {
        return contentType;
    }

    public void setContentType(ContentTypeEnum contentType) {
        this.contentType = contentType;
    }

    public SMSSender properties(List<Properties> properties) {
        this.properties = properties;
        return this;
    }

    public SMSSender addPropertiesItem(Properties propertiesItem) {
        if (this.properties == null) {
            this.properties = new ArrayList<Properties>();
        }
        this.properties.add(propertiesItem);
        return this;
    }

    /**
     * Get properties
     *
     * @return properties
     **/
    @Schema(example = "[{\"key\":\"body.scope\",\"value\":\"internal\"},{\"key\":\"http.headers\",\"value\":\"X-Version: 1, Authorization: bearer ,Accept: application/json ,Content-Type: application/json\"}]", description = "")
    public List<Properties> getProperties() {
        return properties;
    }

    public void setProperties(List<Properties> properties) {
        this.properties = properties;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SMSSender smSSender = (SMSSender) o;
        return Objects.equals(this.name, smSSender.name) &&
                Objects.equals(this.provider, smSSender.provider) &&
                Objects.equals(this.providerURL, smSSender.providerURL) &&
                Objects.equals(this.key, smSSender.key) &&
                Objects.equals(this.secret, smSSender.secret) &&
                Objects.equals(this.sender, smSSender.sender) &&
                Objects.equals(this.authentication, smSSender.authentication) &&
                Objects.equals(this.contentType, smSSender.contentType) &&
                Objects.equals(this.properties, smSSender.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, provider, providerURL, key, secret, sender, authentication, contentType, properties);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SMSSender {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
        sb.append("    providerURL: ").append(toIndentedString(providerURL)).append("\n");
        sb.append("    key: ").append(toIndentedString(key)).append("\n");
        sb.append("    secret: ").append(toIndentedString(secret)).append("\n");
        sb.append("    sender: ").append(toIndentedString(sender)).append("\n");
        sb.append("    authentication: ").append(toIndentedString(authentication)).append("\n");
        sb.append("    contentType: ").append(toIndentedString(contentType)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
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
        return o.toString().replace("\n", "\n    ");
    }

}
