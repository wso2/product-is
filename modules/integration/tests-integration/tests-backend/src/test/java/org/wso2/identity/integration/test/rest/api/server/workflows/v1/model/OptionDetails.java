package org.wso2.identity.integration.test.rest.api.server.workflows.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class OptionDetails  {

    private String entity;
    private List<String> values = null;


    /**
     **/
    public OptionDetails entity(String entity) {

        this.entity = entity;
        return this;
    }

    @ApiModelProperty(example = "roles", value = "")
    @JsonProperty("entity")
    @Valid
    public String getEntity() {
        return entity;
    }
    public void setEntity(String entity) {
        this.entity = entity;
    }

    /**
     **/
    public OptionDetails values(List<String> values) {

        this.values = values;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("values")
    @Valid
    public List<String> getValues() {
        return values;
    }
    public void setValues(List<String> values) {
        this.values = values;
    }

    public OptionDetails addValuesItem(String valuesItem) {
        if (this.values == null) {
            this.values = new ArrayList<>();
        }
        this.values.add(valuesItem);
        return this;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OptionDetails optionDetails = (OptionDetails) o;
        return Objects.equals(this.entity, optionDetails.entity) &&
                Objects.equals(this.values, optionDetails.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity, values);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class OptionDetails {\n");

        sb.append("    entity: ").append(toIndentedString(entity)).append("\n");
        sb.append("    values: ").append(toIndentedString(values)).append("\n");
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


