var moreGroups = [];
$(document).ready(function () {

    $('#domainSelector option[value=' + $('#domainSelector').attr('data-primary') + ']').prop('selected', 'selected');
    var primaryDomain = $('#domainSelector').val();
    $("#domain").val(primaryDomain);

    //hide the no users added message when users exists
    if($('#selected-users-dataTable > tbody > tr').length > 0){
       $('.no-users').hide();
    }

    var userArray = users;
    var thisTable = $('#groups-table').DataTable({
        wso2: true,
        data: userArray,
        columns: [{"data": "picture"}, //ToDO get profile picture from actual image
            {"data": "username"},
            {"data": "uid"}],

        "columnDefs": [
            {
                "targets": 0,
                "render": function (data, type, full, meta) {
                    return '<div class="thumbnail icon">' +
                        '<i class="square-element text fw fw-user" style="font-size: 0px;"></i>' +
                        '</div>';
                }
            },
            {
                "targets": 1,
                "render": function (data, type, full, meta) {
                    return '<div>' + data + '</div>';
                }
            },
            {
                "targets": 2,
                "render": function (data, type, full, meta) {
                    return '<input class="uid" type="hidden" name="userUniqueId" id="userUniqueId" value="' + data + '"/>';
                }
            }
        ],
        "fnCreatedRow": function (nRow, aData, iDataIndex) {

            $('td:eq(0)', nRow)
                .attr('data-search', 'user')
                .attr('data-display', 'user')
                .addClass('remove-padding icon-only content-fill');

        },
        initComplete: function () {
            $('.random-thumbs .thumbnail.icon').random_background_color();
        }
    });

    //Disable modal add users button
    $('.save').prop("disabled",true);


    //TODO remove when the new datatable plugin is updated in UUF
    $("li button[data-click-event='toggle-list-view']").parent().hide();
    $("li button i.fw-sort").parent('button').hide();

    thisTable.rows().every(function () {
        $(this.node()).attr('data-type','selectable');
    });
    $('#groups-table').addClass('table-selectable');
    var button = $("button[data-click-event='toggle-select']");
    $(button).closest('li').siblings('.select-all-btn').show();
    $(button).hide();
});

//------------------ Customizations for datatables plugin ----------------------------------------//

var ROW_SELECTED_CLASS = 'DTTT_selected';

$(document).on('click', '.save', function () {
    thisTable = $('.user-select-dataTable').dataTable();
    var selected = false;
    var assignedUsersArray = [];
    thisTable.api().rows().every(function () {
        if ($(this.node()).hasClass(ROW_SELECTED_CLASS)) {
            selected = true;
            assignedUsersArray.push(thisTable.api().row($(this)).data());
        }
    });
    createAssignedUserTable(assignedUsersArray);

});

//Enable/disable modal add user button
$(document).on('click', '.user-select-dataTable tr[data-type="selectable"], #groups-table_wrapper .select-all-btn button', function () {
    if($('.user-select-dataTable').find('.DTTT_selected').length > 0){
        $('.save').prop("disabled",false);
    }else{
        $('.save').prop("disabled",true);
    }
});

function groupNameExists(url) {
    var groupName = document.getElementById('groupname').value;
    if (!groupName) {
        return;
    }
    var groupNameClaimUri = "http://wso2.org/claims/groupname";
    var domain = null;
    if (document.getElementById('domainSelector')) {
        domain = document.getElementById('domainSelector').value;
    }
    $.ajax({
        type: "GET",
        url: url,
        data: {groupName: groupName, groupNameClaimUri: groupNameClaimUri, domain: domain},
        success: function (data) {
            if (data === "true") {
                var fillingObject = {
                    "groupExists": true
                };
                var callbacks = {
                    onSuccess: function () {
                        $("#addGroup").prop('disabled', true);
                    },
                    onFailure: function (e) {
                    }
                };
                UUFClient.renderFragment("group-existence", fillingObject,
                    "groupExistsError-area", "OVERWRITE", callbacks);
            } else {
                var fillingObject = {};
                var callbacks = {
                    onSuccess: function () {
                        $("#addGroup").prop('disabled', false);
                    },
                    onFailure: function (e) {
                    }
                };
                UUFClient.renderFragment("group-existence", fillingObject,
                    "groupExistsError-area", "OVERWRITE", callbacks);
            }
        }
    });
}

function createAssignedUserTable(userList) {
    var usersTable = $('#selected-users-dataTable').DataTable({
        wso2: true,
        data: userList,
        columns: [{"data": "picture"}, //ToDO get profile picture from actual image
            {"data": "username"},
            {"data": "uid"},
            {"data": null}
        ],

        "columnDefs": [
            {
                "targets": 0,
                "render": function (data, type, full, meta) {
                    return '<div class="thumbnail icon">' +
                        '<i class="square-element text fw fw-user" style="font-size: 0px;"></i>' +
                        '</div>';
                }
            },
            {
                "targets": 1,
                "render": function (data, type, full, meta) {
                    return '<div>' + data + '</div>';

                }
            },
            {
                "targets": 2,
                "render": function (data, type, full, meta) {
                    return '<input class="uid" type="hidden" name="userUniqueId" id="userUniqueId" value="' + data + '"/>';
                }
            },
            {
                "targets": 3,
                "render": function (data, type, full, meta) {
                    // TODO user remove from selected list
                    return '<a href="#" data-click-event="remove-form" class="pull-right add-padding-right-2x">' +
                        // '<i class="fw fw-cancel"></i>' +
                        '</a>';
                }
            }
        ],
        "fnCreatedRow": function (nRow, aData, iDataIndex) {

            $('td:eq(0)', nRow)
                .attr('data-search', 'user')
                .attr('data-display', 'user')
                .addClass('remove-padding icon-only content-fill');

        },
        initComplete: function () {
            $('.random-thumbs .thumbnail.icon').random_background_color();
        }
    });

    //hide the no users added message when users exists
    if($('#selected-users-dataTable > tbody > tr').length > 0){
        $('.no-users').hide();
    }

    //TODO remove when the new datatable plugin is updated in UUF
    $("li button[data-click-event='toggle-list-view']").parent().hide();
    $("li button i.fw-sort").parent('button').hide();
}


$("#addGroupForm").validate({
    rules: {
        groupname: {
            required: true
        }
    },
    messages: {
        groupname: {
            required: "Required to provide a group name"
        }

    }
});

$("#editGroupForm").validate({
    rules: {
        groupname: {
            required: true
        }
    },
    messages: {
        groupname: {
            required: "Required to provide a group name"
        }

    }
});


