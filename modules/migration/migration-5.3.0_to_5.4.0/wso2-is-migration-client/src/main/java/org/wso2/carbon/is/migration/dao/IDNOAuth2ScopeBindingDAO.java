package org.wso2.carbon.is.migration.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.ISMigrationException;
import org.wso2.carbon.is.migration.SQLConstants;
import org.wso2.carbon.is.migration.bean.OAuth2ScopeBinding;
import org.wso2.carbon.is.migration.util.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;


public class IDNOAuth2ScopeBindingDAO {

    private static Log log = LogFactory.getLog(IDNOAuth2ScopeBindingDAO.class);

    private static IDNOAuth2ScopeBindingDAO idnoAuth2ScopeBindingDAO = new IDNOAuth2ScopeBindingDAO();

    private IDNOAuth2ScopeBindingDAO() {
    }

    public static IDNOAuth2ScopeBindingDAO getInstance() {
        return idnoAuth2ScopeBindingDAO;
    }

    public void addOAuth2ScopeBinding(List<OAuth2ScopeBinding> oAuth2ScopeBindingList, boolean continueOnError)
            throws ISMigrationException {

        log.info(Constants.MIGRATION_LOG + " call addOAuth2ScopeBinding.");
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
            log.info(Constants.MIGRATION_LOG + "");
        } catch (Exception e) {
            log.error(e);
            if (!continueOnError) {
                throw new ISMigrationException("Error while adding OAuth2ScopeBinding, ", e);
            }
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }
}
