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

package org.wso2.is.portal.user.client.api.util;

import org.wso2.carbon.identity.meta.claim.mgt.mapping.profile.ClaimConfigEntry;

import java.util.HashMap;
import java.util.Map;

public class ProfileMgtUtil {

    public Map<String, String> getClaimProfile(ClaimConfigEntry claimForProfile) {
        Map<String, String> claimProfileMap = new HashMap<>();
        claimProfileMap.put("displayName", claimForProfile.getDisplayName());
        claimProfileMap.put("claimURI", claimForProfile.getClaimURI());
        if (claimForProfile.getDefaultValue() != null) {
            claimProfileMap.put("defaultValue", claimForProfile.getDefaultValue());
        }
        claimProfileMap.put("displayLabel", claimForProfile.getClaimURI().replace(ClientServiceConstants.DEFAULT_CLAIM_DIALECT, ""));
        claimProfileMap.put("required", Boolean.toString(claimForProfile.getRequired()));
        claimProfileMap.put("regex", claimForProfile.getRegex());
        claimProfileMap.put("readonly", Boolean.toString(claimForProfile.getReadonly()));
        claimProfileMap.put("dataType", claimForProfile.getDataType());
        return claimProfileMap;
    }
}
