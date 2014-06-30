var render = function (theme, data, meta, require) {
	var header = meta.session.get('user-header');
	header = parse(stringify(header).replace("class='changeMyPwd'", "class='active'"));

	theme('index', {
        body: [
                {
                    partial: 'change_password',
                    context: {
                        messages : data.messages
                    }
                }
        ],
        header: [
            {
                partial: 'header',
                context:{
                    title:'Change My Password',
                    changeMyPwd:true,
                    logReqLinks : header['logReqLinks'],
                    logNotReqLinks : header['logNotReqLinks'],
                    username : meta.session.get('user'),
                    messages : data.messages,
                    breadcrumb:[
                        {link:'/', name:'Home',isLink:true},
                        {link:'', name:'Change My Password',isLink:false}
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
