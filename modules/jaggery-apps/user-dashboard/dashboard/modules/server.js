var SERVER = 'server';

var SYSTEM_REGISTRY = 'system.registry';

var ANONYMOUS_REGISTRY = 'anonymous.registry';

var SERVER_USER_MANAGER = 'server.usermanager';

var SERVER_OPTIONS = 'server.options';

var init = function (options) {
    var carbon = require('carbon'),
        server = new carbon.server.Server(options.server.https + '/admin'),
        system = new carbon.registry.Registry(server, {
            system: true,
            tenantId: carbon.server.superTenant.tenantId
        }),
        anonymous = new carbon.registry.Registry(server, {
            tenantId: carbon.server.superTenant.tenantId
        }),
        um = new carbon.user.UserManager(server, carbon.server.tenantDomain());
    application.put(SERVER, server);
    application.put(SYSTEM_REGISTRY, system);
    application.put(ANONYMOUS_REGISTRY, anonymous);
    application.put(SERVER_USER_MANAGER, um);
    application.put(SERVER_OPTIONS, options);
};

var options = function () {
    return application.get(SERVER_OPTIONS);
};

var systemRegistry = function () {
    return application.get(SYSTEM_REGISTRY);
};

var anonRegistry = function () {
    return application.get(ANONYMOUS_REGISTRY);
};

var server = function () {
    return application.get(SERVER);
};

var userManager = function () {
    return application.get(SERVER_USER_MANAGER);
};

