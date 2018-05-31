function onInitialRequest(context) {
    var acr = selectAcrFrom(context, ["acr1", "acr2"]);
    Log.info("--------------- ACR selected "+ acr);
    //comment
    switch(acr) {
        case "acr1" :
            executeStep(1, {
                onSuccess : function(context) {
                    var isAdmin = hasRole(context.steps[1].subject, 'admin');
                    Log.info("--------------- Has Admin "+isAdmin);
                    if(isAdmin) {
                        executeStep({id :'2'});
                    }
                }
            });
            break;
        case "acr2" :
            executeStep(1); executeStep(2);  break;
        default :
            executeStep(1);  executeStep(2);
    }
}