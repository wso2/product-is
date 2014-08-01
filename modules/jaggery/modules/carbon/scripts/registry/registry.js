var registry = {};

(function (registry) {
    var ActionConstants = Packages.org.wso2.carbon.registry.core.ActionConstants;

    registry.Registry = function (server, auth) {
        var osgi = require('registry-osgi.js').registry,
            o = new osgi.Registry(server, auth);
        o.prototype = this;
        return o;
    };

    registry.actions = {};

    registry.actions.GET = ActionConstants.GET;

    registry.actions.PUT = ActionConstants.PUT;

    registry.actions.DELETE = ActionConstants.DELETE;

}(registry));