/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.samples.workflow.template;

import org.wso2.carbon.identity.workflow.mgt.bean.metadata.InputData;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowRuntimeException;
import org.wso2.carbon.identity.workflow.mgt.template.AbstractTemplate;

import static org.wso2.carbon.identity.samples.workflow.template.Constants.APPROVAL_TEMPLATE_NAME;
import static org.wso2.carbon.identity.samples.workflow.template.Constants.DESCRIPTION;
import static org.wso2.carbon.identity.samples.workflow.template.Constants.TEMPLATE_ID;

public class SampleTemplate extends AbstractTemplate {

    public SampleTemplate(String metaDataXML) throws WorkflowRuntimeException {
        super(metaDataXML);
    }

    @Override
    protected InputData getInputData(String parameterName) throws WorkflowException {
        return null;
    }

    @Override
    public String getTemplateId() {
        return TEMPLATE_ID;
    }

    @Override
    public String getName() {
        return APPROVAL_TEMPLATE_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
