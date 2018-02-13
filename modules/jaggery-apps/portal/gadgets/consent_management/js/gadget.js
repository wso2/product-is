const EXPIRY_DATE_STRING = "VALID_UNTIL:";
var receiptData; //populated with initial JSON payload

/*
* Get receipt details for the given receipt ID and renders receipt details
*/
function getReceiptDetails(receiptID) {
    if (cookie != null) {
        var str = PROXY_CONTEXT_PATH + "/portal/gadgets/consent_management/receipt.jag";
        var consentJSON;

        $.ajax({
            type: "POST",
            url: str,
            data: {cookie: cookie, user: userName, id: receiptID}

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

/*
 * Revokes a receipt for a given receipt ID
 */
function revokeReceipt(receiptID) {
    if (cookie != null) {
        var str = PROXY_CONTEXT_PATH + "/portal/gadgets/consent_management/revoke_receipt.jag";

        $.ajax({
            type: "POST",
            url: str,
            data: {cookie: cookie, user: userName, id: receiptID}

        })
            .done(function (data) {
                location.reload();
            })
            .fail(function () {
                console.log('error');

            })
            .always(function () {
                console.log('completed');
            });
    }
}

/*
 * Updates a receipt by it's receipt ID, By posting changed receipt data
 */
function updateReceipt(receiptData) {
    if (cookie != null) {
        var str = PROXY_CONTEXT_PATH + "/portal/gadgets/consent_management/update_receipt.jag";
        $.ajax({
            type: "POST",
            url: str,
            data: {cookie: cookie, user: userName, receiptData: JSON.stringify(receiptData)}

        })
            .done(function (data) {
                location.reload();
            })
            .fail(function () {
                console.log('error');

            })
            .always(function () {
                console.log('completed');
            });
    }
}

/*
 * Renders the receipt list to html
 */
function renderReceiptList(data) {
    var receiptData = {receipts: data.data};
    var content = '{{#receipts}}<div class="panel panel-default panel-consents">' +
        '<div class="panel-body flex-container">' +
        '<div class="left">' +
        '<h4>{{#if spDisplayName}}{{spDisplayName}} <span>({{consentReceiptID}})</span>{{else}}{{consentReceiptID}}{{/if}}</h4>' +
        '<p>{{spDescription}}</p>' +
        '</div>' +
        '<div class="right">' +
        '<div class="btn-group" role="group" aria-label="actions">' +
        '<button type="button" class="btn btn-primary btn-settings" data-id="{{consentReceiptID}}"><span class="icon-cog icon-font-size"></span></button>' +
        '<button type="button" class="btn btn-default btn-revoke">Revoke</button></div>' +
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

/*
 * Renders the receipt details to html.
 * Appends purposes and purpose categories to the rendered.
 * initiates jstree.
 */
function renderReceiptDetails(data) {
    receiptData = {receipts: data.data};
    var content = '{{#receipts}}{{#services}}<div class="panel panel-default panel-consents">' +
        '<div class="panel-heading">' +
        '<button type="button" class="close btn-cancel-settings" data-target="#cancel" data-dismiss="alert">' +
        '<span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>' +
        '<h3 class="panel-title">{{#if serviceDisplayName}}{{serviceDisplayName}}{{else}}{{service}}{{/if}}</h3>' +
        '<p>{{serviceDescription}}</p>' +
        '</div>' +
        '<div class="panel-body no-padding">' +
        '<div class="more-info">' +
        '<p>More about this service: <span class="pull-right datepicker">' +
        '<label>Valid Until</label>' +
        '<input type="text" name="date_picker" id="date_picker" value="{{extractDate purposes.1.termination}}"/>' +
        '<input type="hidden" name="date_picker_old_expiry" id="date_picker_old_expiry" value="{{purposes.1.termination}}"/>' +
        '<input type="hidden" name="date_picker_new_expiry" id="date_picker_new_expiry" value="{{purposes.1.termination}}"/>' +
        '<button type="button" class="ui-datepicker-reset action-reset" title="Reset Date"><i class="icon-undo"></i></button>' +
        '</span>' +
        '</p>' +
        '<ul>' +
        '<li><strong>Collection Method : </strong>{{../collectionMethod}}</li>' +
        '<li><strong>Version : </strong>{{../version}}</li>' +
        '</ul>' +
        '</div>' +
        '<div class="tree-table-container">' +
        '<p>Deselect consents that you wish to revoke</p>' +
        '<div id="tree-table"></div>' +
        '</div>' +
        '<div class="panel-footer text-right">' +
        '<button type="button" class="btn btn-primary btn-update-settings">Update</button>' +
        '<button type="button" class="btn btn-default btn-cancel-settings">Cancel</button>' +
        '</div>' +
        '</div>{{/services}}{{/receipts}}';

    var theTemplate = Handlebars.compile(content);

    Handlebars.registerHelper('extractDate', function (title) {
        return constructDate(title);
    });
    var html = theTemplate(receiptData);

    $("#consent-listing").hide();
    $("#consent-settings").empty().show();
    $("#consent-settings").append(html);

    var treeTemplate =
        '<div id="html1">' +
        '<ul>' +
        '<li class="jstree-open" data-jstree=\'{"icon":"icon-book"}\'>All' +
        '<ul>' +
        '{{#receipts}}{{#services}}' +
        '{{#purposes}}<li data-jstree=\'{"icon":"icon-book"}\' purposeid="{{purposeId}}">{{purpose}}' +
        '<ul>' +
        '{{#piiCategory}}<li data-jstree=\'{"icon":"icon-user"}\' piicategoryid="{{piiCategoryId}}">{{piiCategoryName}}</li>{{/piiCategory}}' +
        '</ul>' +
        '</li>' +
        '</li>{{/purposes}}' +
        '{{/services}}{{/receipts}}' +
        '</ul>' +
        '</ul>' +
        '</div>';

    var tree = Handlebars.compile(treeTemplate);
    var treeRendered = tree(receiptData);

    $("#tree-table").html(treeRendered);

    var container = $("#html1").jstree({
        plugins: ["table", "sort", "checkbox", "actions", "wholerow"],
        checkbox: {"keep_selected_style": false},
    });

    container.jstree("check_all");
    addActions(container);
}

/*
 * Binds all click event handlers
 */
function addActions(container) {

    $(".btn-settings").click(function () {
        var receiptID = $(this).data("id");
        getReceiptDetails(receiptID);
    });
    $(".btn-revoke").click(function () {
        var receiptID = $(this).prev().data("id");
        var responseText = confirm("Are you sure you want to revoke this consent? this is not reversible...");

        if (responseText == true) {
            revokeReceipt(receiptID);
        }
    });
    $(".btn-cancel-settings").click(function () {
        renderReceiptList(json);
    });
    $(".btn-update-settings").click(function () {
        var responseText = confirm("Are you sure you want to update/revoke this consent? this is not reversible...");

        if (responseText == true) {
            revokeAndAddNewReceipt(receiptData, container);
        }

    });
    var today = new Date();
    $("#date_picker").datepicker({
        showOn: "button",
        buttonImageOnly: false,
        buttonText: '<i class="icon-calendar action-calendar"></i>',
        minDate: today,
        changeMonth: true,
        changeYear: true,
        onSelect: function (dateText) {
            var split = dateText.split("/");
            var newDate = Date.UTC(split[2], split[0], split[1]);
            var expiry = EXPIRY_DATE_STRING + newDate;

            $("#date_picker_new_expiry").val(expiry);

        }
    });
    $(".ui-datepicker-reset").click(function () {
        var old_val = $("#date_picker_old_expiry").val();
        $("#date_picker").val(constructDate(old_val));
        $("#date_picker_new_expiry").val(old_val);

    });


}

/*
 * Pushes changed purposes and purpose categories.
 * changes can occur on tree change or date picker date change.
 */
function populateNewPurposes(purposes, oldPurposes, expiryDate, newPurposes) {
    for (var i = 0; i < purposes.length; i++) {
        var purpose = purposes[i];
        var newPurpose = {};
        newPurpose["purposeId"] = purpose.li_attr.purposeid;
        var oldPurpose = filterPurpose(oldPurposes, purpose.li_attr.purposeid);
        newPurpose = oldPurpose[0];
        newPurpose['piiCategory'] = [];
        newPurpose['purposeCategoryId'] = [1];
        newPurpose['termination'] = expiryDate;
        delete(newPurpose['purpose']);
        delete(newPurpose['purposeCategory']);

        var piiCategory = [];
        var categories = purpose.children;
        for (var j = 0; j < categories.length; j++) {
            var category = categories[j];
            var c = {};
            c['piiCategoryId'] = category.li_attr.piicategoryid;
            piiCategory.push(c);
        }
        newPurpose['piiCategory'] = piiCategory;
        newPurposes.push(newPurpose);
    }
}

/*
 * Updates the receipt with new receipt data
 */
function revokeAndAddNewReceipt(receiptData, container) {
    var oldReceipt = receiptData.receipts;
    var newReceipt = {};
    var services = [];
    var service = {};

    var expiryDate = $("#date_picker_new_expiry").val();

    var selectedNodes = container.jstree(true).get_selected('full', true);
    var undeterminedNodes = container.jstree(true).get_undetermined('full', true);

    if (!selectedNodes || selectedNodes.length < 1) {
        revokeReceipt(oldReceipt.consentReceiptID);
        return;
    }
    selectedNodes = selectedNodes.concat(undeterminedNodes);

    //Populate from existing receipt data
    newReceipt['jurisdiction'] = oldReceipt.jurisdiction;
    newReceipt['collectionMethod'] = oldReceipt.collectionMethod;
    newReceipt['policyURL'] = oldReceipt.policyUrl;
    newReceipt['piiPrincipalId'] = oldReceipt.piiPrincipalId;
    newReceipt['language'] = oldReceipt.language;

    service['service'] = oldReceipt.services[0].service;
    service['serviceDescription'] = oldReceipt.services[0].serviceDescription;
    service['serviceDisplayName'] = oldReceipt.services[0].serviceDisplayName;
    service['tenantDomain'] = oldReceipt.services[0].tenantDomain;

    var oldPurposes = oldReceipt.services[0].purposes;
    var relationshipTree = unflatten(selectedNodes); //Build relationship tree
    var purposes = relationshipTree[0].children;
    var newPurposes = [];
    populateNewPurposes(purposes, oldPurposes, expiryDate, newPurposes);
    service['purposes'] = newPurposes;
    services.push(service);
    newReceipt['services'] = services;

    updateReceipt(newReceipt);
}

/*
 * returns a relationship tree when checked nodes and children are passed
 */
function unflatten(arr) {
    var tree = [],
        mappedArr = {},
        arrElem,
        mappedElem;

    // First map the nodes of the array to an object -> create a hash table.
    for (var i = 0, len = arr.length; i < len; i++) {
        arrElem = arr[i];
        mappedArr[arrElem.id] = arrElem;
        mappedArr[arrElem.id]['children'] = [];
    }

    for (var id in mappedArr) {
        if (mappedArr.hasOwnProperty(id)) {
            mappedElem = mappedArr[id];
            // If the element is not at the root level, add it to its parent array of children.
            if (mappedElem.parent && mappedElem.parent != "#" && mappedArr[mappedElem['parent']]) {
                mappedArr[mappedElem['parent']]['children'].push(mappedElem);
            }
            // If the element is at the root level, add it to first level elements array.
            else {
                tree.push(mappedElem);
            }
        }
    }
    return tree;
}

/*
 * returns objects that matches purpose id
 */
function filterPurpose(purposes, id) {
    return purposes.filter(function (obj) {
        if (obj.purposeId == id) {
            return obj;
        }
        ;
    });
}

/*
 * validate whether the passed is a date object or not
 */
function checkValidDate(dateObj) {
    if (Object.prototype.toString.call(dateObj) === "[object Date]") {
        // it is a date
        if (isNaN(dateObj.getTime())) {
            return false;
        }
        else {
            return true;
        }
    }
    else {
        return false;
    }
}

/*
 * construct a datepicker friendly date from the expiry value
 */
function constructDate(expiry) {
    if (!expiry || expiry.length < 1) {
        return getDefaultExpiry();
    }
    var t = expiry.split(EXPIRY_DATE_STRING);
    if (t.length < 2) {
        return getDefaultExpiry();
    }
    var date = new Date(parseInt(t[1]));
    if (!checkValidDate(date)) {
        return getDefaultExpiry();
    }
    var goodDate = date.getMonth() + "/" + date.getDate() + "/" + date.getFullYear();
    return goodDate;
}

/*
 * change return value to set datepicker default
 */
function getDefaultExpiry() {
    return "Forever";
}
