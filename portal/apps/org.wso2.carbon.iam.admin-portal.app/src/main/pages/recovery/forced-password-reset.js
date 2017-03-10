
function onGet(env) {
    var session = getSession();
    if (!session || !session.getUser()) {
        sendRedirect(env.contextPath + env.config['loginPageUri']);
    }
}

