function drawPage() {
    console.log(json);
    var isQ1Ans = false;
    var q1ID = 0;
    var isQ2Ans = false;
    var q2ID = 0;
    var body = "    <div class=\"col-lg-12 content-section\">\n" +
        "            <fieldset>\n" +
        "                <input type=\"hidden\" name=\"challangedId_1\" value=\"http://wso2.org/claims/challengeQuestion1\"/>\n" +
        "                <legend>Challenge Question Set 1</legend>\n" +
        "                <div class=\"control-group\">\n" +
        "                    <label class=\"control-label\" for=\"q1\">Challenge Question <span class=\"required\">*</span></label>\n" +
        "                    <div class=\"controls\">\n" +
        "                        <select id=\"q1\" name=\"question_1\" class=\"input-xxlarge\">\n";
    for (var i in json.challenge1) {
        if (json.challenge1[i].selected == 'true') {
            isQ1Ans = true;
            q1ID = i;
            body = body + "<option name=\"q\" selected=\"selected\" value=\"" + json.challenge1[i].question + "\">" + json.challenge1[i].question + "</option>\n";
        } else {
            body = body + "<option name=\"q\" value=\"" + json.challenge1[i].question + "\">" + json.challenge1[i].question + "</option>\n";
        }
    }
    body = body + "                        </select>\n" +
        "                    </div>\n" +
        "                </div>\n" +
        "                <div class=\"control-group\">\n" +
        "                    <label class=\"control-label\" for=\"q1answer\">Your Answer <span class=\"required\">*</span></label>\n" +
        "                    <div class=\"controls\">\n";
    if (isQ1Ans) {
        body = body + "                        <input type=\"password\" name=\"ans_1\" class=\"input-xxlarge\" value=\"" + json.challenge1[q1ID].answer + "\" />\n";
    } else {
        body = body + "                        <input type=\"password\" name=\"ans_1\" class=\"input-xxlarge\" value=\"\" />\n";
    }
    body = body + "                    </div>\n" +
        "                </div>\n" +
        "            </fieldset>\n" +

        "            <fieldset>\n" +
        "                <input type=\"hidden\" name=\"challangedId_2\" value=\"http://wso2.org/claims/challengeQuestion2\"/>\n" +
        "                <legend>Challenge Question Set 2</legend>\n" +
        "                <div class=\"control-group\">\n" +
        "                    <label class=\"control-label\" for=\"q1\">Challenge Question <span class=\"required\">*</span></label>\n" +
        "                    <div class=\"controls\">\n" +
        "                        <select id=\"q2\" name=\"question_2\" class=\"input-xxlarge\">\n";
    for (var i in json.challenge2) {
        if (json.challenge2[i].selected == 'true') {
            isQ2Ans = true;
            q2ID = i;
            body = body + "<option name=\"q\" selected=\"selected\" value=\"" + json.challenge2[i].question + "\">" + json.challenge2[i].question + "</option>\n";
        } else {
            body = body + "<option name=\"q\" value=\"" + json.challenge2[i].question + "\">" + json.challenge2[i].question + "</option>\n";
        }
    }
    body = body + "                        </select>\n" +
        "                    </div>\n" +
        "                </div>\n" +
        "                <div class=\"control-group\">\n" +
        "                    <label class=\"control-label\" for=\"q1answer\">Your Answer <span class=\"required\">*</span></label>\n" +
        "                    <div class=\"controls\">\n";
    if (isQ2Ans) {
        body = body + "                        <input type=\"password\" name=\"ans_2\" class=\"input-xxlarge\" value=\"" + json.challenge2[q2ID].answer + "\" />\n";
    } else {
        body = body + "                        <input type=\"password\" name=\"ans_2\" class=\"input-xxlarge\" value=\"\" />\n";
    }
    body = body + "                    </div>\n" +
        "                </div>\n" +
        "            </fieldset>\n" +


        "            <div class=\"control-group\">\n" +
        "                <div class=\"controls\">\n" +
        "                    <input type=\"button\" class=\"btn btn-primary\" value=\"Update\" onclick=\"validate();\"/>\n" +
        "                    <input type=\"button\" class=\"btn\" value=\"Cancel\" onclick=\"cancel();\"/>\n" +
        "                </div>\n" +
        "            </div>\n" +
        "    </div>";


    output = body;
    $("#gadgetBody").empty();
    $("#gadgetBody").append(output);
}

function cancel() {
    gadgets.Hub.publish('org.wso2.is.dashboard', {
        msg:'A message from User profile',
        id:"account_recovery  .shrink-widget"
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


    if (validateEmpty("ans_1").length > 0) {
        var msg = "Challenge Question Set 1 is required";
        message({content:msg, type:'warning', cbk:function () {
        } });
        return false;
    }
    if (validateEmpty("ans_2").length > 0) {
        var msg = "Challenge Question Set 2 is required";
        message({content:msg, type:'warning', cbk:function () {
        } });
        return false;
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
