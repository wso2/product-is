var render = function (theme, data, meta, require) {
	var header = meta.session.get('user-header');
	header = parse(stringify(header).replace("class='multifactorAuthentication'", "class='active'"));

	theme('index', {
        body: [
                {
                    partial: 'multifactor_authentication',
                    context: {
                        messages : data.messages,
                        ctxData : data.xmppConfig
                    }
                }
        ],
        header: [
            {
                partial: 'header',
                context:{
                    title:'Multifactor Authentication',
                    multifactorAuthentication:true,
                    logReqLinks : header['logReqLinks'],
                    logNotReqLinks : header['logNotReqLinks'],
                    username : meta.session.get('user'),
                    messages : data.messages,
                    breadcrumb:[
                        {link:'/', name:'Home',isLink:true},
                        {link:'', name:'Multifactor Authentication',isLink:false}
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
