function drawPage() {
    console.log(json);
    var output = "";
    var start = "    <div class=\"col-lg-12 content-section\">\n" +
        "        <table class=\"table table-bordered\">\n" +
        "            <thead>\n" +
        "                <tr>\n" +
        "                    <th>Social Login</th>\n" +
        "                    <th>&nbsp;</th>\n" +
        "                </tr>\n" +
        "            </thead>\n";

    var body = "            <tbody>\n";
    if (json != null) {
        if (isArray(json.return)) {

            for (var i in json.return) {
                body = body + "                <tr>\n" +
                    "                    <td>" + json.return[i] + "</td>\n" +
                    "                    <td>\n";
                if (json.return[i] == json.primary) {
                    // "                    {{#if this.primary}}\n" +
                    body = body + "Primary OpenID";
                } else {
                    //  "                    {{else}}\n" +
                    body = body + "                        <a title=\"\" onclick=\"validate('" + json.return[i]
                        + "');\" href=\"javascript:void(0)\"><i class=\"icon-trash\"></i> \n" +
                        "                        Remove</a>\n";
                }
                body = body + "                    </td>\n" +
                    "                </tr>\n";
            }
        }
        else {
            body = body + "                <tr>\n" +
                "                    <td>" + json.return + "</td>\n" +
                "                    <td>\n";
            if (json.return == json.primary) {
                // "                    {{#if this.primary}}\n" +
                body = body + "Primary OpenID";
            } else {
                //  "                    {{else}}\n" +
                body = body + "                        <a title=\"\" onclick=\"validate('" + json.return
                    + "');\" href=\"javascript:void(0)\"><i class=\"icon-trash\"></i> \n" +
                    "                        Remove</a>\n";
            }
            body = body + "                    </td>\n" +
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
    var str = "/portal/gadgets/identity_management/controllers/identity-management/edit.jag";
    $.ajax({
        url:str,
        type:"POST",
        data:"id=" + providerId + "&cookie=" + cookie + "&user=" + userName
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
    var msg = "You are about to remove " + appName + ". Do you want to proceed?";
    message({content:msg, type:'confirm', okCallback:function () {
        itemRemove(appName);
    }, cancelCallback:function () {
    }});
} 

