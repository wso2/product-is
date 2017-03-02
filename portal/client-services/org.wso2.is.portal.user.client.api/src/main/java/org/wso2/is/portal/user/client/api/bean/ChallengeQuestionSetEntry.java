/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.is.portal.user.client.api.bean;


import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;

import java.util.List;

/**
 * This class contains the security question set id and the corresponding question list for that set id.
 */
public class ChallengeQuestionSetEntry {

    private String challengeQuestionSetId;
    private List<ChallengeQuestion> challengeQuestionList;

    public List<ChallengeQuestion> getChallengeQuestionList() {
        return challengeQuestionList;
    }

    public void setChallengeQuestionList(List<ChallengeQuestion> challengeQuestionList) {
        this.challengeQuestionList = challengeQuestionList;
    }

    public String getChallengeQuestionSetId() {
        return challengeQuestionSetId;
    }

    public void setChallengeQuestionSetId(String challengeQuestionSetId) {
        this.challengeQuestionSetId = challengeQuestionSetId;
    }
}
