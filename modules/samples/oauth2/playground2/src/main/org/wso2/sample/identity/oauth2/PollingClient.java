package org.wso2.sample.identity.oauth2;

/**
 * Created by hasanthi on 3/24/15.
 */

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class PollingClient {
    /**
     *
     * @return Response true/false
     */
    public String getOPResponse() {
        String opResponse = null;
        try {
            Client client = Client.create();
            WebResource webResource = client
                    .resource("https://localhost:9443/oauth2/session");
            ClientResponse clientResponse = webResource.accept("text/plain")
                    .get(ClientResponse.class);
            if (clientResponse.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + clientResponse.getStatus());
            }

            opResponse = clientResponse.getEntity(String.class);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return opResponse;
        }

    }

}
