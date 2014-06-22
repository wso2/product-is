function drawPage() {
    var output = "";
    var start = " <div class=\"col-lg-12 content-section\">" +
        "<fieldset>" +
        "              <table class=\"table table-bordered\">\n" +
        "                  <thead>\n" +
        "                      <tr>\n" +
        "                          <th colspan=\"2\">Enter New Password</th>\n" +
        "                      </tr>\n" +
        "                  </thead>\n" +
        "                  <tbody>\n" +
        "                     <tr>\n" +
        "                        <td><label class=\"\">Current Password</label> </td>\n" +
        "                        <td>\n" +
        "                            <input type=\"password\" value=\"\" id=\"currentPwd\" name=\"currentPwd\"  class=\"col-lg-3\" />\n" +
        "                        </td>\n" +
        "                     </tr>\n" +
        "                      <tr>\n" +
        "                         <td><label class=\"\">New Password</label> </td>\n" +
        "                         <td>\n" +
        "                             <input type=\"password\" value=\"\" id=\"newPwd\" name=\"newPwd\"  class=\"col-lg-3\" />\n" +
        "                         </td>\n" +
        "                      </tr>\n" +
        "                       <tr>\n" +
        "                          <td><label class=\"\">Confirm New Password</label> </td>\n" +
        "                          <td>\n" +
        "                              <input type=\"password\" value=\"\" id=\"confirmNewPwd\" name=\"confirmNewPwd\"  class=\"col-lg-3\" />\n" +
        "                          </td>\n" +
        "                       </tr>\n" +
        "                  </tbody>\n" +
        "               </table>" +
        "                </fieldset>\n" +
        "                <div class=\"control-group\">\n" +
        "                    <div class=\"controls\">\n" +
        "                        <input type=\"button\" onclick=\"validate();\" class=\"btn btn-primary\" value=\"Update\"/>\n" +
        "                        <input type=\"button\" onclick=\"cancel();\" class=\"btn\" value=\"Cancel\"/>\n" +
        "                    </div>\n" +
        "                </div>";
    output = start;
    $("#gadgetBody").empty();
    $("#gadgetBody").append(output);
}

function cancel() {
    gadgets.Hub.publish('org.wso2.is.dashboard', {
        msg:'A message from User profile',
        id:"pwd_change .shrink-widget"
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
    validatePWD();


}

function validatePWD() {
    var valid = true;
    $("form[id='gadgetForm'] input[type='password']").each(function () {
        if ($(this).val().length <= 0) {
            message({content:$(this).attr("name") + ' is required', type:'error', cbk:function () {
            } });
            valid = false;
            return false;
        }
    });
    var pwd = $("input[id='newPwd']").val();
    var retype = $("input[id='confirmNewPwd']").val();
    if (pwd != retype) {
        message({content:'Passwords do not match', type:'error', cbk:function () {
        } });
        valid = false;
    }
    if (valid) {
        submitUpdate();
    }
} 





