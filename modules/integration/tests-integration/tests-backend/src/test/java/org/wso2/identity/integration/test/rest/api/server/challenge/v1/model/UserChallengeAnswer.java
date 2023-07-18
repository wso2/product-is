/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.challenge.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.identity.integration.test.rest.api.server.challenge.v1.model.ServerChallengeModel.Questions;

import javax.validation.Valid;
import java.util.Objects;

public class UserChallengeAnswer {
    private Questions challengeQuestion;
    private String answer;

    /**
     *
     **/
    public UserChallengeAnswer challengeQuestion(Questions challengeQuestion) {

        this.challengeQuestion = challengeQuestion;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("challengeQuestion")
    @Valid
    public Questions getChallengeQuestion() {
        return challengeQuestion;
    }

    public void setChallengeQuestion(Questions challengeQuestion) {
        this.challengeQuestion = challengeQuestion;
    }

    /**
     *
     **/
    public UserChallengeAnswer answer(String answer) {

        this.answer = answer;
        return this;
    }

    @ApiModelProperty(example = "Colombo")
    @JsonProperty("answer")
    @Valid
    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserChallengeAnswer userChallengeAnswer = (UserChallengeAnswer) o;
        return Objects.equals(this.challengeQuestion, userChallengeAnswer.challengeQuestion) &&
                Objects.equals(this.answer, userChallengeAnswer.answer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(challengeQuestion, answer);
    }

    @Override
    public String toString() {

        return "class UserChallengeAnswer {\n" +
                "    challengeQuestion: " + toIndentedString(challengeQuestion) + "\n" +
                "    answer: " + toIndentedString(answer) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString();
    }
}
