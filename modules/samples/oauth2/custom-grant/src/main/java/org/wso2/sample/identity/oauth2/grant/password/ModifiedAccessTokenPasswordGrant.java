/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.sample.identity.oauth2.grant.password;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.PasswordGrantHandler;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.UUID;

/**
 * Modified version of password grant type to modify the access token.
 */
public class ModifiedAccessTokenPasswordGrant extends PasswordGrantHandler {

    private static Log log = LogFactory.getLog(ModifiedAccessTokenPasswordGrant.class);

    @Override
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        // calling super
        OAuth2AccessTokenRespDTO tokenRespDTO =  super.issue(tokReqMsgCtx);

        // set modified access token
        tokenRespDTO.setAccessToken(generateAccessToken(tokReqMsgCtx.getAuthorizedUser().toString()));

        return tokenRespDTO;

    }


    /**
     * Demo sample for generating custom access token
     *
     * @param userName
     * @return
     */
    private String generateAccessToken(String userName){

        String token = UUID.randomUUID().toString();

        // retrieve user's email address and append it to access token
        userName = MultitenantUtils.getTenantAwareUsername(userName);
        String email = null;

        try {
            email = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager()
                    .getUserClaimValue(userName, "http://wso2.org/claims/emailaddress", null);
        } catch (UserStoreException e) {
            log.error(e);
        }

        if(email != null){
            token = token + ":" + email;
        }

        return token;
    }
}
