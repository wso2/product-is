/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.challenge.v1.model;

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

    public ServerChallengeModel(String questionSetId, List<Questions> questions) {

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

        public ChallengeQuestionOperation(Questions challengeQuestion, String operation) {

            this.challengeQuestion = challengeQuestion;
            this.operation = operation;
        }
    }
}
