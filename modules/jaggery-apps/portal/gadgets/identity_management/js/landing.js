
$(function () {
    $('.register').click(function (e) {
	e.preventDefault();
	var json = "";
 var str = PROXY_CONTEXT_PATH + "/portal/gadgets/identity_management/idpManager.jag"

        $.ajax({
            type:"GET",
            url:str

        })
            .done(function (data) {

                json = $.parseJSON(data);
if(json.idpNames != null){

        document.getElementById('light').style.display='block';
        document.getElementById('fade').style.display='block';
	drawPage2(json);
}
else{
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
            message({content:'No registered identity providers found !', type:'info', cbk:function () {
            cancel();
            } });
}

            })
            .fail(function () {
                console.log('error');

            })
            .always(function () {
                console.log('completed');
            });
    } );

    } );




function drawPage2(json) {
                var output = "";
       var head = "" +
        "    <div class=\"container content-section-wrapper\">\n" +
               "        <div class=\"row\">\n" +
               "            <div class=\"col-lg-12 content-section\">\n" +
               "                <form method=\"post\" class=\"form-horizontal\" id=\"associateForm\" name=\"selfReg\" action=\"" + PROXY_CONTEXT_PATH + "/portal/gadgets/identity_management/controllers/identity-management/associateID.jag?\" >\n" +
               "";
       var body = "" ;
                             
       body = body + "                  <div><div class=\"control-group\">\n" +
               "                        <div class=\"controls\">\n" +
               "                        <label class=\"control-label inputlabel\" for=\"domain\">IDP ID<span class=\"required\">*</span></label>\n" +
               "                            <select class=\"col-lg-3 inputContent\" name=\"idpID\">\n" ;
if(isArray(json.idpNames)){
                                    for (var i in json.idpNames) {
       body = body +"                                <option value=\""+json.idpNames[i]+"\">"+json.idpNames[i]+"</option>\n" ;
               }
	}
else	{
 	body = body +"                                <option value=\""+json.idpNames+"\">"+json.idpNames+"</option>\n" ;
	}
       body = body +        "                            </select>\n" +
               "                        </div>\n" +
               "                    </div>\n" +
               "                    <div class=\"control-group\">\n" +
               "                        <div class=\"controls\">\n" +
               "                            <label class=\"control-label inputlabel\" for=\"User Name\">User Name<span class=\"required\">*</span></label>\n" +
               "                            <input class=\"col-lg-3 inputContent\" type=\"text\" value=\"\" id=\"user_name\" name=\"associateID\"  />\n" +
               "                        </div></div>\n" ;

       var end = "";

	   end = end +  "                    <div class=\"control-group\" style=\"margin-left: 110px;\">\n" +
               "                        <div class=\"controls\">\n" +
               "                            <input type=\"button\" onclick=\"validate2();\" class=\"btn btn-primary\"  style=\"margin-right: 5px;\" value=\"Register\"/>\n" +
               "                            <input type=\"button\" onclick=\"cancelProcessToLogin();\" class=\"btn\" value=\"Cancel\"/>\n" +
               "                        </div>\n" +
               "                    </div></div>\n" +
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


function validate2() {
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

  if (validateEmpty("associateID").length > 0) {
                    var msg = $("input[name='associateID']").attr("id") + "{{messages.required}}";
                   message({content:msg,type:'error', cbk:function(){} });
                   return false;
               }


associateID();
 

}

function associateID() {
  var str = PROXY_CONTEXT_PATH + "/portal/gadgets/identity_management/controllers/identity-management/associateID.jag";
                $.ajax({
                    url:str,
                    type:"POST",
                    data:$('#associateForm').serialize() + "&profileConfiguration=default" + "&cookie=" + cookie + "&user=" + userName
                })
                        .done(function (data) {
			cancel();
			//message({content:'Successfully saved changes to the profile',type:'info', cbk:function(){} });

                        })
                        .fail(function () {
                        message({content:'Error while updating Profile',type:'error', cbk:function(){} });

                        })
                        .always(function () {
                            console.log('completed');
                        });
}

