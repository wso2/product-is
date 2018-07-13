var username;
function onInitialRequest(context) {
    promptUsername ();
}

function promptUsername () {
    promptIdentifierForStep(1, {
        onSuccess: function(context, data) {
            if (context.request.params.username) {
                username = context.request.params.username[0];
            }
            promptBasic(username);
        },
        onSkip: function(context, data) {
            executeStep(1);

        },
        onFail: function(context, data) {
            Log.info('================================= onFail prompt');
        }
    });
}

function promptBasic(username) {
    executeStep(1, {
        authenticatorParams: {
            "common": {
                "username": username,"inputType":"identifierFirst"
            }
        }
    }, {
        onSuccess: function(context) {
            Log.info("================================= onSuccess basic auth");
        }, onUserAbort: function(context) {
            Log.info("================================= onUserAbort basic auth");
            promptUsername ();
        }, onFail: function(context) {
            promptBasic(username);
        }
    });
}