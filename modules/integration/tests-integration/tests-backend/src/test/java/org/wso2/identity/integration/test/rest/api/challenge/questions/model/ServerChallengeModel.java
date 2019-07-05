/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 */

package org.wso2.identity.integration.test.rest.api.challenge.questions.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * This class is used to store data about the challenge questions.
 * Class will be sent in the response.
 * This is mapped to the request body.
 */
public class ServerChallengeModel {
    @Expose
    @SerializedName("questionSetId")
    private String questionSetId;
    @Expose
    @SerializedName("questions")
    private List<Questions> questions;

    @Override
    public String toString() {
        return "{" +
                "questionSetId='" + questionSetId + '\'' +
                ", questions=" + questions +
                '}';
    }

    public ServerChallengeModel(String questionSetId,
            List<Questions> questions) {
        this.questionSetId = questionSetId;
        this.questions = questions;
    }

    public static class Questions {
        @Expose
        @SerializedName("locale")
        private String locale;
        @Expose
        @SerializedName("question")
        private String question;
        @Expose
        @SerializedName("questionId")
        private String questionId;

        public Questions(String locale, String question, String questionId) {
            this.locale = locale;
            this.question = question;
            this.questionId = questionId;
        }

        @Override
        public String toString() {
            return "{" +
                    "locale='" + locale + '\'' +
                    ", question='" + question + '\'' +
                    ", questionId='" + questionId + '\'' +
                    '}';
        }
    }

    public static class ChallengeQuestionOperation {

        @Expose
        @SerializedName("challengeQuestion")
        private Questions challengeQuestion;
        @Expose
        @SerializedName("operation")
        private String operation;

        public ChallengeQuestionOperation(
                Questions challengeQuestion, String operation) {
            this.challengeQuestion = challengeQuestion;
            this.operation = operation;
        }
    }
}
