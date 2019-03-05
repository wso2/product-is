package org.wso2.identity.integration.test.template.mgt;

import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.BasicAuthSecurityHandler;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import javax.ws.rs.core.MediaType;

/**
 * Template management test cases.
 */
public class TemplateManagementTestCase extends ISIntegrationTest {

    public static final String TEMPLATE_MGT_ENDPOINT_SUFFIX = "/api/identity/template/mgt/v1.0.0/templates/";
    private String isServerBackendUrl;
    private String templateMgtEndpoint;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        isServerBackendUrl = isServer.getContextUrls().getWebAppURLHttps();
        templateMgtEndpoint = isServerBackendUrl + TEMPLATE_MGT_ENDPOINT_SUFFIX;
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() {

    }

    @Test(alwaysRun = true, description = "Add template test")
    public void testAddTemplate() {

        String templateName = "Template1";
        String description = "This is a template created by Alex";
        String templateScript = "sample template script";
        JSONObject response = addTemplate(templateName, description, templateScript);

    }

    private JSONObject addTemplate(String templateName, String description, String templateScript) {

        ClientConfig clientConfig = new ClientConfig();
        BasicAuthSecurityHandler basicAuth = new BasicAuthSecurityHandler();
        basicAuth.setUserName(userInfo.getUserName());
        basicAuth.setPassword(userInfo.getPassword());
        clientConfig.handlers(basicAuth);

        RestClient restClient = new RestClient(clientConfig);
        Resource templateResource = restClient.resource(templateMgtEndpoint);

        String addTemplateString = "{" +
                "\"templateName\": \"" + templateName + "\", " +
                "\"description\": \"" + description + "\"," +
                "\"templateScript\": \"" + templateScript + "\"}";

        String response = templateResource.contentType(MediaType.APPLICATION_JSON_TYPE).
                accept(MediaType.APPLICATION_JSON_TYPE).post(String.class, addTemplateString);

        return (JSONObject) JSONValue.parse(response);
    }

}
