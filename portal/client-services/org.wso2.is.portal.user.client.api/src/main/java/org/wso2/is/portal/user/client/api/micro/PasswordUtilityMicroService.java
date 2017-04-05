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
import org.wso2.carbon.identity.policy.password.pattern.validation.PasswordValidationService;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.util.BufferUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.IntStream;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * This is the micorservice to handle password utility services
 */
public class PasswordUtilityMicroService implements Microservice {

    @POST
    @Path("/validatePassword")
    public Response isValidPassword(@Context Request password) throws UnsupportedEncodingException {
        PasswordValidationService passwordValidationService = null;
        boolean isValidPassword = false;
        ByteBuffer fullContent = BufferUtil.merge(password.getFullMessageBody());
        //TODO: Replace toString method, once a way to retrieve char[] without encoded values for special characters is
        //found.
        String passwordAsString = StandardCharsets.UTF_8.decode(fullContent).toString();
        String decodedPassword = URLDecoder.decode(passwordAsString, StandardCharsets.UTF_8.toString());
        char[] passwordCharArray = decodedPassword.toCharArray();
        int index = IntStream.range(0, passwordCharArray.length).filter(i -> passwordCharArray[i] == '=')
                .findFirst().orElse(-1);
        char[] newArray = Arrays.copyOfRange(passwordCharArray, index + 1, passwordCharArray.length);
        BundleContext bundleContext = FrameworkUtil.getBundle(PasswordValidationService.class).getBundleContext();
        ServiceReference<PasswordValidationService> serviceReference =
                bundleContext.getServiceReference(PasswordValidationService.class);
        if (serviceReference != null) {
            passwordValidationService = bundleContext.getService(serviceReference);
        }
        if (passwordValidationService != null) {
            isValidPassword = passwordValidationService.validatePassword(newArray);
        }
        return Response.ok(isValidPassword).build();
    }

}
