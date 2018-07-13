function onInitialRequest(context) {
    executeStep(1, {
        onSuccess: function (context) {
            var username = context.steps[1].subject.username;
            callAnalytics({'ReceiverUrl': '/RiskBasedLogin/InputStream'}, {"username": username}, {
                onSuccess: function (context, data) {
                    if (data.event.riskScore > 0) {
                        executeStep(2);
                    }
                }, onFail: function (context, data) {
                    Log.info('fail Called');
                    executeStep(2);
                }
            });
        }
    });
}
