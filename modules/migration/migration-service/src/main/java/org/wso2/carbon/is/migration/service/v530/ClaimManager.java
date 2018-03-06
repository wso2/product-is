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

package org.wso2.carbon.is.migration.service.v530;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ClaimDialectDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.is.migration.service.v530.bean.Claim;
import org.wso2.carbon.is.migration.service.v530.bean.MappedAttribute;
import org.wso2.carbon.is.migration.service.v530.dao.ClaimDAO;

import java.util.List;

/**
 * Claim Manager
 */
public class ClaimManager {

    private static Log log = LogFactory.getLog(ClaimDialectDAO.class);
    private ClaimDAO claimDAO = ClaimDAO.getInstance();
    private static ClaimManager claimManager = new ClaimManager();

    private ClaimManager() {
    }

    public static ClaimManager getInstance() {
        return claimManager;
    }

    /**
     * Adding claim dialects
     *
     * @param claimList
     * @throws ISMigrationException
     */
    public void addClaimDialects(List<Claim> claimList, StringBuilder report) throws MigrationClientException {

        log.info("started adding claim dialects");

        for (Claim claim : claimList) {
            int id = claimDAO.getClaimDialect(claim.getDialectURI(), claim.getTenantId());
            if (id == 0) {
                claimDAO.addClaimDialect(claim.getDialectURI(), claim.getTenantId());
                report.append("\n\n Added claim Dialect : " + claim.getDialectURI() + " in tenant domain :" +
                              IdentityTenantUtil.getTenantDomain(claim.getTenantId()));

                if (log.isDebugEnabled()) {
                    log.debug("\n\n Added claim Dialect : " + claim.getDialectURI() + " in tenant domain :" +
                              IdentityTenantUtil.getTenantDomain(claim.getTenantId()));
                }
            }
        }
        report.append("\n\n");
        log.info("end adding claim dialects");
    }

    /**
     * Adding local claims
     *
     * @param claimCList
     * @throws ISMigrationException
     */
    public void addLocalClaims(List<Claim> claimCList, StringBuilder report) throws MigrationClientException {

        log.info("started adding local claims");

        for (Claim claim : claimCList) {
            if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(claim.getDialectURI())) {
                claimDAO.addLocalClaim(claim);
                report.append("\n Added Local Claim: " + claim.getDialectURI() + " in tenant domain :" +
                              IdentityTenantUtil.getTenantDomain(claim.getTenantId()) + ", Mapped Attributes :");
                for (MappedAttribute mappedAttribute : claim.getAttributes()) {
                    if (mappedAttribute.getDomain() == null) {
                        report.append(" " + IdentityUtil.getPrimaryDomainName() + "/" +
                                      mappedAttribute.getAttribute() + " ,");
                    } else {
                        report.append(" " + mappedAttribute.getDomain() + "/" +
                                      mappedAttribute.getAttribute() + " ,");
                    }
                }
                report.append("\n");

                if (log.isDebugEnabled()) {
                    log.debug("\n Added Local Claim " + claim.getDialectURI() + " in tenant domain :" +
                              IdentityTenantUtil.getTenantDomain(claim.getTenantId()) + "Mapped Attributes :");
                    for (MappedAttribute mappedAttribute : claim.getAttributes()) {
                        if (mappedAttribute.getDomain() == null) {
                            log.debug(" " + IdentityUtil.getPrimaryDomainName() + "/" + mappedAttribute.getAttribute()
                                      + "" + " ,");
                        } else {
                            log.debug(" " + mappedAttribute.getDomain() + "/" + mappedAttribute.getAttribute() + " ,");
                        }
                    }
                }
            }
        }
        report.append("\n\n");
    }

    /**
     * Adding external claims
     *
     * @param claims
     * @throws ISMigrationException
     */
    public void addExternalClaim(List<Claim> claims, StringBuilder report) throws MigrationClientException {

        log.info("started adding external claims");

        for (Claim claim : claims) {
            if (!ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(claim.getDialectURI())) {
                claimDAO.addExternalClaim(claim, report);
            }
        }
        log.info("end adding external claims");
    }
}
