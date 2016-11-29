/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.samples.entitlement.kmarket.trading.stub.factory;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceStub;

public class EntitlementServiceStubFactory extends BasePoolableObjectFactory {

    private ConfigurationContext configurationContext;
    private String targetEndpoint;
    private HttpTransportProperties.Authenticator authenticator;

    public EntitlementServiceStubFactory (ConfigurationContext configurationContext, String targetEndpoint,
            HttpTransportProperties.Authenticator authenticator) {
        this.configurationContext = configurationContext;
        this.targetEndpoint = targetEndpoint;
        this.authenticator = authenticator;
    }

    @Override public Object makeObject() throws Exception {
        EntitlementServiceStub stub = new EntitlementServiceStub(configurationContext,
                targetEndpoint);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(HTTPConstants.AUTHENTICATE, authenticator);
        return stub;
    }
}
