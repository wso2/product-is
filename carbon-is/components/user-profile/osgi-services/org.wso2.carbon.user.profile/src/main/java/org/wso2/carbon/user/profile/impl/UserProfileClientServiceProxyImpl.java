/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.user.profile.impl;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.context.PrivilegedCarbonContext;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.security.caas.api.CarbonPrincipal;
import org.wso2.carbon.security.caas.api.ProxyCallbackHandler;
import org.wso2.carbon.security.caas.user.core.bean.User;
import org.wso2.carbon.security.caas.user.core.claim.Claim;
import org.wso2.carbon.security.caas.user.core.claim.MetaClaim;
import org.wso2.carbon.user.profile.service.UserProfileClientService;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of User Profile Service
 */
@Component(name = "org.wso2.carbon.user.profile.impl.UserProfileClientServiceProxyImpl",
           service = { UserProfileClientService.class },
           immediate = true)
public class UserProfileClientServiceProxyImpl implements UserProfileClientService {
    private static final Logger log = LoggerFactory.getLogger(UserProfileClientServiceProxyImpl.class);
    private static final HashMap<Integer, ArrayList<MetaClaim>> profileTemplates = null;

    @Override
    public User authenticate(String username, String password) {
        CarbonPrincipal principal;
        PrivilegedCarbonContext.destroyCurrentContext();
        CarbonMessage carbonMessage = new DefaultCarbonMessage();
        carbonMessage.setHeader("Authorization",
                "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));

        ProxyCallbackHandler callbackHandler = new ProxyCallbackHandler(carbonMessage);
        LoginContext loginContext = null;
        try {
            loginContext = new LoginContext("CarbonSecurityConfig", callbackHandler);
            loginContext.login();
            principal = (CarbonPrincipal) PrivilegedCarbonContext.getCurrentContext()
                    .getUserPrincipal();
//            principal.getUser().getClaims();
        } catch (LoginException e) {
            e.printStackTrace();
        }

        return principal.getUser();
    }

    @Override
    public Collection<Claim> getProfile(String profileId, String userid) {
        ArrayList<Claim> claims = new ArrayList<Claim>();
        claims.add(new Claim("http://wso2.org/claims/givenname", "http://wso2.org/claims/givenname", "Lanka"));
        claims.add(new Claim("http://wso2.org/claims/lastname", "http://wso2.org/claims/lastname", "Jayawardhana"));
        claims.add(new Claim("http://wso2.org/claims/country", "http://wso2.org/claims/country", "Sri Lanka"));
        claims.add(new Claim("http://wso2.org/claims/mobile", "http://wso2.org/claims/mobile", "0779716248"));
        claims.add(new Claim("http://wso2.org/claims/profilepicture", "http://wso2.org/claims/profilepicture",
                "../../resources/profilepic.jpg"));
        claims.add(new Claim("http://wso2.org/claims/address", "http://wso2.org/claims/address", "71/D, Kirindiwela"));
        claims.add(new Claim("http://wso2.org/claims/dateofbirth", "http://wso2.org/claims/dateofbirth", "10/10/1988"));

        return claims;
    }

    @Override
    public Collection<Claim> getProfile(String userid) {
        ArrayList<Claim> claims = new ArrayList<Claim>();
        claims.add(new Claim("http://wso2.org/claims/givenname", "http://wso2.org/claims/givenname", "Lanka"));
        claims.add(new Claim("http://wso2.org/claims/lastname", "http://wso2.org/claims/lastname", "Jayawardhana"));
        claims.add(new Claim("http://wso2.org/claims/country", "http://wso2.org/claims/country", "Sri Lanka"));
        claims.add(new Claim("http://wso2.org/claims/mobile", "http://wso2.org/claims/mobile", "0779716248"));
        claims.add(new Claim("http://wso2.org/claims/profilepicture", "http://wso2.org/claims/profilepicture",
                "../../resources/profilepic.jpg"));
        claims.add(new Claim("http://wso2.org/claims/address", "http://wso2.org/claims/address", "71/D, Kirindiwela"));
        claims.add(new Claim("http://wso2.org/claims/dateofbirth", "http://wso2.org/claims/dateofbirth", "10/10/1988"));

        return claims;
    }

    @Override
    public Collection<Claim> getMetaClaims(String dialect, Collection<String> claims) {
        ArrayList<Claim> claims = new ArrayList<Claim>();
        claims.add(new Claim("http://wso2.org/claims/givenname", "http://wso2.org/claims/givenname", "Lanka"));
        claims.add(new Claim("http://wso2.org/claims/lastname", "http://wso2.org/claims/lastname", "Jayawardhana"));
        claims.add(new Claim("http://wso2.org/claims/country", "http://wso2.org/claims/country", "Sri Lanka"));
        claims.add(new Claim("http://wso2.org/claims/mobile", "http://wso2.org/claims/mobile", "0779716248"));
        claims.add(new Claim("http://wso2.org/claims/profilepicture", "http://wso2.org/claims/profilepicture",
                "../../resources/profilepic.jpg"));
        claims.add(new Claim("http://wso2.org/claims/address", "http://wso2.org/claims/address", "71/D, Kirindiwela"));
        claims.add(new Claim("http://wso2.org/claims/dateofbirth", "http://wso2.org/claims/dateofbirth", "10/10/1988"));

        return claims;
    }

    @Override
    public Collection<Claim> getClaims(User user) {
        ArrayList<Claim> claims = new ArrayList<Claim>();
        claims.add(new Claim("http://wso2.org/claims/givenname", "http://wso2.org/claims/givenname", "Lanka"));
        claims.add(new Claim("http://wso2.org/claims/lastname", "http://wso2.org/claims/lastname", "Jayawardhana"));
        claims.add(new Claim("http://wso2.org/claims/country", "http://wso2.org/claims/country", "Sri Lanka"));
        claims.add(new Claim("http://wso2.org/claims/mobile", "http://wso2.org/claims/mobile", "0779716248"));
        claims.add(new Claim("http://wso2.org/claims/profilepicture", "http://wso2.org/claims/profilepicture",
                "../../resources/profilepic.jpg"));
        claims.add(new Claim("http://wso2.org/claims/address", "http://wso2.org/claims/address", "71/D, Kirindiwela"));
        claims.add(new Claim("http://wso2.org/claims/dateofbirth", "http://wso2.org/claims/dateofbirth", "10/10/1988"));

        return claims;
    }

    @Override
    public void createProfile(String profileId, Collection<Claim> claims) {
        log.info("Profile creation with below details started.");
        log.info("Profile Id: " + profileId);

        for (Claim claim : claims) {
            log.info("DialectURI:" + claim.getClaimURI() + "|| ClaimURI:" + claim.getDialectURI() + "|| Value:" + claim
                    .getValue());
        }
    }

    @Override
    public void createProfile(String templateId, String profileId, Collection<Claim> claims) {
        log.info("Profile creation with below details started.");
        log.info("Template Id: " + templateId);
        log.info("Profile Id: " + profileId);

        for (Claim claim : claims) {
            log.info("DialectURI:" + claim.getClaimURI() + "|| ClaimURI:" + claim.getDialectURI() + "|| Value:" + claim
                    .getValue());
        }
    }

    @Override
    public void updateProfile(String profileId, Collection<Claim> claims) {
        log.info("Profile update with below details started.");
        log.info("Profile Id: " + profileId);

        for (Claim claim : claims) {
            log.info("DialectURI:" + claim.getClaimURI() + "|| ClaimURI:" + claim.getDialectURI() + "|| Value:" + claim
                    .getValue());
        }
    }

    @Override
    public void updateProfileName(String profileId, String newName) {
        log.info("Profile name update with below details started.");
        log.info("Profile Id: " + profileId);
        log.info("New profile name: " + newName);
    }

    @Override
    public void deleteProfile(String profileId) {
        log.info("Profile with ID " + profileId + "deleted.");
    }

    @Override
    public Collection<MetaClaim> getProfileTemplate(String profileTempleteId) {
        return null;
    }

    @Override
    public Map<Integer, String> getProfileTemplates() {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(1, "default");
        map.put(2, "work");
        map.put(3, "confidentials");
        map.put(3, "advance");
        map.put(3, "basic");

        return map;
    }

    private HashMap<Integer, ArrayList<MetaClaim>> initiateProfileTemplates() {
        HashMap<Integer, ArrayList<MetaClaim>> metaClaims = new HashMap<>();
        HashMap<String, String> properties = new HashMap<>();
        ArrayList<MetaClaim> claims = new ArrayList<>();

        properties.put("mandatory", "true");


        claims.add(1, new MetaClaim("http://wso2.org/claims/givenname", "http://wso2.org/claims/givenname", properties));
        claims.add(1, new MetaClaim("http://wso2.org/claims/lastname", "http://wso2.org/claims/lastname", null));
        claims.add(1, new MetaClaim("http://wso2.org/claims/country", "http://wso2.org/claims/country", null));
        claims.add(1, new MetaClaim("http://wso2.org/claims/mobile", "http://wso2.org/claims/mobile", properties));
        claims.add(1, new MetaClaim("http://wso2.org/claims/profilepicture", "http://wso2.org/claims/profilepicture", properties));
        claims.add(1, new MetaClaim("http://wso2.org/claims/address", "http://wso2.org/claims/address", null));
        claims.add(1, new MetaClaim("http://wso2.org/claims/dateofbirth", "http://wso2.org/claims/dateofbirth", null));
        metaClaims.put(1, claims);

        return metaClaims;
    }
}
