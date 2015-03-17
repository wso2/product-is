function drawPage() {

    $("#gadgetBody").empty();
    if (json != null) {

        var top =
                "    <div class=\"col-lg-12 content-section\">\n" +
                "        <table class=\"table table-bordered\">\n" +
                "            <thead>\n" +
                "                <tr>\n" +
                "                    <th class='txtAlnCen width80'>User Name</th>\n" +
                "                    <th class='txtAlnCen'>Action</th>\n" +
                "                </tr>\n" +
                "            </thead>\n" +
                "            <tbody>\n";

        var middle = "";
        if (isArray(json)) {
            for (var i in json) {
                middle = middle +
                         "                <tr>\n" +
                         "                    <td>" + json[i] + "</td>\n" +
                         "                    <td class='txtAlnCen'>\n" +
                         "                        <a title=\"\" onclick=\"deleteUserAccountConnection('" + json[i] + "');\" href=\"javascript:void(0)\"><i class=\"icon-trash\"></i> \n" + "Remove</a>\n" +
                         "                    </td>\n" +
                         "                </tr>\n";
            }
        }

        var end =
                "            </tbody>\n" +
                "        </table>\n" +
                "    </div>";

        var output = top + middle + end;
        $("#gadgetBody").append(output);

    }
}

function isArray(element) {
    return Object.prototype.toString.call(element) === '[object Array]';
}

function deleteUserAccountConnection(delUser) {

    var msg = "You are about to remove account '" + delUser + "'. Do you want to proceed?";
    message({content: msg, type: 'confirm', okCallback: function () {
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

function reloadGrid() {
    $.ajax({
               url: "/portal/gadgets/connected_accounts/index.jag",
               type: "GET",
               data: "&cookie=" + cookie + "&action=list",
               success: function (data) {
                   var resp = $.parseJSON(data);
                   if (resp.success == true) {
                       json = resp.data;
                       drawPage();
                       changeDropDownMenu();
                   } else {

                       if (typeof resp.reLogin != 'undefined' && resp.reLogin == true) {
                           window.top.location.href = window.location.protocol + '//' + serverUrl + '/dashboard/logout.jag';
                       } else {
                           if (resp.message != null && resp.message.length > 0) {
                               message({content: resp.message, type: 'error', cbk: function () {
                               }});
                           } else {
                               message({content: 'Error occurred while loading values for the grid.', type: 'error', cbk: function () {
                               }});
                           }
                       }
                   }
               },
               error: function (e) {
                   message({content: 'Error occurred while loading values for the grid.', type: 'error', cbk: function () {
                   }});
               }
           });
}

function changeDropDownMenu(){
    if(json != null){
        if(isArray(json)){
            var htmlContent =  '<div class="dropdown_separator"><span>Switch To</span></div>';
            for(var i in json){
                htmlContent += '<li class="associated_accounts"><a href="javascript:void(0)" onclick="switchAccount(\'' +
                               (json[i].indexOf("@carbon.super") >= 0 ? json[i].split("@carbon.super")[0] : json[i]) +
                               '\');"><i class="icon-user pdR2p"></i>' + (json[i].indexOf("@carbon.super") >= 0 ? json[i].split("@carbon.super")[0] : json[i]) + '</a></li>';

            }
            if($('.dropdown-account', window.parent.document).find('.dropdown_separator') != null){
                $('.dropdown-account', window.parent.document).find('.dropdown_separator').remove();
                $('.dropdown-account', window.parent.document).find('.associated_accounts').remove();
            }
            $('.dropdown-account', window.parent.document).append(htmlContent);
        }
    } else {
        if($('.dropdown-account', window.parent.document).find('.dropdown_separator') != null){
            $('.dropdown-account', window.parent.document).find('.dropdown_separator').remove();
            $('.dropdown-account', window.parent.document).find('.associated_accounts').remove();
        }
    }

    var sessionRefresherUrl = window.location.protocol + '//' + serverUrl + '/dashboard/refresh.jag';

    $.ajax({
               url: sessionRefresherUrl,
               type: "POST",
               data: "&userList=" +JSON.stringify(json)
           });
}
