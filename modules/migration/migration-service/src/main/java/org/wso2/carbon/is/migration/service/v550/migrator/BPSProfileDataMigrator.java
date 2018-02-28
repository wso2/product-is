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
package org.wso2.carbon.is.migration.service.v550.migrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v550.bean.BPSProfile;
import org.wso2.carbon.is.migration.service.v550.dao.BPSProfileDAO;
import org.wso2.carbon.is.migration.util.Constant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Migrator class to encrypt and migrate the BPS profile password.
 */
public class BPSProfileDataMigrator extends Migrator {

    private static final Log log = LogFactory
            .getLog(org.wso2.carbon.is.migration.service.v550.migrator.BPSProfileDataMigrator.class);

    @Override
    public void migrate() throws MigrationClientException {

        migrateBPSProfilePassword();
    }

    /**
     * This method will migrate the BPS profile password encrypted with old encryption algorithm to new encryption
     * algorithm.
     *
     * @throws MigrationClientException
     */
    private void migrateBPSProfilePassword() throws MigrationClientException {

        log.info(Constant.MIGRATION_LOG + "Migration starting on BPS profile table.");

        List<BPSProfile> bpsProfileList;

        try {
            try (Connection connection = getDataSource().getConnection()) {
                bpsProfileList = BPSProfileDAO.getInstance().getAllProfiles(connection);
            }

            List<BPSProfile> updatedBpsProfileList = transformPasswordFromOldToNewEncryption(bpsProfileList);

            try (Connection connection = getDataSource().getConnection()) {
                BPSProfileDAO.getInstance().updateNewPasswords(updatedBpsProfileList, connection);
            }
        } catch (SQLException e) {
            throw new MigrationClientException(
                    "Error while retrieving datasource or database connection for BPS " + "profiles table", e);
        } catch (CryptoException e) {
            throw new MigrationClientException(
                    "Error while checking whether the passwords are encrypted with new " + "encryption algorithm.");
        }
    }

    private List<BPSProfile> transformPasswordFromOldToNewEncryption(List<BPSProfile> bpsProfileList)
            throws CryptoException {

        List<BPSProfile> updatedBpsProfileList = new ArrayList<>();

        for (BPSProfile bpsProfile : bpsProfileList) {
            if (!CryptoUtil.getDefaultCryptoUtil().base64DecodeAndIsSelfContainedCipherText(bpsProfile.getPassword())) {
                byte[] decryptedPassword = CryptoUtil.getDefaultCryptoUtil()
                        .base64DecodeAndDecrypt(bpsProfile.getPassword(), "RSA");
                String newEncryptedPassword = CryptoUtil.getDefaultCryptoUtil()
                        .encryptAndBase64Encode(decryptedPassword);
                BPSProfile updatedBpsProfile = new BPSProfile(bpsProfile.getProfileName(), bpsProfile.getTenantId(),
                        newEncryptedPassword);
                updatedBpsProfileList.add(updatedBpsProfile);
            }
        }

        return updatedBpsProfileList;
    }
}
