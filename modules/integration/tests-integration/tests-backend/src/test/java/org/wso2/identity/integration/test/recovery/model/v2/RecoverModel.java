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

package org.wso2.identity.integration.test.recovery.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

import java.util.Objects;

public class RecoverModel {

    private String recoveryCode;
    private String channelId;

    public RecoverModel recoveryCode(String recoveryCode) {

        this.recoveryCode = recoveryCode;
        return this;
    }

    @ApiModelProperty(example = "9588245c-5d51-4cfa-b8d9-f031f03294af", required = true)
    @JsonProperty("recoveryCode")
    @NotNull(message = "Recovery code cannot be blank.")
    public String getRecoveryCode() {

        return recoveryCode;
    }

    public void setRecoveryCode(String recoveryCode) {

        this.recoveryCode = recoveryCode;
    }

    public RecoverModel channelId(String channelId) {

        this.channelId = channelId;
        return this;
    }

    @ApiModelProperty(example = "2", required = true)
    @JsonProperty("channelId")
    @NotNull(message = "Channel ID cannot be blank.")
    public String getChannelId() {

        return channelId;
    }

    public void setChannelId(String channelId) {

        this.channelId = channelId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecoverModel that = (RecoverModel) o;
        return Objects.equals(recoveryCode, that.recoveryCode) &&
                Objects.equals(channelId, that.channelId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(recoveryCode, channelId);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RecoverModel {\n");
        sb.append("    recoveryCode: ").append(toIndentedString(recoveryCode)).append("\n");
        sb.append("    channelId: ").append(toIndentedString(channelId)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
