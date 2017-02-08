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

import org.wso2.msf4j.Microservice;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * This is the API implementation for profile image download.
 */
public class ProfileImageDownloaderService implements Microservice {

    @GET
    @Path("/image")
    @Produces("image/*")
    public Response getProfileImage(@QueryParam("userid") String userId) {
        File imageDirectory = new File(Paths.get(System.getProperty("carbon.home"), "images").toString());
        String[] imageNames = imageDirectory.list();

        if (imageNames == null) {
            return Response.ok().build();
        }

        return Arrays.stream(imageNames).
                filter(imageName -> imageName.contains(userId)).
                map(imageName -> {
                    File imageFile = new File(
                            Paths.get(System.getProperty("carbon.home"), "images").toString() + File.separator
                                    + imageName);
                    String mimeType = new MimetypesFileTypeMap().getContentType(imageFile);

                    return Response.ok(imageFile, mimeType).build();
                }).
                findAny().
                orElse(Response.ok().build());

    }
}
