package org.wso2.carbon.identity.test.integration.service.dao;

import org.wso2.carbon.user.core.model.Condition;

public class ConditionDTO implements Condition {

    private String operation;

    @Override
    public String getOperation() {

        return operation;
    }

    @Override
    public void setOperation(String operation) {

        this.operation = operation;
    }
}
