/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.is.migration.service.v530;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityIOStreamUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.identity.recovery.util.Utils;
import org.wso2.carbon.is.migration.util.Utility;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.user.api.Tenant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.context.RegistryType.SYSTEM_CONFIGURATION;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.Questions.CHALLENGE_QUESTION_ID;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.Questions.CHALLENGE_QUESTION_LOCALE;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.Questions.CHALLENGE_QUESTION_SET_ID;
import static org.wso2.carbon.registry.core.RegistryConstants.PATH_SEPARATOR;
import static org.wso2.carbon.registry.core.RegistryConstants.TAG_MEDIA_TYPE;

public class RegistryDataManager {

    private static RegistryDataManager instance = new RegistryDataManager();

    private static final Log log = LogFactory.getLog(RegistryDataManager.class);

    private static final String SCOPE_RESOURCE_PATH = "/oidc";

    private static final String EMAIL_TEMPLATE_OLD_REG_LOCATION = "/identity/config/emailTemplate";
    private static final String EMAIL_TEMPLATE_NEW_REG_LOCATION_ROOT = "/identity/email/";
    private static final Set<String> TEMPLATE_NAMES = new HashSet<String>() {{
        add("accountConfirmation");
        add("accountDisable");
        add("accountEnable");
        add("accountIdRecovery");
        add("accountUnLock");
        add("askPassword");
        add("otp");
        add("passwordReset");
        add("temporaryPassword");
    }};

    private static final Map<String, String> PLACEHOLDERS_MAP = new HashMap<String, String>() {{
        put("\\{first-name\\}", "{{user.claim.givenname}}");
        put("\\{user-name\\}", "{{user-name}}");
        put("\\{confirmation-code\\}", "{{confirmation-code}}");
        put("\\{userstore-domain\\}", "{{userstore-domain}}");
        put("\\{url:user-name\\}", "{{url:user-name}}");
        put("\\{tenant-domain\\}", "{{tenant-domain}}");
        put("\\{temporary-password\\}", "{{temporary-password}}");
    }};


    /*
        Constants related challenge question migration.
     */
    private static final String OLD_CHALLENGE_QUESTIONS_PATH =
            "/repository/components/org.wso2.carbon.identity.mgt/questionCollection";
    private static final String NEW_CHALLENGE_QUESTIONS_PATH = "/identity/questionCollection";

    private static final String OLD_QUESTION_SET_PROPERTY = "questionSetId";
    private static final String OLD_QUESTION_PROPERTY = "question";


    private static final String DEFAULT_LOCALE = "en_US";
    private static final String TEMPLATE_NAME = "migratedQuestion%d";


    private RegistryDataManager() {

    }

    public static RegistryDataManager getInstance() {

        return instance;
    }

    @Deprecated
    public void migrateEmailTemplates(boolean migrateActiveTenantsOnly) throws Exception {

        //migrating super tenant configurations
        try {
            migrateTenantEmailTemplates();
            log.info("Email templates migrated for tenant : " + SUPER_TENANT_DOMAIN_NAME);
        } catch (Exception e) {
            log.error("Error while migrating email templates for tenant : " + SUPER_TENANT_DOMAIN_NAME, e);
        }

        //migrating tenant configurations
        Set<Tenant> tenants = Utility.getTenants();
        for (Tenant tenant : tenants) {
            if (migrateActiveTenantsOnly && !tenant.isActive()) {
                log.info("Tenant " + tenant.getDomain() + " is inactive. Skipping Email Templates migration!!!!");
                continue;
            }
            try {
                startTenantFlow(tenant);
                IdentityTenantUtil.getTenantRegistryLoader().loadTenantRegistry(tenant.getId());
                migrateTenantEmailTemplates();
                log.info("Email templates migrated for tenant : " + tenant.getDomain());
            } catch (Exception e) {
                log.error("Error while migrating email templates for tenant : " + tenant.getDomain(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    public void migrateEmailTemplates(boolean migrateActiveTenantsOnly, boolean continueOnError) throws Exception {

        //migrating super tenant configurations
        try {
            migrateTenantEmailTemplates();
            log.info("Email templates migrated for tenant : " + SUPER_TENANT_DOMAIN_NAME);
        } catch (Exception e) {
            String msg = "Error while migrating email templates for tenant : " + SUPER_TENANT_DOMAIN_NAME;
            if (!continueOnError) {
                throw e;
            }
            log.error(msg, e);
        }

        //migrating tenant configurations
        Set<Tenant> tenants = Utility.getTenants();
        for (Tenant tenant : tenants) {
            if (migrateActiveTenantsOnly && !tenant.isActive()) {
                log.info("Tenant " + tenant.getDomain() + " is inactive. Skipping Email Templates migration!!!!");
                continue;
            }
            try {
                startTenantFlow(tenant);
                IdentityTenantUtil.getTenantRegistryLoader().loadTenantRegistry(tenant.getId());
                migrateTenantEmailTemplates();
                log.info("Email templates migrated for tenant : " + tenant.getDomain());
            } catch (Exception e) {
                if (!continueOnError) {
                    throw e;
                }
                log.error("Error while migrating email templates for tenant : " + tenant.getDomain(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }


    private void migrateTenantEmailTemplates() throws IdentityException {

        Registry registry = PrivilegedCarbonContext.getThreadLocalCarbonContext().getRegistry(SYSTEM_CONFIGURATION);
        try {
            if (registry.resourceExists(EMAIL_TEMPLATE_OLD_REG_LOCATION)) {
                Properties properties = registry.get(EMAIL_TEMPLATE_OLD_REG_LOCATION).getProperties();

                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    if (!TEMPLATE_NAMES.contains(entry.getKey())) {
                        log.info("Skipping probable invalid template :" + entry.getKey());
                        continue;
                    }
                    String[] templateParts = ((List<String>) entry.getValue()).get(0).split("\\|");
                    if (templateParts.length != 3) {
                        log.warn("Skipping invalid template data. Expected 3 sections, but contains " +
                                templateParts.length);
                    }
                    String newResourcePath =
                            EMAIL_TEMPLATE_NEW_REG_LOCATION_ROOT + entry.getKey().toString().toLowerCase() +
                                    "/en_us";
                    String newContent = String.format("[\"%s\",\"%s\",\"%s\"]",
                            updateContent(templateParts[0]),
                            updateContent(templateParts[1]),
                            updateContent(templateParts[2]));
                    Resource resource;
                    if (registry.resourceExists(newResourcePath)) {
                        resource = registry.get(newResourcePath);
                    } else {
                        resource = registry.newResource();
                        resource.addProperty("display", (String) entry.getKey());
                        resource.addProperty("type", (String) entry.getKey());
                        resource.addProperty("emailContentType", "text/plain");
                        resource.addProperty("locale", "en_US");
                    }
                    resource.setContent(newContent);
                    resource.setMediaType("tag");
                    registry.put(newResourcePath, resource);
                }
            }
        } catch (RegistryException e) {
            throw IdentityException.error("Error while migration registry data", e);
        }
    }

    private String updateContent(String s) {

        //update the placeholders
        for (Map.Entry<String, String> entry : PLACEHOLDERS_MAP.entrySet()) {
            s = s.replaceAll(entry.getKey(), entry.getValue());
        }

        //update the new line
        s = s.replaceAll("\n", "\\\\n");
        return s;
    }

    @Deprecated
    public void migrateChallengeQuestions(boolean migrateActiveTenantsOnly) throws Exception {
        //migrating super tenant configurations
        try {
            migrateChallengeQuestionsForTenant();
            log.info("Challenge Questions migrated for tenant : " + SUPER_TENANT_DOMAIN_NAME);
        } catch (Exception e) {
            log.error("Error while migrating challenge questions for tenant : " + SUPER_TENANT_DOMAIN_NAME, e);
        }

        //migrating tenant configurations
        Set<Tenant> tenants = Utility.getTenants();
        for (Tenant tenant : tenants) {
            if (migrateActiveTenantsOnly && !tenant.isActive()) {
                log.info("Tenant " + tenant.getDomain() + " is inactive. Skipping challenge question migration.");
                continue;
            }
            try {
                startTenantFlow(tenant);
                IdentityTenantUtil.getTenantRegistryLoader().loadTenantRegistry(tenant.getId());
                migrateChallengeQuestionsForTenant();
                log.info("Challenge Questions migrated for tenant : " + tenant.getDomain());
            } catch (Exception e) {
                log.error("Error while migrating challenge questions for tenant : " + tenant.getDomain(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    public void migrateChallengeQuestions(boolean migrateActiveTenantsOnly, boolean continueOnError) throws Exception {

        //migrating super tenant configurations
        try {
            migrateChallengeQuestionsForTenant();
            log.info("Challenge Questions migrated for tenant : " + SUPER_TENANT_DOMAIN_NAME);
        } catch (Exception e) {
            if (!continueOnError) {
                throw e;
            }
            log.error("Error while migrating challenge questions for tenant : " + SUPER_TENANT_DOMAIN_NAME, e);
        }

        //migrating tenant configurations
        Set<Tenant> tenants = Utility.getTenants();
        for (Tenant tenant : tenants) {
            if (migrateActiveTenantsOnly && !tenant.isActive()) {
                log.info("Tenant " + tenant.getDomain() + " is inactive. Skipping challenge question migration!!!!");
                continue;
            }
            try {
                startTenantFlow(tenant);
                IdentityTenantUtil.getTenantRegistryLoader().loadTenantRegistry(tenant.getId());
                migrateChallengeQuestionsForTenant();
                log.info("Challenge Questions migrated for tenant : " + tenant.getDomain());
            } catch (Exception e) {
                if (!continueOnError) {
                    throw e;
                }
                log.error("Error while migrating challenge questions for tenant : " + tenant.getDomain(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }


    private void migrateChallengeQuestionsForTenant() throws Exception {
        // read the old questions
        Registry registry = PrivilegedCarbonContext.getThreadLocalCarbonContext().getRegistry(SYSTEM_CONFIGURATION);
        try {
            if (registry.resourceExists(OLD_CHALLENGE_QUESTIONS_PATH)) {
                Collection questionCollection = (Collection) registry.get(OLD_CHALLENGE_QUESTIONS_PATH);

                Map<String, Integer> countMap = new HashMap<>();
                for (String challengeQuestionPath : questionCollection.getChildren()) {
                    // old challenge question.
                    Resource oldQuestion = registry.get(challengeQuestionPath);

                    String questionSetId = oldQuestion.getProperty(OLD_QUESTION_SET_PROPERTY);
                    String question = oldQuestion.getProperty(OLD_QUESTION_PROPERTY);

                    // find the correct question number for migrated questions
                    int questionCount = countMap.containsKey(questionSetId) ? countMap.get(questionSetId) : 1;
                    countMap.put(questionSetId, questionCount + 1);

                    String questionId = String.format(TEMPLATE_NAME, questionCount);

                    ChallengeQuestion challengeQuestion =
                            new ChallengeQuestion(questionSetId, questionId, question, DEFAULT_LOCALE);

                    // Create a registry resource for the new Challenge Question.
                    Resource resource = createRegistryResource(challengeQuestion);
                    registry.put(getQuestionPath(challengeQuestion), resource);
                }

            }
        } catch (RegistryException e) {
            throw IdentityException.error("Error while migration challenge question registry data", e);
        }
    }


    private Resource createRegistryResource(ChallengeQuestion question) throws RegistryException,
                                                                               UnsupportedEncodingException {

        Resource resource = new ResourceImpl();
        resource.setContent(question.getQuestion().getBytes("UTF-8"));
        resource.addProperty(CHALLENGE_QUESTION_SET_ID, question.getQuestionSetId());
        resource.addProperty(CHALLENGE_QUESTION_ID, question.getQuestionId());
        resource.addProperty(CHALLENGE_QUESTION_LOCALE, question.getLocale());
        resource.setMediaType(TAG_MEDIA_TYPE);

        return resource;
    }

    /**
     * Get the relative path to the parent directory of the challenge question resource.
     *
     * @param challengeQuestion challenge question to which the path is calculated
     * @return Path to the parent of challenge question relative to the root of the registry.
     */
    private String getQuestionPath(ChallengeQuestion challengeQuestion) {
        // challenge set uri
        String questionSetIdUri = challengeQuestion.getQuestionSetId();
        String questionId = challengeQuestion.getQuestionId();
        String questionSetId = Utils.getChallengeSetDirFromUri(questionSetIdUri);
        String locale = challengeQuestion.getLocale().toLowerCase();

        return NEW_CHALLENGE_QUESTIONS_PATH + PATH_SEPARATOR + questionSetId + PATH_SEPARATOR + questionId +
                PATH_SEPARATOR + locale;
    }


    private void startTenantFlow(Tenant tenant) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantId(tenant.getId());
        carbonContext.setTenantDomain(tenant.getDomain());
    }

    private void startTenantFlow(String tenantDomain) {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantId(tenantId);
        carbonContext.setTenantDomain(tenantDomain);
    }

    @Deprecated
    public void copyOIDCScopeData(boolean migrateActiveTenantsOnly) throws Exception {

        // since copying oidc-config file for super tenant is handled by the OAuth component we only need to handle
        // this in migrated tenants.
        Set<Tenant> tenants = Utility.getTenants();
        for (Tenant tenant : tenants) {
            if (migrateActiveTenantsOnly && !tenant.isActive()) {
                log.info("Tenant " + tenant.getDomain() + " is inactive. Skipping copying OIDC Scopes Data !!!!");
                continue;
            }
            try {
                startTenantFlow(tenant);
                IdentityTenantUtil.getTenantRegistryLoader().loadTenantRegistry(tenant.getId());
                initiateOIDCScopes();
                log.info("OIDC Scope data migrated for tenant : " + tenant.getDomain());
            } catch (RegistryException | FileNotFoundException e) {
                log.error("Error while migrating OIDC Scope data for tenant:  " + tenant.getDomain(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    public void copyOIDCScopeData(boolean migrateActiveTenantsOnly, boolean continueOnError) throws Exception {

        copyOIDCScopeDataOfSuperTenant(continueOnError);

        Set<Tenant> tenants = Utility.getTenants();
        for (Tenant tenant : tenants) {
            if (migrateActiveTenantsOnly && !tenant.isActive()) {
                log.info("Tenant " + tenant.getDomain() + " is inactive. Skipping copying OIDC Scopes Data.");
                continue;
            }
            try {
                startTenantFlow(tenant);
                IdentityTenantUtil.getTenantRegistryLoader().loadTenantRegistry(tenant.getId());
                initiateOIDCScopes();
                log.info("OIDC Scope data migrated for tenant : " + tenant.getDomain());
            } catch (RegistryException | FileNotFoundException | IdentityException e) {
                if (!continueOnError) {
                    throw e;
                }
                log.error("Error while migrating OIDC Scope data for tenant:  " + tenant.getDomain(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private void copyOIDCScopeDataOfSuperTenant(boolean continueOnError)
            throws FileNotFoundException, IdentityException, RegistryException {

        try {
            startTenantFlow(SUPER_TENANT_DOMAIN_NAME);
            IdentityTenantUtil.getTenantRegistryLoader().loadTenantRegistry(SUPER_TENANT_ID);
            initiateOIDCScopes();
            log.info("OIDC Scope data migrated for tenant: " + SUPER_TENANT_DOMAIN_NAME);
        } catch (RegistryException | FileNotFoundException | IdentityException e) {
            if (!continueOnError) {
                throw e;
            }
            log.error("Error while migrating OIDC Scope data for tenant:  " + SUPER_TENANT_DOMAIN_NAME, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void initiateOIDCScopes() throws RegistryException, FileNotFoundException, IdentityException {

        Map<String, String> scopes = loadScopeConfigFile();
        Registry registry = PrivilegedCarbonContext.getThreadLocalCarbonContext().getRegistry(SYSTEM_CONFIGURATION);
        if(!registry.resourceExists(SCOPE_RESOURCE_PATH)) {
            Resource resource = registry.newResource();
                for (Map.Entry<String, String> entry : scopes.entrySet()) {
                    resource.setProperty(entry.getKey(), entry.getValue());
            }

            registry.put("/oidc", resource);
        }
    }

    private static Map<String, String> loadScopeConfigFile() throws FileNotFoundException, IdentityException {

        Map<String, String> scopes = new HashMap<>();
        String carbonHome = System.getProperty("carbon.home");
        String confXml = Paths.get(carbonHome,
                new String[]{"repository", "conf", "identity", "oidc-scope-config.xml"}).toString();
        File configfile = new File(confXml);
        if(!configfile.exists()) {
            String errMsg = "OIDC scope-claim Configuration File is not present at: " + confXml;
            throw new FileNotFoundException(errMsg);
        }

        XMLStreamReader parser = null;
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(configfile);
            parser = XMLInputFactory.newInstance().createXMLStreamReader(stream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement documentElement = builder.getDocumentElement();
            Iterator iterator = documentElement.getChildElements();

            while(iterator.hasNext()) {
                OMElement omElement = (OMElement)iterator.next();
                String configType = omElement.getAttributeValue(new QName("id"));
                scopes.put(configType, loadClaimConfig(omElement));
            }
        } catch (XMLStreamException ex) {
            throw IdentityException.error("Error while loading scope config.", ex);
        } finally {
            try {
                if(parser != null) {
                    parser.close();
                }

                if(stream != null) {
                    IdentityIOStreamUtils.closeInputStream(stream);
                }
            } catch (XMLStreamException ex) {
                log.error("Error while closing XML stream", ex);
            }

        }

        return scopes;
    }

    private static String loadClaimConfig(OMElement configElement) {
        StringBuilder claimConfig = new StringBuilder();
        Iterator it = configElement.getChildElements();

        while(it.hasNext()) {
            OMElement element = (OMElement)it.next();
            if("Claim".equals(element.getLocalName())) {
                String commaSeparatedClaimNames = element.getText();
                if(StringUtils.isNotBlank(commaSeparatedClaimNames)) {
                    claimConfig.append(commaSeparatedClaimNames.trim());
                }
            }
        }

        return claimConfig.toString();
    }


}
