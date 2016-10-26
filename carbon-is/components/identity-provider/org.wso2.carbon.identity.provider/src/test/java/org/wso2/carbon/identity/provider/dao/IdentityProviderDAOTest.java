/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.h2.jdbcx.JdbcConnectionPool;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.provider.common.model.IdentityProvider;
import org.wso2.carbon.identity.provider.common.model.MetaIdentityProvider;
import org.wso2.carbon.identity.provider.common.model.ResidentIdentityProvider;

import javax.sql.DataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

@Test
public class IdentityProviderDAOTest {

    @org.testng.annotations.Test
    public void testGetAllIdentityProviders() throws Exception {
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        IdentityProviderDAO identityProviderDAO = new IdentityProviderDAO();
        identityProviderDAO.setJdbcTemplate(jdbcTemplate);

        jdbcTemplate.executeUpdate("INSERT INTO IDP (NAME, DISPLAY_NAME, DESCRIPTION) "
                + "VALUES ('No Name', 'No Name', 'No Name')");
        List<IdentityProvider> identityProviderList = identityProviderDAO.getAllIdentityProviders();
        assertNotNull(identityProviderList, "The identity Providers shuld not be null");
        assertEquals(identityProviderList.size(), 1);

    }

    @org.testng.annotations.Test
    public void testAddIdentityProvider() throws Exception {
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        IdentityProviderDAO identityProviderDAO = new IdentityProviderDAO();
        identityProviderDAO.setJdbcTemplate(jdbcTemplate);

        IdentityProvider identityProvider = createIdentityProvider("Test Name", "Test Label", "Test Desc");

        List<IdentityProvider> identityProviderList = identityProviderDAO.getAllIdentityProviders();
        assertEquals(identityProviderList.size(), 0, "There are not IdP initially");

        identityProviderDAO.addIdentityProvider(identityProvider);
        identityProviderList = identityProviderDAO.getAllIdentityProviders();
        assertEquals(identityProviderList.size(), 1, "There should be on IdP after adding");
    }

    private IdentityProvider createIdentityProvider(String name, String label, String description) {
        IdentityProvider.IdentityProviderBuilder identityProviderBuilder = ResidentIdentityProvider
                .newBuilder(null);
        MetaIdentityProvider.MetaIdentityProviderBuilder metaIdentityProviderBuilder = MetaIdentityProvider
                .newBuilder(0, name).setDialect("wso2").setDisplayName(label).setDescription(description);
        identityProviderBuilder.setMetaIdentityProvider(metaIdentityProviderBuilder.build());

        return identityProviderBuilder.build();
    }

    private JdbcTemplate getJdbcTemplate() {
        DataSource ds = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "user", "password");

        try( InputStream databaseInputStream = this.getClass().getClassLoader().getResourceAsStream("dbscripts/h2.sql");
                Connection conn = ds.getConnection();
            Statement statement = conn.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
            String sql = read(databaseInputStream);
            statement.executeUpdate(sql);
        }catch (SQLException|IOException e){
            fail("Could not create in-memory h2 database", e);
        }

        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        return jdbcTemplate;
    }

    private  String read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }


}