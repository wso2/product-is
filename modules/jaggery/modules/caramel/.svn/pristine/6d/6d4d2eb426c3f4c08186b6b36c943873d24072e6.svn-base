(function (Handlebars) {

    var caramelData = 'X-Caramel-Data',
        resources = {
            js: {},
            css: {},
            code: {}
        };

    /**
     * {{#itr context}}key : {{key}} value : {{value}}{{/itr}}
     */
    Handlebars.registerHelper("itr", function (obj, options) {
        var key, buffer = '';
        for (key in obj) {
            if (obj.hasOwnProperty(key)) {
                buffer += options.fn({key: key, value: obj[key]});
            }
        }
        return buffer;
    });

    /**
     * {{#func myFunction}}{{/func}}
     */
    Handlebars.registerHelper("func", function (context, block) {
        var param,
            args = [],
            params = block.hash;
        for (param in params) {
            if (params.hasOwnProperty(param)) {
                args.push(params[param]);
            }
        }
        return block(context.apply(null, args));
    });

    /**
     * Registers  'url' handler for resolving theme files.
     * {{url "js/jquery-lates.js"}}
     */
    Handlebars.registerHelper('url', function (path) {
        if (path.indexOf('http://') === 0 || path.indexOf('https://') === 0) {
            return path;
        }
        return caramel.url(path);
    });

    /**
     * Registers  't' handler for translating texts.
     * {{t "programming"}}
     */
    Handlebars.registerHelper('t', function (text) {
        return text;
    });

    /**
     * Registers  'json' handler for serializing objects.
     * {{json data}}
     */
    Handlebars.registerHelper('json', function (obj) {
        return obj ? new Handlebars.SafeString(JSON.stringify(obj)) : null;
    });

    /**
     * Registers  'cap' handler for resolving theme files.
     * {{url "js/jquery-lates.js"}}
     */
    Handlebars.registerHelper('cap', function (str) {
        return str.replace(/[^\s]+/g, function (str) {
            return str.substr(0, 1).toUpperCase() + str.substr(1).toLowerCase();
        });
    });

    /**
     * {{#slice start="1" end="10" count="2" size="2"}}{{name}}{{/slice}}
     */
    Handlebars.registerHelper('slice', function (context, block) {
        var html = "",
            length = context.length,
            start = parseInt(block.hash.start) || 0,
            end = parseInt(block.hash.end) || length,
            count = parseInt(block.hash.count) || length,
            size = parseInt(block.hash.size) || length,
            i = start,
            c = 0;
        while (i < end && c++ < count) {
            html += block(context.slice(i, (i += size)));
        }
        return html;
    });

    /**
     * {{#t "text"}}{{/t}}
     */
    Handlebars.registerHelper('t', function (text) {
        return  new Handlebars.SafeString(text);
    });

    caramel.unloaded = {};

    caramel.data = function (areas, options) {
        var headers = options.headers || (options.headers = {});
        options.dataType = 'json';
        headers[caramelData] = JSON.stringify(areas);
        $.ajax(options);
    };

    caramel.render = function (template, context, callback) {
        var partial, fns, html,
            fn = Handlebars.partials[template],
            unloaded = caramel.unloaded;
        if (fn) {
            html = fn(context);
        } else {
            unloaded[template] = true;
        }
        fns = [];
        for (partial in unloaded) {
            if (unloaded.hasOwnProperty(partial)) {
                fns.push(function (partial) {
                    return function (callback) {
                        delete caramel.unloaded[partial];
                        caramel.get('/themes/' + caramel.themer + '/partials/' + partial + '.hbs', function (data) {
                            Handlebars.partials[partial] = Handlebars.compile(data);
                            callback(null);
                        }, 'html');
                    };
                }(partial));
            }
        }
        if (fns.length === 0) {
            callback(null, html);
            return;
        }
        async.parallel(fns, function (err, results) {
            err ? callback(err) : caramel.render(template, context, callback);
        });
    };

    caramel.css = function (el, css, id) {
        var i, length;
        if (id && resources.css[id]) {
            return;
        } else {
            resources.css[id] = true;
        }
        if (css instanceof Array) {
            length = css.length;
            for (i = 0; i < length; i++) {
                el.append('<link rel="stylesheet" type="text/css" href="' + caramel.url('/themes/' + caramel.themer + '/css/' + css[i]) + '"/>');
            }
        } else {
            el.append('<link rel="stylesheet" type="text/css" href="' + caramel.url('/themes/' + caramel.themer + '/css/' + css) + '"/>');
        }
    };

    caramel.js = function (el, js, id, callback) {
        var i, counter, length;
        if (id && resources.js[id]) {
            callback();
            return;
        } else {
            resources.js[id] = true;
        }
        if (js instanceof Array) {
            length = js.length;
            counter = length;
            for (i = 0; i < length; i++) {
                $.getScript(caramel.url('/themes/' + caramel.themer + '/js/' + js[i]), function () {
                    if (--counter > 0) {
                        return;
                    }
                    callback();
                });
            }
        } else {
            $.getScript(caramel.url('/themes/' + caramel.themer + '/js/' + js), callback);
        }
    };

    caramel.code = function (el, code, id) {
        var i, length;
        if (id && resources.code[id]) {
            return;
        } else {
            resources.code[id] = true;
        }
        if (code instanceof Array) {
            length = code.length;
            for (i = 0; i < length; i++) {
                el.append(code[i]);
            }
        } else {
            el.append(code);
        }
    };

    var invoke = Handlebars.VM.invokePartial;

    Handlebars.VM.invokePartial = function (partial, name, context, helpers, partials, data) {
        var p = Handlebars.partials[name];
        if (p) {
            return invoke.apply(Handlebars.VM, Array.prototype.slice.call(arguments));
        }
        caramel.unloaded[name] = true;
        return '';
    };

}(Handlebars));