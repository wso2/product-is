function getFieldValues(){
    var fieldValues = [
        {fieldValue:'Common Name',required:"true",fieldId:"commonName",val:""},
        {fieldValue:'Department',required:"false",fieldId:"department",val:""},
        {fieldValue:'Organization',required:"false",fieldId:"organization",val:""},
        {fieldValue:'City',required:"false",fieldId:"city",val:""},
        {fieldValue:'State/Province',required:"false",fieldId:"state",val:""},
        {fieldValue:'Country',required:"false",fieldId:"country",val:"" }
    ];
    return fieldValues;
}

function drawCSRGeneratingPage() {
    console.log(json);
    var fieldValues = getFieldValues();
    var output = "";
    var start = "<div class=\"container-fluid\" style=\"width:95%\">\n" +
        "    <div class=\"row\">\n" +
        "        <div class=\"col-lg-12\">\n" +
        "                <div class=\"col-lg-12 content-section\">\n" +
        "                   <h4>Certificate Signing Request Generation</h4><legend></legend></div>"+
        "\n" ;

    var body = "";

    for (var i in fieldValues) {

        body = body + "                <div class=\"control-group\">\n" +
            "                    <label class=\"control-label\">" +fieldValues[i].fieldValue;
        if(fieldValues[i].required=="true") {

            body = body + " <span class=\"required\">*</span>";
        }


        body = body + " </label>\n" +
            "\n" +
            "                    <div class=\"controls\">";
        body = body + "<input type=\"text\"  id=\"" + fieldValues[i].fieldId + "\" name=\"" + fieldValues[i].fieldId + "\"  />";


        body = body + "                    </div>\n" +
            "                </div>";

    }

    body = body+"                <div class=\"control-group\">\n" +
        "                    <label class=\"control-label\">Key Size</label>"+
        "                    <div  class=\"controls\">\n"+
        "                         <select id =\"keySize\"><option value=\"2048\">2048 (Recommended)</option><option value=\"4096\">4096</option></select>"+
        "                    </div>\n"+
    "                    </div>\n" ;

    var endString =
        "                <div class=\"control-group\">\n" +
        "                    <div class=\"controls\">\n" +
        "                        <input type=\"button\" onclick=\"validate();\" class=\"btn btn-primary\" value=\"Send\"/>\n" +
        "                        <input type=\"reset\" class=\"btn btn-primary\" value=\"Clear\"/>\n" +
        "                        <input type=\"button\" onclick=\"cancel();\" class=\"btn\" value=\"Cancel\"/>\n" +
        "                        <input type=\"button\"  class=\"btn btn\" onclick=\"drawOptionPage();\" value=\"Back\"/>\n"+
        "                            <a hidden='hidden' id=cert >Download</a>"+
        "                    </div>\n" +
        "                </div>\n" +
        "            <!--</form>-->\n" +
        "        </div>\n" +
        "        </div>\n" +
        "    </div>";

    output = start + body + endString;
    $("#gadgetBody").empty();
    $("#gadgetBody").append(output);
}

function readKeySize(){
    var keySize = document.getElementById("keySize");
    var keySizeVal = keySize.options[keySize.selectedIndex].value;
    return keySizeVal;
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
    var fieldValues = getFieldValues();

    for (var i in fieldValues) {
        var fldname = fieldValues[i].fieldId;
        var displayName = fieldValues[i].fieldValue;

        if(fieldValues[i].required=="true"){
            if (validateEmpty(fldname).length > 0) {
                message({content: displayName + ' is required', type: 'warning', cbk: function () {
                } });
                return false;

            }
        }
    }
    for (var i in fieldValues) {
        var fldname = fieldValues[i].fieldId;

        fieldValues[i].val = document.getElementById(fldname).value;
    }


    var unsafeCharPattern = /[<>`\"]/;
    var elements = document.getElementsByTagName("input");
    for (i = 0; i < elements.length; i++) {
        if ((elements[i].type === 'text' || elements[i].type === 'password') &&
            elements[i].value != null && elements[i].value.match(unsafeCharPattern) != null) {
            message({content:'Unauthorized characters are specified', type:'warning', cbk:function () {
            } });
            return false;
        }
    }
    sendCSRToAdmin(fieldValues[0].val,fieldValues[1].val,fieldValues[2].val,fieldValues[3].val,
        fieldValues[4].val,fieldValues[5].val, readKeySize());
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

function displayCSR(csrDetails) {
    var csrDetailArray = csrDetails
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

    if(csrDetailArray.length >0){
        message({content:'No Certificate Signing Requests Found!', type:'ok', cbk:function () {
            cancel();
        } });
    }

    console.log(json);
    var output = "";
    var start =
        "        <div class=\"col-lg-12 content-section\">\n<h4>Certificate Signing Request Details</h4><legend></legend>" +
        "\n" ;


    var body ="";

    body = body +
        "                <div class=\"control-group\">\n" +
        "                    <label class=\"control-label\">Serial Number</label>\n" +
        "                    <div class=\"controls\">\n"+
        "                        <input type=\"text\" disabled=\"\"  value=\""+csrDetailArray.serialNo+"\" id=\"serialNo\" name=\"serialNo\"/>\n"+
        "                    </div>\n" +
        "                </div>\n"+
        "                <div class=\"control-group\">\n" +
        "                    <label class=\"control-label\">Status</label>\n" +
        "                    <div class=\"controls\">\n"+
        "                        <input type=\"text\" disabled=\"\"  value=\""+csrDetailArray.status+"\" id=\"serialNo\" name=\"serialNo\"/>\n";


    body = body +
        "                    </div>\n" +
        "                </div>\n"+
        "                <div class=\"control-group\">\n" +
        "                    <label class=\"control-label\">Common Name</label>\n" +
        "                    <div class=\"controls\">\n"+
        "                        <input type=\"text\" disabled=\"\"  value=\""+csrDetailArray.commonName+"\" id=\"serialNo\" name=\"serialNo\"/>\n"+
        "                    </div>\n" +
        "                </div>\n";
    if(csrDetailArray.department != "") {
        body = body +
            "                <div class=\"control-group\">\n" +
            "                    <label class=\"control-label\">Department</label>\n" +
            "                    <div class=\"controls\">\n" +
            "                        <input type=\"text\" disabled=\"\"  value=\"" + csrDetailArray.department + "\" id=\"serialNo\" name=\"serialNo\"/>\n" +
            "                    </div>\n" +
            "                </div>\n";
    }
    if(csrDetailArray.organization != "") {
        body = body +

            "                <div class=\"control-group\">\n" +
            "                    <label class=\"control-label\">Organization</label>\n" +
            "                    <div class=\"controls\">\n" +
            "                        <input type=\"text\" disabled=\"\"  value=\"" + csrDetailArray.organization + "\" id=\"serialNo\" name=\"serialNo\"/>\n" +
            "                    </div>\n" +
            "                </div>\n";
    }
    if(csrDetailArray.city != "") {
        body = body +
            "                <div class=\"control-group\">\n" +
            "                    <label class=\"control-label\">City</label>\n" +
            "                    <div class=\"controls\">\n" +
            "                        <input type=\"text\" disabled=\"\"  value=\"" + csrDetailArray.city + "\" id=\"serialNo\" name=\"serialNo\"/>\n" +
            "                    </div>\n" +
            "                </div>\n";
    }
    if(csrDetailArray.state != "") {
        body = body +
            "                <div class=\"control-group\">\n" +
            "                    <label class=\"control-label\">State/Province</label>\n" +
            "                    <div class=\"controls\">\n" +
            "                        <input type=\"text\" disabled=\"\"  value=\"" + csrDetailArray.state + "\" id=\"serialNo\" name=\"serialNo\"/>\n" +
            "                    </div>\n" +
            "                </div>\n";
    }
    if(csrDetailArray.state != "") {
        body = body +
            "                <div class=\"control-group\">\n" +
            "                    <label class=\"control-label\">Country</label>\n" +
            "                    <div class=\"controls\">\n" +
            "                        <input type=\"text\" disabled=\"\"  value=\"" + csrDetailArray.country + "\" id=\"serialNo\" name=\"serialNo\"/>\n" +
            "                    </div>\n" +
            "                </div>\n";
    }
    body = body +
        "                <div class=\"control-group\">\n" +
        "                    <label class=\"control-label\">Requested Date</label>\n" +
        "                    <div style='border: transparent;background-color: transparent' class=\"controls\">\n"+
        "                        <input type=\"text\" disabled=\"\" value=\""+csrDetailArray.reqestedDate+"\" id=\"serialNo\" name=\"serialNo\"/>\n"+
        "                    </div>\n" +
        "                </div>\n"+

        "                <div class=\"control-group\">\n" +
        "                    <label class=\"control-label\">CSR Request</label>\n" +
        "                    <div  class=\"controls\">\n"+
        "                         <textarea style=\"width: 60%; height: 40%; border: none;\" readonly id =\"csrRequest\" name=\"csrRequest\" >"+
        csrDetailArray.csrRequest+
        "                         </textarea>" +
        "                    </div>\n" +
        "                </div>\n";

    var endString =
        "                <div class=\"control-group\">\n" +
        "                    <div class=\"controls\">\n" +
        "                        <input type=\"button\"  class=\"btn btn-primary\" onclick=\"download(readSerialNo(),'.csr',readCSR());\" value=\"Download CSR\"/>\n";

    if(csrDetailArray.status == 'SIGNED'){
        endString = endString+
            "                        <input type=\"button\"  class=\"btn btn-primary\" onclick=\"getCert(readSerialNo(),'true');\" value=\"Download Certificate\"/>\n" ;
    }
    endString = endString+
        "                        <input type=\"button\"  class=\"btn btn\" onclick=\"getList();\" value=\"Back\"/>\n" +
        "                            <a hidden='hidden' id=cert >Download</a>"+
        "                    </div>\n" +
        "                </div>\n" +

        "    </div>";

    output = start + body + endString;
    $("#gadgetBody").empty();
    $("#gadgetBody").append(output);
}




function displayCertificate(certDetails) {
    var certDetailArray = certDetails
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

    if(certDetailArray.length >0){
        message({content:'No Certificates Found!', type:'ok', cbk:function () {
            cancel();
        } });
    }

    console.log(json);
    var output = "";
    var start = "<div class=\"container-fluid\" style=\"width:95%\">\n" +
        "        <div class=\"col-lg-12 content-section\">\n<h4>Certificate Details</h4><legend></legend>"+
        "\n" ;


    var body ="";

    body = body +
        "                <div class=\"control-group\">\n" +
        "                    <label class=\"control-label\">Serial Number</label>\n" +
        "                    <div class=\"controls\">\n"+
        "                        <input type=\"text\" disabled=\"\"  value=\""+certDetails.serialNo+"\" id=\"serialNo\" name=\"serialNo\"/>\n"+
        "                    </div>\n" +
        "                </div>\n"+
        "                <div class=\"control-group\">\n" +
        "                    <label class=\"control-label\">Status</label>\n" +
        "                    <div class=\"controls\">\n"+
        "                        <input type=\"text\" disabled=\"\"  value=\""+certDetails.status+"\" id=\"status\" name=\"status\"/>\n"+
        "                    </div>\n" +
        "                </div>\n"+
        "                <div class=\"control-group\">\n" +
        "                    <label class=\"control-label\">Issued Date</label>\n" +
        "                    <div class=\"controls\">\n"+
        "                        <input type=\"text\" disabled=\"\"  value=\""+certDetails.issuedDate+"\" id=\"issuedDate\" name=\"issuedDate\"/>\n"+
        "                    </div>\n" +
        "                </div>\n"+
        "                <div class=\"control-group\">\n" +
        "                    <label class=\"control-label\">Expiary Date</label>\n" +
        "                    <div class=\"controls\">\n"+
        "                        <input type=\"text\" disabled=\"\"  value=\""+certDetails.expiaryDate+"\" id=\"expiaryDate\" name=\"expiaryDate\"/>\n"+
        "                    </div>\n" +
        "                </div>\n"+
        "                         <textarea style=\"visibility: hidden;width: 0px;height:1%\" id =\"publicCertificate\" name=\"publicCertificate\" >"+
        certDetails.publicCertificate+
        "                         </textarea>" ;



    var endString =   "                <div class=\"control-group\">\n" +
        "                    <label class=\"control-label\">Revoke</label>\n" +
        "                    <div  class=\"controls\">\n"+
        "<select><option value=\"volvo\">Key Compromise</option><option value=\"saab\">Affiliation Changed</option><option value=\"mercedes\">Certificate Hold</option><option value=\"audi\">Privileges Withdrawn</option></select>"+
        "                        <input type=\"button\"  class=\"btn btn-primary\"   onclick=\"\"  value=\"Revoke\"/>\n" +
        "                    </div>\n" +
        "                </div>\n"+
        "                <div class=\"control-group\">\n" +
        "                    <div class=\"controls\">\n" +
        "                            <a hidden='hidden' id=cert >Download</a>"+
        "                        <input type=\"button\"  class=\"btn btn-primary\"   onclick=\"download(readSerialNo(),'.crt',readCert());\"  value=\"Download Certificate\"/>\n" +
        "                        <input type=\"button\"  class=\"btn btn\" onclick=\"getList();\" value=\"Back\"/>\n" +

        "                    </div>\n" +
        "                </div>\n" +
        "    </div>";

    output = start + body + endString;
    $("#gadgetBody").empty();
    $("#gadgetBody").append(output);
}

function drawOptionPage(){
    var output = "";
    var start = "";

    start = start+" <legend></legend>"+
        "           <table  style='border:transparent;width: 100%;height: 40%'>\n" +
        "               <td></td>"+
        "               <td width='25%' height='35%'>\n" +
        "                   <a  href=\"#\" style='height: 100%;border: ;text-decoration: none;' class='thumbnail' onclick=\"uploadCSR();\" >" +
        "<br>"+
        "                       <img src=\"/portal/gadgets/certificate_authority/js/ui/img/upload-csr.png\"  width=\"170\" height=\"170\">"+
        "                       <h4  style='text-align: center'>Upload Certificate Sign Request</h4>"+
        "                   </a>" +
        "               </td >\n" +
        "               <td></td>"+
        "               <td width='25%' height='40%'>\n" +
        "                   <a  href=\"#\"  style='height: 100%;text-decoration: none;' class='thumbnail' onclick=\"drawCSRGeneratingPage();\" >" +
        "<br>"+
        "                       <img src=\"/portal/gadgets/certificate_authority/js/ui/img/add-csr.png\" alt=\"some_text\" width=\"170\" height=\"170\">"+
        "                       <h4  style='text-align: center'>Generate Certificate Signing Request</h4>"+
        "                   </a>" +
        "               </td >\n" +
        "               <td></td>"+
        "               <td width='25%' height='40%'>\n" +

        "                   <a href=\"#\"  style='height: 100%;text-decoration: none;' class='thumbnail'  onclick=\"getList();\" >" +
        "<br>"+
        "                       <img src=\"/portal/gadgets/certificate_authority/js/ui/img/manage.png\" alt=\"some_text\" width=\"170\" height=\"170\">"+
        "                       <h4  style='text-align: center'>Manage Certificates</h4>"+
        "                   </a>" +
        "               </td >\n" +
        "               <td></td>"+
        "          </table>";



    output = start;
    $("#gadgetBody").empty();
    $("#gadgetBody").append(output);
}

function uploadCSR(){
    console.log(json);
    var output = "";
    var start =

        "\n" ;

    var body =
        "                <div class=\"col-lg-12 content-section\">\n" +
        "                   <h4>Upload CSR File</h4><legend></legend>"+

        "                           <input style='background-color: #d3d3d3' class=\"btn\"  type=file id=files />"+
        "                           <input id =\"upload\" type=\"button\"  class=\"btn btn-primary\" onclick=\"uploadCSRFromFile();\" value=\"Upload\"/>\n" +

        "                </div>"+
        "                <div class=\"col-lg-12 content-section\">\n" +
        "                         <div>\n" +
        "                           <textarea style=\"width: 50%; height: 55%; border:ridge;display: inline-block;\"  id =\"csrReq\" name=\"csrReq\" >"+
        "                         </textarea>" +
        "                         </div>"+
        "                </div>";

    var endString =
        "                <div class=\"col-lg-12 content-section\">\n" +
        "                    <div>\n" +
        "                        <input type=\"button\"  class=\"btn btn-primary\" onclick=\"sendUploadedCSRToAdmin(readUploadedCSRVal());\" value=\"Send\"/>\n" +
        "                        <input type=\"button\"  class=\"btn\" onclick=\"cancel();\" value=\"Cancel\"/>\n" +
        "                        <input type=\"button\"  class=\"btn btn\" onclick=\"drawOptionPage();\" value=\"Back\"/>\n"+
        "                    </div>\n" +
        "                </div>\n" +

        "   ";

    output = start + body + endString;
    $("#gadgetBody").empty();
    $("#gadgetBody").append(output);
}

function uploadCSRFromFile() {

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

    var fileInput = $('#files');
    if (!window.FileReader) {
        message({content:'Your browser is not supported', type:'error', cbk:function () {
        } });
    }
    var input = fileInput.get(0);

    var reader = new FileReader();
    if (input.files.length) {
        var textFile = input.files[0];
        reader.readAsText(textFile);
        $(reader).on('load', processFile);
    } else {
        message({content:'Please upload a file before continuing', type:'error', cbk:function () {
        } });
    }

    function processFile(e) {
        var file = e.target.result,
            results;
        if (file && file.length) {
            results = file;
            console.log(results)
            $('#csrReq').val(results);
        }
    }
}

function drawCSRSListPage(csrListArrayObj){




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

    if(csrListArrayObj == null){
        message({content:'No Certificate Signing Requests Found!', type:'ok', cbk:function () {
            drawOptionPage();
        } });

    }

    var csrListArray = [];
    if( Object.prototype.toString.call( csrListArrayObj ) === '[object Array]' ) {
        csrListArray=csrListArrayObj;
    }else{
        csrListArray.push(csrListArrayObj);
    }

    var output = "";
    var start = " <div class=\"col-lg-12 content-section\">" +

        "                   <h4>Certificate Signing Requests List</h4><legend></legend>"+
        "<br>"+

        "<fieldset>" +
        "              <table id=\"csr_details\" class=\"table table-striped table-bordered\" style='width: 85%'>\n" +
        "                  <thead>\n" +
        "                      <tr>\n" +
        "                          <th >CSR Serial Number</th>\n" +
        "                          <th>Common Name</th>\n" +
        "                          <th>Status</th>\n" +
        "                          <th >Actions</th>\n" +
        "                      </tr>\n" +
        "                  </thead>\n" ;

    var body = "";
    body = "                  <tbody>\n";


    for(var i in csrListArray) {
        var serialNo = csrListArray[i].serialNo;
        body = body+"                     <tr>\n" +
            "                        <td  style='width: 25%'\n>" +
            "                             <label class=\"\">"+csrListArray[i].serialNo+" </label> \n" +
            "                        </td>\n" +
            "                        <td  style='width: 25%'\n>" +
            "                             <label class=\"\">"+csrListArray[i].commonName+" </label> \n" +
            "                        </td>\n" +
            "                        <td  style='width: 25%'\n>" +
            "                             <label class=\"\">"+csrListArray[i].status+" </label> \n" +
            "                        </td>\n"+
            "                        <td\n>" +
            "                             <input type=\"button\" class=\"btn btn-primary\" onclick=\"getCSRFromSerialNo("+csrListArray[i].serialNo+");\"  value=\"View CSR\"/>\n" ;
        if(csrListArray[i].status == 'SIGNED'){
            body = body+  "                             <input type=\"button\" class=\"btn btn-primary\" onclick=\"getCert("+csrListArray[i].serialNo+",'false');\"  value=\"View Certificate\"/>\n" ;
        }
        body = body+ "                        </td>\n";
    }


    body = body+"                  </tbody>\n" ;
    var end = "";
    end ="               </table>\n" +
        "                </fieldset>\n";
    end = end+
        "                        <input style='float: inherit' type=\"button\"  class=\"btn btn\" onclick=\"drawOptionPage();\" value=\"Back\"/>\n" ;

    output = start+body+end;


    $("#gadgetBody").empty();
    $("#gadgetBody").append(output);
}


function cancel() {
    gadgets.Hub.publish('org.wso2.is.dashboard', {
        msg:'A message from Certificate Authority',
        id:"certificate_authority  .shrink-widget"
    });

}


function readUploadedCSRVal(){

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

    var csr =  $('#csrReq').val();
    return csr;
}

function readCSR(){
    var csr =  $('#csrRequest').val();
    return csr;
}

function readSerialNo(){
    var sn =  $('#serialNo').val();
    return sn;
}
function readCert(){
    var sn =  $('#publicCertificate').val();
    return sn;
}
function downloadCert(filenamePrefix,extension,content){

    var filename = filenamePrefix+extension;
    cert.href="data:text/plain,"+encodeURIComponent(content);
    cert.download=filename;
    cert.click();
}




