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
    var element = "<div class=\"modal fade\" id=\"messageModal\">\n" +
                  "  <div class=\"modal-dialog width40\">\n" +
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
    $("#message").empty();
    $("#message").append(element);
}


var messageDisplay = function (params) {
    createMessageBody();
    $('#messageModal').html($('#confirmation-data').html());
    if (params.title == undefined) {
        $('#messageModal h3.modal-title').html('Dashboard');
    } else {
        $('#messageModal h3.modal-title').html(params.title);
    }
    $('#messageModal div.modal-body').html(params.content);
    if (params.buttons != undefined) {
        //$('#messageModal a.btn-primary').hide();
        $('#messageModal div.modal-footer').html('');
        for (var i = 0; i < params.buttons.length; i++) {
            $('#messageModal div.modal-footer').append($('<a class="btn ' + params.buttons[i].cssClass + '">' + params.buttons[i].name + '</a>').click(params.buttons[i].cbk));
        }
    } else {
        $('#messageModal a.btn-primary').html('OK').click(function () {
            $('#messageModal').modal('hide');
        });
    }
    $('#messageModal a.btn-other').hide();
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
    params.content = '<table class="msg-table"><tr><td class="imageCell ' + params.type + '"><i class="' + icon + '"></i></td><td class="messageText-wrapper"><span class="messageText">' + params.content + '</span></td></tr></table>';
    if (params.type == "confirm") {
        if (params.title == undefined) {
            params.title = "Dashboard"
        }
        messageDisplay({content: params.content, title: params.title, buttons: [
            {name: "Yes", cssClass: "btn btn-primary", cbk: function () {
                $('#messageModal').modal('hide');
                if (typeof params.okCallback == "function") {
                    params.okCallback()
                }
                ;
            }},
            {name: "No", cssClass: "btn", cbk: function () {
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
        {name: "OK", cssClass: "btn btn-primary", cbk: function () {
            $('#messageModal').modal('hide');
            if (params.cbk && typeof params.cbk == "function") {
                params.cbk();
            }
        }}
    ]
                   });
};


