/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.rest.api.server.certificate.validation.management.v1.cacertificates;

import org.wso2.identity.integration.test.rest.api.server.certificate.validation.management.v1.common.CertificateValidationTestBase;

public class CACertificatesTestBase extends CertificateValidationTestBase {

    protected static final String CA_CERTIFICATES_PATH = "/ca";
    protected static final String ADD_CA_CERTIFICATE_VALIDATION_CONFIG = "certificate_validation_config.toml";
    protected static final String CERTIFICATES_KEY = "Certificates";
}
