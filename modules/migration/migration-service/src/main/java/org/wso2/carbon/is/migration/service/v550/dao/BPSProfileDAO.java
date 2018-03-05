/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.is.migration.service.v550.dao;

import org.wso2.carbon.is.migration.service.v550.bean.BPSProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_ALL_BPS_PROFILES;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.UPDATE_BPS_PROFILE_PASSWORD;

public class BPSProfileDAO {

    private static BPSProfileDAO instance = new BPSProfileDAO();

    private BPSProfileDAO() {

    }

    public static BPSProfileDAO getInstance() {

        return instance;
    }

    public List<BPSProfile> getAllProfiles(Connection connection) throws SQLException {

        List<BPSProfile> bpsProfileList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(RETRIEVE_ALL_BPS_PROFILES);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                bpsProfileList.add(new BPSProfile(resultSet.getString("PROFILE_NAME"),
                        resultSet.getInt("TENANT_ID"),
                        resultSet.getString("PASSWORD")));
            }
            connection.commit();
        }
        return bpsProfileList;
    }

    /**
     * Method to update BPS profile passwords to new encrypted format.
     *
     * @param updatedBpsProfileList BPS profile info list containing updated passwords.
     * @param connection
     * @throws SQLException
     */
    public void updateNewPasswords(List<BPSProfile> updatedBpsProfileList, Connection connection) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_BPS_PROFILE_PASSWORD)) {
            for (BPSProfile bpsProfile : updatedBpsProfileList) {
                preparedStatement.setString(1, bpsProfile.getPassword());
                preparedStatement.setString(2, bpsProfile.getProfileName());
                preparedStatement.setInt(3, bpsProfile.getTenantId());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            connection.commit();
        }
    }
}
