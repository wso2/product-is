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

package org.wso2.identity.integration.test.inheritance;

import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyRevertReq;
import org.wso2.identity.integration.test.rest.api.server.input.validation.v1.model.MappingModel;
import org.wso2.identity.integration.test.rest.api.server.input.validation.v1.model.RevertFields;
import org.wso2.identity.integration.test.rest.api.server.input.validation.v1.model.RuleModel;
import org.wso2.identity.integration.test.rest.api.server.input.validation.v1.model.ValidationConfigModelForField;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.ValidationRulesRestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Integration tests for inheriting, overriding and reverting login and registration configurations in sub-organizations.
 */
public class ConfigInheritanceTestCase extends ISIntegrationTest {

    private static final String SUB_ORG_NAME = "subOrg";
    private static final String ORG_VERSION_V0 = "v0.0.0";
    private static final String ORG_VERSION_V1 = "v1.0.0";
    private static final String AUTHORIZED_APIS_JSON = "authorized-apis.json";
    private static final String ACCOUNT_MGT_CATEGORY_ID = "QWNjb3VudCBNYW5hZ2VtZW50";
    private static final String MULTI_ATTRIBUTE_CONNECTOR_ID = "bXVsdGlhdHRyaWJ1dGUubG9naW4uaGFuZGxlcg";
    private static final String MULTI_ATTRIBUTE_ENABLE_PROPERTY = "account.multiattributelogin.handler.enable";
    private static final String MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_PROPERTY =
            "account.multiattributelogin.handler.allowedattributes";
    private static final String MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_DEFAULT_PROPERTY_VALUE =
            "http://wso2.org/claims/username";
    private static final String MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_ROOT_ORG_PROPERTY_VALUE =
            "http://wso2.org/claims/username, http://wso2.org/claims/emailaddress";
    private static final String MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_SUB_ORG_PROPERTY_VALUE =
            "http://wso2.org/claims/username, http://wso2.org/claims/mobile";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String PROPERTIES = "properties";
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String PASSWORD_FIELD = "password";
    private static final String VALIDATION_RULES_MIN_LENGTH_PROPERTY = "min.length";
    private static final String VALIDATION_RULES_MAX_LENGTH_PROPERTY = "max.length";
    private static final String VALIDATION_RULES_LENGTH_VALIDATOR = "LengthValidator";
    private static final String VALIDATION_RULES_ROOT_ORG_MIN_LENGTH = "6";
    private static final String VALIDATION_RULES_ROOT_ORG_MAX_LENGTH = "25";
    private static final String VALIDATION_RULES_SUB_ORG_MIN_LENGTH = "7";
    private static final String VALIDATION_RULES_SUB_ORG_MAX_LENGTH = "30";
    private static final String VALIDATION_RULES_DEFAULT_MIN_LENGTH = "8";
    private static final String VALIDATION_RULES_DEFAULT_MAX_LENGTH = "64";

    public static final String FIELD = "field";
    public static final String RULES = "rules";
    public static final String VALIDATOR = "validator";
    public static final String KEY = "key";

    private OrgMgtRestClient orgMgtRestClient;
    private IdentityGovernanceRestClient identityGovernanceRestClient;
    private ValidationRulesRestClient validationRulesRestClient;
    private String subOrgId;
    private String switchedM2MToken;

    @Override
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL,
                new JSONObject(RESTTestBase.readResource(AUTHORIZED_APIS_JSON, this.getClass())));
        subOrgId = orgMgtRestClient.addOrganization(SUB_ORG_NAME);
        switchedM2MToken = orgMgtRestClient.switchM2MToken(subOrgId);
        orgMgtRestClient.updateOrganizationVersion(ORG_VERSION_V1);

        identityGovernanceRestClient = new IdentityGovernanceRestClient(serverURL, tenantInfo);
        validationRulesRestClient = new ValidationRulesRestClient(serverURL, tenantInfo);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        orgMgtRestClient.deleteOrganization(subOrgId);
        revertGovernanceConfig(false);
        revertPasswordValidationRules(false);
        orgMgtRestClient.closeHttpClient();
        identityGovernanceRestClient.closeHttpClient();
        validationRulesRestClient.closeHttpClient();
    }

    @Test(description = "Test the inheritance of governance configurations from the parent " +
            "organization to a child organization.")
    public void testGovernanceConfigurationInheritance() throws Exception {

        updateGovernanceConfig(false);
        org.json.simple.JSONObject configInSubOrg =
                identityGovernanceRestClient.getConnectorInSubOrg(ACCOUNT_MGT_CATEGORY_ID, MULTI_ATTRIBUTE_CONNECTOR_ID,
                        switchedM2MToken);
        JSONArray connectorProperties = (JSONArray) configInSubOrg.get(PROPERTIES);
        for (Object property : connectorProperties) {
            org.json.simple.JSONObject jsonProperty = (org.json.simple.JSONObject) property;
            switch (jsonProperty.get(NAME).toString()) {
                case MULTI_ATTRIBUTE_ENABLE_PROPERTY:
                    Assert.assertEquals(jsonProperty.get(VALUE).toString(), TRUE,
                            "Multi attribute login handler enable property is not inherited correctly.");
                    break;
                case MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_PROPERTY:
                    Assert.assertEquals(jsonProperty.get(VALUE).toString(),
                            MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_ROOT_ORG_PROPERTY_VALUE,
                            "Multi attribute allowed attributes property is not inherited correctly.");
                    break;
                default:
                    throw new AssertionError("Unexpected property found: " + jsonProperty.get(NAME));
            }
        }
    }

    @Test(dependsOnMethods = "testGovernanceConfigurationInheritance",
            description = "Test if the overridden configurations are returned after modifying the governance " +
                    "configurations in a child organization.")
    public void testGovernanceConfigurationOverride() throws Exception {

        updateGovernanceConfig(true);
        org.json.simple.JSONObject configInSubOrg =
                identityGovernanceRestClient.getConnectorInSubOrg(ACCOUNT_MGT_CATEGORY_ID, MULTI_ATTRIBUTE_CONNECTOR_ID,
                        switchedM2MToken);
        JSONArray connectorProperties = (JSONArray) configInSubOrg.get(PROPERTIES);
        for (Object property : connectorProperties) {
            org.json.simple.JSONObject jsonProperty = (org.json.simple.JSONObject) property;
            switch (jsonProperty.get(NAME).toString()) {
                case MULTI_ATTRIBUTE_ENABLE_PROPERTY:
                    Assert.assertEquals(jsonProperty.get(VALUE).toString(), TRUE,
                            "Multi attribute login handler enable property is not inherited correctly.");
                    break;
                case MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_PROPERTY:
                    Assert.assertEquals(jsonProperty.get(VALUE).toString(),
                            MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_SUB_ORG_PROPERTY_VALUE,
                            "Multi attribute allowed attributes property is not overridden correctly.");
                    break;
                default:
                    throw new AssertionError("Unexpected property found: " + jsonProperty.get(NAME));
            }
        }
    }

    @Test(dependsOnMethods = "testGovernanceConfigurationOverride",
            description = "Test if the inherited configurations are returned after reverting the customized " +
                    "governance configurations in a child organization.")
    public void testGovernanceConfigurationRevert() throws Exception {

        revertGovernanceConfig(true);
        org.json.simple.JSONObject configInSubOrg =
                identityGovernanceRestClient.getConnectorInSubOrg(ACCOUNT_MGT_CATEGORY_ID, MULTI_ATTRIBUTE_CONNECTOR_ID,
                        switchedM2MToken);
        JSONArray connectorProperties = (JSONArray) configInSubOrg.get(PROPERTIES);
        for (Object property : connectorProperties) {
            org.json.simple.JSONObject jsonProperty = (org.json.simple.JSONObject) property;
            switch (jsonProperty.get(NAME).toString()) {
                case MULTI_ATTRIBUTE_ENABLE_PROPERTY:
                    Assert.assertEquals(jsonProperty.get(VALUE).toString(), TRUE,
                            "Multi attribute login handler enable property is not inherited correctly.");
                    break;
                case MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_PROPERTY:
                    Assert.assertEquals(jsonProperty.get(VALUE).toString(),
                            MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_ROOT_ORG_PROPERTY_VALUE,
                            "Inherited value of the multi attribute allowed attributes property is " +
                                    "not returned after reverting the value in sub-organization.");
                    break;
                default:
                    throw new AssertionError("Unexpected property found: " + jsonProperty.get(NAME));
            }
        }
    }

    @Test(dependsOnMethods = "testGovernanceConfigurationRevert", description = "Test the inheritance of validation " +
            "rules from the parent organization to a child organization.")
    public void testValidationRulesInheritance() throws Exception {

        updatePasswordValidationRules(false);
        org.json.simple.JSONObject validationRulesInSubOrg =
                validationRulesRestClient.getValidationRulesForFieldInSubOrg(PASSWORD_FIELD, subOrgId);

        JSONArray rules = (JSONArray) validationRulesInSubOrg.get(RULES);
        for (Object rule : rules) {
            org.json.simple.JSONObject ruleObject = (org.json.simple.JSONObject) rule;
            if (ruleObject.get(VALIDATOR).equals(VALIDATION_RULES_LENGTH_VALIDATOR)) {
                JSONArray properties = (JSONArray) ruleObject.get(PROPERTIES);
                for (Object property : properties) {
                    org.json.simple.JSONObject propertyObject = (org.json.simple.JSONObject) property;
                    switch (propertyObject.get(KEY).toString()) {
                        case VALIDATION_RULES_MIN_LENGTH_PROPERTY:
                            Assert.assertEquals(propertyObject.get(VALUE).toString(),
                                    VALIDATION_RULES_ROOT_ORG_MIN_LENGTH,
                                    "Validation rules min length property is not inherited correctly.");
                            break;
                        case VALIDATION_RULES_MAX_LENGTH_PROPERTY:
                            Assert.assertEquals(propertyObject.get(VALUE).toString(),
                                    VALIDATION_RULES_ROOT_ORG_MAX_LENGTH,
                                    "Validation rules max length property is not inherited correctly.");
                            break;
                        default:
                            throw new AssertionError("Unexpected property found: "
                                    + propertyObject.get(NAME));
                    }
                }
            }
        }
    }

    @Test(dependsOnMethods = "testValidationRulesInheritance", description = "Test if the overridden configurations " +
            "are returned after modifying the validation rules configuration in a child organization.")
    public void testValidationRulesOverride() throws Exception {

        updatePasswordValidationRules(true);
        org.json.simple.JSONObject validationRulesInSubOrg =
                validationRulesRestClient.getValidationRulesForFieldInSubOrg(PASSWORD_FIELD, subOrgId);

        JSONArray rules = (JSONArray) validationRulesInSubOrg.get(RULES);
        for (Object rule : rules) {
            org.json.simple.JSONObject ruleObject = (org.json.simple.JSONObject) rule;
            if (ruleObject.get(VALIDATOR).equals(VALIDATION_RULES_LENGTH_VALIDATOR)) {
                JSONArray properties = (JSONArray) ruleObject.get(PROPERTIES);
                for (Object property : properties) {
                    org.json.simple.JSONObject propertyObject = (org.json.simple.JSONObject) property;
                    switch (propertyObject.get(KEY).toString()) {
                        case VALIDATION_RULES_MIN_LENGTH_PROPERTY:
                            Assert.assertEquals(propertyObject.get(VALUE).toString(),
                                    VALIDATION_RULES_SUB_ORG_MIN_LENGTH,
                                    "Validation rules min length property is not overridden correctly.");
                            break;
                        case VALIDATION_RULES_MAX_LENGTH_PROPERTY:
                            Assert.assertEquals(propertyObject.get(VALUE).toString(),
                                    VALIDATION_RULES_SUB_ORG_MAX_LENGTH,
                                    "Validation rules max length property is not overridden correctly.");
                            break;
                        default:
                            throw new AssertionError("Unexpected property found: "
                                    + propertyObject.get(NAME));
                    }
                }
            }
        }
    }

    @Test(dependsOnMethods = "testValidationRulesOverride", description = "Test if the inherited configurations are " +
            "returned after reverting the customized validation rules configuration in a child organization.")
    public void testValidationRulesRevert() throws Exception {

        revertPasswordValidationRules(true);
        org.json.simple.JSONObject validationRulesInSubOrg =
                validationRulesRestClient.getValidationRulesForFieldInSubOrg(PASSWORD_FIELD, subOrgId);

        JSONArray rules = (JSONArray) validationRulesInSubOrg.get(RULES);
        for (Object rule : rules) {
            org.json.simple.JSONObject ruleObject = (org.json.simple.JSONObject) rule;
            if (ruleObject.get(VALIDATOR).equals(VALIDATION_RULES_LENGTH_VALIDATOR)) {
                JSONArray properties = (JSONArray) ruleObject.get(PROPERTIES);
                for (Object property : properties) {
                    org.json.simple.JSONObject propertyObject = (org.json.simple.JSONObject) property;
                    switch (propertyObject.get(KEY).toString()) {
                        case VALIDATION_RULES_MIN_LENGTH_PROPERTY:
                            Assert.assertEquals(propertyObject.get(VALUE).toString(),
                                    VALIDATION_RULES_ROOT_ORG_MIN_LENGTH,
                                    "Validation rules min length property is not inherited " +
                                            "correctly after reverting the configuration in sub-organization.");
                            break;
                        case VALIDATION_RULES_MAX_LENGTH_PROPERTY:
                            Assert.assertEquals(propertyObject.get(VALUE).toString(),
                                    VALIDATION_RULES_ROOT_ORG_MAX_LENGTH,
                                    "Validation rules max length property is not inherited correctly " +
                                            "after reverting the configuration in sub-organization.");
                            break;
                        default:
                            throw new AssertionError("Unexpected property found: "
                                    + propertyObject.get(NAME));
                    }
                }
            }
        }
    }

    @Test(dependsOnMethods = "testValidationRulesRevert",
            description = "Test if the inheritance is disabled when the root organization is v0.0.0")
    public void testInheritanceDisabled() throws Exception {

        orgMgtRestClient.updateOrganizationVersion(ORG_VERSION_V0);

        // When there are no overridden configurations in the sub-organization.
        // Validating governance configurations.
        org.json.simple.JSONObject defaultConfigInSubOrg =
                identityGovernanceRestClient.getConnectorInSubOrg(ACCOUNT_MGT_CATEGORY_ID, MULTI_ATTRIBUTE_CONNECTOR_ID,
                        switchedM2MToken);
        JSONArray defaultConnectorProperties = (JSONArray) defaultConfigInSubOrg.get(PROPERTIES);
        for (Object property : defaultConnectorProperties) {
            org.json.simple.JSONObject jsonProperty = (org.json.simple.JSONObject) property;
            switch (jsonProperty.get(NAME).toString()) {
                case MULTI_ATTRIBUTE_ENABLE_PROPERTY:
                    Assert.assertEquals(jsonProperty.get(VALUE).toString(), FALSE,
                            "Default value should be returned for multi attribute login handler enable property.");
                    break;
                case MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_PROPERTY:
                    Assert.assertEquals(jsonProperty.get(VALUE).toString(),
                            MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_DEFAULT_PROPERTY_VALUE,
                            "Default value should be returned for multi attribute allowed attributes property.");
                    break;
                default:
                    throw new AssertionError("Unexpected property found: " + jsonProperty.get(NAME));
            }
        }

        // Validating validation rules configurations.
        org.json.simple.JSONObject defaultValidationRulesInSubOrg =
                validationRulesRestClient.getValidationRulesForFieldInSubOrg(PASSWORD_FIELD, subOrgId);

        JSONArray rules = (JSONArray) defaultValidationRulesInSubOrg.get(RULES);
        for (Object rule : rules) {
            org.json.simple.JSONObject ruleObject = (org.json.simple.JSONObject) rule;
            if (ruleObject.get(VALIDATOR).equals(VALIDATION_RULES_LENGTH_VALIDATOR)) {
                JSONArray properties = (JSONArray) ruleObject.get(PROPERTIES);
                for (Object property : properties) {
                    org.json.simple.JSONObject propertyObject = (org.json.simple.JSONObject) property;
                    switch (propertyObject.get(KEY).toString()) {
                        case VALIDATION_RULES_MIN_LENGTH_PROPERTY:
                            Assert.assertEquals(propertyObject.get(VALUE).toString(),
                                    VALIDATION_RULES_DEFAULT_MIN_LENGTH,
                                    "Default value should be returned for validation rules min " +
                                            "length property since it's not overridden.");
                            break;
                        case VALIDATION_RULES_MAX_LENGTH_PROPERTY:
                            Assert.assertEquals(propertyObject.get(VALUE).toString(),
                                    VALIDATION_RULES_DEFAULT_MAX_LENGTH,
                                    "Default value should be returned for validation rules max " +
                                            "length property since it's not overridden.");
                            break;
                        default:
                            throw new AssertionError("Unexpected property found: "
                                    + propertyObject.get(NAME));
                    }
                }
            }
        }

        // When there are overridden configurations in the sub-organization.
        updateGovernanceConfig(true);
        updatePasswordValidationRules(true);

        // Validating governance configurations.
        org.json.simple.JSONObject updatedConfigInSubOrg =
                identityGovernanceRestClient.getConnectorInSubOrg(ACCOUNT_MGT_CATEGORY_ID, MULTI_ATTRIBUTE_CONNECTOR_ID,
                        switchedM2MToken);
        JSONArray overriddenConnectorProperties = (JSONArray) updatedConfigInSubOrg.get(PROPERTIES);
        for (Object property : overriddenConnectorProperties) {
            org.json.simple.JSONObject jsonProperty = (org.json.simple.JSONObject) property;
            switch (jsonProperty.get(NAME).toString()) {
                case MULTI_ATTRIBUTE_ENABLE_PROPERTY:
                    Assert.assertEquals(jsonProperty.get(VALUE).toString(), FALSE,
                            "Default value should be returned for Multi attribute login handler enable property " +
                                    "since it's not overridden.");
                    break;
                case MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_PROPERTY:
                    Assert.assertEquals(jsonProperty.get(VALUE).toString(),
                            MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_SUB_ORG_PROPERTY_VALUE,
                            "Multi attribute allowed attributes property should be overridden in sub-organization.");
                    break;
                default:
                    throw new AssertionError("Unexpected property found: " + jsonProperty.get(NAME));
            }
        }

        // Validating validation rules configurations.
        org.json.simple.JSONObject updatedValidationRulesInSubOrg =
                validationRulesRestClient.getValidationRulesForFieldInSubOrg(PASSWORD_FIELD, subOrgId);
        JSONArray updatedRules = (JSONArray) updatedValidationRulesInSubOrg.get(RULES);
        for (Object rule : updatedRules) {
            org.json.simple.JSONObject ruleObject = (org.json.simple.JSONObject) rule;
            if (ruleObject.get(VALIDATOR).equals(VALIDATION_RULES_LENGTH_VALIDATOR)) {
                JSONArray properties = (JSONArray) ruleObject.get(PROPERTIES);
                for (Object property : properties) {
                    org.json.simple.JSONObject propertyObject = (org.json.simple.JSONObject) property;
                    switch (propertyObject.get(KEY).toString()) {
                        case VALIDATION_RULES_MIN_LENGTH_PROPERTY:
                            Assert.assertEquals(propertyObject.get(VALUE).toString(),
                                    VALIDATION_RULES_SUB_ORG_MIN_LENGTH,
                                    "Validation rules min length property is not overridden correctly.");
                            break;
                        case VALIDATION_RULES_MAX_LENGTH_PROPERTY:
                            Assert.assertEquals(propertyObject.get(VALUE).toString(),
                                    VALIDATION_RULES_SUB_ORG_MAX_LENGTH,
                                    "Validation rules max length property is not overridden correctly.");
                            break;
                        default:
                            throw new AssertionError("Unexpected property found: "
                                    + propertyObject.get(NAME));
                    }
                }
            }
        }
    }

    private void updateGovernanceConfig(boolean isSubOrg) throws Exception {

        ConnectorsPatchReq connectorPatchReq = new ConnectorsPatchReq();
        connectorPatchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);

        if (!isSubOrg) {
            PropertyReq enableProperty = new PropertyReq();
            enableProperty.setName(MULTI_ATTRIBUTE_ENABLE_PROPERTY);
            enableProperty.setValue(TRUE);
            connectorPatchReq.addProperties(enableProperty);
        }

        PropertyReq allowedAttributesProperty = new PropertyReq();
        allowedAttributesProperty.setName(MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_PROPERTY);

        if (isSubOrg) {
            allowedAttributesProperty.setValue(MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_SUB_ORG_PROPERTY_VALUE);
        } else {
            allowedAttributesProperty.setValue(MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_ROOT_ORG_PROPERTY_VALUE);
        }
        connectorPatchReq.addProperties(allowedAttributesProperty);

        if (isSubOrg) {
            identityGovernanceRestClient.updateSubOrgConnector(ACCOUNT_MGT_CATEGORY_ID, MULTI_ATTRIBUTE_CONNECTOR_ID,
                    connectorPatchReq, switchedM2MToken);
        } else {
            identityGovernanceRestClient.updateConnectors(ACCOUNT_MGT_CATEGORY_ID, MULTI_ATTRIBUTE_CONNECTOR_ID,
                    connectorPatchReq);
        }
    }

    private void revertGovernanceConfig(boolean isSubOrg) throws Exception {

        PropertyRevertReq propertyRevertReq = new PropertyRevertReq();
        List<String> properties = new ArrayList<>();
        properties.add(MULTI_ATTRIBUTE_ALLOWED_ATTRIBUTES_PROPERTY);
        propertyRevertReq.setProperties(properties);

        if (isSubOrg) {
            identityGovernanceRestClient.revertSubOrgConnectorProperties(ACCOUNT_MGT_CATEGORY_ID,
                    MULTI_ATTRIBUTE_CONNECTOR_ID, propertyRevertReq, switchedM2MToken);
        } else {
            identityGovernanceRestClient.revertConnectorProperties(ACCOUNT_MGT_CATEGORY_ID,
                    MULTI_ATTRIBUTE_CONNECTOR_ID, propertyRevertReq);
        }
    }

    private void updatePasswordValidationRules(boolean isSubOrg) throws Exception {

        ValidationConfigModelForField validationConfig = new ValidationConfigModelForField();
        RuleModel rule1 = new RuleModel();

        MappingModel property1 = new MappingModel();
        property1.setKey(VALIDATION_RULES_MIN_LENGTH_PROPERTY);
        if (isSubOrg) {
            property1.setValue(VALIDATION_RULES_SUB_ORG_MIN_LENGTH);
        } else {
            property1.setValue(VALIDATION_RULES_ROOT_ORG_MIN_LENGTH);
        }

        MappingModel property2 = new MappingModel();
        property2.setKey(VALIDATION_RULES_MAX_LENGTH_PROPERTY);
        if (isSubOrg) {
            property2.setValue(VALIDATION_RULES_SUB_ORG_MAX_LENGTH);
        } else {
            property2.setValue(VALIDATION_RULES_ROOT_ORG_MAX_LENGTH);
        }

        rule1.setValidator(VALIDATION_RULES_LENGTH_VALIDATOR);
        rule1.setProperties(List.of(property1, property2));
        validationConfig.setRules(List.of(rule1));

        if (isSubOrg) {
            validationRulesRestClient.updateValidationRulesForFieldInSubOrg(PASSWORD_FIELD, validationConfig,
                    switchedM2MToken);
        } else {
            validationRulesRestClient.updateValidationRulesForField(PASSWORD_FIELD, validationConfig);
        }
    }

    private void revertPasswordValidationRules(boolean isSubOrg) throws Exception {

        RevertFields requestBody = new RevertFields();
        requestBody.setFields(List.of(PASSWORD_FIELD));

        if (isSubOrg) {
            validationRulesRestClient.revertValidationRulesInSubOrg(requestBody, switchedM2MToken);
        } else {
            validationRulesRestClient.revertValidationRules(requestBody);
        }
    }
}
