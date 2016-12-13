/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.is.migration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.identity.recovery.util.Utils;
import org.wso2.carbon.is.migration.client.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.user.api.Tenant;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.context.RegistryType.SYSTEM_CONFIGURATION;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.Questions.CHALLENGE_QUESTION_ID;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.Questions.CHALLENGE_QUESTION_LOCALE;
import static org.wso2.carbon.identity.recovery.IdentityRecoveryConstants.Questions.CHALLENGE_QUESTION_SET_ID;
import static org.wso2.carbon.registry.core.RegistryConstants.PATH_SEPARATOR;
import static org.wso2.carbon.registry.core.RegistryConstants.TAG_MEDIA_TYPE;

public class RegistryDataManager {

    private static RegistryDataManager instance = new RegistryDataManager();

    private static final Log log = LogFactory.getLog(RegistryDataManager.class);

    private static final String EMAIL_TEMPLATE_OLD_REG_LOCATION = "/identity/config/emailTemplate";
    private static final String EMAIL_TEMPLATE_NEW_REG_LOCATION_ROOT = "/identity/Email/";
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
    private static final String OLD_CHALLENGE_QUESTIONS_PATH = "/repository/components/org.wso2.carbon.identity.mgt/questionCollection";
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

    public void migrateEmailTemplates() throws Exception {

        //migrating super tenant configurations
        try {
            migrateTenantEmailTemplates();
            log.info("Email templates migrated for tenant : " + SUPER_TENANT_DOMAIN_NAME);
        } catch (Exception e) {
            log.error("Error while migrating email templates for tenant : " + SUPER_TENANT_DOMAIN_NAME, e);
        }

        //migrating tenant configurations
        Tenant[] tenants = ISMigrationServiceDataHolder.getRealmService().getTenantManager().getAllTenants();
        for (Tenant tenant : tenants) {
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


    public void migrateChallengeQuestions() throws Exception {
        //migrating super tenant configurations
        try {
            migrateChallengeQuestionsForTenant();
            log.info("Challenge Questions migrated for tenant : " + SUPER_TENANT_DOMAIN_NAME);
        } catch (Exception e) {
            log.error("Error while migrating challenge questions for tenant : " + SUPER_TENANT_DOMAIN_NAME, e);
        }

        //migrating tenant configurations
        Tenant[] tenants = ISMigrationServiceDataHolder.getRealmService().getTenantManager().getAllTenants();
        for (Tenant tenant : tenants) {
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


}
