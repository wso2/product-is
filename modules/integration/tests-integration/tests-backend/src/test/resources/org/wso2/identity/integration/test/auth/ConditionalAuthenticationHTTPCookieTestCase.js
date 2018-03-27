function httpcontext(context) {

    if (context.request.cookies.testcookie) {
        log.info("--------------- cookie testcookie found in request.");
        log.info("--------------- cookie testcookie.value: " + context.request.cookies.testcookie.value);
        log.info("--------------- cookie testcookie.domain: " + context.request.cookies.testcookie.domain);
        log.info("--------------- cookie testcookie.max-age: " + context.request.cookies.testcookie["max-age"]);
        log.info("--------------- cookie testcookie.path: " + context.request.cookies.testcookie.path);
        log.info("--------------- cookie testcookie.secure: " + context.request.cookies.testcookie.secure);
        log.info("--------------- cookie testcookie.version: " + context.request.cookies.testcookie.version);
        log.info("--------------- cookie testcookie.httpOnly: " + context.request.cookies.testcookie.httpOnly);
    } else {
        executeStep({
            id: '1',
            on: {
                success: function (context) {
                    log.info("--------------- setting cookie : testcookie");
                    context.response.headers["Set-Cookie"] = "testcookie=1FD36B269C61; Path=/; Secure;" +
                        " HttpOnly; Expires=Wed, 31 Jan 2018 07:28:00 GMT"
                }
            }
        });
    }
}
