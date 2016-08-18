/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var accessingEngine;
var listOptions = "DEFAULT";
var serverList;
var serverIndex = 0;

function drawPage(engine) {
    serverList = profileList;
    setEngines();
}

function setEngines() {
    var servers = serverList;
    if (servers.length < 1) {
        drawNoServerErrorpage("No connected BPEL profiles found.<br>Please try again.");
    } else {
        accessingEngine = servers[0].host + "/HumanTaskClientAPIAdmin";
        serverList = servers;
        getList(accessingEngine, servers[0].cookie);
    }
}

function drawNoServerErrorpage(errorMessage) {
    var page = "<div class=\"col-lg-12 sectionSub\">" +
        "<table class=\"carbonFormTable\" style=\"width:100%; padding-left: 10px;\">" +
        "<tr>" +
        "<td style=\"width:20%\" class=\"leftCol-med labelField\">BPEL Profile Engine :     </td>" +
        "<td>" +
        "<select id=\"engine\" onchange='getList2()'>";
    for (var i = 0; i < serverList.length; i++) {
        page = page + "<option value=\"" + serverList[i].host + "/HumanTaskClientAPIAdmin" + "\">" + serverList[i].profile + "</option>";
    }

    page = page + "</select>" +
        "</td>" +
        "</tr>" +
        "</table></div>";
    var error = "<div align=\"center\" ><h1 style=\"color: #8b0000;\"><br><br><br><br><br>" + errorMessage + "<br><br><br><br></h1></div>";
    $("#gadgetBody").empty();
    $("#gadgetBody").append(page + error);
}

function drawTablepage(json, engineValue) {

    var dropDown = " <script type=\"text/javascript\" charset=\"utf-8\" src=\"https://cdn.datatables.net/1.10.0/js/jquery.dataTables.js\"></script><div class=\"col-lg-12 sectionSub\">" +
        "<table class=\"carbonFormTable\" style=\"width:100%; padding-left: 10px;\">" +
        "<tr>" +
        "<td style=\"width:20%\" class=\"leftCol-med labelField\">Task Status :     </td>" +
        "<td>" +
        "<select id=\"option\" onchange='changeOption()'>" +
        "<option value=\"DEFAULT\">DEFAULT</option>" +
        "<option value=\"READY\">READY</option>" +
        "<option value=\"RESERVED\">RESERVED</option>" +
        "<option value=\"COMPLETED\">COMPLETED</option>" +
        "<option value=\"ALL_TASKS\">ALL_TASKS</option>" +
        "</select>" +
        "</td>" +
        "</tr>" +
        "<tr>" +
        "<td style=\"width:20%\" class=\"leftCol-med labelField\">BPEL Profile Engine :     </td>" +
        "<td>" +
        "<select id=\"engine\" onchange='getList2()'>";


    for (var i = 0; i < serverList.length; i++) {
        dropDown = dropDown + "<option value=\"" + serverList[i].host + "/HumanTaskClientAPIAdmin" + "\">" + serverList[i].profile + "</option>";
    }

    dropDown = dropDown + "</select>" +
        "</td>" +
        "</tr>" +
        "</table>" +

        "</div>" +
        "<br><br>";

    var top =
        "    <div class=\"col-lg-12 content-section\">\n" +
        "        <table id=\"task_list\" class=\"table table-bordered pagginated\">\n" +
        "            <thead>\n" +
        "                <tr>\n" +
        "                    <th class='txtAlnCen width20p'>Task Id</th>\n" +
        "                    <th class='txtAlnCen width20p'>Subject</th>\n" +
        "                    <th class='txtAlnCen width20p'>Status</th>\n" +
        "                    <th class='txtAlnCen width20p'>Priority</th>\n" +
        "                    <th class='txtAlnCen'>Created On</th>\n" +
        "                </tr>\n" +
        "            </thead>\n            <tbody>\n";

    var middle = "";

    var obj = JSON.parse(json);
    var hasRows = 0;
    if (obj.taskSimpleQueryResultSet.row != undefined) {
        for (var i = 0; i < obj.taskSimpleQueryResultSet.row.length; i++) {
            var entry = obj.taskSimpleQueryResultSet.row[i];
            if (listOptions == "ALL_TASKS" || listOptions == entry.status || (listOptions == "DEFAULT" &&
                (entry.status == "READY" || entry.status == "RESERVED"))) {
                middle = middle +
                    "                <tr>\n" +
                    "                    <td><input type='button' id='" + entry.id + "' class=\"btn btn-info\" onclick='table_button_click(\"" + entry.id + "\",\"" + entry.status + "\")' value='" + entry.id + "'/></td>" +
                    "                    <td>" + entry.presentationSubject + "</td>" +
                    "                    <td>" + entry.status + "</td>" +
                    "                    <td>" + entry.priority + "</td>" +
                    "                    <td>" + entry.createdTime + "</td>" +
                    "                </tr>\n";
                hasRows = 1;
            }
        }
        if (obj.taskSimpleQueryResultSet.row != null && obj.taskSimpleQueryResultSet.row.length == undefined) {
            var entry = obj.taskSimpleQueryResultSet.row;
            if (listOptions == "ALL_TASKS" || listOptions == entry.status || (listOptions == "DEFAULT" &&
                (entry.status == "READY" || entry.status == "RESERVED"))) {
                middle = middle +
                    "                <tr>\n" +
                    "                    <td><input type='button' id='" + entry.id + "' class=\"btn btn-info\" onclick='table_button_click(\"" + entry.id + "\",\"" + entry.status + "\")' value='" + entry.id + "'/></td>" +
                    "                    <td>" + entry.presentationSubject + "</td>" +
                    "                    <td>" + entry.status + "</td>" +
                    "                    <td>" + entry.priority + "</td>" +
                    "                    <td>" + entry.createdTime + "</td>" +
                    "                </tr>\n";
                hasRows = 1;
            }
        }
        if (hasRows == 0) {
            middle = middle + "<tr>" +
                "<td colspan=\"6\"><i>No requests found.</i></td>" +
                "</tr>";
        }
    } else {
        middle = middle + "<tr>" +
            "<td colspan=\"6\"><i>No requests found.</i></td>" +
            "</tr>";
    }

    var end = "            </tbody>\n" +
        "        </table>\n" +
        "    </div>";


    var output = dropDown + top + middle + end;
    $("#gadgetBody").empty();
    $("#gadgetBody").append(output);


    if (accessingEngine != null) {
        $("#engine").val(accessingEngine);
    }
    $("#option").val(listOptions);

    $('table.pagginated').each(function () {
        var currentPage = 0;
        var numPerPage = 8;
        var $table = $(this);
        $table.bind('repaginate', function () {
            $table.find('tbody tr').hide().slice(currentPage * numPerPage, (currentPage + 1) * numPerPage).show();
        });
        $table.trigger('repaginate');
        var numRows = $table.find('tbody tr').length;
        var numPages = Math.ceil(numRows / numPerPage);
        var $pager = $('<div class="pager"></div>');
        for (var page = 0; page < numPages; page++) {
            $('<span class="page-number"></span>').text(page + 1).bind('click', {
                newPage: page
            }, function (event) {
                currentPage = event.data['newPage'];
                $table.trigger('repaginate');
                $(this).addClass('active').siblings().removeClass('active');
            }).appendTo($pager).addClass('clickable');
        }
        $pager.insertAfter($table).find('span.page-number:first').addClass('active');

    });
}

function cancel() {
    gadgets.Hub.publish('org.wso2.is.dashboard', {
        msg: 'A message from User profile',
        id: "approvals  .shrink-widget"
    });

}

function drawForm(xml, id, state) {

    xml = $.parseXML(xml);
    var top =
        "    <div class=\"col-lg-12 content-section\">\n" +
        "<style>h1 {text-align: center;}</style>" +
        "       <body>" +
        "           <h1>" + xml.getElementsByTagName("taskSubject")[0].textContent + "</h1>" +
        "           <br><br>" +
        " <div class=\"sectionSub\">" +
        "<table class=\"carbonFormTable\" style=\"width:100%\">" +
        "<tr>" +
        "<td style=\"width:20%\" class=\"leftCol-med labelField\">Description :     </td>" +
        "<td>" +
        "<textarea style=\"width:80%\" type=\"text\" name=\"sp-description\" id=\"sp-description\" class=\"text-box-big\" readonly>" +
        xml.getElementsByTagName("taskDescription")[0].textContent +
        "</textarea>" +
        "</td>" +
        "</tr>" +
        "</table>" +

        "</div>" +
        "<br><br>" +

        "       </body>" +
        "        <table class=\"table table-bordered\">\n" +
        "            <thead>\n" +
        "                <tr>\n" +
        "                    <th class='txtAlnCen width20p'>" + "Parameter" + "</th>" +
        "                    <th class='txtAlnCen width20p'>" + "Value" + "</th>" +
        "                </tr>\n" +
        "            </thead>\n";

    var middle = "";
    //
    for (var i = 0; i < xml.getElementsByTagName("xsd-complex-type-wrapper").length; i++) {
        middle = middle +
            "            <tbody>\n" +
            "                <tr>\n" +
            "                    <td>" + xml.getElementsByTagName("xsd-complex-type-wrapper")[i].getAttributeNS("http://ht.bpel.mgt.workflow.identity.carbon.wso2.org/wsdl/schema", "itemName") + "</td>" +
            "                    <td>" + xml.getElementsByTagName("xsd-complex-type-wrapper")[i].textContent.split(",")[0] + "</td>" +
            "                </tr>\n";
    }

    var end =
        "            </tbody>\n" +
        "        </table>\n" +
        "<div id=\"completeButtonDiv\">";

    if (state == "RESERVED") {

        end = end + "<td><input type='button' id='approveTaskButton' class=\"btn btn-primary\" onclick='approve_button_click(\"1\", \"" + id + "\")' ' value='Approve' style=\"float: left; margin-right:10px;\"/></td>" +
            "<td><input type='button' id='disapprovetaskButton' class=\"btn btn-primary\" onclick='approve_button_click(\"2\", \"" + id + "\")' ' value='Disapprove' style=\"float: left; margin-right:10px;\"/></td>" +
            "<td><input type='button' id='releaseTaskButton' class=\"btn btn-primary\" onclick='startReleaseButtonClick(\"2\", \"" + id + "\")' ' value='Release' style=\"float: left; margin-right:10px;\"/></td>";

    } else if (state == "READY") {

        end = end + "<td><input type='button' id='claimTaskButton' class=\"btn btn-primary\" onclick='startReleaseButtonClick(\"5\", \"" + id + "\")' ' value='Claim' style=\"float: left; margin-right:10px;\"/></td>" +
            "<td><input type='button' id='approveTaskButton' class=\"btn btn-primary\" onclick='approve_button_click(\"1\", \"" + id + "\")' ' value='Approve' style=\"float: left; margin-right:10px;\"/></td>" +
            "<td><input type='button' id='disapprovetaskButton' class=\"btn btn-primary\" onclick='approve_button_click(\"2\", \"" + id + "\")' ' value='Disapprove' style=\"float: left; margin-right:10px;\"/></td>";

    } else if (state == "APPROVED" || state == "REJECTED") {
        end = end + "<table class=\"carbonFormTable\" style=\"width:100%\">" +
            "<tr>" +
            "<td style=\"width:20%\" class=\"leftCol-med labelField\">Status :     </td>" +
            "<td>" +
            "<textarea style=\"width:80%\" type=\"text\" name=\"sp-description\" id=\"sp-description\" class=\"text-box-big\" readonly>" +
            state +
            "</textarea>" +
            "</td>" +
            "</tr>" +
            "</table><br><br>";
    }
    end = end + "<td><input type='button' id='test' class=\"btn\" onclick='approve_button_click(\"3\", \"" + id + "\")' ' value='<< Back' style=\"float: left; margin-right:10px;\"/></td><br><br>" +
        " </div>" +
        "    </div>";

    var output = top + middle + end;
    $("#gadgetBody").empty();
    $("#gadgetBody").append(output);


}

function table_button_click(id, state) {

    getFormDetails(id, state);

}

function approve_button_click(state, id) {

    if (state == "1") {
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
        message({
            content: 'Are you sure you want to Approve this task?', type: 'warning', cbk: function () {
                complete("APPROVED", id);
            }
        });
    } else if (state == "2") {
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
        message({
            content: 'Are you sure you want to Disapprove this task?', type: 'warning', cbk: function () {
                complete("REJECTED", id);
            }
        });
    } else {
        getList(accessingEngine, serverList[serverIndex].cookie);
    }

}

function startReleaseButtonClick(requestType, id) {

    if (requestType == "2") {
        start("release", id);
    } else if (requestType == "5") {
        start("claim", id);
    } else {
        getList(accessingEngine, serverList[serverIndex].cookie);
    }

}

function getList2() {
    var e = document.getElementById("engine");
    accessingEngine = e.options[e.selectedIndex].value;
    serverIndex = e.selectedIndex;
    getList(accessingEngine, serverList[serverIndex].cookie);

}

function changeOption() {
    var opt = document.getElementById("option");
    listOptions = opt.options[opt.selectedIndex].value;
    getList(accessingEngine, serverList[serverIndex].cookie);
}


function getList(engine, serverCookie) {
    $.ajax({
        url: "/portal/gadgets/approvals/controllers/approvals/ht-client.jag",
        type: "POST",
        data: "&cookie=" + serverCookie + "&endPoint=" + accessingEngine + "&user=" + userName,
        success: function (data) {
            if (data == "") {
                drawNoServerErrorpage('Error occurred while retrieving human resource tasks.<br>Please try again. ');
            } else {
                drawTablepage(data, engine);
            }
        },
        error: function (e) {
            drawNoServerErrorpage('Error occurred while retrieving human resource tasks.<br>Please try again. ');
            message({
                content: 'Error occurred while retrieving human resource tasks.', type: 'error', cbk: function () {
                }
            });
        }
    });
}

function getFormDetails(id, state) {

    $.ajax({
        url: "/portal/gadgets/approvals/controllers/approvals/form-client.jag",
        type: "POST",
        data: "&cookie=" + serverList[serverIndex].cookie + "&id=" + id + "&endPoint=" + accessingEngine + "&user=" + userName,
        success: function (data) {

            if (state == "RESERVED" || state == "READY") {
                drawForm(data, id, state);
            } else if (state == "COMPLETED") {
                getCompletedState(data, id);
            }
        },
        error: function (e) {
            drawNoServerErrorpage('Error occurred while retrieving task information.<br>Please try again. ');

            message({
                content: 'Error occurred while retrieving task information.', type: 'error', cbk: function () {
                }
            });
        }
    });
}

function getCompletedState(oldData, id) {

    $.ajax({
        url: "/portal/gadgets/approvals/controllers/approvals/getState-client.jag",
        type: "POST",
        data: "&cookie=" + serverList[serverIndex].cookie + "&id=" + id + "&endPoint=" + accessingEngine + "&user=" + userName,
        success: function (data) {
            var obj = $.parseXML(data);
            var inXML = $.parseXML(obj.getElementsByTagNameNS("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803", "taskData")[0].textContent);

            var newState = inXML.getElementsByTagName("approvalStatus")[0].textContent;

            drawForm(oldData, id, newState);
        },
        error: function (e) {
            drawNoServerErrorpage('Error occurred while retriening task information.<br>Please try again. ');
            message({
                content: 'Error occurred while retriening task information.', type: 'error', cbk: function () {
                }
            });
        }
    });

}

function complete(command, id) {

    $.ajax({
        url: "/portal/gadgets/approvals/controllers/approvals/approve-client.jag",
        type: "POST",
        data: "&cookie=" + serverList[serverIndex].cookie + "&id=" + id + "&command=" + command + "&endPoint=" + accessingEngine + "&user=" + userName,
        success: function (data) {
            getFormDetails(id, "COMPLETED");
        },
        error: function (e) {
            drawNoServerErrorpage('Error occurred while updating task.<br>Please try again. ');
            message({
                content: 'Error occurred while updating task.', type: 'error', cbk: function () {
                }
            });
        }
    });
}

function start(requestType, id) {

    $.ajax({
        url: "/portal/gadgets/approvals/controllers/approvals/start-release-suspend-client.jag",
        type: "POST",
        data: "&cookie=" + serverList[serverIndex].cookie + "&id=" + id + "&requestType=" + requestType + "&endPoint=" + accessingEngine + "&user=" + userName,
        success: function (data) {
            if (requestType == "claim") {
                getFormDetails(id, "RESERVED");
            } else {
                getList(accessingEngine, serverList[serverIndex].cookie);
            }
        },
        error: function (e) {
            drawNoServerErrorpage('Error occurred while updating task.<br>Please try again. ');
            message({
                content: 'Error occurred while updating task.', type: 'error', cbk: function () {
                }
            });
        }
    });
}



