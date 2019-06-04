package org.wso2.identity.integration.test.scim2;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;

public class SCIM2BaseTestCase extends ISIntegrationTest {

    public static final String SERVER_URL = "https://localhost:9853";
    public static final String SCIM2_ME_ENDPOINT = "/scim2/Me";
    public static final String SCIM2_USERS_ENDPOINT = "/scim2/Users";
    public static final String SCIM2_GROUPS_ENDPOINT = "/scim2/Groups";
    public static final String SCIM_RESOURCE_TYPES_ENDPOINT = "/scim2/ResourceTypes";

    public static final String USER_NAME_ATTRIBUTE = "userName";
    public static final String FAMILY_NAME_ATTRIBUTE = "familyName";
    public static final String GIVEN_NAME_ATTRIBUTE = "givenName";
    public static final String EMAIL_TYPE_WORK_ATTRIBUTE = "work";
    public static final String EMAIL_TYPE_HOME_ATTRIBUTE = "home";
    public static final String ID_ATTRIBUTE = "id";
    public static final String PASSWORD_ATTRIBUTE = "password";
    public static final String EMAILS_ATTRIBUTE = "emails";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String SCHEMAS_ATTRIBUTE = "schemas";
    public static final String ROLE_ATTRIBUTE = "roles";
    public static final String TYPE_PARAM = "type";
    public static final String VALUE_PARAM = "value";
    public static final String DISPLAY_NAME_ATTRIBUTE = "displayName";
    public static final String DISPLAY_ATTRIBUTE = "display";
    public static final String MEMBERS_ATTRIBUTE = "members";
    public static final String MEMBER_DISPLAY_ATTRIBUTE = "members.display";
    public static final String META_LOCATION_ATTRIBUTE = "meta.location";
    public static final String LIST_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:ListResponse";
    public static final String RESOURCE_TYPE_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:ResourceType";
    public static final String ERROR_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:Error";

    private ServerConfigurationManager serverConfigurationManager;

    @BeforeTest(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        changeISConfiguration();
    }

    @AfterTest(alwaysRun = true)
    public void tearDownTest() throws Exception {
        super.init();
        resetISConfiguration();
    }

    private void changeISConfiguration() throws Exception {

        String carbonHome = Utils.getResidentCarbonHome();
        File identityXML = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator + "identity" + File.separator + "identity" +
                ".xml");
        File configuredIdentityXML = new File(getISResourceLocation() + File.separator + "scim2" + File.separator +
                "me-unsecured-identity.xml");

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXML, identityXML, true);
        serverConfigurationManager.restartForcefully();
    }

    private void resetISConfiguration() throws Exception {
        log.info("Replacing identity.xml with default configurations");

        serverConfigurationManager.restoreToLastConfiguration(false);
    }

}
