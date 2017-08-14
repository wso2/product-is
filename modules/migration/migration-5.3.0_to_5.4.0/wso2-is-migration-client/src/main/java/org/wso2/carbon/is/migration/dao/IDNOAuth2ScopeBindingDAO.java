package org.wso2.carbon.is.migration.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.ISMigrationException;
import org.wso2.carbon.is.migration.SQLConstants;
import org.wso2.carbon.is.migration.bean.OAuth2Scope;
import org.wso2.carbon.is.migration.bean.OAuth2ScopeBinding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by harshat on 8/13/17.
 */
public class IDNOAuth2ScopeBindingDAO {

    private static Log log = LogFactory.getLog(IDNOAuth2ScopeBindingDAO.class);

    private static IDNOAuth2ScopeBindingDAO idnoAuth2ScopeBindingDAO = new IDNOAuth2ScopeBindingDAO();

    private IDNOAuth2ScopeBindingDAO() {
    }

    public static IDNOAuth2ScopeBindingDAO getInstance() {
        return idnoAuth2ScopeBindingDAO;
    }

    public void addOAuth2ScopeBinding(List<OAuth2ScopeBinding> oAuth2ScopeBindingList) throws
                                                                        ISMigrationException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        String query = SQLConstants.ADD_SCOPE_BINDINGS;
        try {
            prepStmt = connection.prepareStatement(query);

            for (OAuth2ScopeBinding oAuth2ScopeBinding : oAuth2ScopeBindingList) {
                prepStmt.setString(1, oAuth2ScopeBinding.getScopeId());
                prepStmt.setString(2, oAuth2ScopeBinding.getScopeBinding());
                prepStmt.addBatch();
            }

            prepStmt.executeBatch();
            connection.commit();

        } catch (SQLException e) {
            throw new ISMigrationException("Error while adding OAuth2ScopeBinding " , e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

}
