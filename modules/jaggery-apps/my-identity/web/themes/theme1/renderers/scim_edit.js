var render = function (theme, data, meta, require) {
	var header = meta.session.get('user-header');
	header = parse(stringify(header).replace("class='mySCIMProviders'", "class='active'"));

	theme('index', {
        body: [
                {
                    partial: 'scim_provider_edit',
                    context: {
                        messages : data.messages,
                        ctxData : data.provider,
                        isEdit : data.isEdit
                    }
                }
        ],
        header: [
            {
                partial: 'header',
                context:{
                    title:'View/Update SCIM Provider Configuration',
                    mySCIMProviders:true,
                    logReqLinks : header['logReqLinks'],
                    logNotReqLinks : header['logNotReqLinks'],
                    username : meta.session.get('user'),
                    messages : data.messages,
                    breadcrumb:[
                        {link:'/', name:'Home',isLink:true},
                        {link:'/my_scim_providers.jag', name:'My SCIM Providers',isLink:true}
                    ]
                }
            }
        ]
    });
};
