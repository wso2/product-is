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

package org.wso2.identity.integration.test.rest.api.server.certificate.validation.management.v1.cacertificates.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import javax.validation.Valid;

public class CACertificateAddRequest  {

    private String certificate;

    /**
    * Base64 encoded certificate
    **/
    public CACertificateAddRequest certificate(String certificate) {

        this.certificate = certificate;
        return this;
    }

    @ApiModelProperty(example = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNMRENDQWRLZ0F3SUJBZ0lCQURBS0JnZ3Foa2pPUFFRREFqQjlNUXN3Q1FZRFZRUUdFd0pDUlRFUE1BMEcKQTFVRUNoTUdSMjUxVkV4VE1TVXdJd1lEVlFRTEV4eEhiblZVVEZNZ1kyVnlkR2xtYVdOaGRHVWdZWFYwYUc5eQphWFI1TVE4d0RRWURWUVFJRXdaTVpYVjJaVzR4SlRBakJnTlZCQU1USEVkdWRWUk1VeUJqWlhKMGFXWnBZMkYwClpTQmhkWFJvYjNKcGRIa3dIaGNOTVRFd05USXpNakF6T0RJeFdoY05NVEl4TWpJeU1EYzBNVFV4V2pCOU1Rc3cKQ1FZRFZRUUdFd0pDUlRFUE1BMEdBMVVFQ2hNR1IyNTFWRXhUTVNVd0l3WURWUVFMRXh4SGJuVlVURk1nWTJWeQpkR2xtYVdOaGRHVWdZWFYwYUc5eWFYUjVNUTh3RFFZRFZRUUlFd1pNWlhWMlpXNHhKVEFqQmdOVkJBTVRIRWR1CmRWUk1VeUJqWlhKMGFXWnBZMkYwWlNCaGRYUm9iM0pwZEhrd1dUQVRCZ2NxaGtqT1BRSUJCZ2dxaGtqT1BRTUIKQndOQ0FBUlMySTBqaXVObjE0WTJzU0FMQ1gzSXlicWlJSlV2eFVwaitvTmZ6bmd2ai9OaXl2MjM5NEJXblc0WAp1UTRSVEVpeXdLODdXUmNXTUdnSkI1a1gvdDJubzBNd1FUQVBCZ05WSFJNQkFmOEVCVEFEQVFIL01BOEdBMVVkCkR3RUIvd1FGQXdNSEJnQXdIUVlEVlIwT0JCWUVGUEMwZ2Y2WUVyKzFLTGxrUUFQTHpCOW1UaWdETUFvR0NDcUcKU000OUJBTUNBMGdBTUVVQ0lER3V3RDFLUHlHK2hSZjg4TWV5TVFjcU9GWkQwVGJWbGVGK1VzQUdRNGVuQWlFQQpsNHdPdUR3S1FhK3VwYzhHZnRYRTJDLy80bUtBTkJDNkl0MDFnVWFUSXBvPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0t", value = "Base64 encoded certificate")
    @JsonProperty("certificate")
    @Valid
    public String getCertificate() {
        return certificate;
    }
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CACertificateAddRequest caCertificateAddRequest = (CACertificateAddRequest) o;
        return Objects.equals(this.certificate, caCertificateAddRequest.certificate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certificate);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class CACertificateAddRequest {\n");

        sb.append("    certificate: ").append(toIndentedString(certificate)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
    * Convert the given object to string with each line indented by 4 spaces
    * (except the first line).
    */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}
