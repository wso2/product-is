$(function () {
    $('#connectBtn').click(function (e) {
        e.preventDefault();
        $('#light').show();
        $('#fade').show();
        drawAddAccountPopup();
    });
});

function drawAddAccountPopup() {

    var top =
            "    <div class=\"container content-section-wrapper\">\n" +
            "        <div class=\"row\">\n" +
            "            <div class=\"col-lg-12 content-section\">\n" +
            "                <div class=\"headerDiv\">\n" +
            "                   <span class=\"headerText\">Associate User Account<span>\n" +
            "                </div>" +
            "                <form method=\"post\" class=\"form-horizontal\" id=\"associateForm\" name=\"selfReg\" >\n";

    var middle =
            "                    <div class=\"control-group\">\n" +
            "                        <div class=\"controls\">\n" +
            "                            <label class=\"control-label inputlabel\" for=\"userName\">User Name<span class=\"required\">*</span></label>\n" +
            "                            <input class=\"col-lg-3 inputContent requiredField userInputs\" type=\"text\" value=\"\" id=\"userName\" name=\"userName\"  />\n" +
            "                        </div>\n" +
            "                    </div>\n" +
            "                    <div class=\"control-group\">\n" +
            "                        <div class=\"controls\">\n" +
            "                            <label class=\"control-label inputlabel\" for=\"password\">Password<span class=\"required\">*</span></label>\n" +
            "                            <input class=\"col-lg-3 inputContent requiredField userInputs\" type=\"password\" value=\"\" id=\"password\" name=\"password\"  />\n" +
            "                        </div>\n" +
            "                    </div>\n";

    var end =
            "                    <div class=\"control-group\" style=\"margin-left: 116px;\">\n" +
            "                        <div class=\"controls\">\n" +
            "                            <input type=\"button\" onclick=\"connect();\" class=\"btn btn-primary\"  style=\"margin-right: 5px;\" value=\"Connect\"/>\n" +
            "                            <input type=\"button\" onclick=\"cancelConnect();\" class=\"btn\" value=\"&nbsp;Cancel&nbsp;\"/>\n" +
            "                        </div>\n" +
            "                    </div></div>\n" +
            "                </form>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "    </div>   ";

    var output = top + middle + end;

    $("#light").empty();
    $("#light").append(output);

    $(".userInputs").keypress(function (event) {
        if (event.which == 13) {
            event.preventDefault();
            connect();
        }
    });
}

function cancelConnect() {

    $('#light').hide();
    $('#fade').hide();
}

function connect() {

    if (hasValidInputs()) {

        $('#light').hide();
        $('#fade').hide();

        $.ajax({
                   url: "/portal/gadgets/connected_accounts/index.jag",
                   type: "POST",
                   data: $('#associateForm').serialize() + "&cookie=" + cookie + "&action=connect",
                   success: function (data) {
                       var resp = $.parseJSON(data);
                       if (resp.success == true) {
                           reloadGrid();
                       } else {
                           debugger;
                           if (typeof resp.reLogin != 'undefined' && resp.reLogin == true) {
                               window.top.location.href = window.location.protocol + '//' + serverUrl + '/dashboard/logout.jag';
                           } else {
                               if (resp.message != null && resp.message.length > 0) {
                                   message({content: resp.message, type: 'error', cbk: function () {
                                   }});
                               } else {
                                   message({content: 'Error occurred while connecting user accounts.', type: 'error', cbk: function () {
                                   }});
                               }
<<<<<<< HEAD
=======
                               reloadGrid();
>>>>>>> b1dd18c... Committing user account association feature
                           }
                       }
                   },
                   error: function (e) {
                       message({content: 'Error occurred while connecting user accounts.', type: 'error', cbk: function () {
                       }});
<<<<<<< HEAD
=======
                       reloadGrid();
>>>>>>> b1dd18c... Committing user account association feature
                   }
               });
    }
}

function hasValidInputs() {

    var valid = true;

    $(".requiredField").each(function () {
        if ($(this).val() == null || $(this).val().trim().length == 0) {

            message({content: $('label[for=' + $(this).attr('id') + ']').text() + ' is required', type: 'warning', cbk: function () {
            }});
            valid = false;
            return false;
        }
    });

    return valid;
}



