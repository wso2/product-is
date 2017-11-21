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

function cancelProcess(parameters) {
    location.href = "index.jag?" + (parameters ? parameters : "");
}

function cancelProcessToLogin(parameters) {
    location.href = "login.jag?" + (parameters ? parameters : "");
}

function createMessageBody() {
    var element = "<div class=\"d_modal fade\" id=\"messageModal\">\n" +
                  "  <div class=\"d_modal-dialog width40\">\n" +
                  "    <div class=\"d_modal-content\">\n" +
                  "      <div class=\"d_modal-header\">\n" +
                  "        <button type=\"button\" class=\"d_close\" data-dismiss=\"modal\" aria-hidden=\"true\">&times;</button>\n" +
                  "        <h3 class=\"d_modal-title d_header\">Modal title</h3>\n" +
                  "      </div>\n" +
                  "      <div class=\"d_modal-body\">\n" +
                  "        <p>One fine body&hellip;</p>\n" +
                  "      </div>\n" +
                  "      <div class=\"d_modal-footer\">\n" +
                  "      </div>\n" +
                  "    </div>\n" +
                  "  </div>\n" +
                  "</div>";
    $("#message").empty();
    $("#message").append(element);
}


var messageDisplay = function (params) {
    createMessageBody();
    $('#messageModal').html($('#confirmation-data').html());
    if (params.title == undefined) {
        $('#messageModal h3.d_modal-title').html('Dashboard');
    } else {
        $('#messageModal h3.d_modal-title').html(params.title);
    }
    $('#messageModal div.d_modal-body').html(params.content);
    if (params.buttons != undefined) {
        //$('#messaged_modal a.btn-primary').hide();
        $('#messageModal div.d_modal-footer').html('');
        for (var i = 0; i < params.buttons.length; i++) {
            $('#messageModal div.d_modal-footer').append($('<a class="d_btn ' + params.buttons[i].cssClass + '">' + params.buttons[i].name + '</a>').click(params.buttons[i].cbk));
        }
    } else {
        $('#messageModal a.d_btn-primary').html('OK').click(function () {
            $('#messageModal').modal('hide');
        });
    }
    $('#messageModal a.d_btn-other').hide();
    $('#messageModal').modal();
};
/*
 usage
 Show info dialog
 message({content:'foo',type:'info', cbk:function(){alert('Do something here.')} });

 Show warning
 dialog message({content:'foo',type:'warning', cbk:function(){alert('Do something here.')} });

 Show error dialog
 message({content:'foo',type:'error', cbk:function(){alert('Do something here.')} });

 Show confirm dialog
 message({content:'foo',type:'confirm',okCallback:function(){},cancelCallback:function(){}});
 */
var message = function (params) {
    if (params.type == "custom") {
        messageDisplay(params);
        return;
    }

    var icon = "";
    if (params.type == "warning") {
        icon = "icon-warning-sign";
    } else if (params.type == "info") {
        icon = "icon-info";
    } else if (params.type == "error") {
        icon = "icon-remove-sign";
    } else if (params.type == "confirm") {
        icon = "icon-question-sign";
    }
    if(params.type == "error") {
        params.content = '<table class="msg-table"><tr><td class="imageCell errorBg"><i class="' + icon + ' errorIcon"></i></td><td class="messageText-wrapper"><span class="messageText pdL10">' + params.content + '</span></td></tr></table>';
    } else {
        params.content = '<table class="msg-table"><tr><td class="imageCell ' + params.type + '"><i class="' + icon + '"></i></td><td class="messageText-wrapper"><span class="messageText">' + params.content + '</span></td></tr></table>';
    }
    if (params.type == "confirm") {
        if (params.title == undefined) {
            params.title = "Dashboard"
        }
        messageDisplay({content: params.content, title: params.title, buttons: [
            {name: "Yes", cssClass: "d_btn d_btn-primary", cbk: function () {
                $('#messageModal').modal('hide');
                if (typeof params.okCallback == "function") {
                    params.okCallback()
                }
                ;
            }},
            {name: "No", cssClass: "d_btn", cbk: function () {
                $('#messageModal').modal('hide');
                if (typeof params.cancelCallback == "function") {
                    params.cancelCallback()
                }
                ;
            }}
        ]
                       });
        return;
    }

    if (params.title == undefined || params.title == null) {
        if (params.type == "info") {
            params.title = "Dashboard Notification"
        }
        if (params.type == "warning") {
            params.title = "Dashboard Warning"
        }
        if (params.type == "error") {
            params.title = "Dashboard Error"
        }
    }
    messageDisplay({content: params.content, title: params.title, buttons: [
        {name: "OK", cssClass: "d_btn d_btn-primary", cbk: function () {
            $('#messageModal').modal('hide');
            if (params.cbk && typeof params.cbk == "function") {
                params.cbk();
            }
        }}
    ]
                   });
};


