/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.is.migration.service.v530.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ClaimDialectDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.is.migration.service.v530.SQLConstants;
import org.wso2.carbon.is.migration.service.v530.bean.Claim;
import org.wso2.carbon.is.migration.service.v530.bean.MappedAttribute;
import org.wso2.carbon.utils.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Claim DAO
 */
public class ClaimDAO {

    private static Log log = LogFactory.getLog(ClaimDialectDAO.class);

    private static ClaimDAO claimDAO = new ClaimDAO();

    private ClaimDAO() {
    }

    public static ClaimDAO getInstance() {
        return claimDAO;
    }

    /**
     * Add claim dialect
     *
     * @param claimDialectURI
     * @param tenantId
     * @throws MigrationClientException
     */
    public void addClaimDialect(String claimDialectURI, int tenantId) throws MigrationClientException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        String query = SQLConstants.ADD_CLAIM_DIALECT;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, claimDialectURI);
            prepStmt.setInt(2, tenantId);
            prepStmt.executeUpdate();
            connection.commit();

        } catch (SQLException e) {
            throw new MigrationClientException("Error while adding claim dialect " + claimDialectURI, e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Get claim dialect
     *
     * @param claimDialectURI
     * @param tenantId
     * @return
     * @throws MigrationClientException
     */
    public int getClaimDialect(String claimDialectURI, int tenantId) throws MigrationClientException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        String getQuery = SQLConstants.GET_CLAIM_DIALECT;
        ResultSet rs;
        int id = 0;

        try {
            prepStmt = connection.prepareStatement(getQuery);
            prepStmt.setString(1, claimDialectURI);
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();

            if (rs.next()) {
                id = rs.getInt("ID");
            }

        } catch (SQLException e) {
            throw new MigrationClientException("Error while adding claim dialect " + claimDialectURI, e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
        return id;
    }

    /**
     * Add local claim
     *
     * @param claim
     * @throws MigrationClientException
     */
    public void addLocalClaim(Claim claim) throws MigrationClientException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        String localClaimURI = claim.getClaimURI();
        int localClaimId = 0;
        try {
            // Start transaction
            connection.setAutoCommit(false);
            localClaimId = getClaimId(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI, localClaimURI,
                                      claim.getTenantId());
            if (localClaimId == 0) {
                //Claim record is not in the table
                //Add record
                localClaimId = addClaim(connection, claim.getDialectURI(), localClaimURI, claim.getTenantId());

                // Some JDBC Drivers returns this in the result, some don't
                if (localClaimId == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("JDBC Driver did not return the claimId, executing Select operation");
                    }
                    localClaimId = getClaimId(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI, localClaimURI,
                                              claim.getTenantId());
                }

                Map<String, String> claimProperties = new HashMap<>();
                //Reason for null check, Oracle db not allows to insert null values
                if (StringUtils.isNotBlank(claim.getDescription())) {
                    claimProperties.put("Description", claim.getDescription());
                }

                if (StringUtils.isNotBlank(claim.getDisplayTag())) {
                    claimProperties.put("DisplayName", claim.getDisplayTag());
                }

                if (StringUtils.isNotBlank(claim.getRegEx())) {
                    claimProperties.put("RegEx", claim.getRegEx());
                }

                if (StringUtils.isNotBlank(String.valueOf(claim.getDisplayOrder()))) {
                    claimProperties.put("DisplayOrder", String.valueOf(claim.getDisplayOrder()));
                }

                if (StringUtils.isNotBlank(String.valueOf(claim.isReadOnly()))) {
                    claimProperties.put("ReadOnly", String.valueOf(claim.isReadOnly()));
                }

                if (StringUtils.isNotBlank(String.valueOf(claim.isRequired()))) {
                    claimProperties.put("Required", String.valueOf(claim.isRequired()));
                }

                if (StringUtils.isNotBlank(String.valueOf(claim.isSupportedByDefault()))) {
                    claimProperties.put("SupportedByDefault", String.valueOf(claim.isSupportedByDefault()));
                }

                addClaimProperties(connection, localClaimId, claimProperties, claim.getTenantId());

                for (MappedAttribute mappedAttribute : claim.getAttributes()) {
                    addClaimAttributeMappings(connection, localClaimId, mappedAttribute.getAttribute(),
                                              mappedAttribute.getDomain(), claim.getTenantId());
                }
            }

            // End transaction
            connection.commit();
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollBack(connection);
            throw new MigrationClientException("Error while adding local claim " + localClaimURI, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Add claim
     *
     * @param connection
     * @param claimDialectURI
     * @param claimURI
     * @param tenantId
     * @return
     * @throws MigrationClientException
     */
    public int addClaim(Connection connection, String claimDialectURI, String claimURI, int tenantId)
            throws MigrationClientException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        int claimId = 0;
        String query = SQLConstants.ADD_CLAIM;
        try {
            String dbProductName = connection.getMetaData().getDatabaseProductName();
            prepStmt = connection.prepareStatement(query, new String[]{
                    DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, SQLConstants.ID_COLUMN)});

            prepStmt.setString(1, claimDialectURI);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, claimURI);
            prepStmt.setInt(4, tenantId);
            prepStmt.executeUpdate();

            rs = prepStmt.getGeneratedKeys();

            if (rs.next()) {
                claimId = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new MigrationClientException("Error while adding claim " + claimURI + " to dialect " +
                                               claimDialectURI, e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(rs);
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }

        return claimId;
    }

    /**
     * Add claim attribute mapping
     *
     * @param connection
     * @param localClaimId
     * @param mappedAttribute
     * @param mappedAttributeDomain
     * @param tenantId
     * @throws MigrationClientException
     */
    private void addClaimAttributeMappings(Connection connection, int localClaimId, String mappedAttribute,
                                           String mappedAttributeDomain, int tenantId) throws MigrationClientException {

        PreparedStatement prepStmt = null;
        if (localClaimId > 0 && mappedAttribute != null) {
            try {
                if (mappedAttributeDomain == null) {
                    mappedAttributeDomain = IdentityUtil.getPrimaryDomainName();
                }
                String query = SQLConstants.ADD_CLAIM_MAPPED_ATTRIBUTE;
                prepStmt = connection.prepareStatement(query);

                prepStmt.setInt(1, localClaimId);
                prepStmt.setString(2, mappedAttributeDomain);
                prepStmt.setString(3, mappedAttribute);
                prepStmt.setInt(4, tenantId);

                prepStmt.execute();
            } catch (SQLException e) {
                throw new MigrationClientException("Error while adding attribute mappings", e);
            } finally {
                IdentityDatabaseUtil.closeStatement(prepStmt);
            }
        }
    }

    /**
     * Add claim properties
     *
     * @param connection
     * @param localClaimId
     * @param claimProperties
     * @param tenantId
     * @throws MigrationClientException
     */
    private void addClaimProperties(Connection connection, int localClaimId, Map<String, String> claimProperties,
                                    int tenantId) throws MigrationClientException {

        PreparedStatement prepStmt = null;
        if (localClaimId > 0 && claimProperties != null) {
            try {
                String query = SQLConstants.ADD_CLAIM_PROPERTY;
                prepStmt = connection.prepareStatement(query);

                for (Map.Entry<String, String> property : claimProperties.entrySet()) {
                    prepStmt.setInt(1, localClaimId);
                    prepStmt.setString(2, property.getKey());
                    prepStmt.setString(3, property.getValue() != null ? property.getValue() : "");
                    prepStmt.setInt(4, tenantId);
                    prepStmt.addBatch();
                }

                prepStmt.executeBatch();
            } catch (SQLException e) {
                throw new MigrationClientException("Error while adding claim properties", e);
            } finally {
                IdentityDatabaseUtil.closeStatement(prepStmt);
            }
        }
    }

    /**
     * Get claim Id
     *
     * @param connection
     * @param claimDialectURI
     * @param claimURI
     * @param tenantId
     * @return
     * @throws MigrationClientException
     */
    public int getClaimId(Connection connection, String claimDialectURI, String claimURI, int tenantId)
            throws MigrationClientException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        int claimId = 0;
        String query = SQLConstants.GET_CLAIM_ID;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, claimDialectURI);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, claimURI);
            prepStmt.setInt(4, tenantId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                claimId = rs.getInt(SQLConstants.ID_COLUMN);
            }
        } catch (SQLException e) {
            throw new MigrationClientException("Error while retrieving ID for claim " + claimURI + " in dialect "
                                               + claimDialectURI, e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(rs);
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
        return claimId;
    }

    public String getClaimURI(Connection connection, String claimDialectURI, int claimId, int tenantId)
            throws MigrationClientException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String claimURI = null;
        String query = SQLConstants.GET_CLAIM_URI;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, claimDialectURI);
            prepStmt.setInt(2, tenantId);
            prepStmt.setInt(3, claimId);
            prepStmt.setInt(4, tenantId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                claimURI = rs.getString("CLAIM_URI");
            }
        } catch (SQLException e) {
            throw new MigrationClientException("Error while retrieving URI for claim " + claimURI + " in dialect "
                                               + claimDialectURI, e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(rs);
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
        return claimURI;
    }


    /**
     * Get claim Id from mapped Attribute
     *
     * @param connection
     * @param mappedAttribute
     * @param mappedAttributeDomain
     * @param tenantId
     * @return
     * @throws MigrationClientException
     */
    public List<Integer> getClaimIdFromMappedAttributes(Connection connection, String mappedAttribute,
                                                        String mappedAttributeDomain, int tenantId)
            throws MigrationClientException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        List<Integer> localIds = new ArrayList<>();
        String query = SQLConstants.GET_CLAIM_FROM_MAPPED_ATTRIBUTE;
        try {
            if (mappedAttributeDomain == null) {
                mappedAttributeDomain = IdentityUtil.getPrimaryDomainName();
            }
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, mappedAttributeDomain);
            prepStmt.setString(2, mappedAttribute);
            prepStmt.setInt(3, tenantId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                localIds.add(rs.getInt("LOCAL_CLAIM_ID"));
            }
        } catch (SQLException e) {
            throw new MigrationClientException("Error while retrieving LOCAL_CLAIM_ID for claim " + mappedAttribute +
                                               " " + "in tenant : " + IdentityTenantUtil.getTenantDomain(tenantId), e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(rs);
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
        return localIds;
    }

    /**
     * Add External claim
     *
     * @param claim
     */
    public void addExternalClaim(Claim claim, StringBuilder report) throws MigrationClientException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();

        String externalClaimURI = claim.getClaimURI();
        String externalClaimDialectURI = claim.getDialectURI();
        int externalClaimId = 0;
        try {
            // Start transaction
            connection.setAutoCommit(false);
            externalClaimId = getClaimId(connection, externalClaimDialectURI, externalClaimURI, claim.getTenantId());

            //Nothing to do if the claim is already exists
            if (externalClaimId == 0) {
                externalClaimId = addClaim(connection, externalClaimDialectURI, externalClaimURI, claim.getTenantId());

                // Some JDBC Drivers returns this in the result, some don't
                if (externalClaimId == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("JDBC Driver did not return the claimId, executing Select operation");
                    }
                    externalClaimId = getClaimId(connection, externalClaimDialectURI, externalClaimURI,
                                                 claim.getTenantId());
                }

                //Ge the relevant local claim from mapped attributes.If all mapped attributes are matching to the
                // same local claim, add that association. If the multiple local claims are matching to remote claim,
                // we can't add multiple mapping to one remote claim. So, create a new local claim and add mapping to
                // that claim with mapped attributes

                List<Integer> commonLocalClaimIds = null;

                for (MappedAttribute mappedAttribute : claim.getAttributes()) {
                    List<Integer> localClaimIds = getClaimIdFromMappedAttributes(connection, mappedAttribute
                            .getAttribute(), mappedAttribute.getDomain(), claim.getTenantId());
                    if (commonLocalClaimIds == null) {
                        commonLocalClaimIds = localClaimIds;
                    } else {
                        commonLocalClaimIds = intersection(commonLocalClaimIds, localClaimIds);
                    }
                }

                if (commonLocalClaimIds.size() > 0) {
                    addClaimMapping(connection, externalClaimId, commonLocalClaimIds.get(0), claim.getTenantId());
                    String localClaimURI = getClaimURI(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI,
                                                       commonLocalClaimIds.get(0), claim.getTenantId());

                    report.append("\n\n Added Remote Claim  :" + claim.getDialectURI() + " in Dialect : " + claim
                            .getDialectURI()
                                  + " in  tenant domain :" + IdentityTenantUtil.getTenantDomain(claim.getTenantId())
                                  + " , " + "Mapped Local claim :" + localClaimURI);
                    if (log.isDebugEnabled()) {
                        log.debug("\n Added Remote Claim  :" + claim.getDialectURI() + " in Dialect : "
                                  + claim.getDialectURI() + " in  tenant domain :"
                                  + IdentityTenantUtil.getTenantDomain(claim.getTenantId()) + " , "
                                  + "Mapped Local claim :" + localClaimURI);
                    }

                } else {
                    //Create a new Local claim for matching the remote claim
                    int newClaimId;
                    Random random = new Random();
                    int random_number = random.nextInt(100000);

                    if (StringUtils.isNotBlank(claim.getDisplayTag())) {
                        newClaimId = addClaim(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI,
                                              ClaimConstants.LOCAL_CLAIM_DIALECT_URI + "/migration__" +
                                              claim.getDisplayTag().toLowerCase() + "__" + random_number,
                                              claim.getTenantId());

                        logReport(report, "\n\n No matching local claim found for external claim :"
                                          + claim.getClaimURI() + " in claim dialect :" + claim.getDialectURI()
                                          + " in tenant domain :"
                                          + IdentityTenantUtil.getTenantDomain(claim.getTenantId())
                                          + ". So create a new local " + "claim named : "
                                          + ClaimConstants.LOCAL_CLAIM_DIALECT_URI + "/migration__"
                                          + claim.getDisplayTag().toLowerCase() + "__" + random_number);
                    } else {
                        newClaimId = addClaim(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI,
                                              ClaimConstants.LOCAL_CLAIM_DIALECT_URI + "/migration__" + random_number,
                                              claim.getTenantId());

                        logReport(report, "\n\n No matching local claim found for external claim :"
                                          + claim.getClaimURI() + " in claim dialect :" + claim.getDialectURI()
                                          + " in tenant domain :"
                                          + IdentityTenantUtil.getTenantDomain(claim.getTenantId())
                                          + ". So create a new local " + "claim named : "
                                          + ClaimConstants.LOCAL_CLAIM_DIALECT_URI + "/migration__" + random_number);
                    }


                    Map<String, String> claimProperties = new HashMap<>();
                    claimProperties.put("Description", claim.getDescription());
                    claimProperties.put("DisplayName", "Migration__" + claim.getDisplayTag());
                    addClaimProperties(connection, newClaimId, claimProperties, claim.getTenantId());

                    for (MappedAttribute mappedAttribute : claim.getAttributes()) {
                        addClaimAttributeMappings(connection, newClaimId, mappedAttribute.getAttribute(),
                                                  mappedAttribute.getDomain(), claim.getTenantId());
                    }
                    addClaimMapping(connection, externalClaimId, newClaimId, claim.getTenantId());
                }
            }
            // End transaction
            connection.commit();
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollBack(connection);
            throw new MigrationClientException("Error while adding external claim " + externalClaimURI + " to " +
                                               "dialect " + externalClaimDialectURI, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    private void logReport(StringBuilder report, String str) {
        report.append(str);

        if (log.isDebugEnabled()) {
            log.debug(str);
        }
    }

    /**
     * Add claim mappings
     *
     * @param connection
     * @param externalClaimId
     * @param localClaimId
     * @param tenantId
     * @throws MigrationClientException
     */
    private void addClaimMapping(Connection connection, int externalClaimId, int localClaimId, int tenantId)
            throws MigrationClientException {

        PreparedStatement prepStmt = null;

        String query = SQLConstants.ADD_CLAIM_MAPPING;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, localClaimId);
            prepStmt.setInt(2, externalClaimId);
            prepStmt.setInt(3, tenantId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new MigrationClientException("Error while adding claim mapping", e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    private <T> List<T> intersection(List<T> list1, List<T> list2) {

        List<T> list = new ArrayList<T>();

        for (T t : list1) {
            if (list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }

}
