var render = function (theme, data, meta, require) {

	var header = meta.session.get('user-header');
	header = parse(stringify(header).replace("class='dashboard'", "class='active'"));

    theme('index', {
        body: [
            {
                partial: 'dashboard',
                context: {logReqDash : header['logReqDash']}
            }
        ],
        header: [
            {
                partial: 'header',
                context:{
                    title:'My Profiles',
                    dashboard:true,
                    logReqLinks : header['logReqLinks'],
                    logNotReqLinks : header['logNotReqLinks'],
                    username : meta.session.get('user'),
                    messages : data.messages,
                    breadcrumb:[
                                {link:'/', name:'Home',isLink:false}
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