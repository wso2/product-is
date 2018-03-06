/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

const EXPIRY_DATE_STRING = "VALID_UNTIL:";
var receiptData; //populated with initial JSON payload
var confirmationDialog = "<div class=\"modal fade\" id=\"messageModal\">\n" +
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
            .fail(function (error) {
                publishErrorAndShrink(error);
            })
            .always(function () {
                console.log('completed');
            });
    }
}

function getResidentIDPReceiptDetails(receiptID) {
    if (cookie != null) {
        var str = PROXY_CONTEXT_PATH + "/portal/gadgets/consent_management/default_receipt.jag";
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
            .fail(function (error) {
                publishErrorAndShrink(error);
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
            .fail(function (error) {
                publishErrorAndShrink(error);
            })
            .always(function () {
                console.log('completed');
            });
    }
}

function publishErrorAndShrink(error) {
    console.log(error);
    gadgets.Hub.publish('org.wso2.is.dashboard', {
        msg: 'A message from Consent Management',
        id: "consent_management .shrink-widget",
        status: error.status
    });
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
            .fail(function (error) {
                publishErrorAndShrink(error);
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
    var receiptData = data;
    var content = '<div id="default-receipt"><h3>System Consent</h3>{{#default}}<div class="panel' +
        ' panel-default' +
        ' panel-consents">' +
        '<div class="panel-body flex-container">' +
        '<div class="left">' +
        '<h4>{{spDisplayName}}</h4>' +
        '<p><span>Receipt Id: {{consentReceiptID}}</span></p><p>"{{spDescription}}"</span></p>' +
        '</div>' +
        '<div class="right">' +
        '<div class="btn-group" role="group" aria-label="actions">' +
        '<button type="button" class="btn btn-primary btn-default-settings" data-toggle="tooltip" data-placement="top" title="Settings" data-id="{{consentReceiptID}}"><span' +
        ' class="icon-cog icon-font-size"></span></button>' +
        '{{#if consentReceiptID}}<button type="button" class="btn btn-default' +
        ' btn-revoke">Revoke</button></div>{{/if}}' +
        '</div>' +
        '</div>' +
        '</div>{{/default}}</div>' +
        '{{#if receipts.length}}<div id="receipts"><h3>Consents</h3>{{#receipts}}<div class="panel panel-default' +
        ' panel-consents">' +
        '<div class="panel-body flex-container">' +
        '<div class="left">' +
        '<h4>{{spDisplayName}}</h4>' +
        '<p><span>Receipt Id: {{consentReceiptID}}</span></p><p>"{{spDescription}}"</span></p>' +
        '</div>' +
        '<div class="right">' +
        '<div class="btn-group" role="group" aria-label="actions">' +
        '<button type="button" class="btn btn-primary btn-settings" data-toggle="tooltip" data-placement="top" title="Settings" data-id="{{consentReceiptID}}"><span class="icon-cog icon-font-size"></span></button>' +
        '<button type="button" class="btn btn-default btn-revoke">Revoke</button></div>' +
        '</div>' +
        '</div>' +
        '</div>{{/receipts}}</div>{{/if}}';

    var theTemplate = Handlebars.compile(content);
    var html = theTemplate(receiptData);

    $("#consent-settings").hide();
    $("#consent-listing").empty().show();
    $("#consent-listing").append(html);
    addActions();
    $('[data-toggle="tooltip"]').tooltip();
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
        '<input type="text" name="date_picker" id="date_picker" value="{{{extractDate purposes.0.termination}}}"/>' +
        '<input type="hidden" name="date_picker_old_expiry" id="date_picker_old_expiry" value="{{purposes.0.termination}}"/>' +
        '<input type="hidden" name="date_picker_new_expiry" id="date_picker_new_expiry" value="{{purposes.0.termination}}"/>' +
        '<button type="button" class="ui-datepicker-reset action-reset"><i class="icon-undo" data-toggle="tooltip" data-placement="top" title="Reset Date"></i></button>' +
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
        '<button type="button" class="btn btn-default btn-cancel btn-cancel-settings">Cancel</button>' +
        '</div>' +
        '</div>{{/services}}{{/receipts}}';

    Handlebars.registerHelper('extractDate', function (expiry) {
        return constructDate(expiry);
    });

    var theTemplate = Handlebars.compile(content);
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
        '{{#piiCategory}}<li data-jstree=\'{"icon":"icon-user", "selected":true}\' piicategoryid="{{piiCategoryId}}"' +
        ' class="selected">' +
        '{{#if piiCategoryDisplayName}}{{piiCategoryDisplayName}}{{else}}{{piiCategoryName}}{{/if}}</li>' +
        '{{/piiCategory}}' +
        '{{#piiCategories}}<li data-jstree={{#if accepted}}\'{"icon":"icon-user",' +
        ' "selected":true}\'{{else}}\'{"icon":"icon-user"}\'{{/if}} piicategoryid="{{piiCategoryId}}">' +
        '{{#if displayName}}{{displayName}}{{else}}{{piiCategory}}{{/if}}</li>{{/piiCategories}}' +
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

    addActions(container);
    $('[data-toggle="tooltip"]').tooltip();
}

/*
 * Binds all click event handlers
 */
function addActions(container) {

    $(".btn-settings").click(function () {
        var receiptID = $(this).data("id");
        getReceiptDetails(receiptID);
    });

    $(".btn-default-settings").click(function () {
        var receiptID = $(this).data("id");
        getResidentIDPReceiptDetails(receiptID);
    });
    $(".btn-revoke").click(function () {
        var receiptID = $(this).prev().data("id");
        $("#message").append(confirmationDialog);
        message({
            title: "Consent Confirmation",
            content: 'Are you sure you want to revoke this consent? This is not reversible.',
            type: 'confirm',
            okCallback: function () {
                revokeReceipt(receiptID);
            }
        });
    });

    $(".btn-cancel-settings").click(function () {
        renderReceiptList(json);
    });

    $(".btn-update-settings").click(function () {
        $("#message").append(confirmationDialog);
        message({
            title: "Consent Confirmation",
            content: 'Are you sure you want to update/revoke this consent? This is not reversible.',
            type: 'confirm',
            okCallback: function () {
                revokeAndAddNewReceipt(receiptData, container);
            }
        });

    });

    var today = new Date();
    $("#date_picker").datepicker({
        showOn: "button",
        buttonImageOnly: false,
        buttonText: '<i class="icon-calendar action-calendar" data-toggle="tooltip" data-placement="top" title="Pick a Date"></i>',
        minDate: today,
        changeMonth: true,
        changeYear: true,
        onSelect: function (dateText) {
            var split = dateText.split("/");
            var newDate = new Date(split[2], split[0] - 1, split[1]).getTime();
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
        delete(newPurpose['description']);
        delete(newPurpose['piiCategories']);

        var piiCategory = [];
        var categories = purpose.children;
        for (var j = 0; j < categories.length; j++) {
            var category = categories[j];
            var c = {};
            c['piiCategoryId'] = category.li_attr.piicategoryid;
            c['validity'] = expiryDate;
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
    delete(newReceipt['piiPrincipalId']);
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
    var goodDate = date.getMonth() + 1 + "/" + date.getDate() + "/" + date.getFullYear();
    return goodDate;
}

/*
 * change return value to set datepicker default
 */
function getDefaultExpiry() {
    return "Forever";
}
