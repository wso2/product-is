package org.wso2.carbon.identity.test.integration.service.dao;

public class FailureReasonDTO {

    private String failureReason;
    private int id;

    public FailureReasonDTO() {
        super();
    }

    public FailureReasonDTO(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getFailureReason() {

        return failureReason;
    }

    public void setFailureReason(String failureReason) {

        this.failureReason = failureReason;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }
}
