(function (server, user) {

    user.Space = function (user, space, options) {
        var reg = require('registry-space.js').user,
            o = new reg.Space(user, space, options);
        o.prototype = this;
        return o;
    };

}(server, user));