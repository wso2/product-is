var log = new Log();

var render = function (theme, data, meta, require) {
	var header = meta.session.get('user-header');
	header = parse(stringify(header).replace("class='selfSignup'", "class='active'"));

    theme('index', {
        body: [
            {
                partial: 'self_registration',
                context: {
                    messages : data.messages,
                    ctxData : data.regData
                }
            }
        ],
        header: [
            {
                partial: 'header',
                context:{
                    title:'Sign-up with User Name/Password',
                    selfSignup:true,
                    logReqLinks : header['logReqLinks'],
                    logNotReqLinks : header['logNotReqLinks'],
                    username : false,
                    messages : data.messages,
                    breadcrumb:[
                        {link:'/', name:'Home',isLink:true},
                        {link:'', name:'Sign-up with User Name/Password',isLink:false}
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
