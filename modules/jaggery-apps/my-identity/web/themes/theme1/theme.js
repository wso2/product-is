var engine = caramel.engine('handlebars', (function () {
    //to do move this code to caramel
    
    return {
        /*render: function (data, meta) {

            var replacer = function (match, pIndent, pKey, pVal, pEnd) {
                var key = '<span class=json-key>';
                var val = '<span class=json-value>';
                var str = '<span class=json-string>';
                var r = pIndent || '';
                if (pKey)
                    r = r + key + pKey.replace(/[": ]/g, '') + '</span>: ';
                if (pVal)
                    r = r + (pVal[0] == '"' ? str : val) + pVal + '</span>';
                return r + (pEnd || '');
            };
            var prettyPrint = function (obj) {
                var jsonLine = /^( *)("[\w]+": )?("[^"]*"|[\w.+-]*)?([,[{])?$/mg;
                return JSON.stringify(obj, null, 3)
                    .replace(/&/g, '&amp;').replace(/\\"/g, '&quot;')
                    .replace(/</g, '&lt;').replace(/>/g, '&gt;')
                    .replace(jsonLine, replacer);
            };

            print('<style> .json-key { color: tomato; } .json-value { color: indigo; } .json-string { color: darkgreen; } </style><pre><code>')
            print(prettyPrint(data));
            print('</code></pre>');
        },*//*
        partials: function (Handlebars) {
	        Handlebars.registerHelper('if_eq', function(context, options) {
	            if (context == options.hash.compare)
	                return options.fn(this);
	            return options.inverse(this);
	        });

    	}*/
    };
}()));