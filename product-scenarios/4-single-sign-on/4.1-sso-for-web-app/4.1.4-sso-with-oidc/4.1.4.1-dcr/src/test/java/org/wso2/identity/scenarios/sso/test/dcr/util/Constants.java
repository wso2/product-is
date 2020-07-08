/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.scenarios.sso.test.dcr.util;

import java.util.ArrayList;
import java.util.List;

public class Constants {

    private Constants() {

    }

    public static class RegistrationRequestElements {

        public static final String REDIRECT_URIS = "redirect_uris";

        public static final String CLIENT_NAME = "client_name";

        public static final String GRANT_TYPES = "grant_types";

        public static final String APPLICATION_TYPE = "application_type";

        public static final String JWKS_URI = "jwks_uri";

        public static final String URL = "url";

        public static final String CLIENT_ID = "ext_param_client_id";

        public static final String CLIENT_SECRET = "ext_param_client_secret";

        public static final String CONTACTS = "contacts";

        public static final String POST_LOGOUT_REDIRECT_URIS = "post_logout_redirect_uris";

        public static final String REQUEST_URIS = "request_uris";

        public static final String RESPONSE_TYPES = "response_types";

        public static final String TOKEN_TYPE = "token_type_extension";

        public static final String SP_TEMPLATE_NAME = "ext_param_sp_template";
    }

    public static class UpdateRequestElements {

        public static final String REDIRECT_URIS = "redirect_uris";

        public static final String CLIENT_NAME = "client_name";

        public static final String GRANT_TYPES = "grant_types";

        public static final String TOKEN_TYPE = "token_type_extension";

        public static final String CLIENT_ID = "client_id";

        public static final String CLIENT_SECRET = "client_secret";
    }

    public static class ApplicationResponseElements {

        public static final String CLIENT_NAME = "client_name";

        public static final String CLIENT_ID = "client_id";

        public static final String CLIENT_SECRET = "client_secret";

        public static final String REDIRECT_URIS = "redirect_uris";
    }
}
