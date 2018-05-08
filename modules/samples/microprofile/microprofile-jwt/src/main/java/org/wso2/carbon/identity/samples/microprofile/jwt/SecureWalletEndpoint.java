/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.samples.microprofile.jwt;

import org.eclipse.microprofile.jwt.JsonWebToken;

import java.math.BigDecimal;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@DeclareRoles({"admin","ViewBalance", "Debtor", "Creditor"})
@Path("/")
public class SecureWalletEndpoint {
    private BigDecimal currentBalance = new BigDecimal("13492838.45");
    private String currency = "USD";

    @Inject
    private JsonWebToken jwt;

    @GET
    @Path("/balance")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "ViewBalance", "Debtor"})
    public JsonObject getBalance() {
        return generateBalanceInfo();
    }

    @GET
    @Path("/debit")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin","Debtor"})
    public Response debit(@QueryParam("amount") String amount) {
        Double debitAmount = Double.valueOf(amount);
        if (currentBalance.doubleValue() > debitAmount) {
            currentBalance = currentBalance.subtract(new BigDecimal(amount));
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Not enough balance to debit: " + currency + " " + currentBalance).build();
        }
        return Response.ok(generateBalanceInfo()).build();
    }

    @GET
    @Path("/credit")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin","Creditor"})
    public Response credit(@QueryParam("amount") String amount) {
        currentBalance = currentBalance.add(new BigDecimal(amount));
        return Response.ok(generateBalanceInfo()).build();
    }

    private JsonObject generateBalanceInfo() {
        JsonObjectBuilder result = Json.createObjectBuilder().add(currency, currentBalance);
        double warningLimit = 10000.00;

        if (warningLimit > currentBalance.doubleValue()) {
            String warningMsg = String.format("balance is below warning limit: %s", warningLimit);
            result.add("warning", warningMsg);
        }

        return result.build();
    }
}
