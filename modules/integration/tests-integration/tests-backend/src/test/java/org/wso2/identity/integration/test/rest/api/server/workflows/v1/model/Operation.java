package org.wso2.identity.integration.test.rest.api.server.workflows.v1.model;

import io.swagger.annotations.ApiModel;
import javax.validation.constraints.*;

/**
 * Name of the user operation
 **/
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlType(name="")
@XmlEnum(String.class)
public enum Operation {

    @XmlEnumValue("ADD_USER") ADD_USER(String.valueOf("ADD_USER")), @XmlEnumValue("DELETE_USER") DELETE_USER(String.valueOf("DELETE_USER")), @XmlEnumValue("UPDATE_ROLES_OF_USERS") UPDATE_ROLES_OF_USERS(String.valueOf("UPDATE_ROLES_OF_USERS")), @XmlEnumValue("ADD_ROLE") ADD_ROLE(String.valueOf("ADD_ROLE")), @XmlEnumValue("DELETE_ROLE") DELETE_ROLE(String.valueOf("DELETE_ROLE")), @XmlEnumValue("UPDATE_ROLE_NAME") UPDATE_ROLE_NAME(String.valueOf("UPDATE_ROLE_NAME")), @XmlEnumValue("UPDATE_USERS_OF_ROLES") UPDATE_USERS_OF_ROLES(String.valueOf("UPDATE_USERS_OF_ROLES")), @XmlEnumValue("DELETE_USER_CLAIMS") DELETE_USER_CLAIMS(String.valueOf("DELETE_USER_CLAIMS")), @XmlEnumValue("UPDATE_USER_CLAIMS") UPDATE_USER_CLAIMS(String.valueOf("UPDATE_USER_CLAIMS"));


    private String value;

    Operation(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static Operation fromValue(String value) {
        for (Operation b : Operation.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}



