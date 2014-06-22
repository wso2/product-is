(function (server, user) {

    var processPerms = function (perms, fn) {
        var perm, actions, i, length;
        for (perm in perms) {
            if (perms.hasOwnProperty(perm)) {
                actions = perms[perm];
                length = actions.length;
                for (i = 0; i < length; i++) {
                    fn(perm, actions[i]);
                }
            }
        }
    };

    var UserManager = function (serv, tenant) {
        tenant = tenant || server.superTenant.domain;
        this.server = serv;
        this.tenantId = server.tenantId({
            domain: tenant
        });
        var realmService = server.osgiService('org.wso2.carbon.user.core.service.RealmService'),
            realm = realmService.getTenantUserRealm(this.tenantId);
        this.manager = realm.getUserStoreManager();
        this.authorizer = realm.getAuthorizationManager();
    };
    user.UserManager = UserManager;

    UserManager.prototype.getUser = function (username) {
        if (!this.manager.isExistingUser(username)) {
            return null;
        }
        return new user.User(this, username);
    };

    UserManager.prototype.addUser = function (username, password, roles, claims, profile) {
        this.manager.addUser(username, password, roles || [], claims || null, profile);
    };

    UserManager.prototype.removeUser = function (username) {
        this.manager.deleteUser(username);
    };

    UserManager.prototype.userExists = function (username) {
        return this.manager.isExistingUser(username);
    };

    UserManager.prototype.roleExists = function (role) {
        return this.manager.isExistingRole(role);
    };

    UserManager.prototype.getClaims = function (username, profile) {
        return this.manager.getUserClaimValues(username, profile);
    };

    UserManager.prototype.setClaims = function (username, claims, profile) {
        return this.manager.setUserClaimValues(username, claims, profile);
    };

    UserManager.prototype.isAuthorized = function (role, permission, action) {
        return this.authorizer.isRoleAuthorized(role, permission, action);
    };

    UserManager.prototype.addRole = function (role, users, permissions) {
        var perms = [],
            Permission = Packages.org.wso2.carbon.user.api.Permission;
        processPerms(permissions, function (id, action) {
            perms.push(new Permission(id, action));
        });
        this.manager['addRole(java.lang.String,java.lang.String[],org.wso2.carbon.user.api.Permission[])']
            (role, users, perms);
    };

    UserManager.prototype.removeRole = function (role) {
        this.manager.deleteRole(role);
    };

    UserManager.prototype.allRoles = function () {
        return this.manager.getRoleNames();
    };

    /**
     * um.authorizeRole('store-admin', '/permissions/mypermission', 'ui-execute');
     *
     * um.authorizeRole('store-admin', {
     *      '/permissions/myperm1' : ['read', 'write'],
     *      '/permissions/myperm2' : ['read', 'write']
     * });
     *
     * @param role
     * @param permission
     * @param action
     */
    UserManager.prototype.authorizeRole = function (role, permission, action) {
        var that = this;
        if (permission instanceof String || typeof permission === 'string') {
            if (!that.isAuthorized(role, permission, action)) {
                that.authorizer.authorizeRole(role, permission, action);
            }
        } else {
            processPerms(permission, function (id, action) {
                if (!that.isAuthorized(role, id, action)) {
                    that.authorizer.authorizeRole(role, id, action);
                }
            });
        }
    };

    /**
     * um.denyRole('store-admin', '/permissions/mypermission', 'ui-execute');
     *
     * um.denyRole('store-admin', {
     *      '/permissions/myperm1' : ['read', 'write'],
     *      '/permissions/myperm2' : ['read', 'write']
     * });
     *
     * @param role
     * @param permission
     * @param action
     */
    UserManager.prototype.denyRole = function (role, permission, action) {
        var deny = this.authorizer.denyRole;
        if (permission instanceof String || typeof permission === 'string') {
            deny(role, permission, action);
        } else {
            processPerms(permission, function (id, action) {
                deny(role, id, action);
            });
        }
    };

}(server, user));