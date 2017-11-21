var USER = 'server.user';

var USER_REGISTRY = 'server.user.registry';

var USER_OPTIONS = 'server.user.options';

var USER_SPACE = 'server.user.space';

var USER_ROLE_PREFIX = 'private_';

var init = function (options) {
    var role, roles, user,
        server = require('/modules/server.js'),
        um = server.userManager();
    roles = options.roles;
    for (role in roles) {
        if (roles.hasOwnProperty(role)) {
            if (um.roleExists(role)) {
                um.authorizeRole(role, roles[role]);
            } else {
                um.addRole(role, [], roles[role]);
            }
        }
    }
    user = um.getUser(options.user.username);
    if (!user.hasRoles(options.userRoles)) {
        user.addRoles(options.userRoles);
    }
    application.put(USER_OPTIONS, options);
};

var options = function () {
    return application.get(USER_OPTIONS);
};

var login = function (username, password) {
    var user, perm, perms, actions, i, length,
        authorized = false,
        opts = options(),
        carbon = require('carbon'),
        server = require('/modules/server.js'),
        serv = server.server(),
        um = server.userManager();
    if (!serv.authenticate(username, password)) {
        return false;
    }
    user = um.getUser(username);
    perms = opts.permissions.login;
    L1:
        for (perm in perms) {
            if (perms.hasOwnProperty(perm)) {
                actions = perms[perm];
                length = actions.length;
                for (i = 0; i < length; i++) {
                    if (user.isAuthorized(perm, actions[i])) {
                        authorized = true;
                        break L1;
                    }
                }
            }
        }
    if (!authorized) {
        return false;
    }
    session.put(USER, new carbon.user.User(um, username));
    session.put(USER_REGISTRY, new carbon.registry.Registry(serv, {
        username: username,
        tenantId: carbon.server.tenantId()
    }));
    session.put(USER_SPACE, new carbon.user.Space(username, opts.userSpace.space, opts.userSpace.options));
    if (opts.login) {
        opts.login(user, password, session);
    }
    return true;
};

var isAuthorized = function (permission, action) {
alert('test');
    var user = current(),
        server = require('/modules/server.js'),
        um = server.userManager();
    user = user ? user.username : options().user.username;
    return um.getUser(user).isAuthorized(permission, action);
};

var userSpace = function () {
    try {
        return session.get(USER_SPACE);
    } catch (e) {
        return null;
    }
};

var userRegistry = function () {
    try {
        return session.get(USER_REGISTRY);
    } catch (e) {
        return null;
    }
};

var logout = function () {
    var opts = options(),
        user = current();
    if (opts.logout) {
        opts.logout(user, session);
    }
    session.remove(USER);
    session.remove(USER_SPACE);
    session.remove(USER_REGISTRY);
};

var userExists = function (username) {
    var server = require('/modules/server.js');
    return server.userManager().userExists(username);
};

var privateRole = function (username) {
    return USER_ROLE_PREFIX + username;
};

var register = function (username, password) {
    var user, role, id, perms, r, p,
        server = require('/modules/server.js'),
        um = server.userManager(),
        opts = options();
    um.addUser(username, password, opts.userRoles);
    user = um.getUser(username);
    role = privateRole(username);
    id = opts.userSpace.options.path + '/' + opts.userSpace.space;
    perms = {};
    perms[id] = [
        'http://www.wso2.org/projects/registry/actions/get',
        'http://www.wso2.org/projects/registry/actions/add',
        'http://www.wso2.org/projects/registry/actions/delete',
        'authorize'
    ];
    p = opts.permissions.login;
    for (r in p) {
        if (p.hasOwnProperty(r)) {
            perms[r] = p[r];
        }
    }
    um.addRole(role, [], perms);
    user.addRoles([role]);
    if (opts.register) {
        opts.register(user, password, session);
    }
    login(username, password);
};

/**
 * Returns the currently logged in user
 */
var current = function () {
    try {
        return session.get(USER);
    } catch (e) {
        return null;
    }
};

var loginWithSAML = function (username) {
    var user, perm, perms, actions, i, length,
        authorized = false,
        opts = options(),
        carbon = require('carbon'),
        server = require('/modules/server.js'),
        serv = server.server(),
        um = server.userManager();

    user = um.getUser(username);
    perms = opts.permissions.login;
    L1:
        for (perm in perms) {
            if (perms.hasOwnProperty(perm)) {
                actions = perms[perm];
                length = actions.length;
                for (i = 0; i < length; i++) {
                    if (user.isAuthorized(perm, actions[i])) {
                        authorized = true;
                        break L1;
                    }
                }
            }
        }
    if (!authorized) {
        return false;
    }
    session.put(USER, new carbon.user.User(um, username));
    session.put(USER_REGISTRY, new carbon.registry.Registry(serv, {
        username: username,
        tenantId: carbon.server.tenantId()
    }));
    session.put(USER_SPACE, new carbon.user.Space(username, opts.userSpace.space, opts.userSpace.options));
    if (opts.login) {
        opts.login(user, "", session);
    }

    var permission = {};
    permission[opts.userSpace.options.path + '/' + username ] = [
        carbon.registry.actions.GET,
        carbon.registry.actions.PUT,
        carbon.registry.actions.DELETE
    ];
    um.authorizeRole(privateRole(username), permission);

    return true;
};
