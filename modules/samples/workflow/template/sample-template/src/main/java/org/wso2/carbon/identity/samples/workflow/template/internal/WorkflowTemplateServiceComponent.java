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

package org.wso2.carbon.identity.samples.workflow.template.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.samples.workflow.template.Constants;
import org.wso2.carbon.identity.samples.workflow.template.SampleTemplate;
import org.wso2.carbon.identity.samples.workflow.template.SampleTemplateImplementation;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowRuntimeException;
import org.wso2.carbon.identity.workflow.mgt.template.AbstractTemplate;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowManagementUtil;
import org.wso2.carbon.identity.workflow.mgt.workflow.AbstractWorkflow;
import org.wso2.carbon.identity.workflow.impl.BPELDeployer;
import org.wso2.carbon.identity.workflow.impl.RequestExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

@Component(
        name = "identity.samples.workflow.template.component",
        immediate = true
)
public class WorkflowTemplateServiceComponent {

    private static Log log = LogFactory.getLog(WorkflowTemplateServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            BundleContext bundleContext = componentContext.getBundleContext();

            bundleContext.registerService(AbstractTemplate.class,
                    new SampleTemplate(readFileContent(Constants.TEMPLATE_PARAMETER_METADATA_FILE_NAME)), null);

            bundleContext.registerService(AbstractWorkflow.class, new SampleTemplateImplementation(BPELDeployer.class,
                    RequestExecutor.class, readFileContent(Constants.WORKFLOW_IMPL_PARAMETER_METADATA_FILE_NAME)),
                    null);
        } catch (Throwable e) {
            log.error("Error occurred while activating WorkflowTemplateServiceComponent bundle.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Stopping WorkflowTemplateServiceComponent");
        }
    }

    private String readFileContent(String fileName) throws WorkflowRuntimeException {
        String content;
        try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
            content = WorkflowManagementUtil.readFileFromResource(resourceAsStream);
        } catch (URISyntaxException | IOException e) {
            String errorMsg = "Error occurred while reading file from class path, " + e.getMessage();
            log.error(errorMsg);
            throw new WorkflowRuntimeException(errorMsg, e);
        }
        return content;
    }
}
