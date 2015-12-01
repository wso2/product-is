/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.is.migration.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResourceUtil {

    public static String setMySQLDBName(Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT DATABASE() FROM DUAL;");
        ResultSet rs = ps.executeQuery();
        String name = null;
        if(rs.next()){
            name = rs.getString(1);
            ps = conn.prepareStatement("SET @databasename = ?;");
            ps.setString(1, name);
            ps.execute();
        }
        return name;
    }

}
