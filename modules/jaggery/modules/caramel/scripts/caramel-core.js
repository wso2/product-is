var meta, theme, engine, render, configs, url, themeUrl, build, translate;

(function () {
    var load, Theme, Engine, context, parseRequest, languagesDir, themesDir,
        languages = {},
        log = new Log(),
        themes = {},
        engines = {};

    /**
     * Loads the specified theme.
     * @param name
     */
    load = function (name) {
        var option,
            options = require((configs().themes || '/themes') + '/' + name + '/theme.js');
        name = (themes[name] = new Theme(name, options));
        for (option in options) {
            if (options.hasOwnProperty(option)) {
                name[option] = options[option];
            }
        }
        if (name.engine.init) {
            name.engine.init(name);
        }
        if (log.isDebugEnabled()) {
            log.debug('Registered new theme : ' + name);
        }
        if (log.isDebugEnabled()) {
            log.debug('Loaded theme : ' + name);
        }
        return name;
    };

    /**
     * Constructor of the Engine class.
     * @constructor
     */
    Engine = function (name, options) {
        var option;
        this.name = name;
        for (option in options) {
            if (options.hasOwnProperty(option)) {
                this[option] = options[option];
            }
        }
        if (log.isDebugEnabled()) {
            log.debug('Created new engine : ' + stringify(options));
        }
    };

    Engine.prototype = {
        init: function (theme) {

        },
        render: function (data) {
            print(data);
        }
    };

    /**
     * The method to create or register a new engine or
     * extend an existing engine. i.e. If an existing engine can be
     * found with the specified name, new engine will be created by
     * extending it. Otherwise, totally new engine will be created.
     * Further newly created engine will be returned.
     * @param engine Existing engine name or engine object to be extended
     * @param options
     * @return {Engine}
     */
    engine = function (engine, options) {
        var e;
        if (!options) {
            return engines[engine];
        }

        if (engine instanceof Engine) {
            e = new Engine('extended > ' + engine.name, options);
            e.__proto__ = engine;
            if (log.isDebugEnabled()) {
                log.debug('Extended theme engine : ' + engine.name);
            }
        } else {
            e = new Engine(engine, options);
            if (engines[engine]) {
                e.__proto__ = engines[engine];
            } else {
                engines[engine] = e;
            }
            if (log.isDebugEnabled()) {
                log.debug('Registered theme engine : ' + e.name);
            }
        }
        return e;
    };

    /**
     * Constructor for the Theme class.
     * @param name
     * @param options
     * @constructor
     */
    Theme = function (name, options) {
        var option,
            config = configs();
        this.name = name;
        this.themesDir = config.themesDir || themesDir;
        var prototype = typeof options['prototype'] === 'string' ? theme(options['prototype']) : options['prototype'];
        if (prototype) {
            this.__proto__ = prototype;
            delete options['prototype'];
        }
        for (option in options) {
            if (options.hasOwnProperty(option)) {
                this[option] = options[option];
            }
        }
        if (log.isDebugEnabled()) {
            log.debug('Created new theme : ' + name);
        }
    };

    Theme.prototype = {

        base: function () {
            return this.themesDir + '/' + this.name;
        },

        resolve: function (path) {
            var fn, p;
            if (log.isDebugEnabled()) {
                log.debug('Resolving path : ' + path);
            }
            path = (path.charAt(0) !== '/' ? '/' : '') + path;
            p = this.base() + path;
            if (new File(p).isExists() || !(this.__proto__ instanceof Theme)) {
                if (log.isDebugEnabled()) {
                    log.debug('Resolved path : ' + p);
                }
                return p;
            }
            fn = this.__proto__.base;
            p = fn ? fn.call(this.__proto__) + path : p;
            if (log.isDebugEnabled()) {
                log.debug('Inherited path : ' + p);
            }
            return p;
        },

        url: function (path) {
            context = context || caramel.configs().context;
            return context + this.resolve(path);
        }
    };

    /**
     * The method to create/retrieve a theme
     * @param name
     * @return {Theme}
     */
    theme = function (name) {
        var theme;
        name = name || configs().themer();
        theme = themes[name];
        if (theme) {
            return theme;
        }
        load(name);
        return themes[name];
    };

    languagesDir = '/i18n';

    themesDir = '/themes';

    /**
     * Get and set caramel configurations.
     * @param configs
     * @return {*}
     */
    configs = function (configs) {
        if (configs) {
            application.put('caramel', configs);
            if (log.isDebugEnabled()) {
                log.debug('Updated configs : ' + stringify(configs));
            }
        } else {
            configs = application.get('caramel');
        }
        return configs;
    };

    /**
     * Generic render method called by pages. This will delegate rendering
     * to the engine.render(data, meta) of the active theme.
     * @param data
     */
    render = function (data) {
        var meta = {
            data: data,
            request: request,
            response: response,
            session: session,
            application: application
        };
        caramel.meta(meta);
        if (log.isDebugEnabled()) {
            data = build(data);
            log.debug('Rendering request : ' + stringify(parseRequest(meta.request)));
            log.debug('Rendering data : ' + stringify(data));
        }
        caramel.theme().engine.render(data, meta);
    };

    build = function (obj, depth) {
        var name, type, i, length;
        depth = (depth !== undefined) ? (depth - 1) : 0;
        if (depth <= 0) {
            return obj;
        }
        type = typeof obj;
        if (type === 'function') {
            return build(obj(), depth);
        }
        if (obj instanceof String || type === 'string' ||
            obj instanceof Number || type === 'number' ||
            obj instanceof Boolean || type === 'boolean') {
            return obj;
        }
        if (obj instanceof Array) {
            length = obj.length;
            for (i = 0; i < length; i++) {
                obj[i] = build(obj[i], depth);
            }
            return obj;
        }
        for (name in obj) {
            if (obj.hasOwnProperty(name)) {
                obj[name] = build(obj[name], depth);
            }
        }
        return obj;
    };

    /**
     * Get and set request specific metadata related to caramel.
     * @param meta
     * @return {*}
     */
    meta = function (meta) {
        return meta ? (request.__caramel_page_metadata__ = meta) : request.__caramel_page_metadata__;
    };

    /**
     * Resolves absolute paths by adding app context prefix.
     * @param path
     * @return {*}
     */
    url = function (path) {
        context = context || configs().context;
        return (context !== '/' ? context : '') + path;
    };


    /**
     * Resolves absolute paths by adding theme prefix.
     * @param path
     * @return {*}
     */
    themeUrl = function (path) {
        context = context || configs().context;
        return (context !== '/' ? context : '') + themesDir + '/' + configs().themer() + path;
    };

    parseRequest = function (req) {
        return {
            method: req.getMethod(),
            url: req.getRequestURI(),
            query: req.getQueryString(),
            content: req.getContent(),
            headers: req.getAllHeaders(),
            parameters: req.getAllParameters()
        };
    };

    translate = function (text) {
        var language, dir, path,
            config = configs(),
            code = config.language ? config.language() : 'en';
        language = languages[code];
        if (!language) {
            dir = config.languagesDir || languagesDir;
            path = dir + '/' + code + '.json';
            if (!new File(path).isExists()) {
                return text;
            }
            language = (languages[code] = require(path));
            if (log.isDebugEnabled()) {
                log.debug('Language json loaded : ' + path);
            }
        }
        return language[text];
    };
})();