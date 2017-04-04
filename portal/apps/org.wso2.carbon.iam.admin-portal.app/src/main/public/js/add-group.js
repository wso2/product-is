var moreGroups = [];
$(document).ready(function () {

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
                    //
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


$(document).ready(function () {
    $('#domainSelector option[value=' + $('#domainSelector').attr('data-primary') + ']').prop('selected', 'selected');
    var primaryDomain = $('#domainSelector').val();
    $("#domain").val(primaryDomain);
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
                    return '<a href="#" data-click-event="remove-form" class="btn btn-default">' +
                        '<span class="fw-stack">' +
                        '<i class="fw fw-circle-outline fw-stack-2x"></i>' +
                        '<i class="fw fw-delete fw-stack-1x"></i>' +
                        '</span>' +
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


