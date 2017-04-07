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

/**
 * Temporary GroupBean to be used in admin portal.
 * <p>
 * //todo enable cs
 */
public class GroupBean {

    private static final long serialVersionUID = 56118812467L;

    private String groupName;

    private String groupId;

    private String groupDescription;

    private String domainName;

    public GroupBean(String groupName, String groupId, String groupDescription, String domainName) {
        this.groupName = groupName;
        this.groupId = groupId;
        this.groupDescription = groupDescription;
        this.domainName = domainName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String toString() {
        return "Group name - " + groupName + " : Group Description - " + groupDescription + " : Group Id - " + groupId +
                " : Domain name - " + domainName;
    }
}
