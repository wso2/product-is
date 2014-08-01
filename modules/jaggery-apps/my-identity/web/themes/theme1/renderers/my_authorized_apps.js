var render = function (theme, data, meta, require) {
	var header = meta.session.get('user-header');
	header = parse(stringify(header).replace("class='myAuthorizedApps'", "class='active'"));

	theme('index', {
        body: [
            {
                partial: 'my_authorized_apps',
                context: {
                    messages : data.messages,
                    ctxData : data.apps
                }

            }
        ],
        header: [
            {
                partial: 'header',
                context:{
                    title:'My Authorized Apps',
                    myAuthorizedApps:true,
                    logReqLinks : header['logReqLinks'],
                    logNotReqLinks : header['logNotReqLinks'],
                    username : meta.session.get('user'),
                    messages : data.messages,
                    breadcrumb:[
                        {link:'/', name:'Home',isLink:true},
                        {link:'', name:'My Authorized Apps',isLink:false}
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
