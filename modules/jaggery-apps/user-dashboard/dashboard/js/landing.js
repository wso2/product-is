
$(function () {
    $('.register').click(function () {
        document.getElementById('light').style.display='block';
        document.getElementById('fade').style.display='block';
        var serverUrlBase = document.getElementById('serverUrlBase').innerHTML;
        if (serverUrlBase == null) {
            serverUrlBase = '';
        }
        var str = serverUrlBase + "/dashboard/self_registration.jag";

        $.ajax({
            type:"GET",
            url:str

        })
            .done(function (data) {
                json = $.parseJSON(data);
		drawPage(json);

            })
            .fail(function () {
                console.log('error');

            })
            .always(function () {
                console.log('completed');
            });
    } );



$('.login').click(function () {
    window.location=('index.jag');
});
});

function drawPage(json) {
                var output = "";
       var head = "" +
        "    <div class=\"container content-section-wrapper\">\n" +
               "        <div class=\"row\">\n" +
               "            <div class=\"col-lg-12 content-section\">\n" +
               "                <form method=\"post\" class=\"form-horizontal\" id=\"selfReg\" name=\"selfReg\" action=\"controllers/user-registration/add.jag?\" >\n" +
               "";
       var body = "" ;
                                  for (var i in json.pwdRegexps.return) {
               body = body + "                    <input type=\"hidden\" name=\"regExp_"+json.pwdRegexps.return[i].domainName+"\" value=\""+json.pwdRegexps.return[i].regEx+"\" />\n";
                                }
       body = body + "                    <div class=\"control-group\">\n" +
               "                        <label class=\"control-label\" for=\"domain\">Domain Name<span class=\"required\">*</span></label>\n" +
               "                        <div class=\"controls\">\n" +
               "                            <select class=\"col-lg-3\" name=\"domain\">\n" ;
                                    for (var i in json.pwdRegexps.return) {
       body = body +"                                <option value=\""+json.pwdRegexps.return[i].domainName+"\">"+json.pwdRegexps.return[i].domainName+"</option>\n" ;
               }
       body = body +        "                            </select>\n" +
               "                        </div>\n" +
               "                    </div>\n" +
               "                    <div class=\"control-group\">\n" +
               "                        <label class=\"control-label \" for=\"User Name\">User Name<span class=\"required\">*</span></label>\n" +
               "                        <div class=\"controls\">\n" +
               "                            <input class=\"col-lg-3\" type=\"text\" value=\"\" id=\"user_name\" name=\"userName\"  />\n" +
               "                        </div>\n" +
               "                    </div>\n" +
               "                    <div class=\"control-group\">\n" +
               "                        <label class=\"control-label\" for=\"Password\">Password <span class=\"required\">*</span> </label>\n" +
               "                        <div class=\"controls\">\n" +
               "                            <input class=\"col-lg-3\" type=\"password\" value=\"\" id=\"password\" name=\"pwd\"  />\n" +
               "                        </div>\n" +
               "                    </div>\n" +
               "                    <div class=\"control-group\">\n" +
               "                        <label class=\"control-label\" for=\"Retype Password\">Retype Password <span class=\"required\">*</span> </label>\n" +
               "                        <div class=\"controls\">\n" +
               "                            <input class=\"col-lg-3\" type=\"password\" value=\"\" id=\"retype_pwd\" name=\"retypePwd\"  />\n" +
               "                        </div>\n" +
               "                    </div>\n" ;
       
              for (var i in json.fieldValues.return) {  
if (json.fieldValues.return[i].required == "true") {
       body = body +        "                    <div class=\"control-group\">\n" +
               "                        <label class=\"control-label\" for=\""+json.fieldValues.return[i].fieldName+"\">"+json.fieldValues.return[i].fieldName;
                    if (json.fieldValues.return[i].required == "true") {
body = body + " <span class=\"required\">*</span>";
                    if (json.fieldValues.return[i].regEx != "") {
body = body + "      <input type=\"hidden\" name=\"mailRegEx\" value=\""+json.fieldValues.return[i].regEx+"\" />\n"+
"                    <input type=\"hidden\" name=\"mailInput\" value=\""+json.fieldValues.return[i].claimUri+"\" />\n";
}
 
} 

body = body + "</label>\n" +
               "                        <div class=\"controls\">\n" +
               "                            <input class=\"col-lg-3\" type=\"text\" value=\"\" id=\""+json.fieldValues.return[i].fieldName+"\" name=\""+json.fieldValues.return[i].claimUri+"\"  />\n" +
               "                        </div>\n" +
               "                    </div>\n" ;
               } }
       var end = "";

	   end = end +  "                    <div class=\"control-group\">\n" +
               "                        <div class=\"controls\">\n" +
               "                            <input type=\"button\" onclick=\"validate();\" class=\"btn btn-primary\" value=\"Register\"/>\n" +
               "                            <input type=\"button\" onclick=\"cancelProcessToLogin();\" class=\"btn\" value=\"Cancel\"/>\n" +
               "                        </div>\n" +
               "                    </div>\n" +
               "                </form>\n" +
               "            </div>\n" +
               "        </div>\n" +
               "    </div>   " ;
                output = head + body + end;
		$("#light").empty();
                $("#light").append(output);
                	
            }

function cancelProcessToLogin(){
document.getElementById('light').style.display='none';
document.getElementById('fade').style.display='none';

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
    $("#message").empty();
    $("#message").append(element);

  if (validateEmpty("userName").length > 0) {
                    var msg = "User Name is required";
                   message({content:msg,type:'error', cbk:function(){} });
                   return false;
               }

               if (validateEmpty("pwd").length > 0) {
                    var msg = "Password is required";;
                   message({content:msg,type:'error', cbk:function(){} });
                   return false;
               }

               if (validateEmpty("retypePwd").length > 0) {
                    var msg = "Password verification is required";;
                   message({content:'',type:'error', cbk:function(){} });
                   return false;
               }

                var pwd = $("input[name='pwd']").val();
                var retypePwd = $("input[name='retypePwd']").val();

                if(pwd != retypePwd){
                	var msg = "Password does not match";
                   message({content:msg,type:'error', cbk:function(){} });
                   return false;
                }

                var domain = $("select[name='domain']").val();
                var pwdRegex = $("input[name='regExp_"+ domain +"']").val();

                var reg = new RegExp(pwdRegex);
                var valid = reg.test(pwd);
                if (pwd != '' && !valid) {
                    message({content:'Password does not match with password policy',type:'error', cbk:function(){} });
                    return false;
                }



                var unsafeCharPattern = /[<>`\"]/;
                var elements = document.getElementsByTagName("input");
                for(i = 0; i < elements.length; i++){
                    if((elements[i].type === 'text' || elements[i].type === 'password') &&
                       elements[i].value != null && elements[i].value.match(unsafeCharPattern) != null){
                       message({content:'Unsafe input found',type:'error', cbk:function(){} });
                        return false;
                    }
                }

		for(i = 0; i < elements.length; i++){
                    if((elements[i].type === 'text' || elements[i].type === 'password') &&
                       (elements[i].value == null || elements[i].value == "" )){
                       message({content:'Input value should not be empty',type:'error', cbk:function(){} });
                        return false;
                    }
		}

		var mailRegex = $("input[name='mailRegEx']").val();
		var mailInputName = $("input[name='mailInput']").val();
		var mailValue = $("input[name='"+mailInputName+"']").val();
		var regMail = new RegExp(mailRegex);
		var validMail = regMail.test(mailValue);
                if (mailValue != '' && !validMail) {
                    message({content:'Email is not valid ',type:'error', cbk:function(){} });
                    return false;
                }

document.selfReg.submit();

}

