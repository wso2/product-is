function onRequest(env) {
    
    var session = getSession();
    if (!session) {
        sendRedirect(env.contextPath + env.config['loginPageUri']);
    }
    return {username: session.getUser().getUsername()};
}