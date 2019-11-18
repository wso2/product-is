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

package org.wso2.identity.integration.test.rest.api.server.keystore.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class CertificateRequest  {
  
    private String alias;
    private String certificate;

    /**
    **/
    public CertificateRequest alias(String alias) {

        this.alias = alias;
        return this;
    }
    
    @ApiModelProperty(example = "newcert", required = true, value = "")
    @JsonProperty("alias")
    @Valid
    @NotNull(message = "Property alias cannot be null.")

    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
    **/
    public CertificateRequest certificate(String certificate) {

        this.certificate = certificate;
        return this;
    }
    
    @ApiModelProperty(example = "MIIDADCCAeigAwIBAgIEnRKL8zANBgkqhkiG9w0BAQQFADBCMREwDwYDVQQDDAh3c28yLmNvbTENMAsGA1UECwwETm9uZTEPMA0GA1UECgwGTm9uZSBMMQ0wCwYDVQQGEwROb25lMB4XDTE5MDkyMjA5MzgyM1oXDTI5MTAxOTA5MzgyM1owQjERMA8GA1UEAwwId3NvMi5jb20xDTALBgNVBAsMBE5vbmUxDzANBgNVBAoMBk5vbmUgTDENMAsGA1UEBhMETm9uZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMY6diX4CyRNARLYQq5tLnjSMbCh4waM7EDniKeZuQAKVdBpzgvan+G8QrgfJqZ7IfYCLFKSQIpblMiULnaf0Zy1VqQG+QNh+CHpMJ0jRPAKWUXQrgzFu24+araRei9v9VBHVwaNVp1uHKx9cL4XAXdIRv+ZDCTrkvJJUPxrNgF14UIUPsNIZF5perptTUUaudyDiGug80baTXxuEf7gJh6LcT5UIJF/moWKhLYhKEFa7nq7sJLcGIUKRlIxF487rTe1zZzN9RsdpH5d11DnltThOdO1mF9BX1U6F3yuJoOlsIcHIeab0XED0jw8PFi5+LT7EOe6xzaB9sL2DB38o8sCAwEAATANBgkqhkiG9w0BAQQFAAOCAQEArqGJfCOIjY5sFhO6Fd5x3dM6OrA7QcjxzNxsNwO7e2zFr4SHJqekfErZxFTLGWMpyMakvTFAk3e/ShvDQ71nBxYl6Rbdco0f2SUI2ig3lQR5ZTxPmxSSNNbX4K2ptAgzikmSjc6lrGRUnMkMKetClDRsOf9banuDImB2aIn2STmFR2U5duui2oEep3C5mlxregBXI8xEWF6VpXzeEz2AEOaKWOPLDTQsBWxSFi9uVVkD9GwN7yJCXWF81enCpQ4U1PHVhAdS40HTXbis/R0+ykDdkDLmbu3Oa+F2uyWnarAR9rKVfjNovXvJPX4D+5/wipDyXUOO58tyF0342JuIdA==", required = true, value = "")
    @JsonProperty("certificate")
    @Valid
    @NotNull(message = "Property certificate cannot be null.")

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
        CertificateRequest certificateRequest = (CertificateRequest) o;
        return Objects.equals(this.alias, certificateRequest.alias) &&
            Objects.equals(this.certificate, certificateRequest.certificate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, certificate);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class CertificateRequest {\n");

        sb.append("    alias: ").append(toIndentedString(alias)).append("\n");
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

