function onLoginRequest(context) {

    if (context.request.cookies.testcookie) {
        Log.info("--------------- cookie testcookie found in request.");
        Log.info("--------------- cookie testcookie.value: " + context.request.cookies.testcookie.value);
        Log.info("--------------- cookie testcookie.domain: " + context.request.cookies.testcookie.domain);
        Log.info("--------------- cookie testcookie.max-age: " + context.request.cookies.testcookie["max-age"]);
        Log.info("--------------- cookie testcookie.path: " + context.request.cookies.testcookie.path);
        Log.info("--------------- cookie testcookie.secure: " + context.request.cookies.testcookie.secure);
        Log.info("--------------- cookie testcookie.version: " + context.request.cookies.testcookie.version);
        Log.info("--------------- cookie testcookie.httpOnly: " + context.request.cookies.testcookie.httpOnly);
    } else {
        executeStep(1, {
            onSuccess: function (context) {
                Log.info("--------------- setting cookie : testcookie");
                context.response.headers["Set-Cookie"] = "testcookie=1FD36B269C61; Path=/; Secure;" +
                    " HttpOnly; Expires=Wed, 31 Jan 2018 07:28:00 GMT";
            }
        });
    }
}
