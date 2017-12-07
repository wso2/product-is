function drawPage() {
    console.log(json);
    var output = "";
    var u2fScript="<script src=\"u2f-api.js\"></script>";
    var start = "<div class=\"container-fluid\" style=\"width:95%\">\n" +
        "    <div class=\"row\">\n" +
        "        <div class=\"col-lg-12\" style=\"margin-top: 25px; margin-left: 25px; \">\n" +
        "              <table class=\"table\">\n" +
        "                  <thead>\n" +
        "                      <tr>\n" +
        "                          <th colspan=\"2\">Manage Profile</th>\n" +
        "                      </tr>\n" +
        "                  </thead>\n" +
        "                  <tbody>\n" +
        "                <input type=\"hidden\" name=\"profile\" value=\"default\" />\n";

    var body = "";
    var totpEnabled = "";
    for (var i in json.return.fieldValues) {
        if (json.return.fieldValues[i].claimUri =="http://wso2.org/claims/identity/accountDisabled") {
            continue;
        }

    if (json.return.totpAuthenticatorActive && json.return.fieldValues[i].displayName == "Secret Key") {
    	            totpEnabled = json.return.fieldValues[i].fieldValue;
    	            continue;
    	        }
        body = body + "          <tr>\n" +
        "                           <td>" +
        "<label class=\"control-label\">" + json.return.fieldValues[i].displayName;
        if (json.return.fieldValues[i].required == "true") {
            body = body + " <span class=\"required\">*</span>";
        }

        body = body + " </label>\n</td>" +
            "                    <td><div class=\"controls\">";

        if (json.return.fieldValues[i].readOnly == "true") {
            body = body + "                        <input type=\"text\" disabled=\"\" value=\"" + json.return.fieldValues[i].fieldValue + "\" id=\"" + json.return.fieldValues[i].claimUri + "\" name=\"" + json.return.fieldValues[i].claimUri + "\" style=\"height: 30px;  align: left;width: 100%;padding-left: 25px;padding-right: 25px;\" />\n" +
                " <input type=\"hidden\" name=\"" + json.return.fieldValues[i].claimUri + "\" value=\"" + json.return.fieldValues[i].fieldValue + "\" />";
        }
        else {
            body = body + "<input type=\"text\" value=\"" + json.return.fieldValues[i].fieldValue + "\" id=\"" + json.return.fieldValues[i].claimUri + "\" name=\"" + json.return.fieldValues[i].claimUri +
                "\" style=\"height: 30px;  align: left;width: 100%;padding-left: 25px;padding-right: 25px;\" />";

        }
        body = body + "                    </div>\n" +
            "                </td></tr>";

    }
    if (json.return.totpAuthenticatorActive) {
                    body = body + "<tr><td><label class=\"control-label\">Refresh Secret Key</label>\n</td><td><div class=\"controls\">";
                    body = body + "<a class=\"control-label\" onclick=\"validateRefreshSecret();\">Click</a></div>\n<br></div></td></tr>";
                    body = body + "<tr><td><label class=\"control-label\">Enable TOTP</label>\n</td><td><div class=\"controls\">";
                    if (totpEnabled != ""){
                            body += "<input type=\"checkbox\" checked name=\"totpenable\" onclick=\"validateCheckBox();\"/>\n<br><br>"+
                            "<div class=\"container\" style=\"padding-left:0px; padding-right:0px;\" id=\"qrContainer\">"+
                            "<div class=\"panel-group\">"+
                                    "<div class=\"panel panel-default\">"+
                                        "<div class=\"panel-heading\" style=\"padding: 5px 5px 25px 5px;\">"+
                                            "<h4 class=\"panel-title\">"+
                                                "<a data-toggle=\"collapse\" onclick=\"initiateTOTP()\" style=\"display:inline-block; float:left; text-decoration: none;\">"+
                                                "<div id=\"scanQR\" style=\"overflow:inherit; float:left; padding-right:2px;\"><span class=\"glyphicon glyphicon-collapse-down\"></span></div>"+
                                                "Scan QR Code</a>"+
                                            "</h4>"+
                                        "</div>"+
                                        "<div id=\"qrcanvdiv\" class=\"panel-collapse collapse\" style=\"display:none\">"+
                                            "<div id=\"qrdiv\">"+
                                                "<form name=\"qrinp\">"+
                                                    "<input type=\"numeric\" name=\"ECC\" value=\"1\" size=\"1\" style=\"display:none\">"+
                                                    "<canvas id=\"qrcanv\" style=\"display:inline-block; float:right;\">"+
                                                "</form>"+
                                            "</div>"+
                                        "</div>"+
                                    "</div>"+
                                "</div>"+
                                "</div>";
                            } else {
                            body += "<input type=\"checkbox\" name=\"totpenable\" onclick=\"validateCheckBox();\"/>\n<br><br>"+
                            "<div class=\"container\" style=\"display:none; padding-left:0px; padding-right:0px;\" id=\"qrContainer\">"+
                            "<div class=\"panel-group\">"+
                                    "<div class=\"panel panel-default\">"+
                                        "<div class=\"panel-heading\" style=\"padding: 5px 5px 25px 5px;\">"+
                                            "<h4 class=\"panel-title\">"+
                                                "<a data-toggle=\"collapse\" onclick=\"initiateTOTP()\" style=\"display:inline-block; float:left; text-decoration: none;\">"+
                                                "<div id=\"scanQR\" style=\"overflow:inherit; float:left; padding-right:2px;\"><span class=\"glyphicon glyphicon-collapse-down\"></span></div>"+
                                                "Scan QR Code</a>"+
                                            "</h4>"+
                                        "</div>"+
                                        "<div id=\"qrcanvdiv\" class=\"panel-collapse collapse\" style=\"display:none\">"+
                                            "<div id=\"qrdiv\">"+
                                                "<form name=\"qrinp\">"+
                                                    "<input type=\"numeric\" name=\"ECC\" value=\"1\" size=\"1\" style=\"display:none\">"+
                                                    "<canvas id=\"qrcanv\" style=\"display:inline-block; float:right;\">"+
                                                "</form>"+
                                            "</div>"+
                                        "</div>"+
                                    "</div>"+
                                "</div>"+
                                "</div>";
         }
     }
    body = body + "</div></td></tr>";


    var endString ="<tr>\n" +
        "               <td colspan=\"2\">" +
        "                   <div style=\"margin: auto;\">" +
        "                    <button id=\"connectFedBtn\" class=\"btn btn-primary mgL14px\" onclick=\"drawFIDORegistration(this);\" type=\"button\" >Manage U2F Authentication</button>" +
        "                    </td></div></tr>"+
        "<tr><td colspan=\"2\">" +
        "                        <input type=\"button\" onclick=\"validate();\" class=\"btn btn-primary\" value=\"Update\"/>\n" +
        "                        <input type=\"button\" onclick=\"cancel();\" class=\"btn\" value=\"Cancel\"/>\n" +
        "                    </td></tr>" +
        "                  </tbody>\n" +
        "</table>"+
        "        </div>\n" +
        "        </div>\n" +
        "    </div>";


    output =  u2fScript + start + body + endString;
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

function reloadGrid() {
    $.ajax({
               url: "/portal/gadgets/user_profile/index.jag",
               type: "POST",
               data: {cookie : cookie, user : userName},
               success: function (data) {
json = $.parseJSON(data);
drawPage();


               },
               error: function (e) {
                   message({content: 'Error occurred while loading values for the grid.', type: 'error', cbk: function () {
                   }});
               }
           });
}

function deleteFIDOToken(deviceRemarks){

 var msg = "You are about to remove Id '" + username + "' From IDP '" + idPId + "'. Do you want to proceed?";
    message({content: msg, type: 'confirm', okCallback: function () {
        $.ajax({
                   url: "/portal/gadgets/connected_accounts/index.jag",
                   type: "POST",
                   data: {cookie : cookie, username : username, idPId : idPId, action : "fedDelete"},
                   success: function (data) {
                       var resp = $.parseJSON(data);
                       if (resp.success == true) {
                           reloadFedGrid();
                       } else {
                           if (typeof resp.reLogin != 'undefined' && resp.reLogin == true) {
                               window.top.location.href = window.location.protocol + '//' + serverUrl + '/dashboard/logout.jag';
                           } else {
                               if (resp.message != null && resp.message.length > 0) {
                                   message({content: resp.message, type: 'error', cbk: function () {
                                   }});
                               } else {
                                   message({content: 'Error occurred while deleting user account.', type: 'error', cbk: function () {
                                   }});
                               }
                           }
                       }
                   },
                   error: function (e) {
                       message({content: 'Error occurred while deleting user account.', type: 'error', cbk: function () {
                       }});
                   }
               });
    }, cancelCallback: function () {
    }});
}

function drawFIDORegistration() {

$.ajax({
           url: "/portal/gadgets/user_profile/controllers/my-profile/fido-metadata.jag",
           type: "POST",
           data: {cookie : cookie, action : "idPList"},
           success: function (data) {

var deviceMetadata = null;
if(data != null && "" != data){
               var resp = $.parseJSON(data);
	       deviceMetadata = resp.return;
}

                       var top =
                               "    <div class=\"container content-section-wrapper\">\n" +
                               "        <div class=\"row\">\n" +
                               "            <div class=\"col-lg-12 content-section\">\n" +
       			       "                <legend>Manage FIDO U2F Device </legend>\n" +
                               "                <form method=\"post\" class=\"form-horizontal\" id=\"associateForm\" name=\"selfReg\"  >\n";
                       var middle = "";
                   if (deviceMetadata != null && deviceMetadata.length > 0) {
                       var middle =
                               "    <div class=\"control-group\">\n" +
                               "        <table class=\"table table-bordered\">\n" +
                               "            <thead>\n" +
                               "                <tr>\n" +
                               "                    <th class='txtAlnCen width80p'>Device Remarks</th>\n" +
                               "                    <th class='txtAlnCen'>Action</th>\n" +
                               "                </tr>\n" +
                               "            </thead>\n";


                       if (isArray(deviceMetadata)) {
                           for (var i in deviceMetadata) {
                               middle = middle +
                                         "                <tr>\n" +
                                         "                    <td > Registration Time : " + deviceMetadata[i] + "</td>\n" +
                                         "                    <td class='txtAlnCen'>\n" +
                                         "                        <a title=\"\" onclick=\"removeFIDO('" + deviceMetadata[i] + "');\" href=\"javascript:void(0)\"><i class=\"icon-trash\"></i> Remove</a>\n" +
                                         "                    </td>\n" +
                                         "                </tr>\n";
                           }
                       }
                       else {

                           middle = middle +
                                     "                <tr>\n" +
                                     "                    <td > Registration Time : "  + deviceMetadata + "</td>\n" +
                                     "                    <td class='txtAlnCen'>\n" +
                                     "                        <a title=\"\" onclick=\"removeFIDO('" + deviceMetadata + "');\" href=\"javascript:void(0)\"><i class=\"icon-trash\"></i> Remove</a>\n" +
                                     "                    </td>\n" +
                                     "                </tr>\n";

                       }

                       var middle = middle + "            </tbody>\n" +
                                  "        </table>\n" +
                                  "    </div>";
 }
else {
middle = middle + "<label > Device not registered yet please register your device ! </label>";
}


                       var end =
                               "                    <div class=\"control-group\">\n" +
                               "                        <div class=\"controls\">\n" +
                               "                            <input type=\"button\" onclick=\"startFIDO();\" class=\"btn btn-primary\" style=\"margin-right: 5px;\" value=\"Attach FIDO Token\"/>\n" +
                               "                            <input type=\"button\" onclick=\"drawPage();\" class=\"btn\" value=\"Done\"/>\n" +
                               "                        </div>\n" +
                               "                    </div></div>\n" +
                               "                </form>\n" +
                               "            </div>\n" +
                               "        </div>\n" +
                               "    </div>   ";

                       var output = top + middle + end;

                       $("#gadgetBody").empty();
                       $("#gadgetBody").append(output);


           },
           error: function (e) {
               message({content: 'Error occurred while loading identity providers.', type: 'error', cbk: function () {
               }});
           }
       });
}

function isArray(element) {
    return Object.prototype.toString.call(element) === '[object Array]';
}

function validateCheckBox(){
    var fld = document.getElementsByName("totpenable")[0];
    if(fld.checked){
        initiateTOTP();
        document.getElementById('qrContainer').style.display = 'block';
        document.getElementById('qrcanvdiv').style.display = 'none';
        $("#tokenInvalid").empty();
    }else{
        $('#totpQRCode').attr("src","");
        resetTOTP();
    }
}

function getQRCode(){
    initiateTOTP();
}
function validateRefreshSecret(){
    refreshSecretKey();
    alert("SecretKey is refreshed. Please restore the secret key in your mobile app");
}

function getSecretKey(url){
    var loc = jQuery.parseJSON(url).return;
    $('#secret').value(loc);
}

function loadQRCode(url){
    var key = jQuery.parseJSON(url).return;
    var decodedKey = atob(key);
    setupqr();
    doqr(decodedKey);
}

function removeQRCode(){
    $('#totpQRCode').attr("src","");
    $('#totpQRCode').css("visibility","hidden");
    $('#totpQRCode').show();
}