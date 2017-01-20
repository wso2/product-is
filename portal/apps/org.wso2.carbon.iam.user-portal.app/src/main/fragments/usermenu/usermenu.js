function onRequest(env) {
    
    var session = getSession();
    if (!session) {
        sendRedirect(env.contextPath + env.config['loginPageUri']);
    }
    var profileImageResult = isProfileImageAvailbale(session);
    return {
        username: session.getUser().getUsername(), profileImage: profileImageResult.profileImage,
        userId: profileImageResult.userId
    };
}

function isProfileImageAvailbale(session) {
    var Paths = Java.type('java.nio.file.Paths');
    var System = Java.type('java.lang.System');
    var Files = Java.type('java.nio.file.Files');
    var File = Java.type('java.io.File');
    var imageDirPath = Paths.get(System.getProperty('user.dir'), "images");
    if (!Files.exists(imageDirPath)) {
        return {profileImage: false};
    }
    else {
        var file = new File(imageDirPath.toString());
        var names = file.list();
        if (names) {
            for (var i = 0; i < names.length; i++) {
                var imageName = names[i].toString();
                if (imageName.indexOf(session.getUser().getUserId()) !== -1) {
                    return {profileImage: true, userId: session.getUser().getUserId()};
                }
            }
        }

    }
    return {profileImage: false};
}