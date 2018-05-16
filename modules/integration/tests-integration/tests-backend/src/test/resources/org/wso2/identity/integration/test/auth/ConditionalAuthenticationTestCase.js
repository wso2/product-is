function onInitialRequest(context) {
    var acr = selectAcrFrom(context, ["acr1", "acr2"]);
    Log.info("--------------- ACR selected "+ acr);
    //comment
    switch(acr) {
        case "acr1" : executeStep({id :'1',
            on : {
                success : function(context) {
                    var isAdmin = hasRole(context.steps[1].subject, 'admin');
                    Log.info("--------------- Has Admin "+isAdmin);
                    if(isAdmin) {
                        executeStep({id :'2'});
                    }
                }
            }
        });
            break;
        case "acr2" : executeStep({id :'1'}); executeStep({id :'2'});  break;
        default :  executeStep({id :'1'});  executeStep({id :'2'});
    }
}