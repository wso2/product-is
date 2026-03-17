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

package org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Request body for PATCH /users/shared-organizations (V2 role update on shared users).
 * <p>
 * Each {@link PatchOperation} targets a specific organization by ID and specifies roles to
 * add or remove for the given users. Multiple operations may be included in a single request.
 */
@ApiModel(description = "Request body for the V2 shared organization role update (PATCH) endpoint.")
public class UserSharePatchRequestBody {

    private UserShareRequestBodyUserCriteria userCriteria;
    @SerializedName("Operations")
    private List<PatchOperation> operations = new ArrayList<>();

    public UserSharePatchRequestBody userCriteria(UserShareRequestBodyUserCriteria userCriteria) {

        this.userCriteria = userCriteria;
        return this;
    }

    @ApiModelProperty(required = true, value = "User criteria specifying the shared users whose roles are updated.")
    @JsonProperty("userCriteria")
    @Valid
    @NotNull(message = "Property userCriteria cannot be null.")
    public UserShareRequestBodyUserCriteria getUserCriteria() {
        return userCriteria;
    }

    public void setUserCriteria(UserShareRequestBodyUserCriteria userCriteria) {
        this.userCriteria = userCriteria;
    }

    public UserSharePatchRequestBody operations(List<PatchOperation> operations) {

        this.operations = operations;
        return this;
    }

    @ApiModelProperty(required = true, value = "List of patch operations to apply.")
    @JsonProperty("Operations")
    @Valid
    @NotNull(message = "Property Operations cannot be null.")
    public List<PatchOperation> getPatchOperations() {
        return operations;
    }

    public void setPatchOperations(List<PatchOperation> operations) {
        this.operations = operations;
    }

    public UserSharePatchRequestBody addPatchOperationsItem(PatchOperation operation) {

        this.operations.add(operation);
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
        UserSharePatchRequestBody that = (UserSharePatchRequestBody) o;
        return Objects.equals(this.userCriteria, that.userCriteria) &&
                Objects.equals(this.operations, that.operations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userCriteria, operations);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class UserSharePatchRequestBody {\n");
        sb.append("    userCriteria: ").append(toIndentedString(userCriteria)).append("\n");
        sb.append("    operations: ").append(toIndentedString(operations)).append("\n");
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
