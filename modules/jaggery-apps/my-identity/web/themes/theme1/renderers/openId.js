var render = function (theme, data, meta, require) {
	var header = meta.session.get('user-header');
	header = parse(stringify(header).replace("class='openId'", "class='active'"));

	theme('index', {
        body: [
                {
                    partial: 'my_openId',
                    context: {
                        messages : data.messages,
                        ctxData : data.openIds
                    }
                }
        ],
        header: [
            {
                partial: 'header',
                context:{
                    title:'OpenID Dashboard',
                    openId:true,
                    logReqLinks : header['logReqLinks'],
                    logNotReqLinks : header['logNotReqLinks'],
                    username : meta.session.get('user'),
                    messages : data.messages,
                    breadcrumb:[
                        {link:'/', name:'Home',isLink:true},
                        {link:'', name:'OpenID Dashboard',isLink:false}
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
