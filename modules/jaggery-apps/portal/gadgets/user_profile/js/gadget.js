function drawPage() {
    console.log(json);
    var output = "";
    var u2fScript="<script src=\"chrome-extension://pfboblefjcgdjicmnffhdgionmgcdmne/u2f-api.js\"></script>";
    var start = "<div class=\"container-fluid\" style=\"width:95%\">\n" +
        "    <div class=\"row\">\n" +
        "        <div class=\"col-lg-12\">\n" +
        "            <!--<form method=\"post\" class=\"form-horizontal\" id=\"updateProfileform\" name=\"updateProfileform\" action=\"controllers/my-profile/edit-finish.jag?\" >-->\n" +
        "\n" +
        "                <input type=\"hidden\" name=\"profile\" value=\"default\" />\n" +
        "                <div class=\"control-group\">" +
        "                    <label class=\"control-label\" for=\"profileName\">Profile Name<span class=\"required\">*</span></label>" +
        "                    <div class=\"controls\">" +
        "                        <input type=\"text\" id=\"profileName\" disabled=\"\" name=\"profileName\" placeholder=\"Profile Name\" value=\"" + json.return.profileName + "\" class=\"required\"/>" +
        "                    </div>" +
        "                </div>";

    var body = "";

    for (var i in json.return.fieldValues) {
        body = body + "                <div class=\"control-group\">\n" +
            "                    <label class=\"control-label\">" + json.return.fieldValues[i].displayName;
        if (json.return.fieldValues[i].required == "true") {
            body = body + " <span class=\"required\">*</span>";
        }

        body = body + " </label>\n" +
            "\n" +
            "                    <div class=\"controls\">";

        if (json.return.fieldValues[i].readOnly == "true") {
            body = body + "                        <input type=\"text\" disabled=\"\" value=\"" + json.return.fieldValues[i].fieldValue + "\" id=\"" + json.return.fieldValues[i].claimUri + "\" name=\"" + json.return.fieldValues[i].claimUri + "\"  />\n" +
                " <input type=\"hidden\" name=\"" + json.return.fieldValues[i].claimUri + "\" value=\"" + json.return.fieldValues[i].fieldValue + "\" />";
        }
        else {
            body = body + "<input type=\"text\" value=\"" + json.return.fieldValues[i].fieldValue + "\" id=\"" + json.return.fieldValues[i].claimUri + "\" name=\"" + json.return.fieldValues[i].claimUri + "\"  />";

        }
        body = body + "                    </div>\n" +
            "                </div>";

    }


    var endString = "                <div class=\"control-group\">\n" +
        "                    <div class=\"controls\">\n" +
        "                        <input type=\"button\" onclick=\"validate();\" class=\"btn btn-primary\" value=\"Update\"/>\n" +
        "                        <input type=\"button\" onclick=\"cancel();\" class=\"btn\" value=\"Cancel\"/>\n" +
        "                    </div>\n" +
        "                </div>\n" +
        "            <!--</form>-->\n" +
        "        </div>\n" +
        "        </div>\n" +
        "    </div>";

    var fido = "                <div class=\"control-group\">\n" +
        "                    <div class=\"controls\">\n" +
        "                        <input type=\"button\" onclick=\"startFIDO();\" class=\"btn btn-primary\" value=\"Attach FIDO Token\"/>\n" +
        "                    </div>\n" +
        "                </div>\n" +
        "            <!--</form>-->\n" +
        "        </div>\n" +
        "        </div>\n" +
        "    </div>";

    output =  u2fScript + start + body + fido + endString;
    $("#gadgetBody").empty();
    $("#gadgetBody").append(output);
}

function cancel() {
    gadgets.Hub.publish('org.wso2.is.dashboard', {
        msg:'A message from User profile',
        id:"user_profile  .shrink-widget"
    });

}

function validate() {
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

    for (var i in json.return.fieldValues) {
        var fldname = json.return.fieldValues[i].claimUri;
        var displayName = json.return.fieldValues[i].displayName;

        if (json.return.fieldValues[i].required == "true") {
            if (validateEmpty(fldname).length > 0) {
                message({content:displayName + ' is required', type:'warning', cbk:function () {
                } });
                return false;
            }
        }
        if ((json.return.fieldValues[i].regEx).length > 0) {
            var reg = new RegExp(json.return.fieldValues[i].regEx);
            var value = document.getElementsByName(fldname)[0].value;

            var valid = reg.test(value);
            if (value != '' && !valid) {
                message({content:displayName + ' is not valid', type:'warning', cbk:function () {
                } });
                return false;
            }

        }
    }


    var unsafeCharPattern = /[<>`\"]/;
    var elements = document.getElementsByTagName("input");
    for (i = 0; i < elements.length; i++) {
        if ((elements[i].type === 'text' || elements[i].type === 'password') &&
            elements[i].value != null && elements[i].value.match(unsafeCharPattern) != null) {
            message({content:'Unauthorized characters are specified', type:'warning', cbk:function () {
            } });
            return false;
        }
    }
    submitUpdate();
}

function validateEmpty(fldname) {
    var fld = document.getElementsByName(fldname)[0];
    var error = "";
    var value = fld.value;
    if (value.length == 0) {
        error = fld.name + " ";
        return error;
    }
    value = value.replace(/^\s+/, "");
    if (value.length == 0) {
        error = fld.name + "(contains only spaces) ";
        return error;
    }
    return error;
} 

