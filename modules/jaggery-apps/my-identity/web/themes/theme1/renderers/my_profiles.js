var log = new Log();

var render = function (theme, data, meta, require) {
	var header = meta.session.get('user-header');
	header = parse(stringify(header).replace("class='myProfiles'", "class='active'"));

    theme('index', {
        body: [
            {
                partial: 'my_profiles',
                context: {
                    messages : data.messages,
                    ctxData : data.profile['return']
                }
            }
        ],
        header: [
            {
                partial: 'header',
                context:{
                    title:'My Profiles',
                    myProfiles:true,
                    logReqLinks : header['logReqLinks'],
                    logNotReqLinks : header['logNotReqLinks'],
                    username : meta.session.get('user'),
                    messages : data.messages,
                    breadcrumb:[
                        {link:'/', name:'Home',isLink:true},
                        {link:'', name:'My Profiles',isLink:false}
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
