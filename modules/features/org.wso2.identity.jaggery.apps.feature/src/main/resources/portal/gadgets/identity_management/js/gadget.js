function drawPage() {
    console.log(json);
    var output = "";
    var start = "    <div class=\"col-lg-12 content-section\">\n" +
        "        <table class=\"table table-bordered\">\n" +
        "            <thead>\n" +
        "                <tr>\n" +
        "                    <th>Social Login</th>\n" +
        "                    <th>Identity Provider</th>\n" +
        "                    <th>Action</th>\n" +
        "                </tr>\n" +
        "            </thead>\n";

    var body = "            <tbody>\n";
            body = body + "                <tr>\n" +
                "                    <td>" + json.return + "</td>\n" +
                "                    <td> Primary OpenID </td>" +
                "                    <td> </td>" ;
    if (json != null) {
        if (isArray(json.associatedID)) {

            for (var i in json.associatedID) {
                body = body + "                <tr>\n" +
                    "                    <td>" + json.associatedID[i].split(/:(.+)?/)[1] + "</td>\n" +
                    "                    <td>" + json.associatedID[i].split(':')[0] + "</td>\n" +
                    "                    <td>\n"+
"                        <a title=\"\" onclick=\"validate('" + json.associatedID[i]
                        + "');\" href=\"javascript:void(0)\"><i class=\"icon-trash\"></i> \n" +
                        "                        Remove</a>\n"+
"                    </td>\n" +
                    "                </tr>\n";
            }
        }
        else if(json.associatedID != null) {
            body = body + "                <tr>\n" +
                    "                    <td>" + json.associatedID.split(/:(.+)?/)[1] + "</td>\n" +
                    "                    <td>" + json.associatedID.split(':')[0] + "</td>\n" +
                "                    <td>\n"+
"                        <a title=\"\" onclick=\"validate('" + json.associatedID
                        + "');\" href=\"javascript:void(0)\"><i class=\"icon-trash\"></i> \n" +
                        "                        Remove</a>\n"+
"                    </td>\n" +
                    "                </tr>\n";
        }
    }
    body = body + "            </tbody>\n" +
        "        </table>\n" +
        "    </div>";

    output = start + body;
    $("#gadgetBody").empty();
    $("#gadgetBody").append(output);
}

function itemRemove(providerId) {
    var str = PROXY_CONTEXT_PATH + "/portal/gadgets/identity_management/controllers/identity-management/removeID.jag";
    $.ajax({
        url:str,
        type:"POST",
        data:"idpID=" + providerId.split(':')[0] + "&associatedID=" +providerId.split(/:(.+)?/)[1]+ "&cookie=" + cookie + "&user=" + userName
    })
        .done(function (data) {
            cancel();

        })
        .fail(function () {
            message({content:'Error while removing Social login ID ', type:'error', cbk:function () {
            } });
            console.log('error');

        })
        .always(function () {
            console.log('completed');
        });

}

function isArray(element) {
    return Object.prototype.toString.call(element) === '[object Array]';
}

function cancel() {
    gadgets.Hub.publish('org.wso2.is.dashboard', {
        msg:'A message from User profile',
        id:"identity_management .shrink-widget"
    });

}

function validate(appName) {
    var element = "<div class=\"modal fade\" id=\"messageModal\">\n" +
        "  <div class=\"modal-dialog\">\n" +
        "    <div class=\"modal-content\">\n" +
        "      <div class=\"modal-header\">\n" +
        "        <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\">&times;</button>\n" +
        "        <h3 class=\"modal-title\">Modal title</h4>\n" +
        "      </div>\n" +
        "      <div class=\"modal-body\">\n" +
        "        <p>One fine body&hellip;</p>\n" +
        "      </div>\n" +
        "      <div class=\"modal-footer\">\n" +
        "      </div>\n" +
        "    </div>\n" +
        "  </div>\n" +
        "</div>";
    $("#message").append(element);
    itemRemoveValidate(appName);
}

function itemRemoveValidate(appName) {
    var msg = "You are about to remove  Id " + appName.split(/:(.+)?/)[1] + " From IDP " +appName.split(':')[0] +". Do you want to proceed?";
    message({content:msg, type:'confirm', okCallback:function () {
        itemRemove(appName);
    }, cancelCallback:function () {
    }});
} 

