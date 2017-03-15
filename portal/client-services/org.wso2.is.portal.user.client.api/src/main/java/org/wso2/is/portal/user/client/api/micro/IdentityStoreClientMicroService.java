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

package org.wso2.is.portal.user.client.api.micro;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.recovery.forced.AdminForcePasswordResetManager;
import org.wso2.is.portal.user.client.api.IdentityStoreClientService;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;
import org.wso2.msf4j.Microservice;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * This is the micro service implementation for identity store client services.
 */
public class IdentityStoreClientMicroService implements Microservice {
    @GET
    @Path("/userExists")
    public Response getUserExistence(@QueryParam("username") String username,
                                     @QueryParam("usernameClaimUri") String usernameClaimUri,
                                     @QueryParam("domain") String domain) throws UserPortalUIException {
        IdentityStoreClientService identityStoreClientService = null;
        boolean isUserExists = false;
        Map<String, String> userClaims = new HashMap<>();
        userClaims.put(usernameClaimUri, username);

        BundleContext bundleContext = FrameworkUtil.getBundle(IdentityStoreClientService.class).getBundleContext();
        ServiceReference<IdentityStoreClientService> serviceReference =
                bundleContext.getServiceReference(IdentityStoreClientService.class);
        if (serviceReference != null) {
            identityStoreClientService = bundleContext.getService(serviceReference);
        }

        if (identityStoreClientService != null) {
            isUserExists = identityStoreClientService.isUserExist(userClaims, domain);
        }

        return Response.ok(isUserExists).build();
    }

    @GET
    @Path("/generateOTP")
    public Response getGeneratedPassword() throws UserNotFoundException, IdentityStoreException, UserPortalUIException {
        String generatedPassword = AdminForcePasswordResetManager.getInstance().generateOTPValue();
        return Response.ok(generatedPassword).build();
    }

}
