var render = function (theme, data, meta, require) {
	var header = meta.session.get('user-header');
	header = parse(stringify(header).replace("class='login'", "class='active'"));

	theme('index', {
        body: [
            {
                partial: 'login',
                context: {
            	 	messages : data.messages
            	}
            }
        ],
        header: [
            {
                partial: 'header',
                context:{
                    title:'Login',
                    login:true,
                    logReqLinks : header['logReqLinks'],
                    logNotReqLinks : header['logNotReqLinks'],
                    username : false,
                    messages : data.messages,
                    breadcrumb:[
                                {link:'/', name:'Login',isLink:false}
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
