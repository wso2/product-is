/*
 * Copyright (c) 2024-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdatepassword.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionUpdateModel;

import java.util.Objects;

import javax.validation.Valid;

/**
 * Pre update password action update model.
 **/
public class PreUpdatePasswordActionUpdateModel extends ActionUpdateModel {

    private PasswordSharingUpdateModel passwordSharing;

    public PreUpdatePasswordActionUpdateModel() {
        // Default constructor required for Jackson
    }

    public PreUpdatePasswordActionUpdateModel(ActionUpdateModel actionUpdateModel) {

        setName(actionUpdateModel.getName());
        setDescription(actionUpdateModel.getDescription());
        setEndpoint(actionUpdateModel.getEndpoint());
        setRule(actionUpdateModel.getRule());
    }

    public PreUpdatePasswordActionUpdateModel passwordSharing(PasswordSharingUpdateModel passwordSharing) {

        this.passwordSharing = passwordSharing;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("passwordSharing")
    @Valid
    public PasswordSharingUpdateModel getPasswordSharing() {

        return passwordSharing;
    }

    public void setPasswordSharing(PasswordSharingUpdateModel passwordSharing) {

        this.passwordSharing = passwordSharing;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PreUpdatePasswordActionUpdateModel actionUpdateModel = (PreUpdatePasswordActionUpdateModel) o;
        return Objects.equals(this.getName(), actionUpdateModel.getName()) &&
                Objects.equals(this.getDescription(), actionUpdateModel.getDescription()) &&
                Objects.equals(this.getEndpoint(), actionUpdateModel.getEndpoint()) &&
                Objects.equals(this.passwordSharing, actionUpdateModel.passwordSharing) &&
                Objects.equals(this.getRule(), actionUpdateModel.getRule());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription(), getEndpoint(), passwordSharing, getRule());
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class PreUpdatePasswordActionModel {\n");
        sb.append("    name: ").append(toIndentedString(getName())).append("\n");
        sb.append("    description: ").append(toIndentedString(getDescription())).append("\n");
        sb.append("    endpoint: ").append(toIndentedString(getEndpoint())).append("\n");
        sb.append("    passwordSharing: ").append(toIndentedString(passwordSharing)).append("\n");
        sb.append("    rule: ").append(toIndentedString(getRule())).append("\n");
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
        return o.toString().replace("\n", "\n    ");
    }
}
