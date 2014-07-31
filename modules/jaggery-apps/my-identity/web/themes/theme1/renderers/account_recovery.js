var render = function (theme, data, meta, require) {
	var header = meta.session.get('user-header');
	header = parse(stringify(header).replace("class='accountRecovery'", "class='active'"));
    theme('index', {
        body: [
                {
                    partial: 'account_recovery',
                    context: {
                        messages : data.messages,
                        ctxData : data.questions
                    }
                }
        ],
        header: [
            {
                partial: 'header',
                context:{
                    title:'Account Recovery',
                    accountRecovery:true,
                    logReqLinks : header['logReqLinks'],
                    logNotReqLinks : header['logNotReqLinks'],
                    username : meta.session.get('user'),
                    messages : data.messages,
                    breadcrumb:[
                        {link:'/', name:'Home',isLink:true},
                        {link:'', name:'Account Recovery',isLink:false}
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
