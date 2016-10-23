/*
 * Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.idp.mgt.dao;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.LocalRole;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.utils.DBUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class is used to access the data storage to retrieve and store identity provider configurations.
 */
public class IdentityProviderDAO {

    private static final Log log = LogFactory.getLog(org.wso2.carbon.idp.mgt.dao.IdentityProviderDAO.class);

    /**
     * @param dbConnection
     * @param tenantId
     * @param tenantDomain
     * @return
     * @throws IdentityProviderManagementException
     */
    public List<IdentityProvider> getIdPs(Connection dbConnection, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        boolean dbConnInitialized = true;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<IdentityProvider> idps = new ArrayList<IdentityProvider>();
        if (dbConnection == null) {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
        } else {
            dbConnInitialized = false;
        }
        try {

            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDPS_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                IdentityProvider identityProvider = new IdentityProvider();
                identityProvider.setIdentityProviderName(rs.getString(1));
                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_PRIMARY"))) {
                    identityProvider.setPrimary(true);
                } else {
                    identityProvider.setPrimary(false);
                }
                identityProvider.setHomeRealmId(rs.getString("HOME_REALM_ID"));
                identityProvider.setIdentityProviderDescription(rs.getString("DESCRIPTION"));

                // IS_FEDERATION_HUB_IDP
                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_FEDERATION_HUB"))) {
                    identityProvider.setFederationHub(false);
                }

                // IS_LOCAL_CLAIM_DIALECT
                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_LOCAL_CLAIM_DIALECT"))) {
                    if (identityProvider.getClaimConfig() == null) {
                        identityProvider.setClaimConfig(new ClaimConfig());
                    }
                    identityProvider.getClaimConfig().setLocalClaimDialect(true);
                }

                // IS_ENABLE
                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_ENABLED"))) {
                    identityProvider.setEnable(true);
                } else {
                    identityProvider.setEnable(false);
                }

                identityProvider.setDisplayName(rs.getString("DISPLAY_NAME"));

                if (!IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME
                        .equals(identityProvider.getIdentityProviderName())) {
                    idps.add(identityProvider);
                }
                List<IdentityProviderProperty> propertyList = getIdentityPropertiesByIdpId(dbConnection,
                        Integer.parseInt(rs.getString("ID")));
                identityProvider
                        .setIdpProperties(propertyList.toArray(new IdentityProviderProperty[propertyList.size()]));

            }
            dbConnection.commit();
            return idps;
        } catch (SQLException e) {
            IdentityApplicationManagementUtil.rollBack(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while retrieving registered Identity " +
                    "Provider Entity IDs " + "for tenant " + tenantDomain, e);
        } finally {
            if (dbConnInitialized) {
                IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            }else{
                IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            }
        }
    }

    /**
     * Get Identity properties map
     * @param dbConnection database connection
     * @param idpId IDP Id
     * @return Identity provider properties
     */
    private List<IdentityProviderProperty> getIdentityPropertiesByIdpId(Connection dbConnection, int idpId)
            throws SQLException {

        String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_METADATA_BY_IDP_ID;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<IdentityProviderProperty> idpProperties = new ArrayList<IdentityProviderProperty>();
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idpId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                IdentityProviderProperty property = new IdentityProviderProperty();
                property.setName(rs.getString("NAME"));
                property.setValue(rs.getString("VALUE"));
                property.setDisplayName(rs.getString("DISPLAY_NAME"));
                idpProperties.add(property);
            }
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
        return idpProperties;
    }

    /**
     * Add Identity provider properties
     *
     * @param dbConnection
     * @param idpId
     * @param properties
     * @throws SQLException
     */
    private void addIdentityProviderProperties(Connection dbConnection, int idpId,
            List<IdentityProviderProperty> properties, int tenantId)
            throws SQLException {
        String sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_METADATA;
        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);

            for (IdentityProviderProperty property : properties) {
                prepStmt.setInt(1, idpId);
                prepStmt.setString(2, property.getName());
                prepStmt.setString(3, property.getValue());
                prepStmt.setString(4, property.getDisplayName());
                prepStmt.setInt(5, tenantId);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();

        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    /**
     * Update Identity provider properties
     *
     * @param dbConnection
     * @param idpId
     * @param properties
     * @throws SQLException
     */
    private void updateIdentityProviderProperties(Connection dbConnection, int idpId,
            List<IdentityProviderProperty> properties, int tenantId)
            throws SQLException {

        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(IdPManagementConstants.SQLQueries.DELETE_IDP_METADATA);
            prepStmt.setInt(1, idpId);
            prepStmt.executeUpdate();

            prepStmt = dbConnection.prepareStatement(IdPManagementConstants.SQLQueries.ADD_IDP_METADATA);

            for (IdentityProviderProperty property : properties) {
                prepStmt.setInt(1, idpId);
                prepStmt.setString(2, property.getName());
                prepStmt.setString(3, property.getValue());
                prepStmt.setString(4, property.getDisplayName());
                prepStmt.setInt(5, tenantId);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();

        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    /**
     * @param dbConnection
     * @param idPName
     * @param tenantId
     * @return
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private FederatedAuthenticatorConfig[] getFederatedAuthenticatorConfigs(
            Connection dbConnection, String idPName, IdentityProvider federatedIdp, int tenantId)
            throws IdentityProviderManagementException, SQLException {

        int idPId = getIdentityProviderIdentifier(dbConnection, idPName, tenantId);

        PreparedStatement prepStmt1 = null;
        PreparedStatement prepStmt2 = null;
        ResultSet rs = null;
        ResultSet proprs = null;
        String defaultAuthName = null;

        if (federatedIdp != null && federatedIdp.getDefaultAuthenticatorConfig() != null) {
            defaultAuthName = federatedIdp.getDefaultAuthenticatorConfig().getName();
        }

        String sqlStmt = IdPManagementConstants.SQLQueries.GET_ALL_IDP_AUTH_SQL;
        Set<FederatedAuthenticatorConfig> federatedAuthenticatorConfigs = new HashSet<FederatedAuthenticatorConfig>();
        try {
            prepStmt1 = dbConnection.prepareStatement(sqlStmt);
            prepStmt1.setInt(1, idPId);
            rs = prepStmt1.executeQuery();

            while (rs.next()) {
                FederatedAuthenticatorConfig authnConfig = new FederatedAuthenticatorConfig();
                int authnId = rs.getInt("ID");
                authnConfig.setName(rs.getString("NAME"));

                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_ENABLED"))) {
                    authnConfig.setEnabled(true);
                } else {
                    authnConfig.setEnabled(false);
                }

                authnConfig.setDisplayName(rs.getString("DISPLAY_NAME"));

                if (defaultAuthName != null && authnConfig.getName().equals(defaultAuthName)) {
                    federatedIdp.getDefaultAuthenticatorConfig().setDisplayName(authnConfig.getDisplayName());
                }

                sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_AUTH_PROPS_SQL;
                prepStmt2 = dbConnection.prepareStatement(sqlStmt);
                prepStmt2.setInt(1, authnId);
                proprs = prepStmt2.executeQuery();
                Set<Property> properties = new HashSet<Property>();
                while (proprs.next()) {
                    Property property = new Property();
                    property.setName(proprs.getString("PROPERTY_KEY"));
                    property.setValue(proprs.getString("PROPERTY_VALUE"));
                    if ((IdPManagementConstants.IS_TRUE_VALUE).equals(proprs.getString("IS_SECRET"))) {
                        property.setConfidential(true);
                    }
                    properties.add(property);
                }
                authnConfig.setProperties(properties.toArray(new Property[properties.size()]));
                federatedAuthenticatorConfigs.add(authnConfig);
            }

            return federatedAuthenticatorConfigs
                    .toArray(new FederatedAuthenticatorConfig[federatedAuthenticatorConfigs.size()]);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, proprs, prepStmt2);
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt1);
        }
    }

    /**
     * @param newFederatedAuthenticatorConfigs
     * @param oldFederatedAuthenticatorConfigs
     * @param dbConnection
     * @param idpId
     * @param tenantId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private void updateFederatedAuthenticatorConfigs(
            FederatedAuthenticatorConfig[] newFederatedAuthenticatorConfigs,
            FederatedAuthenticatorConfig[] oldFederatedAuthenticatorConfigs,
            Connection dbConnection, int idpId, int tenantId)
            throws IdentityProviderManagementException, SQLException {

        Map<String, FederatedAuthenticatorConfig> oldFedAuthnConfigMap = new HashMap<String, FederatedAuthenticatorConfig>();
        if (oldFederatedAuthenticatorConfigs != null && oldFederatedAuthenticatorConfigs.length > 0) {
            for (FederatedAuthenticatorConfig fedAuthnConfig : oldFederatedAuthenticatorConfigs) {
                oldFedAuthnConfigMap.put(fedAuthnConfig.getName(), fedAuthnConfig);
            }
        }

        if (newFederatedAuthenticatorConfigs != null && newFederatedAuthenticatorConfigs.length > 0) {
            for (FederatedAuthenticatorConfig fedAuthenticator : newFederatedAuthenticatorConfigs) {
                if (oldFedAuthnConfigMap.containsKey(fedAuthenticator.getName())
                        && oldFedAuthnConfigMap.get(fedAuthenticator.getName()).isValid()) {
                    updateFederatedAuthenticatorConfig(fedAuthenticator, oldFedAuthnConfigMap.get(fedAuthenticator
                            .getName()), dbConnection, idpId, tenantId);
                } else {
                    addFederatedAuthenticatorConfig(fedAuthenticator, dbConnection, idpId, tenantId);
                }
            }
        }
    }

    /**
     * @param newFederatedAuthenticatorConfig
     * @param oldFederatedAuthenticatorConfig
     * @param dbConnection
     * @param idpId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private void updateFederatedAuthenticatorConfig(FederatedAuthenticatorConfig newFederatedAuthenticatorConfig,
                                                    FederatedAuthenticatorConfig oldFederatedAuthenticatorConfig,
                                                    Connection dbConnection, int idpId, int tenantId) throws
            IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt1 = null;

        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.UPDATE_IDP_AUTH_SQL;
            prepStmt1 = dbConnection.prepareStatement(sqlStmt);

            if (newFederatedAuthenticatorConfig.isEnabled()) {
                prepStmt1.setString(1, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt1.setString(1, IdPManagementConstants.IS_FALSE_VALUE);
            }
            prepStmt1.setInt(2, idpId);
            prepStmt1.setString(3, newFederatedAuthenticatorConfig.getName());
            prepStmt1.executeUpdate();

            int authnId = getAuthenticatorIdentifier(dbConnection, idpId,
                    newFederatedAuthenticatorConfig.getName());

            List<Property> singleValuedProperties = new ArrayList<>();
            List<Property> multiValuedProperties = new ArrayList<>();

            for (Property property : newFederatedAuthenticatorConfig.getProperties()) {
                if (Pattern.matches(IdPManagementConstants.MULTI_VALUED_PROPERT_IDENTIFIER_PATTERN, property.getName
                        ())) {
                    multiValuedProperties.add(property);
                } else {
                    singleValuedProperties.add(property);
                }
            }
            if (CollectionUtils.isNotEmpty(singleValuedProperties)) {
                updateSingleValuedFederatedConfigProperties(dbConnection, authnId, tenantId, singleValuedProperties);
            }
            if (CollectionUtils.isNotEmpty(multiValuedProperties)) {
                updateMultiValuedFederatedConfigProperties(dbConnection, oldFederatedAuthenticatorConfig
                        .getProperties(), authnId, tenantId, multiValuedProperties);
            }
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt1);
        }
    }

    /**
     * @param authnConfigs
     * @param dbConnection
     * @param idpId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    public void addFederatedAuthenticatorConfigs(FederatedAuthenticatorConfig[] authnConfigs,
                                                 Connection dbConnection, int idpId, int tenantId)
            throws IdentityProviderManagementException, SQLException {

        for (FederatedAuthenticatorConfig authnConfig : authnConfigs) {
            addFederatedAuthenticatorConfig(authnConfig, dbConnection, idpId, tenantId);
        }
    }

    public void addFederatedAuthenticatorConfig(FederatedAuthenticatorConfig authnConfig,
                                                Connection dbConnection, int idpId, int tenantId)
            throws IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt1 = null;
        PreparedStatement prepStmt2 = null;
        String sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_AUTH_SQL;

        try {
            prepStmt1 = dbConnection.prepareStatement(sqlStmt);
            prepStmt1.setInt(1, idpId);
            prepStmt1.setInt(2, tenantId);
            if (authnConfig.isEnabled()) {
                prepStmt1.setString(3, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt1.setString(3, IdPManagementConstants.IS_FALSE_VALUE);
            }
            prepStmt1.setString(4, authnConfig.getName());
            prepStmt1.setString(5, authnConfig.getDisplayName());
            prepStmt1.execute();

            int authnId = getAuthenticatorIdentifier(dbConnection, idpId, authnConfig.getName());

            sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_AUTH_PROP_SQL;

            if (authnConfig.getProperties() == null) {
                authnConfig.setProperties(new Property[0]);
            }
            for (Property property : authnConfig.getProperties()) {

                prepStmt2 = dbConnection.prepareStatement(sqlStmt);
                prepStmt2.setInt(1, authnId);
                prepStmt2.setInt(2, tenantId);
                prepStmt2.setString(3, property.getName());
                prepStmt2.setString(4, property.getValue());
                if (property.isConfidential()) {
                    prepStmt2.setString(5, IdPManagementConstants.IS_TRUE_VALUE);
                } else {
                    prepStmt2.setString(5, IdPManagementConstants.IS_FALSE_VALUE);
                }
                prepStmt2.executeUpdate();
            }
        } finally {

            IdentityDatabaseUtil.closeStatement(prepStmt2);
            IdentityDatabaseUtil.closeStatement(prepStmt1);
        }
    }

    private void updateSingleValuedFederatedConfigProperties(Connection dbConnection, int authnId, int tenantId,
                                                             List<Property> singleValuedProperties) throws
            SQLException {

        PreparedStatement prepStmt2 = null;
        PreparedStatement prepStmt3 = null;
        String sqlStmt;

        try {
            for (Property property : singleValuedProperties) {

                sqlStmt = IdPManagementConstants.SQLQueries.UPDATE_IDP_AUTH_PROP_SQL;
                prepStmt2 = dbConnection.prepareStatement(sqlStmt);
                prepStmt2.setString(1, property.getValue());
                if (property.isConfidential()) {
                    prepStmt2.setString(2, IdPManagementConstants.IS_TRUE_VALUE);
                } else {
                    prepStmt2.setString(2, IdPManagementConstants.IS_FALSE_VALUE);
                }
                prepStmt2.setInt(3, authnId);
                prepStmt2.setString(4, property.getName());
                int rows = prepStmt2.executeUpdate();

                if (rows == 0) {
                    // this should be an insert.
                    sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_AUTH_PROP_SQL;
                    prepStmt3 = dbConnection.prepareStatement(sqlStmt);
                    prepStmt3.setInt(1, authnId);
                    prepStmt3.setInt(2, tenantId);
                    prepStmt3.setString(3, property.getName());
                    prepStmt3.setString(4, property.getValue());
                    if (property.isConfidential()) {
                        prepStmt3.setString(5, IdPManagementConstants.IS_TRUE_VALUE);
                    } else {
                        prepStmt3.setString(5, IdPManagementConstants.IS_FALSE_VALUE);
                    }

                    prepStmt3.executeUpdate();
                }

            }
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt3);
            IdentityDatabaseUtil.closeStatement(prepStmt2);
        }
    }


    private void updateMultiValuedFederatedConfigProperties(Connection dbConnection, Property[]
            oldFederatedAuthenticatorConfigProperties, int authnId, int tenantId, List<Property>
            multiValuedProperties) throws SQLException {

        PreparedStatement deleteOldValuePrepStmt = null;
        PreparedStatement addNewPropsPrepStmt = null;
        String sqlStmt;
        try {
            for (Property property : oldFederatedAuthenticatorConfigProperties) {
                if (Pattern.matches(IdPManagementConstants.MULTI_VALUED_PROPERT_IDENTIFIER_PATTERN, property.getName
                        ())) {
                    sqlStmt = IdPManagementConstants.SQLQueries.DELETE_IDP_AUTH_PROP_WITH_KEY_SQL;
                    deleteOldValuePrepStmt = dbConnection.prepareStatement(sqlStmt);
                    deleteOldValuePrepStmt.setString(1, property.getName());
                    deleteOldValuePrepStmt.executeUpdate();
                }
            }

            for (Property property : multiValuedProperties) {
                sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_AUTH_PROP_SQL;
                addNewPropsPrepStmt = dbConnection.prepareStatement(sqlStmt);
                addNewPropsPrepStmt.setInt(1, authnId);
                addNewPropsPrepStmt.setInt(2, tenantId);
                addNewPropsPrepStmt.setString(3, property.getName());
                addNewPropsPrepStmt.setString(4, property.getValue());
                if (property.isConfidential()) {
                    addNewPropsPrepStmt.setString(5, IdPManagementConstants.IS_TRUE_VALUE);
                } else {
                    addNewPropsPrepStmt.setString(5, IdPManagementConstants.IS_FALSE_VALUE);
                }

                addNewPropsPrepStmt.executeUpdate();
            }

        } finally {
            IdentityDatabaseUtil.closeStatement(deleteOldValuePrepStmt);
            IdentityDatabaseUtil.closeStatement(addNewPropsPrepStmt);
        }

    }
    /**
     * @param dbConnection
     * @param idPName
     * @param userClaimUri
     * @param roleClaimUri
     * @param idpId
     * @param tenantId
     * @return
     * @throws SQLException
     */
    private ClaimConfig getLocalIdPDefaultClaimValues(Connection dbConnection, String idPName,
                                                      String userClaimUri, String roleClaimUri,
                                                      int idpId, int tenantId) throws SQLException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlStmt;
        ClaimConfig claimConfig = new ClaimConfig();

        try {

            claimConfig.setLocalClaimDialect(true);
            claimConfig.setRoleClaimURI(roleClaimUri);
            claimConfig.setUserClaimURI(userClaimUri);

            sqlStmt = IdPManagementConstants.SQLQueries.GET_LOCAL_IDP_DEFAULT_CLAIM_VALUES_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);

            prepStmt.setInt(1, idpId);
            prepStmt.setInt(2, tenantId);

            List<ClaimMapping> claimMappings = new ArrayList<ClaimMapping>();

            rs = prepStmt.executeQuery();

            while (rs.next()) {
                ClaimMapping claimMapping = new ClaimMapping();

                // empty claim.
                Claim remoteClaim = new Claim();

                Claim localClaim = new Claim();
                localClaim.setClaimUri(rs.getString("CLAIM_URI"));

                claimMapping.setLocalClaim(localClaim);
                claimMapping.setRemoteClaim(remoteClaim);
                claimMapping.setDefaultValue(rs.getString("DEFAULT_VALUE"));

                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_REQUESTED"))) {
                    claimMapping.setRequested(true);
                } else if (rs.getString("IS_REQUESTED").equals(IdPManagementConstants.IS_TRUE_VALUE)) {
                    claimMapping.setRequested(false);
                }

                claimMappings.add(claimMapping);
            }

            claimConfig.setClaimMappings(claimMappings.toArray(new ClaimMapping[claimMappings
                    .size()]));

            return claimConfig;

        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    /**
     * @param dbConnection
     * @param idPName
     * @param tenantId
     * @return
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private ClaimConfig getIdPClaimConfiguration(Connection dbConnection, String idPName,
                                                 String userClaimUri, String roleClaimUri, int idPId, int tenantId)
            throws IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt1 = null;
        PreparedStatement prepStmt2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {

            List<Claim> claimList = new ArrayList<Claim>();
            // SP_IDP_CLAIM_ID, SP_IDP_CLAIM
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_CLAIMS_SQL;
            prepStmt1 = dbConnection.prepareStatement(sqlStmt);
            prepStmt1.setInt(1, idPId);
            rs1 = prepStmt1.executeQuery();

            ClaimConfig claimConfig = new ClaimConfig();

            while (rs1.next()) {
                Claim identityProviderClaim = new Claim();
                identityProviderClaim.setClaimId(rs1.getInt(1));
                identityProviderClaim.setClaimUri(rs1.getString(2));
                claimList.add(identityProviderClaim);
            }

            // populate claim configuration with identity provider claims.
            claimConfig.setIdpClaims(claimList.toArray(new Claim[claimList.size()]));

            claimConfig.setUserClaimURI(userClaimUri);
            claimConfig.setRoleClaimURI(roleClaimUri);

            List<ClaimMapping> claimMappings = new ArrayList<ClaimMapping>();

            // SP_IDP_CLAIMS.SP_IDP_CLAIM SP_IDP_CLAIM_MAPPINGS.SP_LOCAL_CLAIM
            sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_CLAIM_MAPPINGS_SQL;
            prepStmt2 = dbConnection.prepareStatement(sqlStmt);
            prepStmt2.setInt(1, idPId);
            rs2 = prepStmt2.executeQuery();

            while (rs2.next()) {
                ClaimMapping claimMapping = new ClaimMapping();

                Claim idpClaim = new Claim();
                idpClaim.setClaimUri(rs2.getString("CLAIM"));

                Claim localClaim = new Claim();
                localClaim.setClaimUri(rs2.getString("LOCAL_CLAIM"));

                claimMapping.setLocalClaim(localClaim);
                claimMapping.setRemoteClaim(idpClaim);
                claimMapping.setDefaultValue(rs2.getString("DEFAULT_VALUE"));
                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs2.getString("IS_REQUESTED"))) {
                    claimMapping.setRequested(true);
                } else if ((IdPManagementConstants.IS_FALSE_VALUE).equals(rs2.getString("IS_REQUESTED"))) {
                    claimMapping.setRequested(false);
                }
                claimMappings.add(claimMapping);

            }

            claimConfig.setClaimMappings(claimMappings.toArray(new ClaimMapping[claimMappings
                    .size()]));

            return claimConfig;
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs2, prepStmt2);
            IdentityDatabaseUtil.closeAllConnections(null, rs1, prepStmt1);
        }
    }

    /**
     * @param dbConnection
     * @param idPName
     * @param tenantId
     * @return
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    public PermissionsAndRoleConfig getPermissionsAndRoleConfiguration(Connection dbConnection,
                                                                       String idPName, int idPId, int tenantId)
            throws IdentityProviderManagementException,
            SQLException {

        PreparedStatement prepStmt1 = null;
        PreparedStatement prepStmt2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        PermissionsAndRoleConfig permissionRoleConfiguration = new PermissionsAndRoleConfig();

        try {

            List<String> idpRoleList = new ArrayList<String>();
            // SP_IDP_ROLE
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_ROLES_SQL;
            prepStmt1 = dbConnection.prepareStatement(sqlStmt);
            prepStmt1.setInt(1, idPId);
            rs1 = prepStmt1.executeQuery();
            while (rs1.next()) {
                idpRoleList.add(rs1.getString("ROLE"));
            }

            permissionRoleConfiguration.setIdpRoles(idpRoleList.toArray(new String[idpRoleList
                    .size()]));

            List<RoleMapping> roleMappings = new ArrayList<RoleMapping>();
            // SP_IDP_ROLE_MAPPINGS.SP_USER_STORE_ID, SP_IDP_ROLE_MAPPINGS.SP_LOCAL_ROLE,
            // SP_IDP_ROLES.SP_IDP_ROLE

            sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_ROLE_MAPPINGS_SQL;
            prepStmt2 = dbConnection.prepareStatement(sqlStmt);
            prepStmt2.setInt(1, idPId);
            rs2 = prepStmt2.executeQuery();
            while (rs2.next()) {
                LocalRole localRole = new LocalRole(rs2.getString("USER_STORE_ID"),
                        rs2.getString("LOCAL_ROLE"));
                RoleMapping roleMapping = new RoleMapping(localRole, rs2.getString("ROLE"));
                roleMappings.add(roleMapping);
            }

            permissionRoleConfiguration.setRoleMappings(roleMappings
                    .toArray(new RoleMapping[roleMappings.size()]));
            return permissionRoleConfiguration;
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs2, prepStmt2);
            IdentityDatabaseUtil.closeAllConnections(null, rs1, prepStmt1);
        }
    }

    /**
     * @param provisioningConnectors
     * @param dbConnection
     * @param idpId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private void addProvisioningConnectorConfigs(
            ProvisioningConnectorConfig[] provisioningConnectors, Connection dbConnection,
            int idpId, int tenantId) throws IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt = null;
        PreparedStatement prepBaseStmt = null;
        ResultSet rs = null;

        try {
            // SP_IDP_ID,SP_IDP_PROV_CONNECTOR_TYPE, SP_IDP_PROV_CONFIG_KEY,
            // SP_IDP_PROV_CONFIG_VALUE, SP_IDP_PROV_CONFIG_IS_SECRET

            // SP_IDP_PROV_CONFIG_PROPERTY
            // TENANT_ID, PROVISIONING_CONFIG_ID, PROPERTY_KEY, PROPERTY_VALUE, PROPERTY_TYPE,
            // IS_SECRET
            String sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_PROVISIONING_PROPERTY_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);

            String sqlBaseStmt = IdPManagementConstants.SQLQueries.ADD_IDP_PROVISIONING_CONFIG_SQL;
            String dbProductName = dbConnection.getMetaData().getDatabaseProductName();
            prepBaseStmt = dbConnection.prepareStatement(sqlBaseStmt,
                    new String[]{DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "ID")});

            if (provisioningConnectors != null) {
                for (ProvisioningConnectorConfig connector : provisioningConnectors) {
                    Property[] connctorProperties = connector.getProvisioningProperties();

                    if (connctorProperties != null) {

                        // SP_IDP_PROVISIONING_CONFIG
                        // TENANT_ID, IDP_ID, PROVISIONING_CONNECTOR_TYPE, IS_ENABLED, IS_DEFAULT
                        prepBaseStmt.setInt(1, tenantId);
                        prepBaseStmt.setInt(2, idpId);
                        prepBaseStmt.setString(3, connector.getName());

                        if (connector.isEnabled()) {
                            prepBaseStmt.setString(4, IdPManagementConstants.IS_TRUE_VALUE);
                        } else {
                            prepBaseStmt.setString(4, IdPManagementConstants.IS_FALSE_VALUE);
                        }

                        if (connector.isBlocking()) {
                            prepBaseStmt.setString(5, IdPManagementConstants.IS_TRUE_VALUE);
                        } else {
                            prepBaseStmt.setString(5, IdPManagementConstants.IS_FALSE_VALUE);
                        }

                        prepBaseStmt.executeUpdate();
                        rs = prepBaseStmt.getGeneratedKeys();

                        if (rs.next()) {
                            int provisioningConfigID = rs.getInt(1);

                            if (connctorProperties.length > 0) {
                                for (Property config : connctorProperties) {

                                    if (config == null) {
                                        continue;
                                    }

                                    // SP_IDP_PROV_CONFIG_PROPERTY
                                    //TENANT_ID, PROVISIONING_CONFIG_ID, PROPERTY_KEY,
                                    // PROPERTY_VALUE, PROPERTY_BLOB_VALUE, PROPERTY_TYPE, IS_SECRET
                                    prepStmt.setInt(1, tenantId);
                                    prepStmt.setInt(2, provisioningConfigID);
                                    prepStmt.setString(3, config.getName());

                                    // TODO : Sect property type accordingly
                                    if (IdentityApplicationConstants.ConfigElements.PROPERTY_TYPE_BLOB.equals
                                            (config.getType())) {
                                        prepStmt.setString(4, null);
                                        setBlobValue(config.getValue(), prepStmt, 5);
                                        prepStmt.setString(6, config.getType());
                                    } else {
                                        prepStmt.setString(4, config.getValue());
                                        setBlobValue(null, prepStmt, 5);
                                        prepStmt.setString(6, IdentityApplicationConstants.ConfigElements.
                                                PROPERTY_TYPE_STRING);
                                    }

                                    if (config.isConfidential()) {
                                        prepStmt.setString(7, IdPManagementConstants.IS_TRUE_VALUE);
                                    } else {
                                        prepStmt.setString(7, IdPManagementConstants.IS_FALSE_VALUE);
                                    }
                                    prepStmt.addBatch();

                                }
                            }

                        }

                        // Adding properties for base config
                        prepStmt.executeBatch();

                    }
                }
            }
        } catch (IOException e) {
            throw new IdentityProviderManagementException("An error occurred while processing content stream.", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            IdentityDatabaseUtil.closeStatement(prepBaseStmt);
        }
    }

    private void setBlobValue(String value, PreparedStatement prepStmt, int index) throws SQLException, IOException {
        if (value != null) {
            InputStream inputStream = new ByteArrayInputStream(value.getBytes());
            if (inputStream != null) {
                prepStmt.setBinaryStream(index, inputStream, inputStream.available());
            } else {
                prepStmt.setBinaryStream(index, new ByteArrayInputStream(new byte[0]), 0);
            }
        } else {
            prepStmt.setBinaryStream(index, new ByteArrayInputStream(new byte[0]), 0);
        }
    }

    /**
     * @param newProvisioningConnectorConfigs
     * @param dbConnection
     * @param idpId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private void updateProvisioningConnectorConfigs(
            ProvisioningConnectorConfig[] newProvisioningConnectorConfigs, Connection dbConnection,
            int idpId, int tenantId) throws IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        try {
            deleteProvisioningConnectorConfigs(dbConnection, idpId);

            if (newProvisioningConnectorConfigs != null
                    && newProvisioningConnectorConfigs.length > 0) {
                addProvisioningConnectorConfigs(newProvisioningConnectorConfigs, dbConnection,
                        idpId, tenantId);
            }

        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    /**
     * @param dbConnection
     * @param idPName
     * @param tenantId
     * @return
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    public ProvisioningConnectorConfig[] getProvisioningConnectorConfigs(Connection dbConnection,
                                                                         String idPName, int idPId, int tenantId)
            throws IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt = null;
        PreparedStatement prepBaseStmt = null;

        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {
            // SP_IDP_PROV_CONNECTOR_TYPE,SP_IDP_PROV_CONFIG_KEY,
            // SP_IDP_PROV_CONFIG_VALUE,SP_IDP_PROV_CONFIG_IS_SECRET
            String sqlBaseStmt = IdPManagementConstants.SQLQueries.GET_IDP_PROVISIONING_CONFIGS_SQL;
            prepBaseStmt = dbConnection.prepareStatement(sqlBaseStmt);

            prepBaseStmt.setInt(1, idPId);
            rs1 = prepBaseStmt.executeQuery();

            Map<String, ProvisioningConnectorConfig> provisioningConnectorMap = new HashMap<String, ProvisioningConnectorConfig>();

            while (rs1.next()) {

                ProvisioningConnectorConfig provisioningConnector;

                String type = rs1.getString("PROVISIONING_CONNECTOR_TYPE");
                if (!provisioningConnectorMap.containsKey(type)) {
                    provisioningConnector = new ProvisioningConnectorConfig();
                    provisioningConnector.setName(type);

                    if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs1.getString("IS_ENABLED"))) {
                        provisioningConnector.setEnabled(true);
                    } else {
                        provisioningConnector.setEnabled(false);
                    }

                    if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs1.getString("IS_BLOCKING"))) {
                        provisioningConnector.setBlocking(true);
                    } else {
                        provisioningConnector.setBlocking(false);
                    }


                    if (provisioningConnector.getProvisioningProperties() == null
                            || provisioningConnector.getProvisioningProperties().length == 0) {

                        String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_PROVISIONING_PROPERTY_SQL;
                        prepStmt = dbConnection.prepareStatement(sqlStmt);

                        int configId = rs1.getInt("ID");
                        prepStmt.setInt(1, tenantId);
                        prepStmt.setInt(2, configId);

                        rs2 = prepStmt.executeQuery();

                        List<Property> provisioningProperties = new ArrayList<Property>();
                        while (rs2.next()) {
                            Property Property = new Property();
                            String name = rs2.getString("PROPERTY_KEY");
                            String value = rs2.getString("PROPERTY_VALUE");
                            String blobValue = getBlobValue(rs2.getBinaryStream("PROPERTY_BLOB_VALUE"));

                            String propertyType = rs2.getString("PROPERTY_TYPE");
                            String isSecret = rs2.getString("IS_SECRET");

                            Property.setName(name);
                            if (propertyType != null && IdentityApplicationConstants.ConfigElements.
                                    PROPERTY_TYPE_BLOB.equals(propertyType.trim())) {
                                Property.setValue(blobValue);
                            } else {
                                Property.setValue(value);
                            }

                            Property.setType(propertyType);

                            if ((IdPManagementConstants.IS_TRUE_VALUE).equals(isSecret)) {
                                Property.setConfidential(true);
                            } else {
                                Property.setConfidential(false);
                            }

                            provisioningProperties.add(Property);
                        }
                        provisioningConnector.setProvisioningProperties(provisioningProperties
                                .toArray(new Property[provisioningProperties.size()]));
                    }

                    provisioningConnectorMap.put(type, provisioningConnector);
                }
            }

            return provisioningConnectorMap.values().toArray(
                    new ProvisioningConnectorConfig[provisioningConnectorMap.size()]);

        } finally {

            IdentityDatabaseUtil.closeAllConnections(null, rs2, prepBaseStmt);
            IdentityDatabaseUtil.closeAllConnections(null, rs1, prepStmt);
        }
    }

    private String getBlobValue(InputStream is) throws IdentityProviderManagementException {

        if (is != null) {
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                throw new IdentityProviderManagementException("Error occurred while reading blob value from input " +
                        "stream", e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        log.error("Error in retrieving the Blob value", e);
                    }
                }
            }

            return sb.toString();
        }
        return null;
    }

    /**
     * @param dbConnection
     * @param idPName
     * @param tenantId
     * @param tenantDomain
     * @return
     * @throws IdentityProviderManagementException
     */
    public IdentityProvider getIdPByName(Connection dbConnection, String idPName, int tenantId,
                                         String tenantDomain) throws IdentityProviderManagementException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        IdentityProvider federatedIdp = null;
        boolean dbConnectionInitialized = true;
        if (dbConnection == null) {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
        } else {
            dbConnectionInitialized = false;
        }

        try {
            // SP_IDP_ID, SP_IDP_PRIMARY, SP_IDP_HOME_REALM_ID,SP_IDP_CERTIFICATE,
            // SP_IDP_TOKEN_EP_ALIAS,
            // SP_IDP_INBOUND_PROVISIONING_ENABLED,SP_IDP_INBOUND_PROVISIONING_USER_STORE_ID,
            // SP_IDP_USER_CLAIM_URI,
            // SP_IDP_ROLE_CLAIM_URI,SP_IDP_DEFAULT_AUTHENTICATOR_NAME,SP_IDP_DEFAULT_PRO_CONNECTOR_NAME
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_BY_NAME_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            prepStmt.setString(3, idPName);
            rs = prepStmt.executeQuery();
            int idpId = -1;

            if (rs.next()) {
                federatedIdp = new IdentityProvider();
                federatedIdp.setIdentityProviderName(idPName);

                idpId = rs.getInt("ID");

                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_PRIMARY"))) {
                    federatedIdp.setPrimary(true);
                } else {
                    federatedIdp.setPrimary(false);
                }

                federatedIdp.setHomeRealmId(rs.getString("HOME_REALM_ID"));
                federatedIdp.setCertificate(getBlobValue(rs.getBinaryStream("CERTIFICATE")));
                federatedIdp.setAlias(rs.getString("ALIAS"));

                JustInTimeProvisioningConfig jitProConfig = new JustInTimeProvisioningConfig();
                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("INBOUND_PROV_ENABLED"))) {
                    jitProConfig.setProvisioningEnabled(true);
                } else {
                    jitProConfig.setProvisioningEnabled(false);
                }

                jitProConfig.setProvisioningUserStore(rs.getString("INBOUND_PROV_USER_STORE_ID"));
                federatedIdp.setJustInTimeProvisioningConfig(jitProConfig);

                String userClaimUri = rs.getString("USER_CLAIM_URI");
                String roleClaimUri = rs.getString("ROLE_CLAIM_URI");

                String defaultAuthenticatorName = rs.getString("DEFAULT_AUTHENTICATOR_NAME");
                String defaultProvisioningConnectorConfigName = rs.getString("DEFAULT_PRO_CONNECTOR_NAME");
                federatedIdp.setIdentityProviderDescription(rs.getString("DESCRIPTION"));

                // IS_FEDERATION_HUB_IDP
                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("IS_FEDERATION_HUB"))) {
                    federatedIdp.setFederationHub(true);
                } else {
                    federatedIdp.setFederationHub(false);
                }

                if (federatedIdp.getClaimConfig() == null) {
                    federatedIdp.setClaimConfig(new ClaimConfig());
                }

                // IS_LOCAL_CLAIM_DIALECT
                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("IS_LOCAL_CLAIM_DIALECT"))) {
                    federatedIdp.getClaimConfig().setLocalClaimDialect(true);
                } else {
                    federatedIdp.getClaimConfig().setLocalClaimDialect(false);
                }

                federatedIdp.setProvisioningRole(rs.getString("PROVISIONING_ROLE"));

                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("IS_ENABLED"))) {
                    federatedIdp.setEnable(true);
                } else {
                    federatedIdp.setEnable(false);
                }

                federatedIdp.setDisplayName(rs.getString("DISPLAY_NAME"));

                if (defaultProvisioningConnectorConfigName != null) {
                    ProvisioningConnectorConfig defaultProConnector = new ProvisioningConnectorConfig();
                    defaultProConnector.setName(defaultProvisioningConnectorConfigName);
                    federatedIdp.setDefaultProvisioningConnectorConfig(defaultProConnector);
                }

                // get federated authenticators.
                federatedIdp.setFederatedAuthenticatorConfigs(getFederatedAuthenticatorConfigs(
                        dbConnection, idPName, federatedIdp, tenantId));

                if (defaultAuthenticatorName != null && federatedIdp.getFederatedAuthenticatorConfigs() != null) {
                    federatedIdp.setDefaultAuthenticatorConfig(IdentityApplicationManagementUtil
                            .getFederatedAuthenticator(federatedIdp.getFederatedAuthenticatorConfigs(),
                                    defaultAuthenticatorName));
                }

                if (federatedIdp.getClaimConfig().isLocalClaimDialect()) {
                    federatedIdp.setClaimConfig(getLocalIdPDefaultClaimValues(dbConnection,
                            idPName, userClaimUri, roleClaimUri, idpId, tenantId));
                } else {
                    // get claim configuration.
                    federatedIdp.setClaimConfig(getIdPClaimConfiguration(dbConnection, idPName,
                            userClaimUri, roleClaimUri, idpId, tenantId));
                }

                // get provisioning connectors.
                federatedIdp.setProvisioningConnectorConfigs(getProvisioningConnectorConfigs(
                        dbConnection, idPName, idpId, tenantId));

                // get permission and role configuration.
                federatedIdp.setPermissionAndRoleConfig(getPermissionsAndRoleConfiguration(
                        dbConnection, idPName, idpId, tenantId));

                List<IdentityProviderProperty> propertyList = getIdentityPropertiesByIdpId(dbConnection,
                        Integer.parseInt(rs.getString("ID")));
                federatedIdp.setIdpProperties(propertyList.toArray(new IdentityProviderProperty[propertyList.size()]));

            }
            dbConnection.commit();
            return federatedIdp;
        } catch (SQLException e) {
            IdentityApplicationManagementUtil.rollBack(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while retrieving Identity Provider " +
                    "information for tenant : " + tenantDomain + " and Identity Provider name : " + idPName, e);
        } finally {
            if (dbConnectionInitialized) {
                IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            }else{
                IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            }
        }
    }

    /**
     * @param dbConnection
     * @param property     Property which has a unique value like EntityID to specifically identify a IdentityProvider
     *                     Unless it will return first matched IdentityProvider
     * @param value
     * @param tenantId
     * @param tenantDomain
     * @return
     * @throws IdentityProviderManagementException
     */
    public IdentityProvider getIdPByAuthenticatorPropertyValue(Connection dbConnection, String property, String value,
                                                               int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        IdentityProvider federatedIdp = null;
        boolean dbConnectionInitialized = true;
        if (dbConnection == null) {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
        } else {
            dbConnectionInitialized = false;
        }
        try {
            // SP_IDP_ID, SP_IDP_NAME, SP_IDP_PRIMARY, SP_IDP_HOME_REALM_ID,SP_IDP_CERTIFICATE,
            // SP_IDP_TOKEN_EP_ALIAS,
            // SP_IDP_INBOUND_PROVISIONING_ENABLED,SP_IDP_INBOUND_PROVISIONING_USER_STORE_ID,
            // SP_IDP_USER_CLAIM_URI,
            // SP_IDP_ROLE_CLAIM_URI,SP_IDP_DEFAULT_AUTHENTICATOR_NAME,SP_IDP_DEFAULT_PRO_CONNECTOR_NAME
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_BY_AUTHENTICATOR_PROPERTY;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, property);
            prepStmt.setString(2, value);
            prepStmt.setInt(3, tenantId);
            rs = prepStmt.executeQuery();
            int idpId = -1;
            String idPName = "";

            if (rs.next()) {
                federatedIdp = new IdentityProvider();

                idpId = rs.getInt("ID");
                idPName = rs.getString("NAME");

                federatedIdp.setIdentityProviderName(idPName);

                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_PRIMARY"))) {
                    federatedIdp.setPrimary(true);
                } else {
                    federatedIdp.setPrimary(false);
                }

                federatedIdp.setHomeRealmId(rs.getString("HOME_REALM_ID"));
                federatedIdp.setCertificate(getBlobValue(rs.getBinaryStream("CERTIFICATE")));
                federatedIdp.setAlias(rs.getString("ALIAS"));

                JustInTimeProvisioningConfig jitProConfig = new JustInTimeProvisioningConfig();
                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("INBOUND_PROV_ENABLED"))) {
                    jitProConfig.setProvisioningEnabled(true);
                } else {
                    jitProConfig.setProvisioningEnabled(false);
                }

                jitProConfig.setProvisioningUserStore(rs.getString("INBOUND_PROV_USER_STORE_ID"));
                federatedIdp.setJustInTimeProvisioningConfig(jitProConfig);

                String userClaimUri = rs.getString("USER_CLAIM_URI");
                String roleClaimUri = rs.getString("ROLE_CLAIM_URI");

                String defaultAuthenticatorName = rs.getString("DEFAULT_AUTHENTICATOR_NAME");
                String defaultProvisioningConnectorConfigName = rs.getString("DEFAULT_PRO_CONNECTOR_NAME");
                federatedIdp.setIdentityProviderDescription(rs.getString("DESCRIPTION"));

                // IS_FEDERATION_HUB_IDP
                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("IS_FEDERATION_HUB"))) {
                    federatedIdp.setFederationHub(true);
                } else {
                    federatedIdp.setFederationHub(false);
                }

                if (federatedIdp.getClaimConfig() == null) {
                    federatedIdp.setClaimConfig(new ClaimConfig());
                }

                // IS_LOCAL_CLAIM_DIALECT
                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("IS_LOCAL_CLAIM_DIALECT"))) {
                    federatedIdp.getClaimConfig().setLocalClaimDialect(true);
                } else {
                    federatedIdp.getClaimConfig().setLocalClaimDialect(false);
                }

                federatedIdp.setProvisioningRole(rs.getString("PROVISIONING_ROLE"));

                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("IS_ENABLED"))) {
                    federatedIdp.setEnable(true);
                } else {
                    federatedIdp.setEnable(false);
                }

                federatedIdp.setDisplayName(rs.getString("DISPLAY_NAME"));

                if (defaultAuthenticatorName != null) {
                    FederatedAuthenticatorConfig defaultAuthenticator = new FederatedAuthenticatorConfig();
                    defaultAuthenticator.setName(defaultAuthenticatorName);
                    federatedIdp.setDefaultAuthenticatorConfig(defaultAuthenticator);
                }

                if (defaultProvisioningConnectorConfigName != null) {
                    ProvisioningConnectorConfig defaultProConnector = new ProvisioningConnectorConfig();
                    defaultProConnector.setName(defaultProvisioningConnectorConfigName);
                    federatedIdp.setDefaultProvisioningConnectorConfig(defaultProConnector);
                }

                // get federated authenticators.
                federatedIdp.setFederatedAuthenticatorConfigs(getFederatedAuthenticatorConfigs(
                        dbConnection, idPName, federatedIdp, tenantId));

                if (federatedIdp.getClaimConfig().isLocalClaimDialect()) {
                    federatedIdp.setClaimConfig(getLocalIdPDefaultClaimValues(dbConnection,
                            idPName, userClaimUri, roleClaimUri, idpId, tenantId));
                } else {
                    // get claim configuration.
                    federatedIdp.setClaimConfig(getIdPClaimConfiguration(dbConnection, idPName,
                            userClaimUri, roleClaimUri, idpId, tenantId));
                }

                // get provisioning connectors.
                federatedIdp.setProvisioningConnectorConfigs(getProvisioningConnectorConfigs(
                        dbConnection, idPName, idpId, tenantId));

                // get permission and role configuration.
                federatedIdp.setPermissionAndRoleConfig(getPermissionsAndRoleConfiguration(
                        dbConnection, idPName, idpId, tenantId));

                List<IdentityProviderProperty> propertyList = getIdentityPropertiesByIdpId(dbConnection,
                        Integer.parseInt(rs.getString("ID")));
                federatedIdp.setIdpProperties(propertyList.toArray(new IdentityProviderProperty[propertyList.size()]));

            }
            dbConnection.commit();
            return federatedIdp;
        } catch (SQLException e) {
            IdentityApplicationManagementUtil.rollBack(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while retrieving Identity Provider " +
                    "information for Authenticator Property : " + property + " and value : " + value, e);
        } finally {
            if (dbConnectionInitialized) {
                IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            }else{
                IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            }
        }
    }

    /**
     * @param realmId
     * @param tenantId
     * @param tenantDomain
     * @return
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    public IdentityProvider getIdPByRealmId(String realmId, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String idPName = null;

        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_NAME_BY_REALM_ID_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            prepStmt.setString(3, realmId);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                idPName = rs.getString("NAME");
            }

            dbConnection.commit();
            return getIdPByName(dbConnection, idPName, tenantId, tenantDomain);
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error while retreiving Identity Provider by realm " +
                    realmId, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }

    }

    /**
     * @param identityProvider
     * @param tenantId
     * @throws IdentityProviderManagementException
     */
    public void addIdP(IdentityProvider identityProvider, int tenantId)
            throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        try {
            if (identityProvider.isPrimary()) {
                // this is going to be the primary. Switch off any other primary set up in the
                // system.
                switchOffPrimary(dbConnection, tenantId);
            }


            // SP_TENANT_ID, SP_IDP_NAME, SP_IDP_PRIMARY, SP_IDP_HOME_REALM_ID, SP_IDP_CERTIFICATE,
            // SP_IDP_TOKEN_EP_ALIAS,
            // SP_IDP_INBOUND_PROVISIONING_ENABLED,SP_IDP_INBOUND_PROVISIONING_USER_STORE_ID,
            // SP_IDP_USER_CLAIM_URI,SP_IDP_ROLE_CLAIM_URI,SP_IDP_DEFAULT_AUTHENTICATOR_NAME,SP_IDP_DEFAULT_PRO_CONNECTOR_NAME
            String sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_SQL;

            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, identityProvider.getIdentityProviderName());

            if (identityProvider.isPrimary()) {
                prepStmt.setString(3, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt.setString(3, IdPManagementConstants.IS_FALSE_VALUE);
            }

            prepStmt.setString(4, identityProvider.getHomeRealmId());

            if (StringUtils.isNotBlank(identityProvider.getCertificate())) {
                try {
                    IdentityApplicationManagementUtil.getCertData(identityProvider.getCertificate());
                } catch (CertificateException ex) {
                    throw new IdentityProviderManagementException("Malformed Public Certificate file has been provided."
                            , ex);
                }
            }
            setBlobValue(identityProvider.getCertificate(), prepStmt, 5);

            prepStmt.setString(6, identityProvider.getAlias());

            if (identityProvider.getJustInTimeProvisioningConfig() != null
                    && identityProvider.getJustInTimeProvisioningConfig().isProvisioningEnabled()) {
                // just in time provisioning enabled for this identity provider.
                // based on the authentication response from the identity provider - user will be
                // provisioned locally.
                prepStmt.setString(7, IdPManagementConstants.IS_TRUE_VALUE);
                // user will be provisioned to the configured user store.
                prepStmt.setString(8, identityProvider.getJustInTimeProvisioningConfig().getProvisioningUserStore());
            } else {
                prepStmt.setString(7, IdPManagementConstants.IS_FALSE_VALUE);
                prepStmt.setString(8, null);
            }

            if (identityProvider.getClaimConfig() != null) {
                // this is how we find the subject name from the authentication response.
                // this claim URI is in identity provider's own dialect.
                prepStmt.setString(9, identityProvider.getClaimConfig().getUserClaimURI());
                // this is how we find the role name from the authentication response.
                // this claim URI is in identity provider's own dialect.
                prepStmt.setString(10, identityProvider.getClaimConfig().getRoleClaimURI());
            } else {
                prepStmt.setString(9, null);
                prepStmt.setString(10, null);
            }

            if (identityProvider.getDefaultAuthenticatorConfig() != null) {
                prepStmt.setString(11, identityProvider.getDefaultAuthenticatorConfig().getName());
            } else {
                prepStmt.setString(11, null);
            }

            if (identityProvider.getDefaultProvisioningConnectorConfig() != null) {
                prepStmt.setString(12, identityProvider.getDefaultProvisioningConnectorConfig().getName());
            } else {
                prepStmt.setString(12, null);
            }

            prepStmt.setString(13, identityProvider.getIdentityProviderDescription());

            if (identityProvider.isFederationHub()) {
                prepStmt.setString(14, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt.setString(14, IdPManagementConstants.IS_FALSE_VALUE);
            }

            if (identityProvider.getClaimConfig() != null
                    && identityProvider.getClaimConfig().isLocalClaimDialect()) {
                prepStmt.setString(15, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt.setString(15, IdPManagementConstants.IS_FALSE_VALUE);
            }

            prepStmt.setString(16, identityProvider.getProvisioningRole());

            // enabled by default
            prepStmt.setString(17, IdPManagementConstants.IS_TRUE_VALUE);

            prepStmt.setString(18, identityProvider.getDisplayName());

            prepStmt.executeUpdate();
            prepStmt.clearParameters();

            // get the id of the just added identity provider.
            int idPId = getIdentityProviderIdByName(dbConnection,
                    identityProvider.getIdentityProviderName(), tenantId);

            if (idPId <= 0) {
                String msg = "Error adding Identity Provider for tenant " + tenantId;
                throw new IdentityProviderManagementException(msg);
            }

            // add provisioning connectors.
            if (identityProvider.getProvisioningConnectorConfigs() != null
                    && identityProvider.getProvisioningConnectorConfigs().length > 0) {
                addProvisioningConnectorConfigs(identityProvider.getProvisioningConnectorConfigs(),
                        dbConnection, idPId, tenantId);
            }

            // add federated authenticators.
            addFederatedAuthenticatorConfigs(identityProvider.getFederatedAuthenticatorConfigs(),
                    dbConnection, idPId, tenantId);

            // add role configuration.
            if (identityProvider.getPermissionAndRoleConfig() != null) {
                if (identityProvider.getPermissionAndRoleConfig().getIdpRoles() != null
                        && identityProvider.getPermissionAndRoleConfig().getIdpRoles().length > 0) {
                    // add roles.
                    addIdPRoles(dbConnection, idPId, tenantId, identityProvider
                            .getPermissionAndRoleConfig().getIdpRoles());

                    if (identityProvider.getPermissionAndRoleConfig().getRoleMappings() != null
                            && identityProvider.getPermissionAndRoleConfig().getRoleMappings().length > 0) {
                        // add role mappings.
                        addIdPRoleMappings(dbConnection, idPId, tenantId, identityProvider
                                .getPermissionAndRoleConfig().getRoleMappings());
                    }
                }
            }

            // add claim configuration.
            if (identityProvider.getClaimConfig() != null
                    && identityProvider.getClaimConfig().getClaimMappings() != null
                    && identityProvider.getClaimConfig().getClaimMappings().length > 0) {
                if (identityProvider.getClaimConfig().isLocalClaimDialect()) {
                    // identity provider is using local claim dialect - we do not need to add
                    // claims.
                    addDefaultClaimValuesForLocalIdP(dbConnection, idPId, tenantId,
                            identityProvider.getClaimConfig().getClaimMappings());
                } else {
                    addIdPClaims(dbConnection, idPId, tenantId, identityProvider.getClaimConfig()
                            .getIdpClaims());

                    addIdPClaimMappings(dbConnection, idPId, tenantId, identityProvider
                            .getClaimConfig().getClaimMappings());
                }

            }
            if(identityProvider.getIdpProperties() != null) {
                addIdentityProviderProperties(dbConnection, idPId, Arrays.asList(identityProvider.getIdpProperties())
                        , tenantId);
            }

            dbConnection.commit();
        } catch (IOException e) {
            throw new IdentityProviderManagementException("An error occurred while processing content stream.", e);
        } catch (SQLException e) {
            IdentityApplicationManagementUtil.rollBack(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while adding Identity Provider for tenant " + tenantId, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, null, prepStmt);
        }
    }

    /**
     * @param newIdentityProvider
     * @param currentIdentityProvider
     * @param tenantId
     * @throws IdentityProviderManagementException
     */
    public void updateIdP(IdentityProvider newIdentityProvider,
                          IdentityProvider currentIdentityProvider, int tenantId)
            throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt1 = null;
        PreparedStatement prepStmt2 = null;
        ResultSet rs = null;
        try {

            int idPId = getIdentityProviderIdByName(dbConnection,
                    currentIdentityProvider.getIdentityProviderName(), tenantId);

            if (idPId <= 0) {
                String msg = "Trying to update non-existent Identity Provider for tenant "
                        + tenantId;
                throw new IdentityProviderManagementException(msg);
            }

            // SP_IDP_NAME=?, SP_IDP_PRIMARY=?,SP_IDP_HOME_REALM_ID=?, SP_IDP_CERTIFICATE=?,
            // SP_IDP_TOKEN_EP_ALIAS=?,
            // SP_IDP_INBOUND_PROVISIONING_ENABLED=?,SP_IDP_INBOUND_PROVISIONING_USER_STORE_ID=?,SP_IDP_USER_CLAIM_URI=?,
            // SP_IDP_ROLE_CLAIM_URI=?,SP_IDP_DEFAULT_AUTHENTICATOR_NAME=?,SP_IDP_DEFAULT_PRO_CONNECTOR_NAME=?
            String sqlStmt = IdPManagementConstants.SQLQueries.UPDATE_IDP_SQL;
            prepStmt1 = dbConnection.prepareStatement(sqlStmt);

            prepStmt1.setString(1, newIdentityProvider.getIdentityProviderName());

            if (newIdentityProvider.isPrimary()) {
                prepStmt1.setString(2, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt1.setString(2, IdPManagementConstants.IS_FALSE_VALUE);
            }

            prepStmt1.setString(3, newIdentityProvider.getHomeRealmId());

            if (StringUtils.isNotBlank(newIdentityProvider.getCertificate())) {
                try {
                    IdentityApplicationManagementUtil.getCertData(newIdentityProvider.getCertificate());
                } catch (CertificateException ex) {
                    throw new IdentityProviderManagementException("Malformed Public Certificate file has been provided.", ex);
                }
            }
            setBlobValue(newIdentityProvider.getCertificate(), prepStmt1, 4);

            prepStmt1.setString(5, newIdentityProvider.getAlias());

            if (newIdentityProvider.getJustInTimeProvisioningConfig() != null
                    && newIdentityProvider.getJustInTimeProvisioningConfig()
                    .isProvisioningEnabled()) {
                prepStmt1.setString(6, IdPManagementConstants.IS_TRUE_VALUE);
                prepStmt1.setString(7, newIdentityProvider.getJustInTimeProvisioningConfig().getProvisioningUserStore());

            } else {
                prepStmt1.setString(6, IdPManagementConstants.IS_FALSE_VALUE);
                prepStmt1.setString(7, null);
            }

            if (newIdentityProvider.getClaimConfig() != null) {
                prepStmt1.setString(8, newIdentityProvider.getClaimConfig().getUserClaimURI());
                prepStmt1.setString(9, newIdentityProvider.getClaimConfig().getRoleClaimURI());
            } else {
                prepStmt1.setString(8, null);
                prepStmt1.setString(9, null);
            }

            // update the default authenticator
            if (newIdentityProvider.getDefaultAuthenticatorConfig() != null
                    && newIdentityProvider.getDefaultAuthenticatorConfig().getName() != null) {
                prepStmt1.setString(10, newIdentityProvider.getDefaultAuthenticatorConfig().getName());
            } else {
                // its not a must to have a default authenticator.
                prepStmt1.setString(10, null);
            }

            // update the default provisioning connector.
            if (newIdentityProvider.getDefaultProvisioningConnectorConfig() != null
                    && newIdentityProvider.getDefaultProvisioningConnectorConfig().getName() != null) {
                prepStmt1.setString(11, newIdentityProvider.getDefaultProvisioningConnectorConfig().getName());
            } else {
                // its not a must to have a default provisioning connector..
                prepStmt1.setString(11, null);
            }

            prepStmt1.setString(12, newIdentityProvider.getIdentityProviderDescription());

            if (newIdentityProvider.isFederationHub()) {
                prepStmt1.setString(13, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt1.setString(13, IdPManagementConstants.IS_FALSE_VALUE);
            }

            if (newIdentityProvider.getClaimConfig() != null
                    && newIdentityProvider.getClaimConfig().isLocalClaimDialect()) {
                prepStmt1.setString(14, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt1.setString(14, IdPManagementConstants.IS_FALSE_VALUE);
            }

            prepStmt1.setString(15, newIdentityProvider.getProvisioningRole());

            if (newIdentityProvider.isEnable()) {
                prepStmt1.setString(16, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt1.setString(16, IdPManagementConstants.IS_FALSE_VALUE);
            }

            prepStmt1.setString(17, newIdentityProvider.getDisplayName());

            prepStmt1.setInt(18, tenantId);
            prepStmt1.setString(19, currentIdentityProvider.getIdentityProviderName());

            prepStmt1.executeUpdate();

            sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_BY_NAME_SQL;
            prepStmt2 = dbConnection.prepareStatement(sqlStmt);
            prepStmt2.setInt(1, tenantId);
            prepStmt2.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            prepStmt2.setString(3, newIdentityProvider.getIdentityProviderName());
            rs = prepStmt2.executeQuery();

            if (rs.next()) {

                // id of the updated identity provider.
                int idpId = rs.getInt("ID");

                // update federated authenticators.
                updateFederatedAuthenticatorConfigs(
                        newIdentityProvider.getFederatedAuthenticatorConfigs(),
                        currentIdentityProvider.getFederatedAuthenticatorConfigs(), dbConnection,
                        idpId, tenantId);

                // update claim configuration.
                updateClaimConfiguration(dbConnection, idpId, tenantId,
                        newIdentityProvider.getClaimConfig());

                // update role configuration.
                updateRoleConfiguration(dbConnection, idpId, tenantId,
                        newIdentityProvider.getPermissionAndRoleConfig());

                // // update provisioning connectors.
                updateProvisioningConnectorConfigs(
                        newIdentityProvider.getProvisioningConnectorConfigs(), dbConnection, idpId,
                        tenantId);

                if(newIdentityProvider.getIdpProperties() != null) {
                    updateIdentityProviderProperties(dbConnection, idpId,
                            Arrays.asList(newIdentityProvider.getIdpProperties()), tenantId);
                }

            }

            dbConnection.commit();
        } catch (IOException e) {
            throw new IdentityProviderManagementException("An error occurred while processing content stream.", e);
        } catch (SQLException e) {
            IdentityApplicationManagementUtil.rollBack(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while updating Identity Provider " +
                    "information  for tenant " + tenantId, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt1);
            IdentityDatabaseUtil.closeStatement(prepStmt2);
        }
    }

    public boolean isIdpReferredBySP(String idPName, int tenantId)
            throws IdentityProviderManagementException {
        boolean isReffered = false;
        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmtFedIdp = null;
        ResultSet rsFedIdp = null;
        PreparedStatement prepStmtProvIdp = null;
        ResultSet rsProvIdp = null;

        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_SP_FEDERATED_IDP_REFS;
            prepStmtFedIdp = dbConnection.prepareStatement(sqlStmt);
            prepStmtFedIdp.setInt(1, tenantId);
            prepStmtFedIdp.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            prepStmtFedIdp.setString(3, idPName);
            rsFedIdp = prepStmtFedIdp.executeQuery();
            if (rsFedIdp.next()) {
                isReffered = rsFedIdp.getInt(1) > 0;
            }
            if (!isReffered) {
                sqlStmt = IdPManagementConstants.SQLQueries.GET_SP_PROVISIONING_CONNECTOR_REFS;
                prepStmtProvIdp = dbConnection.prepareStatement(sqlStmt);
                prepStmtProvIdp.setInt(1, tenantId);
                prepStmtProvIdp.setString(2, idPName);
                rsProvIdp = prepStmtProvIdp.executeQuery();
                if (rsProvIdp.next()) {
                    isReffered = rsProvIdp.getInt(1) > 0;
                }
            }
            dbConnection.commit();
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while searching for IDP references in SP ", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rsProvIdp, prepStmtProvIdp);
            IdentityDatabaseUtil.closeAllConnections(dbConnection, rsFedIdp, prepStmtFedIdp);
        }
        return isReffered;
    }

    /**
     * @param idPName
     * @param tenantId
     * @param tenantDomain
     * @throws IdentityProviderManagementException
     */
    public void deleteIdP(String idPName, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        try {
            IdentityProvider identityProvider = getIdPByName(dbConnection, idPName, tenantId,
                    tenantDomain);
            if (identityProvider == null) {
                String msg = "Trying to delete non-existent Identity Provider for tenant "
                        + tenantDomain;
                log.error(msg);
                return;
            }
            deleteIdP(dbConnection, tenantId, idPName);
            dbConnection.commit();
        } catch (SQLException e) {
            IdentityApplicationManagementUtil.rollBack(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while deleting Identity Provider of tenant "
                    + tenantDomain, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }


    public void deleteTenantRole(int tenantId, String role, String tenantDomain)
            throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_ROLE_LISTENER_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, role);
            prepStmt.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {
            IdentityApplicationManagementUtil.rollBack(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while deleting tenant role " + role +
                    " of tenant " + tenantDomain, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, null, prepStmt);
        }
    }

    public void renameTenantRole(String newRoleName, String oldRoleName, int tenantId,
                                 String tenantDomain) throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.RENAME_ROLE_LISTENER_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, newRoleName);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, oldRoleName);
            prepStmt.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {
            IdentityApplicationManagementUtil.rollBack(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while renaming tenant role " + oldRoleName + " to "
                    + newRoleName + " of tenant " + tenantDomain, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, null, prepStmt);
        }
    }

    /**
     * @param dbConnection
     * @param idpId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private void deleteAllIdPClaims(Connection dbConnection, int idpId)
            throws IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt = null;
        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_ALL_CLAIMS_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idpId);
            prepStmt.executeUpdate();
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    /**
     * @param dbConnection
     * @param idpId
     * @param tenantId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private void deleteLocalIdPClaimValues(Connection dbConnection, int idpId, int tenantId)
            throws IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt = null;
        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_LOCAL_IDP_DEFAULT_CLAIM_VALUES_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idpId);
            prepStmt.setInt(2, tenantId);

            prepStmt.executeUpdate();
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);

        }
    }

    /**
     * @param dbConnection
     * @param idpId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private void deleteAllIdPRoles(Connection dbConnection, int idpId)
            throws IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt = null;
        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_ALL_ROLES_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idpId);
            prepStmt.executeUpdate();
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);

        }
    }

    /**
     * @param newClaimURI
     * @param oldClaimURI
     * @param tenantId
     * @param tenantDomain
     * @throws IdentityProviderManagementException
     */
    public void renameClaimURI(String newClaimURI, String oldClaimURI, int tenantId,
                               String tenantDomain) throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();;
        PreparedStatement prepStmt = null;
        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.RENAME_CLAIM_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, newClaimURI);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, oldClaimURI);
            prepStmt.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while renaming tenant role " + oldClaimURI + " to "
                    + newClaimURI + " of tenant " + tenantDomain, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, null, prepStmt);
        }
    }

    /**
     * @param conn
     * @param tenantId
     * @throws SQLException
     */
    private void switchOffPrimary(Connection conn, int tenantId) throws SQLException {

        PreparedStatement prepStmt = null;
        // SP_IDP_PRIMARY
        String sqlStmt = IdPManagementConstants.SQLQueries.SWITCH_IDP_PRIMARY_SQL;

        try {
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setString(1, "0");
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, "1");
            prepStmt.executeUpdate();
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    private void doAppointPrimary(Connection conn, int tenantId, String tenantDomain)
            throws SQLException, IdentityProviderManagementException {

        List<IdentityProvider> tenantIdPs = getIdPs(conn, tenantId, tenantDomain);
        if (!tenantIdPs.isEmpty()) {
            PreparedStatement prepStmt = null;
            try {
                String sqlStmt = IdPManagementConstants.SQLQueries.SWITCH_IDP_PRIMARY_ON_DELETE_SQL;
                prepStmt = conn.prepareStatement(sqlStmt);
                prepStmt.setString(1, IdPManagementConstants.IS_TRUE_VALUE);
                prepStmt.setInt(2, tenantId);
                prepStmt.setString(3, tenantIdPs.get(0).getIdentityProviderName());
                prepStmt.setString(4, IdPManagementConstants.IS_FALSE_VALUE);
                prepStmt.executeUpdate();
            } finally {
                IdentityDatabaseUtil.closeStatement(prepStmt);
            }
        } else {
            String msg = "No Identity Providers registered for tenant " + tenantDomain;
            log.warn(msg);
        }
    }

    /**
     * @param conn
     * @param idPId
     * @param claims
     * @throws SQLException
     */
    private void addIdPClaims(Connection conn, int idPId, int tenantId, Claim[] claims)
            throws SQLException {
        PreparedStatement prepStmt = null;

        if (claims == null || claims.length == 0) {
            return;
        }

        try {
            // SP_IDP_ID, SP_IDP_CLAIM
            String sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_CLAIMS_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            for (Claim claim : claims) {
                prepStmt.setInt(1, idPId);
                prepStmt.setInt(2, tenantId);
                prepStmt.setString(3, claim.getClaimUri());
                prepStmt.addBatch();
                prepStmt.clearParameters();
            }
            prepStmt.executeBatch();
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    /**
     * @param conn
     * @param idPId
     * @param tenantId
     * @param claimMappings
     * @throws SQLException
     * @throws IdentityProviderManagementException
     */
    private void addDefaultClaimValuesForLocalIdP(Connection conn, int idPId, int tenantId,
                                                  ClaimMapping[] claimMappings) throws SQLException,
            IdentityProviderManagementException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlStmt;

        try {

            if (claimMappings == null || claimMappings.length == 0) {
                return;
            }

            sqlStmt = IdPManagementConstants.SQLQueries.ADD_LOCAL_IDP_DEFAULT_CLAIM_VALUES_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            for (ClaimMapping mapping : claimMappings) {
                if (mapping != null && mapping.getLocalClaim() != null
                        && mapping.getLocalClaim().getClaimUri() != null) {

                    prepStmt.setInt(1, idPId);
                    prepStmt.setString(2, mapping.getLocalClaim().getClaimUri());
                    prepStmt.setString(3, mapping.getDefaultValue());
                    prepStmt.setInt(4, tenantId);
                    if (mapping.isRequested()) {
                        prepStmt.setString(5, IdPManagementConstants.IS_TRUE_VALUE);
                    } else {
                        prepStmt.setString(5, IdPManagementConstants.IS_FALSE_VALUE);
                    }
                    prepStmt.addBatch();
                }
            }

            prepStmt.executeBatch();

        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    /**
     * @param conn
     * @param idPId
     * @param tenantId
     * @param claimMappings
     * @throws SQLException
     * @throws IdentityProviderManagementException
     */
    private void addIdPClaimMappings(Connection conn, int idPId, int tenantId,
                                     ClaimMapping[] claimMappings) throws SQLException,
            IdentityProviderManagementException {

        Map<String, Integer> claimIdMap = new HashMap<String, Integer>();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        try {

            if (claimMappings == null || claimMappings.length == 0) {
                return;
            }

            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_CLAIMS_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("ID");
                String claim = rs.getString("CLAIM");
                claimIdMap.put(claim, id);
            }

            prepStmt.clearParameters();

            if (claimIdMap.isEmpty()) {
                String message = "No Identity Provider claim URIs defined for tenant " + tenantId;
                throw new IdentityProviderManagementException(message);
            }

            sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_CLAIM_MAPPINGS_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            for (ClaimMapping mapping : claimMappings) {
                if (mapping != null && mapping.getRemoteClaim() != null
                        && claimIdMap.containsKey(mapping.getRemoteClaim().getClaimUri())) {

                    int idpClaimId = claimIdMap.get(mapping.getRemoteClaim().getClaimUri());
                    String localClaimURI = mapping.getLocalClaim().getClaimUri();

                    prepStmt.setInt(1, idpClaimId);
                    prepStmt.setInt(2, tenantId);
                    prepStmt.setString(3, localClaimURI);
                    prepStmt.setString(4, mapping.getDefaultValue());

                    if (mapping.isRequested()) {
                        prepStmt.setString(5, IdPManagementConstants.IS_TRUE_VALUE);
                    } else {
                        prepStmt.setString(5, IdPManagementConstants.IS_FALSE_VALUE);
                    }

                    prepStmt.addBatch();
                } else {
                    throw new IdentityProviderManagementException("Cannot find Identity Provider claim mapping for tenant "
                            + tenantId);
                }
            }

            prepStmt.executeBatch();

        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    /**
     * @param conn
     * @param idPId
     * @param idpRoleNames
     * @throws SQLException
     */
    private void addIdPRoles(Connection conn, int idPId, int tenantId, String[] idpRoleNames)
            throws SQLException {

        PreparedStatement prepStmt = null;
        // SP_IDP_ID, SP_IDP_ROLE
        String sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_ROLES_SQL;

        if (idpRoleNames == null || idpRoleNames.length == 0) {
            return;
        }

        try {
            prepStmt = conn.prepareStatement(sqlStmt);

            for (String idpRole : idpRoleNames) {
                prepStmt.setInt(1, idPId);
                prepStmt.setInt(2, tenantId);
                prepStmt.setString(3, idpRole);
                prepStmt.addBatch();
                prepStmt.clearParameters();
            }

            prepStmt.executeBatch();

        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    /**
     * @param conn
     * @param idPId
     * @param tenantId
     * @param roleMappings
     * @throws SQLException
     * @throws IdentityProviderManagementException
     */
    private void addIdPRoleMappings(Connection conn, int idPId, int tenantId,
                                    RoleMapping[] roleMappings) throws SQLException,
            IdentityProviderManagementException {

        Map<String, Integer> roleIdMap = new HashMap<String, Integer>();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        // SP_IDP_ROLE_ID, SP_IDP_ROL
        String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_ROLES_SQL;

        try {

            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                int idpRoleId = rs.getInt("ID");
                String roleName = rs.getString("ROLE");
                roleIdMap.put(roleName, idpRoleId);
            }


            if (roleIdMap.isEmpty()) {
                String message = "No Identity Provider roles defined for tenant " + tenantId;
                throw new IdentityProviderManagementException(message);
            }

            sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_ROLE_MAPPINGS_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);

            for (RoleMapping mapping : roleMappings) {
                if (mapping.getRemoteRole() != null
                        && roleIdMap.containsKey(mapping.getRemoteRole())) {

                    int idpRoleId = roleIdMap.get(mapping.getRemoteRole());

                    String userStoreId = mapping.getLocalRole().getUserStoreId();
                    String localRole = mapping.getLocalRole().getLocalRoleName();

                    // SP_IDP_ROLE_ID, SP_TENANT_ID, SP_USER_STORE_ID, SP_LOCAL_ROLE
                    prepStmt.setInt(1, idpRoleId);
                    prepStmt.setInt(2, tenantId);
                    prepStmt.setString(3, userStoreId);
                    prepStmt.setString(4, localRole);
                    prepStmt.addBatch();
                } else {
                    throw new IdentityProviderManagementException("Cannot find Identity Provider role " +
                            mapping.getRemoteRole() + " for tenant " + tenantId);
                }
            }

            prepStmt.executeBatch();

        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }

    }

    /**
     * @param conn
     * @param idPId
     * @param tenantId
     * @param newClaimConfig
     * @throws SQLException
     * @throws IdentityProviderManagementException
     */
    private void updateClaimConfiguration(Connection conn, int idPId, int tenantId,
                                          ClaimConfig newClaimConfig) throws SQLException,
            IdentityProviderManagementException {

        // remove all identity provider claims - this will also remove associated claim mappings.
        deleteAllIdPClaims(conn, idPId);

        // delete local claim identity provider claim values.
        deleteLocalIdPClaimValues(conn, idPId, tenantId);

        if (newClaimConfig == null) {
            // bad data - we do not need.
            return;
        }

        if (newClaimConfig.isLocalClaimDialect()) {
            if (newClaimConfig.getClaimMappings() != null && newClaimConfig.getClaimMappings().length > 0) {
                // add claim mappings only.
                addDefaultClaimValuesForLocalIdP(conn, idPId, tenantId,
                        newClaimConfig.getClaimMappings());
            }
        } else {
            boolean addedClaims = false;
            if (newClaimConfig.getIdpClaims() != null && newClaimConfig.getIdpClaims().length > 0) {
                // add identity provider claims.
                addIdPClaims(conn, idPId, tenantId, newClaimConfig.getIdpClaims());
                addedClaims = true;
            }
            if (addedClaims && newClaimConfig.getClaimMappings() != null &&
                    newClaimConfig.getClaimMappings().length > 0) {
                // add identity provider claim mappings if and only if IdP claims are not empty.
                addIdPClaimMappings(conn, idPId, tenantId, newClaimConfig.getClaimMappings());
            }
        }
    }

    /**
     * @param conn
     * @param idPId
     * @param addedRoles
     * @param deletedRoles
     * @param renamedOldRoles
     * @param renamedNewRoles
     * @throws SQLException
     */
    private void updateIdPRoles(Connection conn, int idPId, List<String> addedRoles,
                                List<String> deletedRoles, List<String> renamedOldRoles, List<String> renamedNewRoles)
            throws SQLException {

        PreparedStatement prepStmt1 = null;
        PreparedStatement prepStmt2 = null;
        PreparedStatement prepStmt3 = null;
        String sqlStmt = null;

        try {

            for (String deletedRole : deletedRoles) {
                sqlStmt = IdPManagementConstants.SQLQueries.DELETE_IDP_ROLES_SQL;
                prepStmt1 = conn.prepareStatement(sqlStmt);
                prepStmt1.setInt(1, idPId);
                prepStmt1.setString(2, deletedRole);
                prepStmt1.addBatch();
            }

            prepStmt1.executeBatch();

            for (String addedRole : addedRoles) {
                sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_ROLES_SQL;
                prepStmt2 = conn.prepareStatement(sqlStmt);
                prepStmt2.setInt(1, idPId);
                prepStmt2.setString(2, addedRole);
                prepStmt2.addBatch();
            }

            prepStmt2.executeBatch();
            prepStmt2.clearParameters();
            prepStmt2.clearBatch();

            for (int i = 0; i < renamedOldRoles.size(); i++) {
                sqlStmt = IdPManagementConstants.SQLQueries.UPDATE_IDP_ROLES_SQL;
                prepStmt3 = conn.prepareStatement(sqlStmt);
                prepStmt3.setString(1, renamedNewRoles.get(i));
                prepStmt3.setInt(2, idPId);
                prepStmt3.setString(3, renamedOldRoles.get(i));
                prepStmt3.addBatch();
            }

            prepStmt3.executeBatch();

        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt3);
            IdentityDatabaseUtil.closeStatement(prepStmt2);
            IdentityDatabaseUtil.closeStatement(prepStmt1);
        }

    }

    /**
     * @param conn
     * @param idPId
     * @param tenantId
     * @param newRoleConfiguration
     * @param newRoleConfiguration
     * @throws SQLException
     * @throws IdentityProviderManagementException
     */
    private void updateRoleConfiguration(Connection conn, int idPId, int tenantId,
                                         PermissionsAndRoleConfig newRoleConfiguration) throws SQLException,
            IdentityProviderManagementException {

        // delete all identity provider roles - this will also clean up idp role mappings.
        deleteAllIdPRoles(conn, idPId);

        if (newRoleConfiguration == null) {
            // bad data - we do not need to deal with.
            return;
        }

        // add identity provider roles.
        addIdPRoles(conn, idPId, tenantId, newRoleConfiguration.getIdpRoles());

        if (newRoleConfiguration.getRoleMappings() == null
                || newRoleConfiguration.getRoleMappings().length == 0) {
            // we do not have any role mappings in the system.
            return;
        }

        // add identity provider role mappings.
        addIdPRoleMappings(conn, idPId, tenantId, newRoleConfiguration.getRoleMappings());

    }

    /**
     * @param conn
     * @param conn
     * @param idPId
     * @throws SQLException
     */
    private void deleteProvisioningConnectorConfigs(Connection conn, int idPId) throws SQLException {

        PreparedStatement prepStmt = null;
        String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_PROVISIONING_CONNECTORS;

        try {
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            prepStmt.executeUpdate();
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    /**
     * @param conn
     * @param tenantId
     * @param idPName
     * @throws SQLException
     */
    private void deleteIdP(Connection conn, int tenantId, String idPName) throws SQLException {

        PreparedStatement prepStmt = null;
        String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_IDP_SQL;

        try {
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, idPName);
            prepStmt.executeUpdate();
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    /**
     * @param dbConnection
     * @param idpName
     * @param tenantId
     * @return
     * @throws SQLException
     * @throws IdentityProviderManagementException
     */
    private int getIdentityProviderIdByName(Connection dbConnection, String idpName, int tenantId)
            throws SQLException, IdentityProviderManagementException {

        boolean dbConnInitialized = true;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        if (dbConnection == null) {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
        } else {
            dbConnInitialized = false;
        }
        try {

            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_ROW_ID_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            prepStmt.setString(3, idpName);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            if (dbConnInitialized) {
                IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            }else{
                IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            }
        }
        return 0;
    }

    /**
     * @param o1
     * @param o2
     * @return
     */
    private Property[] concatArrays(Property[] o1, Property[] o2) {
        Property[] ret = new Property[o1.length + o2.length];

        System.arraycopy(o1, 0, ret, 0, o1.length);
        System.arraycopy(o2, 0, ret, o1.length, o2.length);

        return ret;
    }

    /**
     * @param dbConnection
     * @param idPName
     * @param tenantId
     * @return
     * @throws SQLException
     * @throws IdentityProviderManagementException
     */
    private int getIdentityProviderIdentifier(Connection dbConnection, String idPName, int tenantId)
            throws SQLException, IdentityProviderManagementException {

        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_BY_NAME_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            prepStmt.setString(3, idPName);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID");
            } else {
                throw new IdentityProviderManagementException("Invalid Identity Provider Name "
                        + idPName);
            }
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    public boolean isIdPAvailableForAuthenticatorProperty(String authenticatorName, String propertyName, String idPEntityId, int tenantId)
            throws IdentityProviderManagementException {
        boolean isAvailable = false;
        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_SIMILAR_IDP_ENTITIY_IDS;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, propertyName);
            prepStmt.setString(2, idPEntityId);
            prepStmt.setInt(3, tenantId);
            prepStmt.setString(4, authenticatorName);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                isAvailable = rs.getInt(1) > 0;
            }
            dbConnection.commit();
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while searching for similar IdP EntityIds", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
        return isAvailable;
    }

    private int getAuthenticatorIdentifier(Connection dbConnection, int idPId, String authnType)
            throws SQLException, IdentityProviderManagementException {

        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_AUTH_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            prepStmt.setString(2, authnType);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID");
            } else {
                throw new IdentityProviderManagementException("Cannot find authenticator : "
                        + authnType);
            }
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }
}
