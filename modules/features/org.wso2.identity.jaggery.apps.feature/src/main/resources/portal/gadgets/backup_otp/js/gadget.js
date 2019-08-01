var otpList = null;

function drawPage() {
    var output = "";
    var start = "<div class=\"container-fluid\" style=\"width:95%\">\n" +
        "    <div class=\"row\">\n" +
        "        <div class=\"col-lg-12\" style=\"margin-top: 25px; margin-left: 25px; \">\n" +
        "              <table class=\"table\">\n" +
        "                  <thead>\n" +
        "                      <tr>\n" +
        "                          <th colspan=\"2\">Generate and Download List of SMS OTP Backup Codes</th>\n" +
        "                      </tr>\n" +
        "                  </thead>\n" +
        "                  <tbody>\n" +
        "                <input type=\"hidden\" name=\"profile\" value=\"default\" />\n";

    var body = "";

    for (var i in json.return.fieldValues) {
      if (json.return.fieldValues[i].claimUri == "http://wso2.org/claims/otpbackupcodes") {
    	  otpList = json.return.fieldValues[i].fieldValue;
          break;
      }
    }
    
	if(otpList == null){
    	console.error("SMS OTP Claim URI not found in JSON object");
    	cancel();
    }


    var endString ="<tr>\n" +
        "<tr><td colspan=\"2\">" +
        "                        <input type=\"button\" onclick=\"generate();\" class=\"btn btn-primary\" value=\"Generate\"/>\n" +
        "                        <input type=\"button\" onclick=\"cancel();\" class=\"btn\" value=\"Cancel\"/>\n" +
        "                    </td></tr>" +
        "                  </tbody>\n" +
        "</table>"+
        "        </div>\n" +
        "        </div>\n" +
        "    </div>";


    output = start + body + endString;
    $("#gadgetBody").empty();
    $("#gadgetBody").append(output);
}


function cancel() {
    gadgets.Hub.publish('org.wso2.is.dashboard', {
        msg:'A message from SMS OTP Backup Codes',
        id:"backup_otp  .shrink-widget"
    });

}

function download(){
    var downloadElement = $('<a>')
    .attr('href', "data:attachment/text," + encodeURI(otpList))
    .attr('target', '_blank')
    .attr('download', 'otp_backup_codes.txt')
    .text('');
    $('#downloadDiv').append(downloadElement);

    downloadElement[0].click();
}
