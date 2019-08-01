var theme = (function () {
    var loading, loaded,
        loaderClass = 'loading';

    loading = function (el) {
        var loader;
        el.children().hide();
        loader = $('.' + loaderClass, el);
        if (loader.length === 0) {
            loader = el.prepend('<div class="' + loaderClass + '">loading.......</div>');
        }
        loader.show();
    };

    loaded = function (el, data) {
        var children;
        $('.' + loaderClass, el).hide();
        children = el.children(':not(.' + loaderClass + ')');
        if (!data) {
            children.show();
            return;
        }
        children.remove();
        el.append(data);
    };

    return {
        loading: loading,
        loaded: loaded
    };
})();