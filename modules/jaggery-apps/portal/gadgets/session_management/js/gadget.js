function drawPage() {
    var output = "";
    var start = "    <div class=\"col-lg-12 content-section\">\n" +
        "        <table class=\"table table-bordered\" id=\"tenantName\">\n" +
        "            <thead>\n" +
        "                <tr>\n" +
        "                    <th>UserName</th>\n" +
        "                    <th>User Store Domain</th>\n" +
        "                    <th>Application</th>\n" +
        "                    <th>Application Tenant Domain</th>\n" +
        "                    <th>Time Created</th>\n" +
        "                    <th>Active Duration (Seconds)</th>\n" +
        "                    <th align='center'>Action</th>\n" +
        "                </tr>\n" +
        "            </thead>\n";

    var body = "";
    var tenantDomain = "";
    var userName, userStoreDomain = "";
    var buttonDisabled = "";

    for (var i in json.return) {
        tenantDomain = json.return[i].tenantDomain;
        userName = json.return[i].userName;
        userStoreDomain = json.return[i].userStoreDomain;

        for (var b in json.return[i].sessionsList) {
            body = body + "<tr>";
            if (b == 0) {
                body = body + "<td rowspan=\"" + json.return[i].sessionsList.length + "\"> "
                    + json.return[i].userName + "</td>" +
                              "<td rowspan=\"" + json.return[i].sessionsList.length + "\"> "
                    + json.return[i].userStoreDomain + "</td>";
            }
            body = body + "<td>" + json.return[i].sessionsList[b].applicationId + "</td>" +
                "                    <td>" + json.return[i].sessionsList[b].applicationTenantDomain + "</td>" +
                "                    <td>" + json.return[i].sessionsList[b].loggedInTimeStamp + "</td>" +
                "                    <td>" + json.return[i].sessionsList[b].loggedInDuration + "</td>";
            if (b == 0) {
                if (json.return[i].hasKillPermission == "false") {
                    buttonDisabled = "disabled";
                }
                body = body + "  <td align='center' rowspan=\"" + json.return[i].sessionsList.length + "\">" +
                    "<input type=\"button\" onclick=killSession('" + userName + "','" +
                    tenantDomain + "','" + userStoreDomain + "'); class=\"btn btn-primary\"  value=\"Kill Sessions\" "
                    + buttonDisabled + " /></td>";
            }
            body = body + "                </tr>";
        }
    }

    body = body + "            </tbody>\n" +
        "        </table>\n" +
        "        <input type='hidden' value='" + tenantDomain + "'/>\n" +
        "    </div>";
    output = start + body;
    $("#gadgetBody").empty();
    $("#gadgetBody").append(output);
}