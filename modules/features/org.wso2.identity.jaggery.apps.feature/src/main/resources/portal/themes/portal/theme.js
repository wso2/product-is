var cache = false;

var engine = caramel.engine('handlebars', ( function () {
    var context = caramel.configs().context, pages = {
        '2-column-left': {
            template: '2-column-left.hbs',
            areas: ['title', 'header', 'navigation', 'footer', 'body', 'left'],
            areaDefault: 'body'
        },
        '1-column': {
            template: '1-column.hbs',
            areas: ['title', 'header', 'navigation', 'footer', 'body'],
            areaDefault: 'body'
        },
         '1-column-fluid': {
            template: '1-column-fluid.hbs',
            areas: ['title', 'header', 'navigation', 'body'],
            areaDefault: 'body'
        },
        '1-column-fluid-shindig': {
            template: '1-column-fluid-shindig.hbs',
            areas: ['title', 'header', 'navigation', 'body'],
            areaDefault: 'body'
        }
    }, map = {
        //TODO : this needs to be picked from the asset extensions directory.
        '': '1-column',
        'index.jag': '1-column',
        'editor.jag': '1-column-fluid-shindig',
        'dashboard.jag': '1-column-fluid'

    };

    //'dashboard.jag': '1-column-fluid',
    return {
        page: function (data, meta) {
            var page, url = meta.request.getRequestURI();
            url = url.substring(url.indexOf(context) + context.length + 1);
            page = map[url];
            return (meta.page = ( page ? pages[page] : pages['1-column-fluid']));
        },
        layout: function (data, layout, meta) {
            return this.__proto__.layout.call(this, data, layout, meta);
        },
        render: function (data, meta) {
            this.__proto__.render.call(this, data, meta);
            //print(data);
        }
    };
}()));

var resolve = function (path) {
    return this.__proto__.resolve.call(this, path);
};