/*
 * Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.provider.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.provider.IdentityProviderException;
import org.wso2.carbon.identity.provider.common.model.IdentityProvider;
import org.wso2.carbon.identity.provider.common.model.MetaIdentityProvider;
import org.wso2.carbon.identity.provider.common.model.ResidentIdentityProvider;

import java.util.List;
/**
 * Data Access Object to the data storage to retrieve and store identity provider and related configurations.
 */
public class IdentityProviderDAO {

    private static final Logger log = LoggerFactory.getLogger(IdentityProviderDAO.class);

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Adds an identity provider to the persistent store.
     * @param identityProvider The IdP to be added.
     * @throws IdentityProviderException when any database level exception occurs.
     */
    public void addIdentityProvider(IdentityProvider identityProvider) throws IdentityProviderException {
        final String INSERT_IDP_SQL = "INSERT INTO IDP (NAME, DISPLAY_NAME, DESCRIPTION) " + "VALUES(?,?,?)";

        this.jdbcTemplate.executeUpdate(INSERT_IDP_SQL, (preparedStatement, bean) -> {
            MetaIdentityProvider metaIdentityProvider = identityProvider.getMetaIdentityProvider();
            preparedStatement.setString(1, metaIdentityProvider.getName());
            preparedStatement.setString(2, metaIdentityProvider.getDisplayName());
            preparedStatement.setString(3, metaIdentityProvider.getDescription());

        }, identityProvider);
    }

    /**
     * @return
     * @throws IdentityProviderException
     */
    public List<IdentityProvider> getAllIdentityProviders() throws IdentityProviderException {

        final String GET_ALL_IDP_SQL = "SELECT ID, NAME, DISPLAY_NAME, DESCRIPTION, "
                + "IS_FEDERATION_HUB, IS_LOCAL_CLAIM_DIALECT, IS_ENABLED, ID FROM IDP";

        List<IdentityProvider> idps = this.jdbcTemplate.executeQuery(GET_ALL_IDP_SQL, (resultSet, rowNumber) -> {
            IdentityProvider.IdentityProviderBuilder identityProviderBuilder = ResidentIdentityProvider
                    .newBuilder(null);
            MetaIdentityProvider.MetaIdentityProviderBuilder metaIdentityProviderBuilder = MetaIdentityProvider
                    .newBuilder(resultSet.getInt("ID"), resultSet.getString("NAME")).setDialect("wso2");
            identityProviderBuilder.setMetaIdentityProvider(metaIdentityProviderBuilder.build());

            return identityProviderBuilder.build();
        });

        return idps;
    }
}
