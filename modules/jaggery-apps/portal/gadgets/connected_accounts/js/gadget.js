function drawPage() {

    $("#gadgetBody").empty();
    if (json != null || fedJson != null) {
        var noRaws = true;
        var top =
            "    <div class=\"col-lg-12 content-section\">\n" +
            "        <table class=\"table table-bordered\">\n" +
            "            <thead>\n" +
            "                <tr>\n" +
            "                    <th class='txtAlnCen width30p'>User ID</th>\n" +
            "                    <th class='txtAlnCen width30p'>Identity Provider</th>\n" +
            "                    <th class='txtAlnCen'>Actions</th>\n" +
            "                </tr>\n" +
            "            </thead>\n" +
            "            <tbody>\n";

        var middle = "";
        if (isArray(json)) {
            for (var i in json) {
                noRaws = false;
                middle = middle +
                    "                <tr>\n" +
                    "                    <td>" + json[i].fullUsername + "</td>\n" +
                    "                    <td><i  class='resident-idp'></i>   Resident Identity Provider   </td>\n";

                var connectedAccount = json[i].username;
                if ('PRIMARY' != json[i].domain) {
                    connectedAccount = json[i].domain + "/" + connectedAccount;
                }
                if ('carbon.super' != json[i].tenantDomain) {
                    connectedAccount = connectedAccount + '@' + json[i].tenantDomain;
                }

                middle = middle +
                    "                    <td>\n" +
                    "                        <a title='' onclick=\"deleteUserAccountConnection('" + connectedAccount + "');\" href=\"javascript:void(0)\"><i class=\"icon-trash\"></i> \n" + "Remove</a>\n" +
                    "                    </td>\n" +
                    "                </tr>\n";
            }
        }
        if (isArray(fedJson.list)) {
            for (var i in fedJson.list) {
                noRaws = false;
                middle = middle +
                    "                <tr>\n" +
                    "                    <td>" + fedJson.list[i].username + "</td>\n" +
                    "                    <td><i class='fedarate'></i>   " + fedJson.list[i].idPName + "   </td>\n" +
                    "                    <td>\n" +
                    "                        <a title=\"\" onclick=\"deleteFedUserAccountConnection('" + fedJson.list[i].idPName + "' ,'" + fedJson.list[i].username + "');\" href=\"javascript:void(0)\"><i class=\"icon-trash\"></i> Remove</a>\n" +
                    "                    </td>\n" +
                    "                </tr>\n";
            }
        }
        if (noRaws) {
            middle = middle +
                "<tr>" +
                "<td colspan=\"3\"><i>No Accounts Found.</i></td>" +
                "</tr>";
        }

        var end =
            "            </tbody>\n" +
            "        </table>\n" +
            "    </div>";

        var output = top + middle + end;
        console.log(output);
        $("#gadgetBody").append(output);

    }
}

function isArray(element) {
    return Object.prototype.toString.call(element) === '[object Array]';
}

function deleteUserAccountConnection(delUser) {

    var msg = "You are about to remove account '" + delUser + "'. Do you want to proceed?";
    message({
        content: msg, type: 'confirm', okCallback: function () {
            $.ajax({
                url: "/portal/gadgets/connected_accounts/index.jag",
                type: "POST",
                data: "&cookie=" + cookie + "&userName=" + delUser + "&action=delete",
                success: function (data) {
                    var resp = $.parseJSON(data);
                    if (resp.success == true) {
                        reloadGrid();
                    } else {
                        if (typeof resp.reLogin != 'undefined' && resp.reLogin == true) {
                            window.top.location.href = window.location.protocol + '//' + serverUrl + '/dashboard/logout.jag';
                        } else {
                            if (resp.message != null && resp.message.length > 0) {
                                message({
                                    content: resp.message, type: 'error', cbk: function () {
                                    }
                                });
                            } else {
                                message({
                                    content: 'Error occurred while deleting user account.',
                                    type: 'error',
                                    cbk: function () {
                                    }
                                });
                            }
                        }
                    }
                },
                error: function (e) {
                    message({
                        content: 'Error occurred while deleting user account.', type: 'error', cbk: function () {
                        }
                    });
                }
            });
        }, cancelCallback: function () {
        }
    });
}

function reloadGrid() {
    $.ajax({
        url: "/portal/gadgets/connected_accounts/index.jag",
        type: "GET",
        data: "&cookie=" + cookie + "&action=list",
        success: function (data) {
            var resp = $.parseJSON(data);
            if (resp.success == true) {
                json = resp.data;
                reloadFedGrid(json);
                drawPage();
                changeDropDownMenu();
            } else {

                if (typeof resp.reLogin != 'undefined' && resp.reLogin == true) {
                    window.top.location.href = window.location.protocol + '//' + serverUrl + '/dashboard/logout.jag';
                } else {
                    if (resp.message != null && resp.message.length > 0) {
                        message({
                            content: resp.message, type: 'error', cbk: function () {
                            }
                        });
                    } else {
                        message({
                            content: 'Error occurred while loading values for the grid.',
                            type: 'error',
                            cbk: function () {
                            }
                        });
                    }
                }
            }
        },
        error: function (e) {
            message({
                content: 'Error occurred while loading values for the grid.', type: 'error', cbk: function () {
                }
            });
        }
    });
}

function changeDropDownMenu() {
    if (json != null) {
        if (isArray(json)) {
            var htmlContent = '<div class="dropdown_separator"><span class="switch_to_div">Switch To :</span></div>';
            for (var i in json) {
                var connectedAccount = json[i].username;
                if ('PRIMARY' != json[i].domain) {
                    connectedAccount = json[i].domain + "/" + connectedAccount;
                }
                if ('carbon.super' != json[i].tenantDomain) {
                    connectedAccount = connectedAccount + '@' + json[i].tenantDomain;
                }
                htmlContent += '<li class="associated_accounts"><a href="javascript:void(0)" onclick="switchAccount(\'' +
                    connectedAccount + '\');"><i class="icon-user pdR2p"></i>' + connectedAccount + '</a></li>';

            }
            if ($('.dropdown-account', window.parent.document).find('.dropdown_separator') != null) {
                $('.dropdown-account', window.parent.document).find('.dropdown_separator').remove();
                $('.dropdown-account', window.parent.document).find('.associated_accounts').remove();
            }
            $('.dropdown-account', window.parent.document).append(htmlContent);
        }
    } else {
        if ($('.dropdown-account', window.parent.document).find('.dropdown_separator') != null) {
            $('.dropdown-account', window.parent.document).find('.dropdown_separator').remove();
            $('.dropdown-account', window.parent.document).find('.associated_accounts').remove();
        }
    }

    var sessionRefresherUrl = window.location.protocol + '//' + serverUrl + '/dashboard/refresh.jag';

    $.ajax({
        url: sessionRefresherUrl,
        type: "POST",
        data: "&userList=" + JSON.stringify(json)
    });
}

function reloadFedGrid(json) {
    $.ajax({
        url: "/portal/gadgets/connected_accounts/index.jag",
        type: "GET",
        data: "&cookie=" + cookie + "&username=" + userName + "&action=associatedIdList",
        success: function (data) {
            var resp = $.parseJSON(data);
            if (resp.success == true) {
                fedJson = resp.data;
                drawPage();
            } else {

                if (typeof resp.reLogin != 'undefined' && resp.reLogin == true) {
                    window.top.location.href = window.location.protocol + '//' + serverUrl + '/dashboard/logout.jag';
                } else {
                    if (resp.message != null && resp.message.length > 0) {
                        message({
                            content: resp.message, type: 'error', cbk: function () {
                            }
                        });
                    } else {
                        message({
                            content: 'Error occurred while loading values for the grid.',
                            type: 'error',
                            cbk: function () {
                            }
                        });
                    }
                }
            }
        },
        error: function (e) {
            message({
                content: 'Error occurred while loading values for the grid.', type: 'error', cbk: function () {
                }
            });
        }
    });
}

function drawFedPage() {

    $("#fedGadgetBody").empty();

    if (fedJson != null) {
        var top =
            "    <div class=\"col-lg-12 content-section\">\n" +
            "        <table class=\"table table-bordered\">\n" +
            "            <thead>\n" +
            "                <tr>\n" +
            "                    <th class='txtAlnCen width40p'>Identity Provider</th>\n" +
            "                    <th class='txtAlnCen width40p'>Federated User ID</th>\n" +
            "                    <th class='txtAlnCen'>Action</th>\n" +
            "                </tr>\n" +
            "            </thead>\n";

        var middle =
            "            <tbody>\n" +
            "                <tr>\n" +
            "                    <td> Primary OpenID </td>" +
            "                    <td>" + fedJson.primaryOpenID + "</td>\n" +
            "                    <td> </td>" +
            "                </tr>\n";


        if (isArray(fedJson.list)) {
            for (var i in fedJson.list) {
                middle = middle +
                    "                <tr>\n" +
                    "                    <td>" + fedJson.list[i].idPName + "</td>\n" +
                    "                    <td>" + fedJson.list[i].username + "</td>\n" +
                    "                    <td class='txtAlnCen'>\n" +
                    "                        <a title=\"\" onclick=\"deleteFedUserAccountConnection('" + fedJson.list[i].idPName + "' ,'" + fedJson.list[i].username + "');\" href=\"javascript:void(0)\"><i class=\"icon-trash\"></i> Remove</a>\n" +
                    "                    </td>\n" +
                    "                </tr>\n";
            }
        }

        var end = "            </tbody>\n" +
            "        </table>\n" +
            "    </div>";

        var output = top + middle + end;

        $("#fedGadgetBody").append(output);
    }
}

function deleteFedUserAccountConnection(idPId, username) {

    var msg = "You are about to remove Id '" + username + "' From IDP '" + idPId + "'. Do you want to proceed?";
    message({
        content: msg, type: 'confirm', okCallback: function () {
            $.ajax({
                url: "/portal/gadgets/connected_accounts/index.jag",
                type: "POST",
                data: "&cookie=" + cookie + "&username=" + username + "&idPId=" + idPId + "&action=fedDelete",
                success: function (data) {
                    var resp = $.parseJSON(data);
                    if (resp.success == true) {
                        reloadFedGrid();
                    } else {
                        if (typeof resp.reLogin != 'undefined' && resp.reLogin == true) {
                            window.top.location.href = window.location.protocol + '//' + serverUrl + '/dashboard/logout.jag';
                        } else {
                            if (resp.message != null && resp.message.length > 0) {
                                message({
                                    content: resp.message, type: 'error', cbk: function () {
                                    }
                                });
                            } else {
                                message({
                                    content: 'Error occurred while deleting user account.',
                                    type: 'error',
                                    cbk: function () {
                                    }
                                });
                            }
                        }
                    }
                },
                error: function (e) {
                    message({
                        content: 'Error occurred while deleting user account.', type: 'error', cbk: function () {
                        }
                    });
                }
            });
        }, cancelCallback: function () {
        }
    });
}
