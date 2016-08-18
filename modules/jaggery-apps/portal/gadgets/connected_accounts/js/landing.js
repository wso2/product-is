var addType;

$(function () {
    $('#connectBtn').click(function (e) {
        e.preventDefault();
        $('#light').show();
        $('#fade').show();
        drawAddAccountPopup();
    });
});

$(function () {
    $('#connectFedBtn').click(function (e) {
        e.preventDefault();
        drawAddFedAccountPopup();
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
        "                <form method=\"post\" class=\"form-horizontal\" id=\"associateForm\" name=\"selfReg\"" +
        " >\n" +
        "<div><div class=\"control-group\">\n" +
        "                        <div class=\"controls\">\n" +
        "                            <label class=\"control-label inputlabel pdR25\" for=\"domain\">Account Type" +
        "                                <span class=\"required\">*</span>" +
        "                            </label>\n" +
        "                            <select class=\"col-lg-3 inputContent\" id=\"accountType\"" +
        " onchange='loadForm()'>>\n" +
        "                                <option value=\"Associated\">Local</option>\n"  +
        "                                <option value=\"Federated\">Federated</option>\n"  +
        "                            </select>\n" +
        "                        </div>\n" +
        "                    </div>\n";

    var middle =
        "                    <div class=\"control-group\">\n" +
        "                        <div class=\"controls\">\n" +
        "                            <label class=\"control-label inputlabel pdR25\" for=\"userName\">User Name<span                             class=\"required\">*</span></label>\n" +
        "                            <input class=\"col-lg-3 inputContent requiredField userInputs\" type=\"text\" value=\"\" id=\"userName\" name=\"userName\"  />\n" +
        "                        </div>\n" +
        "                    </div>\n" +
        "                    <div class=\"control-group\">\n" +
        "                        <div class=\"controls\">\n" +
        "                            <label class=\"control-label inputlabel pdR25\" for=\"password\">Password<span                             class=\"required\">*</span></label>\n" +
        "                            <input class=\"col-lg-3 inputContent requiredField userInputs\" type=\"password\" value=\"\" id=\"password\" name=\"password\"  />\n" +
        "                        </div>\n" +
        "                    </div>\n";

    var end =
        "                    <div class=\"control-group\" style=\"margin-left: 116px;\">\n" +
        "                        <div class=\"controls\">\n" +
        "                            <input type=\"button\" onclick=\"connect();\" class=\"btn btn-primary\"  style=\"margin-right: 5px;\" value=\"Associate\"/>\n" +
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

function loadForm() {
    var e = document.getElementById("accountType");
    addType = e.options[e.selectedIndex].value;
    if (addType == "Federated") {
        drawAddFedAccountPopup();
    } else {
        drawAddAccountPopup();
    }

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
                    if (typeof resp.reLogin != 'undefined' && resp.reLogin == true) {
                        window.top.location.href = window.location.protocol + '//' + serverUrl + '/dashboard/logout.jag';
                    } else {
                        if (resp.message != null && resp.message.length > 0) {
                            message({content: resp.message, type: 'error', cbk: function () {
                            }});
                        } else {
                            message({content: 'Error occurred while associating user accounts.', type: 'error', cbk: function () {
                            }});
                        }
                        reloadGrid();
                    }
                }
            },
            error: function (e) {
                message({content: 'Error occurred while associating user accounts.', type: 'error', cbk: function () {
                }});
                reloadGrid();
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

function drawAddFedAccountPopup() {

    $.ajax({
        url: "/portal/gadgets/connected_accounts/index.jag",
        type: "GET",
        data: "&cookie=" + cookie + "&action=idPList",
        success: function (data) {
            var resp = $.parseJSON(data);
            if (resp.success == true) {
                if (resp.data != null && resp.data.length > 0) {
                    $('#light').show();
                    $('#fade').show();


                    var top =
                        "    <div class=\"container content-section-wrapper\">\n" +
                        "        <div class=\"row\">\n" +
                        "            <div class=\"col-lg-12 content-section\">\n" +
                        "                <div class=\"headerDiv\">\n" +
                        "                   <span class=\"socialHeaderText\">Associate Federated User ID<span>\n" +
                        "                </div>" +
                        "                <form method=\"post\" class=\"form-horizontal\" id=\"associateForm\" name=\"selfReg\"  >\n"+
                        "<div><div class=\"control-group\">\n" +
                        "                        <div class=\"controls\">\n" +
                        "                            <label class=\"control-label inputlabel pdR25\" for=\"domain\">Account Type" +
                        "                                <span class=\"required\">*</span>" +
                        "                            </label>\n" +
                        "                            <select class=\"col-lg-3 inputContent\" id=\"accountType\"" +
                        " onchange='loadForm()'>>\n" +
                        "                                <option value=\"Federated\">Federated</option>\n"  +
                        "                                <option value=\"Associated\">Local</option>\n"  +
                        "                            </select>\n" +
                        "                        </div>\n" +
                        "                    </div>\n";

                    var middle =
                        "                  <div><div class=\"control-group\">\n" +
                        "                        <div class=\"controls\">\n" +
                        "                            <label class=\"control-label inputlabel pdR25\" for=\"domain\">Identity Provider Id" +
                        "                                <span class=\"required\">*</span>" +
                        "                            </label>\n" +
                        "                            <select class=\"col-lg-3 inputContent\" name=\"idPId\">\n" ;

                    for (var i in resp.data) {
                        middle = middle +"                                <option value=\""+resp.data[i]+"\">"+resp.data[i]+"</option>\n" ;
                    }

                    middle = middle +
                        "                            </select>\n" +
                        "                        </div>\n" +
                        "                    </div>\n" +
                        "                    <div class=\"control-group\">\n" +
                        "                        <div class=\"controls\">\n" +
                        "                            <label class=\"control-label inputlabel pdR25\" for=\"user_name\">User Name<span class=\"required\">*</span></label>\n" +
                        "                            <input class=\"col-lg-3 inputContent requiredField\" type=\"text\" value=\"\" id=\"user_name\" name=\"username\"  />\n" +
                        "                        </div></div>\n" ;

                    var end =
                        "                    <div class=\"control-group mgnL135\">\n" +
                        "                        <div class=\"controls\">\n" +
                        "                            <input type=\"button\" onclick=\"fedConnect();\" class=\"btn btn-primary\" style=\"margin-right: 5px;\" value=\"Associate\"/>\n" +
                        "                            <input type=\"button\" onclick=\"cancelConnect();\" class=\"btn\" value=\"Cancel\"/>\n" +
                        "                        </div>\n" +
                        "                    </div></div>\n" +
                        "                </form>\n" +
                        "            </div>\n" +
                        "        </div>\n" +
                        "    </div>   " ;

                    var output = top + middle + end;
                    $("#light").empty();
                    $("#light").append(output);

                } else {
                    message({content: 'No registered identity providers found !', type: 'info', cbk: function () {
                    }});
                }
            } else {

                if (typeof resp.reLogin != 'undefined' && resp.reLogin == true) {
                    window.top.location.href = window.location.protocol + '//' + serverUrl + '/dashboard/logout.jag';
                } else {
                    message({content: 'Error occurred while loading identity providers.', type: 'error', cbk: function () {
                    }});
                }
            }
        },
        error: function (e) {
            message({content: 'Error occurred while loading identity providers.', type: 'error', cbk: function () {
            }});
        }
    });
}

function fedConnect() {
    if (hasValidInputs()) {
        $('#light').hide();
        $('#fade').hide();

        $.ajax({
            url: "/portal/gadgets/connected_accounts/index.jag",
            type: "POST",
            data: $('#associateForm').serialize() + "&cookie=" + cookie + "&action=fedConnect",
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
                            message({content: 'Error occurred while associating federated user accounts.', type: 'error', cbk: function () {
                            }});
                        }
                        reloadGrid();
                    }
                }
            },
            error: function (e) {
                message({content: 'Error occurred while associating federated user accounts.', type: 'error', cbk: function () {
                }});
                reloadGrid();
            }
        });
    }
}



