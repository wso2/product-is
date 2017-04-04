package org.wso2.is.portal.user.client.api;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.claim.service.ProfileMgtService;
import org.wso2.carbon.identity.mgt.Group;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.kernel.utils.StringUtils;
import org.wso2.is.portal.user.client.api.bean.UserUIEntry;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;

import java.util.ArrayList;
import java.util.List;

/**
 * User management client service implementation
 */
@Component(
        name = "org.wso2.is.portal.user.client.api.UserMgtClientServiceImpl",
        service = UserMgtClientService.class,
        immediate = true)
public class UserMgtClientServiceImpl implements UserMgtClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserMgtClientServiceImpl.class);
    private static final String USER_STATE_DISABLED = "DISABLED";
    private static final String ACTIVE_USER_INDICATOR = "Active";
    private static final String DISABLED_USER_INDICATOR = "Disabled";
    private static final String USERNAME_CLAIM = "http://wso2.org/claims/username";
    private static final int MAX_RECORD_LENGTH = 500;

    private RealmService realmService;

    private ProfileMgtService profileMgtService;

    @Activate
    protected void start(final BundleContext bundleContext) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("IdentityStoreClientService activated successfully.");
        }
    }

    @Reference(
            name = "realmService",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {

        this.realmService = null;
    }

    @Override
    public List<User> listUsers(int offset, int length, String domainName) throws UserPortalUIException {
        return null;
    }

    @Override
    public List<User> listUsers(Claim claim, int offset, int length, String domainName) throws UserPortalUIException {
        return null;
    }

    @Override
    public List<Claim> getClaimsOfUser(String uniqueUserId) throws UserPortalUIException {
        return null;
    }

    @Override
    public List<UserUIEntry> getUsersForList(int offset, int length, String usernameClaim)
            throws UserPortalUIException {
        return null;
    }

    @Override
    public List<UserUIEntry> getFilteredList(int offset, int length, String claimURI, String claimValue,
            String domainName) throws UserPortalUIException {

        List<UserUIEntry> userList = new ArrayList<>();
        List<User> users;
        if (length < 0) {
            length = MAX_RECORD_LENGTH;
        }
        if (StringUtils.isNullOrEmpty(claimURI) ||
                StringUtils.isNullOrEmpty(claimValue)) {
            try {
                users = getRealmService().getIdentityStore().listUsers(offset, length, domainName);
            } catch (IdentityStoreException e) {
                String error = "Error while retrieving users";
                LOGGER.error(error, e);
                throw new UserPortalUIException(error);
            }
        } else {
            Claim claim = new Claim();
            claim.setClaimUri(claimURI);
            claim.setValue(claimValue);
            try {
                users = getRealmService().getIdentityStore().listUsers(claim, offset, length, domainName);
            } catch (IdentityStoreException e) {
                String error = "Error while retrieving users for " + claimURI + "= " + claimValue;
                LOGGER.error(error, e);
                throw new UserPortalUIException(error);
            }
        }

        List<MetaClaim> metaClaims = new ArrayList<>();
        MetaClaim metaClaim = new MetaClaim();
        metaClaim.setClaimUri(USERNAME_CLAIM);
        metaClaims.add(metaClaim);

        for (User user : users) {
            List<Group> groups;
            List<Claim> userId;
            String username = null;
            try {
                groups = user.getGroups();
                //TODO:implement getUsername() method in User class and use it to retrieve username
                userId = user.getClaims(metaClaims);
                if (!userId.isEmpty()) {
                    username = userId.get(0).getValue();
                }
            } catch (IdentityStoreException | GroupNotFoundException | UserNotFoundException e) {
                String error = "Error while retrieving user data for user :  " + user.getUniqueUserId();
                LOGGER.error(error, e);
                throw new UserPortalUIException(error);
            }

            UserUIEntry listEntry = new UserUIEntry();
            listEntry.setUserId(username);
            listEntry.setDomainName(user.getDomainName());
            listEntry.setUserUniqueId(user.getUniqueUserId());
            listEntry.setState(user.getState());
            listEntry.setGroups(groups);
            userList.add(listEntry);

        }
        return userList;
    }

    private RealmService getRealmService() {
        if (this.realmService == null) {
            throw new IllegalStateException("Realm Service is null.");
        }
        return this.realmService;
    }

}
