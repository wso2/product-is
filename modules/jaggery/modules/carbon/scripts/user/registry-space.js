(function (server, registry, user) {

    var Space = function (user, space, options) {
        var serv = new server.Server(options.serverUrl);
        this.registry = new registry.Registry(serv, {
            username: options.username || user,
            domain: server.tenantDomain({
                username: options.username
            }) || server.tenantDomain()
        });
        this.prefix = options.path + '/' + user + '/' + space;
        if (!this.registry.exists(this.prefix)) {
            this.registry.put(this.prefix, {
                collection: true
            });
        }
    };
    user.Space = Space;

    Space.prototype.put = function (key, value) {
        value = (!(value instanceof String) && typeof value !== "string") ? stringify(value) : value;
        this.registry.put(this.prefix + '/' + key, {
            content: value
        });
    };

    Space.prototype.get = function (key) {
        var o = this.registry.content(this.prefix + '/' + key);
        return o ? o.toString() : null;
    };

    Space.prototype.remove = function (key) {
        this.registry.remove(this.prefix + '/' + key);
    };

    Space.prototype.find = function (filter) {

    };


}(server, registry, user));