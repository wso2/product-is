package org.wso2.carbon.is.migration.service.v530.migrator;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.is.migration.MigrationClientException;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v530.ClaimManager;
import org.wso2.carbon.is.migration.service.v530.SQLConstants;
import org.wso2.carbon.is.migration.service.v530.bean.Claim;
import org.wso2.carbon.is.migration.service.v530.bean.MappedAttribute;
import org.wso2.carbon.is.migration.util.Utility;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClaimDataMigrator extends Migrator{

    private static Log log = LogFactory.getLog(ClaimDataMigrator.class);

    @Override
    public void migrate() throws MigrationClientException {
        migrateClaimData();
    }


    public boolean migrateClaimData() throws MigrationClientException {
        List<Claim> claims = new ArrayList<>();
        Connection umConnection = null;

        PreparedStatement loadDialectsStatement = null;
        PreparedStatement loadMappedAttributeStatement;

        PreparedStatement updateRole = null;

        ResultSet dialects = null;
        ResultSet claimResultSet;
        StringBuilder report = new StringBuilder();
        report.append("---------------------------------- WSO2 Identity Server 5.3.0 claim Migration Report -----------------------------------------\n \n");


        report.append("\n\n------------------------------------------------- Validating Existing Claims----------------------------------------------\n \n");

        //Is validation success. If not success, it will be created additional calims to be success
        boolean isSuccess = true;
        //This is used to record error log
        int count = 1;
        try {
            umConnection = getDataSource().getConnection();

            umConnection.setAutoCommit(false);

            loadDialectsStatement = umConnection.prepareStatement(SQLConstants.LOAD_CLAIM_DIALECTS);
            dialects = loadDialectsStatement.executeQuery();

            //This is used for validating multiple mapped attribute in each dialect. Format : dialectURL-->
            // MappedAttribute---> List of claim URIs. If any of the mapped attribute corresponds to multiple claim
            // URI, the validation should be false.
            Map<String, Map<String, List<String>>> data = new HashMap<>();

            List<Integer> inactiveTenants = new ArrayList<>();
            if (isIgnoreForInactiveTenants()) {
                inactiveTenants = Utility.getInactiveTenants();
            }

            while (dialects.next()) {
                //Keep the list of claim URI against domain Qualified Mapped Attribute
                Map<String, List<String>> mappedAttributes = new HashMap<>();

                int dialectId = dialects.getInt("UM_ID");
                String dialectUri = dialects.getString("UM_DIALECT_URI");
                int tenantId = dialects.getInt("UM_TENANT_ID");

                if (isIgnoreForInactiveTenants() && inactiveTenants.contains(tenantId)) {
                    log.info("Inactive tenant : " + tenantId + " , " +
                             "Skipping claim data migration for dialect : " + dialectUri);
                    continue;
                }

                loadMappedAttributeStatement = umConnection.prepareStatement(SQLConstants.LOAD_MAPPED_ATTRIBUTE);
                loadMappedAttributeStatement.setInt(1, dialectId);
                claimResultSet = loadMappedAttributeStatement.executeQuery();

                //Read all records in UM_CLAIM one by one
                while (claimResultSet.next()) {
                    List<String> claimURIs;
                    String attribute = claimResultSet.getString("UM_MAPPED_ATTRIBUTE");
                    String claimURI = claimResultSet.getString("UM_CLAIM_URI");
                    String displayTag = claimResultSet.getString("UM_DISPLAY_TAG");
                    String description = claimResultSet.getString("UM_DESCRIPTION");
                    String mappedAttributeDomain = claimResultSet.getString("UM_MAPPED_ATTRIBUTE_DOMAIN");
                    String regEx = claimResultSet.getString("UM_REG_EX");
                    int supportedByDefault = claimResultSet.getInt("UM_SUPPORTED");
                    int required = claimResultSet.getInt("UM_REQUIRED");
                    int displayOrder = claimResultSet.getInt("UM_DISPLAY_ORDER");
                    int readOnly = claimResultSet.getInt("UM_READ_ONLY");

                    boolean isRequired = required == 1;
                    boolean isSupportedByDefault = supportedByDefault == 1;
                    boolean isReadOnly = readOnly == 1;

                    Claim claimDTO = new Claim(claimURI, displayTag, description, regEx, isSupportedByDefault, isRequired, displayOrder, isReadOnly,
                                               tenantId, dialectUri);
                    if (claims.contains(claimDTO)) {
                        for (Claim claim : claims) {
                            if (claim.equals(claimDTO)) {
                                MappedAttribute mappedAttribute = new MappedAttribute(attribute, mappedAttributeDomain);
                                claim.getAttributes().add(mappedAttribute);
                                break;
                            }
                        }
                    } else {
                        MappedAttribute mappedAttribute = new MappedAttribute(attribute, mappedAttributeDomain);
                        List<MappedAttribute> mappedAttributesList = claimDTO.getAttributes();
                        mappedAttributesList.add(mappedAttribute);
                        claimDTO.setAttributes(mappedAttributesList);
                        claims.add(claimDTO);
                    }

                    String domainQualifiedAttribute;
                    if (StringUtils.isBlank(mappedAttributeDomain)) {
                        domainQualifiedAttribute = attribute;
                    } else {
                        domainQualifiedAttribute = mappedAttributeDomain + "/" + attribute;
                    }
                    if (mappedAttributes.get(domainQualifiedAttribute) != null) {
                        claimURIs = mappedAttributes.get(domainQualifiedAttribute);
                    } else {
                        claimURIs = new ArrayList<>();
                    }

                    claimURIs.add(claimURI);
                    mappedAttributes.put(domainQualifiedAttribute, claimURIs);
                }

                //get the tenant qualified dialect URL
                dialectUri = dialectUri + "@" + IdentityTenantUtil.getTenantDomain(tenantId);
                data.put(dialectUri, mappedAttributes);
            }

            //This is used to keep mapped attributes in each dialect in each tenant.
            // Format is tenantDomain:dialectURL->List of Mapped Attributes. If any remote dialect has a mapped
            // attribute which is not matching to any of the local claim's mapped attribute, the validation should be
            // false.
            Map<String, Map<String, List<String>>> tenantDialectMappedAttributes = new HashMap<>();

            for (Map.Entry<String, Map<String, List<String>>> entry : data.entrySet()) {

                //This is used to keep the mapped attributes against dialect URI
                Map<String, List<String>> dialectMappedAttributes = new HashMap<>();

                List<String> attributes = new ArrayList<>();
                String dialect = entry.getKey();
                String[] split = dialect.split("@");
                //separate the dialect URL and tenant domain from domain qualified dialect URL
                dialect = split[0];
                String tenantDomain = split[1];
                if (tenantDialectMappedAttributes.get(tenantDomain) != null) {
                    dialectMappedAttributes = tenantDialectMappedAttributes.get(tenantDomain);
                }

                if (dialectMappedAttributes.get(dialect) != null) {
                    attributes = dialectMappedAttributes.get(dialect);
                }

                if (entry.getValue() != null) {
                    for (Map.Entry<String, List<String>> claimEntry : entry.getValue().entrySet()) {
                        String mappedAttribute = claimEntry.getKey();
                        attributes.add(mappedAttribute.trim());
                        if (claimEntry.getValue() != null && claimEntry.getValue().size() > 1) {
                            isSuccess = false;

                            report.append(count + ")  Duplicate Mapped Attribute found for dialect :" + dialect +
                                          " | Mapped Attribute :" + mappedAttribute + " | " +
                                          "Relevant Claims : " + claimEntry.getValue() + " | Tenant Domain :" + tenantDomain);
                            report.append("\n\n");
                            if (log.isDebugEnabled()) {
                                log.debug("Duplicate Mapped Attribute found for dialect :" + dialect +
                                          " | Mapped Attribute :" + mappedAttribute + " | " +
                                          "Relevant Claims : " + claimEntry.getValue() + " | Tenant Domain :" + tenantDomain);
                            }

                            count++;
                        }
                    }

                    dialectMappedAttributes.put(entry.getKey().replace("@" + tenantDomain, ""), attributes);
                    tenantDialectMappedAttributes.put(tenantDomain, dialectMappedAttributes);
                }
            }

            //If there is no matching mapped attribute in each tenants remote dialect's claims in relevant tenant's
            // local dialect's claims mapped attributes, it is required to create a new mapping the local dialect
            // this variable is used to keep the new local claim URIs which needs to be added in each tenant. Format
            // is tenantDomain--> List of mappedAttributes
            Map<String, Set<String>> claimsToAddMap = new HashMap<>();
            if (tenantDialectMappedAttributes != null) {
                for (Map.Entry<String, Map<String, List<String>>> entry : tenantDialectMappedAttributes.entrySet()) {
                    String tenantDomain = entry.getKey();
                    Set<String> claimsToAdd = new HashSet<>();
                    if (claimsToAddMap.get(tenantDomain) != null) {
                        claimsToAdd = claimsToAddMap.get(tenantDomain);
                    }

                    if (entry.getValue() != null) {
                        List<String> localAttributes = entry.getValue().get(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
                        for (Map.Entry<String, List<String>> dialect : entry.getValue().entrySet()) {
                            if (!ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(dialect.getKey())) {
                                List<String> remoteClaimAttributes = dialect.getValue();
                                if (remoteClaimAttributes != null) {
                                    for (String remoteClaimAttribute : remoteClaimAttributes) {
                                        if (!localAttributes.contains(remoteClaimAttribute)) {
                                            claimsToAdd.add(remoteClaimAttribute);
                                            isSuccess = false;
                                            report.append("\n\n" + count + ")  Mapped Attribute : " +
                                                          remoteClaimAttribute + " in dialect :" + dialect.getKey() + " is not associated to any of the local claim in tenant domain: " + tenantDomain);

                                            if (log.isDebugEnabled()) {
                                                log.debug("Mapped Attribute : " + remoteClaimAttribute + " in dialect :"
                                                          + dialect.getKey() + " is not associated to any of the local claim in tenant domain: " + tenantDomain);
                                            }
                                            count++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    claimsToAddMap.put(tenantDomain, claimsToAdd);
                }
            }

        } catch (SQLException e) {
            log.error("Error while validating claim management data", e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(dialects);
            IdentityDatabaseUtil.closeStatement(loadDialectsStatement);
            IdentityDatabaseUtil.closeStatement(updateRole);
            IdentityDatabaseUtil.closeConnection(umConnection);
        }


        // Migrating claim Data starts here.
        ClaimManager claimManager = ClaimManager.getInstance();

        if (claims != null) {

            report.append("\n\n------------------------------------------------------------------------------ Claim " +
                          "Migration -------------------------------------------------------------------------------\n \n");
            try {
                // Add Claim Dialects
                claimManager.addClaimDialects(claims, report);

                // Add Local Claims.
                claimManager.addLocalClaims(claims, report);

                // Add External Claims
                claimManager.addExternalClaim(claims, report);
            } catch (MigrationClientException e) {
                log.error("Error while migrating claim data", e);
            }
        }

        if (!isSuccess) {
            PrintWriter out = null;
            try {
                out = new PrintWriter("claim-migration.txt");
                out.println(report.toString());
            } catch (FileNotFoundException e) {
                log.error("Error while creating claim Migration Report");
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
        return isSuccess;
    }
}
