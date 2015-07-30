/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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
package org.wso2.sample.user.store.manager;


import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.core.UserStoreConfigConstants;

import java.util.ArrayList;

public class CustomUserStoreManagerConstants {


    //Properties for CustomUserStoreManager
    public static final ArrayList<Property> CUSTOM_USERSTORE_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> OPTIONAL_CUSTOM_USERSTORE_PROPERTIES = new ArrayList<Property>();
    static {
        setMandatoryProperty(UserStoreConfigConstants.connectionName,"uid=admin,ou=system",UserStoreConfigConstants.connectionNameDescription);
        setMandatoryProperty(UserStoreConfigConstants.connectionURL,"ldap://localhost:10389",UserStoreConfigConstants.connectionURLDescription);
        setMandatoryProperty(UserStoreConfigConstants.connectionPassword,"admin",UserStoreConfigConstants.connectionPasswordDescription);
        setMandatoryProperty(UserStoreConfigConstants.userSearchBase,"ou=system",UserStoreConfigConstants.userSearchBaseDescription);
        setMandatoryProperty(UserStoreConfigConstants.disabled,"false",UserStoreConfigConstants.disabledDescription);
        setMandatoryProperty(UserStoreConfigConstants.usernameListFilter, "(objectClass=person)", UserStoreConfigConstants.usernameListFilterDescription);
        setMandatoryProperty(UserStoreConfigConstants.userNameAttribute, "uid", UserStoreConfigConstants.userNameAttributeDescription);
        setMandatoryProperty("ReadOnly","true","Indicates whether the user store of this realm operates in the user read only mode or not");

        setProperty(UserStoreConfigConstants.maxUserNameListLength, "100", UserStoreConfigConstants.maxUserNameListLengthDescription);
        setProperty(UserStoreConfigConstants.maxRoleNameListLength, "100", UserStoreConfigConstants.maxRoleNameListLengthDescription);
        setProperty(UserStoreConfigConstants.userRolesCacheEnabled, "true", UserStoreConfigConstants.userRolesCacheEnabledDescription);

        Property readLDAPGroups = new Property(UserStoreConfigConstants.readGroups,"true",UserStoreConfigConstants.readLDAPGroupsDescription,null);
        //Mandatory only if readGroups is enabled
        Property groupSearchBase = new Property(UserStoreConfigConstants.groupSearchBase,"ou=system",UserStoreConfigConstants.groupSearchBaseDescription,null);
        Property groupNameListFilter = new Property(UserStoreConfigConstants.groupNameListFilter,"(objectClass=groupOfNames)",UserStoreConfigConstants.groupNameListFilterDescription,null);
        Property groupNameAttribute = new Property(UserStoreConfigConstants.groupNameAttribute,"cn",UserStoreConfigConstants.groupNameAttributeDescription,null);
        Property membershipAttribute = new Property(UserStoreConfigConstants.membershipAttribute,"member",UserStoreConfigConstants.membershipAttributeDescription,null);
        readLDAPGroups.setChildProperties(new Property[]{groupSearchBase,groupNameListFilter,groupNameAttribute,membershipAttribute});
        OPTIONAL_CUSTOM_USERSTORE_PROPERTIES.add(readLDAPGroups);

//      LDAP Specific Properties
        setProperty(UserStoreConfigConstants.passwordHashMethod,"PLAIN_TEXT",UserStoreConfigConstants.passwordHashMethodDescription);
        setProperty("ReplaceEscapeCharactersAtUserLogin","true","Whether replace escape character when user login");
        setProperty("ReadOnly","true","Indicates whether the user store of this realm operates in the user read only mode or not");

    }

    private static void setMandatoryProperty(String name,String value,String description){
        Property property = new Property(name,value,description,null);
        CUSTOM_USERSTORE_PROPERTIES.add(property);

    }

    private static void setProperty(String name,String value,String description){
        Property property = new Property(name,value,description,null);
        OPTIONAL_CUSTOM_USERSTORE_PROPERTIES.add(property);

    }


}
