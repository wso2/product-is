

function getReceiptDetails(reciptID) {

    if (cookie != null) {
        var str = PROXY_CONTEXT_PATH + "/portal/gadgets/consent_management/receipt.jag";
        var consentJSON;

        $.ajax({
            type:"POST",
            url:str,
            data: {cookie : cookie, user : userName, id : reciptID}

        })
            .done(function (data) {
                consentJSON = $.parseJSON(data);
                renderReceiptDetails(consentJSON);

            })
            .fail(function () {
                console.log('error');

            })
            .always(function () {
                console.log('completed');
            });
    }
}

function renderReceiptList(data) {
    var receiptData = {receipts: data.data};
    //debugger;
    var content = '{{#receipts}}<div class="panel panel-default panel-consents">' +
        '<div class="panel-body flex-container">' +
        '<div class="left">' +
        '<h4>{{#if spDisplayName}}{{spDisplayName}} <span>({{consentReceiptID}})</span>{{else}}{{consentReceiptID}}{{/if}}</h4>' +
        '<p>{{spDescription}}</p>' +
        '</div>' +
        '<div class="right">' +
        '<div class="btn-group" role="group" aria-label="actions">' +
        '<button type="button" class="btn btn-default btn-settings" data-id="{{consentReceiptID}}"><span class="icon-cog icon-font-size"></span></button>' +
        '<button type="button" class="btn btn-danger btn-revoke">Revoke</button></div>' +
        '</div>' +
        '</div>' +
        '</div>{{/receipts}}';

    var theTemplate = Handlebars.compile(content);
    var html = theTemplate(receiptData);

    $("#consent-settings").hide();
    $("#consent-listing").empty().show();
    $("#consent-listing").append(html);
    addActions();
}

function renderReceiptDetails(data) {
    var receiptData = {receipts: data.data};
    //debugger;
    var content = '{{#receipts}}{{#services}}<div class="panel panel-default panel-consents">' +
        '<div class="panel-heading">' +
        '<button type="button" class="close btn-cancel-settings" data-target="#cancel" data-dismiss="alert">' +
        '<span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>' +
        '<h3 class="panel-title">{{#if serviceDisplayName}}{{serviceDisplayName}}{{else}}{{service}}{{/if}}</h3>' +
        '<p>{{serviceDescription}}</p>' +
        '</div>' +
        '<div class="panel-body no-padding">' +
        '<div class="more-info">' +
        '<p>More about this service: </p>' +
        '<ul>' +
        '<li><strong>Collection Method : </strong>{{../collectionMethod}}</li>' +
        '<li><strong>Version : </strong>{{../version}}</li>' +
        '</ul>' +
        '</div>' +
        '<div id="tree-table"></div>' +
        '<div class="panel-footer text-right">' +
        '<button type="button" class="btn btn-default btn-cancel-settings">Back</button>' +
        '</div>' +
        '</div>{{/services}}{{/receipts}}';


    var theTemplate = Handlebars.compile(content);
    var html = theTemplate(receiptData);

    $("#consent-listing").hide();
    $("#consent-settings").empty().show();
    $("#consent-settings").append(html);
    addActions();

    var treeTemplate =
        '<div id="html1">' +
        '<ul><li class="jstree-open" data-jstree=\'{"icon":"icon-book"}\'>All' +
        '<ul>' +
        '{{#receipts}}{{#services}}{{#purposes}}{{#piiCategory}}' +
        '<li data-jstree=\'{"icon":"icon-book"}\'>{{../purpose}}<ul>' +
        '' +
        '<li data-jstree=\'{"icon":"icon-user"}\'>{{piiCategoryName}}</li>' +
        '' +
        '</ul></li>' +
        '</li>' +
        '{{/piiCategory}}{{/purposes}}{{/services}}{{/receipts}}' +
        '</ul></li>' +
        '</ul>' +
        '</div>';

    var tree = Handlebars.compile(treeTemplate);
    var treeRendered = tree(receiptData);

    $("#tree-table").html(treeRendered);

    var container = $("#html1").jstree({
        plugins: ["table", "sort", "checkbox", "actions", "wholerow"],
        checkbox: { "keep_selected_style" : false },
    });

    container.jstree("check_all");

}


function addActions(){

    $(".btn-settings").click(function(){
        var receiptID = $(this).data("id");
        getReceiptDetails(receiptID);
    });
    $(".btn-revoke").click(function(){
        console.log("Revoke Receipt");
    });
    $(".btn-cancel-settings").click(function(){
        renderReceiptList(json);
    });
}
