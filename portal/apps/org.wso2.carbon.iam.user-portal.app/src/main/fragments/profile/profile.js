function onRequest(env) {

    var session = getSession();
    var success = false;
    var message = "";

    if (env.request.method == "POST" && env.params.profileName && env.params.profileName == env.params.actionId) {

        var updatedClaims = env.request.formParams;
        var result = updateUserProfile(session.getUser().getUserId(), updatedClaims);
        success = result.success;
        message = result.message;
    } else if (env.request.method == "POST" && "image" == env.params.actionId) {
        
        var result = uploadFile(env, session);
        success = result.success;
        message = result.message;
    }

    if (env.params.profileName) {

        var uiEntries = [];
        var result = getProfileUIEntries(env.params.profileName, session.getUser().getUserId());
        if (result.success) {
            if (env.request.method != "POST") {
                success = true;
            }
            uiEntries = buildUIEntries(result.profileUIEntries);
        } else {
            success = false;
            message = result.message;
        }
        var profileImageResult = isProfileImageAvailbale(session);

        return {success: success, profile: env.params.profileName, uiEntries: uiEntries,
            message: message, profileImage: profileImageResult.profileImage, userId: profileImageResult.userId};
    }

    return {success: false, message: "Invalid profile name."};
}

function buildUIEntries(profileUIEntries) {

    var uiEntries = [];
    if (profileUIEntries) {
        for (var i = 0; i < profileUIEntries.length > 0; i++) {
            var entry = {
                claimURI: profileUIEntries[i].claimConfigEntry.claimURI,
                displayName: profileUIEntries[i].claimConfigEntry.displayName,
                value: (profileUIEntries[i].value ? profileUIEntries[i].value : ""),
                readonly: ((profileUIEntries[i].claimConfigEntry.readonly
                && profileUIEntries[i].claimConfigEntry.readonly == true) ? "readonly" : ""),
                required: ((profileUIEntries[i].claimConfigEntry.required
                && profileUIEntries[i].claimConfigEntry.required == true) ? "required" : ""),
                requiredIcon: ((profileUIEntries[i].claimConfigEntry.required
                && profileUIEntries[i].claimConfigEntry.required == true) ? "*" : ""),
                dataType: (profileUIEntries[i].claimConfigEntry.dataType ?
                    profileUIEntries[i].claimConfigEntry.dataType : "text"),
                regex: (profileUIEntries[i].claimConfigEntry.regex ?
                    profileUIEntries[i].claimConfigEntry.regex : ".*")
            };
            uiEntries.push(entry);
        }
    }
    return uiEntries;
}

function getProfileUIEntries(profileName, userId) {

    try {
        var profileUIEntries = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
            "getProfileEntries", [profileName, userId]);
        return {success: true, profileUIEntries: profileUIEntries}
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause != null) {
            //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                message = cause.getTargetException().message;
            }
        }
    }
    return {success: false, message: message};
}

function updateUserProfile(userId, updatedClaims) {

    try {
        var profileUIEntries = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "updateUserProfile", [userId, updatedClaims]);
        return {success: true, message: "Your profile is updated."}
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause != null) {
            //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                message = cause.getTargetException().message;
            }
        }
    }
    return {success: false, message: message};
}

function uploadFile(env, session) {

    try {
        var Paths = Java.type('java.nio.file.Paths');
        var System = Java.type('java.lang.System');
        var Files = Java.type('java.nio.file.Files');
        var StandardCopyOption = Java.type('java.nio.file.StandardCopyOption');

        var uploadedFile = env.request.files["image"];
        var imageDirPath = Paths.get(System.getProperty('user.dir'), "images");
        if (!Files.exists(imageDirPath)) {
            Files.createDirectories(imageDirPath);
        }

        var fileName = session.getUser().getUserId() + '.'
            + uploadedFile.name.substring(uploadedFile.name.lastIndexOf(".") + 1, uploadedFile.name.length);
        var destination = Paths.get(imageDirPath).resolve(fileName);
        var sourcePath = Paths.get(uploadedFile.path);
        var destinationPath = Paths.get(destination);

        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        return {success: true, message: "Profile image successfully updated"}
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause != null) {
            //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                message = cause.getTargetException().message;
            }
        }
    }
    return {success: false, message: message};
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