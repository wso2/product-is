engine('handlebars', (function () {
    var renderData, partials, init, page, render, meta, partialsDir, renderJS, renderCSS,
        pagesDir, populate, serialize, globals, theme, renderersDir, helpersDir, translate, evalCode,
        languages = {},
        caramelData = 'X-Caramel-Data',
        log = new Log(),
        Handlebars = require('handlebars').Handlebars;

    evalCode = function (code, data, theme) {
        var template,
            file = new File(theme.resolve.call(theme, 'code/' + code));
        file.open('r');
        template = Handlebars.compile(file.readAll());
        file.close();
        return template(data);
    };

    /**
     * Registers  'include' handler for area inclusion within handlebars templates.
     * {{include body}}
     */
    Handlebars.registerHelper('include', function (contexts) {
        var i, type,
            length = contexts ? contexts.length : 0,
            html = '';
        if (log.isDebugEnabled()) {
            log.debug('Including : ' + stringify(contexts));
        }
        if (length == 0) {
            return html;
        }
        type = typeof contexts;
        if (contexts instanceof Array) {
            for (i = 0; i < length; i++) {
                html += renderData(contexts[i]);
            }
        } else if (contexts instanceof String || type === 'string' ||
            contexts instanceof Number || type === 'number' ||
            contexts instanceof Boolean || type === 'boolean') {
            html = contexts.toString();
        } else {
            html = renderData(contexts);
        }
        return new Handlebars.SafeString(html);
    });

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
     * Registers  'js' handler for JavaScript inclusion within handlebars templates.
     * {{js .}}
     */
    Handlebars.registerHelper('js', function () {
        var i, url, length,
            html = '',
            theme = caramel.theme(),
            js = caramel.meta().js;
        if (!js) {
            return html;
        }
        length = js.length;
        html += meta(theme);
        if (length == 0) {
            return new Handlebars.SafeString(html);
        }
        url = theme.url;
        for (i = 0; i < length; i++) {
            //remove \n when production = true
            html += '\n' + renderJS(url.call(theme, 'js/' + js[i]));
        }
        return new Handlebars.SafeString(html);
    });

    /**
     * Registers  'css' handler for CSS inclusion within handlebars templates.
     * {{css .}}
     */
    Handlebars.registerHelper('css', function () {
        var i, url, length,
            html = '',
            theme = caramel.theme(),
            css = caramel.meta().css;
        if (!css) {
            return html;
        }
        length = css.length;
        if (length == 0) {
            return new Handlebars.SafeString(html);
        }
        url = theme.url;
        for (i = 0; i < length; i++) {
            html += renderCSS(url.call(theme, 'css/' + css[i]));
        }
        return new Handlebars.SafeString(html);
    });

    /**
     * Registers  'code' handler for JavaScript inclusion within handlebars templates.
     * {{code .}}
     */
    Handlebars.registerHelper('code', function () {
        var i, length,
            html = '',
            theme = caramel.theme(),
            meta = caramel.meta(),
            codes = meta.code;
        if (!codes) {
            return html;
        }
        length = codes.length;
        if (length == 0) {
            return html;
        }
        for (i = 0; i < length; i++) {
            html += evalCode(codes[i], meta.data, theme);
        }
        return new Handlebars.SafeString(html);
    });

    /**
     * Registers  'url' handler for resolving webapp files.
     * {{url "js/jquery-lates.js"}}
     */
    Handlebars.registerHelper('url', function (path) {
        if (path.indexOf('http://') === 0 || path.indexOf('https://') === 0) {
            return path;
        }
        return caramel.url(path);
    });

    /**
     * Serialize the current content to the out put
     */
    Handlebars.registerHelper('dump', function (o) {
        return stringify(o);
    });

    /**
     * Registers  'themeUrl' handler for resolving theme files.
     * {{themeUrl "js/jquery-lates.js"}}
     */
    Handlebars.registerHelper('themeUrl', function (path) {
        if (path.indexOf('http://') === 0 || path.indexOf('https://') === 0) {
            return path;
        }
        return caramel.themeUrl(path);
    });

    /**
     * Registers  't' handler for translating texts.
     * {{t "programming"}}
     */
    Handlebars.registerHelper('t', function (text) {
        return translate(text) || text;
    });

    /**
     * Registers  'json' handler for serializing objects.
     * {{json data}}
     */
    Handlebars.registerHelper('json', function (obj) {
        return obj ? new Handlebars.SafeString(stringify(obj)) : null;
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

    /*
     The snoop helper allows a query to be executed on objects stored inside
     an array.
     Usage: {{{snoop 'target(key=value).property' context}}}
	The target should be an array containing objects.
     The function works by recursively parsing a path expression
     */
    Handlebars.registerHelper('snoop',function(path,objectInstance){

	
	/*
	Checks if the provided string is in the form of a query
	A query should be of the form (field=value)
	@str: The string expression that must be validated
	@return: True if the string is an expression.
	*/
        function checkIfQuery(str){
            return(((str.indexOf('('))!=-1)&&(str.indexOf(')')!=-1))?true:false;
        }


	/*
	The function travels recursively processing the object properties
	@loc: A path expression 
	@object:The object in which the path occurs
	*/
        function rec(loc,object){

            //Determine if the path can be broken down
            var components=loc.split('.');

            //Stop traversal if the object is empty
            if(object==null){
                return '';
            }
            else if(components.length==1){
                //Check if the current string is a key to the object
                return object[loc]||'';
            }
            else{

		/*Given a string path like: A(key=value).B.C
		component[0]=A(key=value)
		component[1]=B
		component[2]=C
		*/

                //Get the starting point of A
                var currentStrIndex=loc.indexOf(components[0]);
		//Get the length of A
                var currentStrLength=components[0].length;
		//Extract just A from the string path A.B.C
                var currentStr=loc.substring(currentStrIndex,currentStrLength);
		
		//Remove A. so that the string is B.C
                var nextStr=loc.replace(currentStr+'.','');

		//Get the property object[A] which should ideally return an object.
		//Note: This will evaluate to null if path is not found.e.g. The currentStr
		//is a query like A(key=value)
                var currentObj=object[currentStr];

                //Determine if the currentStr (e.g. A ) is a query
                if(checkIfQuery(components[0])){

                    //Remove (   )
                    var indexStart=components[0].indexOf('(');
                    var indexEnd=components[0].indexOf(')');
		
		    //Extract the query expression (key=value)
                    var expression=components[0].substring(indexStart,indexEnd);
		
		    //Extract key 
                    var operand=components[0].substring(0,indexStart);

		    //Get the object at property A
                    currentObj=object[operand];
		    
		    //Get rid of the brackets in the (key=value)
                    var removed=expression.replace('(','');
                    removed=removed.replace(')','');

                    //Obtain the key and value pair
                    var kv=removed.split('=');

		    //If the key value pair is malformed we stop the search
                    if(kv.length==0){
                        return '';
                    }

                    //Obtain the value
                    var key=kv[0];
                    var value=kv[1];

                    var stop=false;

                    //Go through all items in the array(Assumption)
                    for (var index=0;((index<currentObj.length)&&(!stop));index++) {
			
			//Check the property by the key
                        var item=currentObj[index];

			//Compare the key to the value 
                        if(item[key]==value){

                            currentObj=item;
                            stop=true;	//Short circuit the search
                        }
                    }

                }

                return rec(nextStr,currentObj);

            }
        }

        return  rec(path,objectInstance);
    });

    /**
     *  Registers mergeContext handler which merge different contexts that needs to be passed in to a single partial.
     *
     * {{#mergeContext thisContext=this nameContext=../../name townContext=../town}}
     *      {{>child-partial}}
     * {{/mergeContext}}
     *
     * In the child-partial
     * {{nameContext.username}}
     *
     */
    Handlebars.registerHelper('mergeContext', function(options) {
        var context = {},
            mergeContext = function(obj) {
                for(var k in obj)context[k]=obj[k];
            };
        mergeContext(options.hash);
        return options.fn(context);
    });


    meta = function (theme) {
        var code, g,
            meta = caramel.meta(),
            config = caramel.configs();
        code = 'var caramel = caramel || {}; caramel.context = "' + config.context + '"; caramel.themer = "' + theme.name + '";';
        code += "caramel.url = function (path) { return this.context + (path.charAt(0) !== '/' ? '/' : '') + path; };";
        g = theme.engine.globals(meta.data, meta);
        code += g || '';
        return renderJS(code, true);
    };

    renderData = function (data) {
        var template,
            context = typeof data.context === 'function' ? data.context() : data.context;
        if (data.partial) {
            if (log.isDebugEnabled()) {
                log.debug('Rendering template ' + data.partial);
            }

            template = Handlebars.compile(Handlebars.partials[data.partial]);
        } else {
            if (log.isDebugEnabled()) {
                log.debug('No template, serializing data');
            }
            template = serialize;
        }
        return template(context);
    };

    serialize = function (o) {
        var type = typeof o;
        switch (type) {
            case 'string':
            case 'number':
                return o;
            default :
                return stringify(o);
        }
    };

    helpersDir = 'helpers';

    renderersDir = 'renderers';

    pagesDir = 'pages';

    partialsDir = 'partials';

    partials = function (Handlebars) {
        var theme = caramel.theme();
        (function register(prefix, file) {
            var i, length, name, files;
            if (file.isDirectory()) {
                files = file.listFiles();
                length = files.length;
                for (i = 0; i < length; i++) {
                    file = files[i];
                    register(prefix ? prefix + '.' + file.getName() : file.getName(), file);
                }
            } else {
                name = file.getName();
                if (name.substring(name.length - 4) !== '.hbs') {
                    return;
                }
                file.open('r');
                Handlebars.registerPartial(prefix.substring(0, prefix.length - 4), file.readAll());
                file.close();
            }
        })('', new File(theme.resolve(partialsDir)));
    };

    /**
     * Init function of handlebars engine. This can be overridden by new themes.
     * @param theme
     */
    init = function (theme) {
        if (log.isDebugEnabled()) {
            log.debug('Initializing engine handlebars with theme : ' + theme.name);
        }
        this.partials(Handlebars);
    };

    render = function (data, meta) {
        var fn,
            path = meta.request.getMappedPath() || meta.request.getRequestURI();
        path = caramel.theme().resolve(renderersDir + path.substring(0, path.length - 4) + '.js');
        if (log.isDebugEnabled()) {
            log.debug('Rendering data for the request using : ' + path);
        }
        if (!new File(path).isExists() || !(fn = require(path).render)) {
            print(caramel.build(data));
            return;
        }
        fn(theme, data, meta, function (path) {
            return require(caramel.theme().resolve(path));
        });
    };

    translate = function (text) {
        var language, dir, path,
            config = caramel.configs(),
            code = config.language ? config.language() : 'en';
        language = languages[code];
        if (!language) {
            dir = 'i18n';
            path = caramel.theme().resolve(dir + '/' + code + '.json');
            if (!new File(path).isExists()) {
                return text;
            }
            language = (languages[code] = require(path));
            if (log.isDebugEnabled()) {
                log.debug('Language json loaded : ' + path);
            }
        }
        return language[text] || caramel.translate(text);
    };

    /**
     * Render function of handlebars engine. This can be overridden by new themes.
     */
    theme = function (page, contexts, js, css, code) {
        var file, template, path, area, blocks, helper, length, i, o, areas, block,
            areaContexts, data, areaData, find, blockData,
            theme = caramel.theme(),
            meta = caramel.meta(),
            xcd = meta.request.getHeader(caramelData);
        js = js || [];
        css = css || [];
        code = code || [];

        if (xcd) {
            find = function (areaContexts, partial) {
                var i, context,
                    length = areaContexts.length;
                for (i = 0; i < length; i++) {
                    if (areaContexts[i].partial === partial) {
                        context = areaContexts[i].context;
                        return typeof context === 'function' ? context() : context;
                    }
                }
                return null;
            };
            data = {
                _: {}
            };
            areas = parse(xcd);
            for (area in areas) {
                if (areas.hasOwnProperty(area)) {
                    areaContexts = contexts[area];
                    if (areaContexts instanceof Array) {
                        blocks = areas[area];
                        areaData = (data[area] = {});
                        length = blocks.length;
                        for (i = 0; i < length; i++) {
                            block = blocks[i];
                            blockData = (areaData[block] = {
                                resources: {}
                            });
                            blockData.context = find(areaContexts, block);
                            path = theme.resolve.call(theme, helpersDir + '/' + block + '.js');
                            if (new File(path).isExists()) {
                                helper = require(path);
                                if (helper.resources) {
                                    o = helper.resources(page, meta);
                                    blockData.resources.js = o.js;
                                    blockData.resources.css = o.css;
                                    blockData.resources.code = o.code ? evalCode(o.code, meta.data, theme) : null;
                                }
                            }
                        }
                    } else {
                        data[area] = areaContexts;
                    }
                }
            }
            data._.js = js;
            data._.css = css;
            data._.code = code;
            meta.response.addHeader('Content-Type', 'application/json');
            print(data);
            return;
        }

        for (area in contexts) {
            if (contexts.hasOwnProperty(area)) {
                blocks = contexts[area];
                if (blocks instanceof Array) {
                    length = blocks.length;
                    for (i = 0; i < length; i++) {
                        path = caramel.theme().resolve(helpersDir + '/' + blocks[i].partial + '.js');
                        if (new File(path).isExists()) {
                            helper = require(path);
                            if (helper.resources) {
                                o = helper.resources(page, meta);
                                js = o.js ? js.concat(o.js) : js;
                                css = o.css ? css.concat(o.css) : css;
                                code = o.code ? code.concat(o.code) : code;
                            }
                        }
                    }
                }
            }
        }
        meta.js = js;
        meta.css = css;
        meta.code = code;
        path = caramel.theme().resolve(pagesDir + '/' + page + '.hbs');
        if (log.isDebugEnabled()) {
            log.debug('Rendering page : ' + path);
        }
        file = new File(path);
        file.open('r');
        template = Handlebars.compile(file.readAll());
        file.close();
        print(template(contexts));
    };

    renderJS = function (js, inline) {
        return '<script' + (inline ? '>' + js : ' src="' + js + '">') + '</script>';
    };

    renderCSS = function (css) {
        return '<link rel="stylesheet" type="text/css" href="' + css + '"/>';
    };

    globals = function (data, meta) {
        return null;
    };

    populate = function (dir, ext, theme) {
        var i, n,
            a = [],
            files = new File(theme.resolve(dir + ext)),
            l1 = ext.length,
            l2 = files.length;
        for (i = 0; i < l2; i++) {
            n = files[i].getName();
            if (n.substring(n.length - l1) !== '.' + ext) {
                continue;
            }
            a.push(ext + '/' + n);
        }
        return a;
    };

    return {
        partials: partials,
        translate: translate,
        globals: globals,
        init: init,
        render: render
    };
})());
