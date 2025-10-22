(function (caramel) {

    /**
     * Resolves absolute paths by adding app context prefix.
     * @param path
     * @return {*}
     */
    caramel.url = function (path) {
        return this.context + (path.charAt(0) !== '/' ? '/' : '') + path;
    };

    caramel.get = function(path) {
        var args = Array.prototype.slice.call(arguments);
        args[0] = caramel.url(args[0]);
        return $.get.apply(this, args)
    };

    caramel.post = function(path) {
        var args = Array.prototype.slice.call(arguments);
        args[0] = caramel.url(args[0]);
        return $.post.apply(this, args)
    };

    caramel.ajax = function(options) {
        options.url = caramel.url(options.url);
        return $.ajax.call(this, options);
    };

})(caramel);