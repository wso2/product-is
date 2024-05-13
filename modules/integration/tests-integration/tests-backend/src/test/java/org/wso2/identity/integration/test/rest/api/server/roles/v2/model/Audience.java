package org.wso2.identity.integration.test.rest.api.server.roles.v2.model;

/**
 * Represents the audience of a role.
 * The audience specifies the intended recipient of the role, which could be an application or organization.
 * It consists of a type and a value, where the type describes the audience category (e.g., APPLICATION or ORGANIZATION)
 * and the value represents the unique identifier of the audience.
 */
public class Audience {
    private String type;
    private String value;

    // Constructors, getters, and setters

    public Audience() {
    }

    public Audience(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Audience{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';
    }}
