package org.wso2.is.portal.user.client.api.dao;

import org.wso2.carbon.identity.meta.claim.mgt.mapping.profile.ClaimConfigEntry;
import org.wso2.carbon.identity.meta.claim.mgt.mapping.profile.ProfileEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileMgtDAO {

    public Map<String, String> getClaimProfile(ClaimConfigEntry claimForProfile) {
        Map<String, String> claimProfileMap = new HashMap<>();
        claimProfileMap.put("displayName", claimForProfile.getDisplayName());
        claimProfileMap.put("claimURI", claimForProfile.getClaimURI());
        if(claimForProfile.getDefaultValue() != null){
            claimProfileMap.put("defaultValue", claimForProfile.getDefaultValue());
        }
        claimProfileMap.put("displayLabel", claimForProfile.getClaimURI().replace("http://wso2.org/claims/", ""));
        return claimProfileMap;
    }
}
