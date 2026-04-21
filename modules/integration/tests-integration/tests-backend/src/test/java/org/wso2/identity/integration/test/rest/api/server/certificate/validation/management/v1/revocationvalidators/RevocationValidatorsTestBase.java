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

package org.wso2.identity.integration.test.rest.api.server.certificate.validation.management.v1.revocationvalidators;

import org.wso2.identity.integration.test.rest.api.server.certificate.validation.management.v1.common.CertificateValidationTestBase;

public class RevocationValidatorsTestBase extends CertificateValidationTestBase {

    protected static final String REVOCATION_VALIDATORS_PATH = "/revocation-validators";
    protected static final String VALIDATORS_KEY = "Validators";
    protected static final String OCSP_VALIDATOR = "ocspvalidator";
    protected static final String CRL_VALIDATOR = "crlvalidator";

    protected static final String ENABLE_KEY = "enable";
    protected static final String PRIORITY_KEY = "priority";
    protected static final String FULL_CHAIN_VALIDATION_KEY = "fullChainValidation";
    protected static final String RETRY_COUNT_KEY = "retryCount";
    protected static final boolean ENABLED_VALUE = true;
    protected static final int PRIORITY_VALUE = 1;
    protected static final boolean FULL_CHAIN_VALIDATION_VALUE = true;
    protected static final int RETRY_COUNT_VALUE = 2;
}
