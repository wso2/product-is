var render = function (theme, data, meta, require) {
	var header = meta.session.get('user-header');
	header = parse(stringify(header).replace("class='mySCIMProviders'", "class='active'"));

	theme('index', {
        body: [
                {
                    partial: 'my_scim_providers',
                    context: {
                        messages : data.messages,
                        ctxData : data.providers
                    }
                }
        ],
        header: [
            {
                partial: 'header',
                context:{
                    title:'My SCIM Providers',
                    mySCIMProviders:true,
                    logReqLinks : header['logReqLinks'],
                    logNotReqLinks : header['logNotReqLinks'],
                    username : meta.session.get('user'),
                    messages : data.messages,
                    breadcrumb:[
                        {link:'/', name:'Home',isLink:true},
                        {link:'', name:'My SCIM Providers',isLink:false}
                    ]
                }
            }
        ],
        error: [
	        {
	        	partial: 'error',	
	        	context:data.error
	        }
        ]
    });
};
