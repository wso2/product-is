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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.InputData;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.Item;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.MapType;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.ParameterMetaData;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.workflow.AbstractWorkflow;
import org.wso2.carbon.identity.workflow.mgt.workflow.TemplateInitializer;
import org.wso2.carbon.identity.workflow.mgt.workflow.WorkFlowExecutor;

import java.util.List;

import static org.wso2.carbon.identity.samples.workflow.template.Constants.BPS_PROFILE;
import static org.wso2.carbon.identity.samples.workflow.template.Constants.HT_SUBJECT;

public class SampleTemplateImplementation extends AbstractWorkflow {

    private static Log log = LogFactory.getLog(SampleTemplateImplementation.class);

    public SampleTemplateImplementation(Class<? extends TemplateInitializer> templateInitializerClass,
                                        Class<? extends WorkFlowExecutor> workFlowExecutorClass, String metaDataXML) {
        super(templateInitializerClass, workFlowExecutorClass, metaDataXML);
    }

    @Override
    protected InputData getInputData(ParameterMetaData parameterMetaData) throws WorkflowException {
        InputData inputData = null;
        if (parameterMetaData != null && parameterMetaData.getName() != null) {
            String parameterName = parameterMetaData.getName();
            if (BPS_PROFILE.equals(parameterName)) {
                inputData = new InputData();
                MapType mapType = new MapType();
                inputData.setMapType(mapType);
                Item item = new Item();
                item.setKey("embeded_bps");
                item.setValue("embeded_bps");
                mapType.setItem(new Item[]{item});
            } else if (HT_SUBJECT.equals(parameterName)) {
                inputData = new InputData();
                MapType mapType = new MapType();
                inputData.setMapType(mapType);
                Item item1 = new Item();
                item1.setKey("subject1");
                item1.setValue("subject1");
                Item item2 = new Item();
                item2.setKey("subject2");
                item2.setValue("subject2");
                mapType.setItem(new Item[]{item1, item2});
            }
        }
        return inputData;
    }

    @Override
    public void deploy(List<Parameter> parameterList) throws WorkflowException {
        super.deploy(parameterList);
    }
}
