var moreGroups = [];
$(document).ready(function() {
    var username;
    var colData = buildColumnHeadings(columnList);
    var lastIndex = colData[0].length - 1;
    var thisTable = $('#users-sample').DataTable({
        wso2: true,
        data: users,
        columns: colData[0],
        "columnDefs": [
            {
                "targets": 0,
                "render": function (data, type, full, meta) {
                    return '<div class="thumbnail icon">'+
                    '<i class="square-element text fw fw-user" style="font-size: 0px;"></i>'+
                    '</div>';
                }
            },
            {
                "targets": 1,
                "render": function (data, type, full, meta) {
                    username = data;
                    return data;
                }
            },
            {
                "targets": parseInt(colData[1]),
                "render": function (data, type, full, meta) {
                    var stringData = String(data);
                    var string = '';
                    var moreString = '';
                    if (stringData) {
                        var array = stringData.split(',');

                        var i;
                        for (i = 0; i < 2; i++) {
                            string = string + '<span class="label label-primary"><i class="fw fw-group"></i>' + array[i] + '</span>';
                        }
                        var size = array.length;
                        if (size > 2) {
                            moreString = '<a data-target="#modalDelete" data-toggle="modal" class="open-more-modal" data-id="' + username + '">and ' + (size - 2) + ' more..</a>';
                        }
                        moreGroups = data;
                    }

                    return '<div>' + string + moreString +'</div>'
                }
            },
            {
                "targets": lastIndex,
                    "render": function (data, type, full, meta) {
                    return   '<a href="#" class="btn btn-default">'+
                    '<span class="fw-stack">'+
                                            '<i class="fw fw-edit fw-stack-1x"></i>'+
                                        '</span>'+
                    '</a>'+
                    '<a href="#" class="btn btn-default">'+
                    '<span class="fw-stack">'+
                                            '<i class="fw fw-view fw-stack-1x"></i>'+
                                        '</span>'+
                    '</a>'+
                    '<a href="#" data-click-event="remove-form" class="btn btn-default">'+
                    '<span class="fw-stack">'+
                                            '<i class="fw fw-delete fw-stack-1x"></i>'+
                                        '</span>'+
                    '</a>' +
                    '<input type="hidden" name="userUniqueId" id="userUniqueId" value="' + data + '"/>';
                }
            }
        ],
        "fnCreatedRow": function(nRow, aData, iDataIndex) {

            $('td:eq(0)', nRow)
            .attr('data-search', 'user')
            .attr('data-display', 'user')
            .addClass('remove-padding icon-only content-fill');

            $(nRow).addClass(aData.Status);
            var columns = buildDataArrays(aData, columnList);

            for (i = 1; i < lastIndex-1; i++) {
                $('td:eq('+i+')', nRow)
                    .attr('data-search', columns[i])
                    .attr('data-display', columns[i])
                    .attr('title', columns[i])
                    .attr('title', 'tooltip')
                    .attr('data-placement', 'bottom')
                    .addClass('fade-edge remove-padding-top');
                }

                $('td:eq('+lastIndex+')', nRow).addClass('text-right content-fill text-left-on-grid-view no-wrap');
            },
            initComplete: function (){
            $('.random-thumbs .thumbnail.icon').random_background_color();
        }
    });

    //------------------ Customizations for datatables plugin ----------------------------------------//
    var ROW_SELECTED_CLASS = 'DTTT_selected';
    var LOCKED_STATE = 'LOCKED';
    var DISABLED_STATE = 'DISABLED';
    var UNLOCKED_STATE = 'UNLOCKED';

    var toggle_select_btn = $(this).find("[data-click-event=toggle-select]");
    $(toggle_select_btn).attr("data-click-event", "toggle-select-custom");

    var toggle_select_all_btn = $(this).find("[data-click-event=toggle-select-all]");
    $(toggle_select_all_btn).attr("data-click-event", "toggle-select-all-custom");

    $('.navbar-right li').eq(3).after('<li class="deselect-all-btn" style="display:none;"><button data-click-event="toggle-deselect-all-custom" class="btn btn-default btn-primary">Deselect All</li>');

    $(this).find("[data-click-event=toggle-list-view]").hide();

    var sAllBtn = $("button[data-click-event='toggle-select-all-custom']");

    $('.bulk-element-delete').attr('disabled', true);
    $('.bulk-element-disable').attr('disabled', true);
    $('.bulk-element-lock').attr('disabled', true);
    $('.bulk-element-activate').attr('disabled', true);

    //Enable/Disable selection on rows
    $('.dataTables_wrapper').off('click', '[data-click-event=toggle-select-custom]');
    $('.dataTables_wrapper').on('click', '[data-click-event=toggle-select-custom]', function () {
       var button = this,
           thisTable = $(this).closest('.dataTables_wrapper').find('.dataTable').dataTable();
       if ($(button).html() == 'Select') {
           $('#action').val("select-list");
       } else if ($(button).html() == 'Cancel'){
           $('#action').val("cancel-select");
           thisTable.api().rows().every(function () {
               $(this.node()).removeAttr('data-type');
               $(this.node()).removeClass(ROW_SELECTED_CLASS);
           });
           thisTable.removeClass("table-selectable");
           $(button).removeClass("active").html('Select');
           $(button).closest('li').siblings('.select-all-btn').hide();
       }
    });

    //Event for row select/deselect
    $('body').on('click', '[data-type=selectable]', function() {
        //$(this).toggleClass(ROW_SELECTED_CLASS);
        var button = this,
            thisTable = $(this).closest('.dataTables_wrapper').find('.dataTable').dataTable();

        var selected = false;
        var locked = false;
        var disabled = false;
        var unlocked = false;
        thisTable.api().rows().every(function () {
            if ($(this.node()).hasClass(ROW_SELECTED_CLASS)) {
                selected = true;
                if ($(this.node()).hasClass(LOCKED_STATE)){
                    locked = true;
                } else if ($(this.node()).hasClass(UNLOCKED_STATE)) {
                    unlocked = true;
                } else if ($(this.node()).hasClass(DISABLED_STATE)) {
                    disabled = true;
                }

                if (locked && unlocked && disabled) {
                    return false;
                }
            }
        });

        if (selected) {
            $(sAllBtn).closest('li').siblings('.deselect-all-btn').show();
            $('.bulk-element-delete').attr('disabled', false);
            if (locked) {
                $('.bulk-element-activate').attr('disabled', false);
                $('.bulk-element-disable').attr('disabled', false);
                $('.bulk-element-lock').attr('disabled', true);
            }
            if (unlocked) {
                $('.bulk-element-activate').attr('disabled', true);
                $('.bulk-element-disable').attr('disabled', false);
                $('.bulk-element-lock').attr('disabled', false);
            }
            if (disabled) {
                $('.bulk-element-activate').attr('disabled', false);
                $('.bulk-element-disable').attr('disabled', true);
                $('.bulk-element-lock').attr('disabled', true);
                $('.bulk-element-delete').attr('disabled', true);
            }
        } else {
            $(sAllBtn).closest('li').siblings('.deselect-all-btn').hide();
        }
    });

    //Select all rows functions
    $('.dataTables_wrapper').off('click', '[data-click-event=toggle-select-all-custom]');
    $('.dataTables_wrapper').on('click', '[data-click-event=toggle-select-all-custom]', function () {
        $('#action').val("select-all");
    });

    //Deselect all rows functions
    $('.dataTables_wrapper').off('click', '[data-click-event=toggle-deselect-all-custom');
    $('.dataTables_wrapper').on('click', '[data-click-event=toggle-deselect-all-custom]', function () {
        $('#action').val("select-list");
    });

    $('#offset-value').val(offset);
    $('#length-value').val(recordLimit);

    if (action === "select-list") {
        thisTable.rows().every(function () {
            $(this.node()).attr('data-type','selectable');
        });
        $('#users-sample').addClass("table-selectable");
        var button = $("button[data-click-event='toggle-select-custom']");
        $(button).closest('li').siblings('.select-all-btn').show();
        $(button).addClass("active").html('Cancel');
        $('#filter-btn').prop('disabled', true);
    }

    if (action === "select-all") {
        var button = $("button[data-click-event='toggle-select-custom']");

        var locked = false;
        var disabled = false;
        var unlocked = false;
        thisTable.rows().every(function () {
            $(this.node()).attr('data-type','selectable');
            $(this.node()).addClass(ROW_SELECTED_CLASS);
            if ($(this.node()).hasClass(LOCKED_STATE)){
                    locked = true;
            } else if ($(this.node()).hasClass(UNLOCKED_STATE)) {
                unlocked = true;
            } else if ($(this.node()).hasClass(DISABLED_STATE)) {
                disabled = true;
            }
        });
        $('.bulk-element-delete').attr('disabled', false);
        if (locked) {
            $('.bulk-element-activate').attr('disabled', false);
            $('.bulk-element-disable').attr('disabled', false);
            $('.bulk-element-lock').attr('disabled', true);
        }
        if (unlocked) {
            $('.bulk-element-activate').attr('disabled', true);
            $('.bulk-element-disable').attr('disabled', false);
            $('.bulk-element-lock').attr('disabled', false);
        }
        if (disabled) {
            $('.bulk-element-activate').attr('disabled', false);
            $('.bulk-element-disable').attr('disabled', true);
            $('.bulk-element-lock').attr('disabled', true);
            $('.bulk-element-delete').attr('disabled', true);
        }

        $('#users-sample').addClass("table-selectable");
        $(button).html('Cancel');
        $(sAllBtn).closest('li').siblings('.deselect-all-btn').show();
        $('#filter-btn').prop('disabled', true);

    }

    if(selectedClaim) {
        var i;
        $($("#claimSelector").children()).each(
                function(){
                      if($(this).attr('value') == selectedClaim)
                            i = $(this).index();
                }
          )
        $('#claimSelector')[0].selectedIndex = i;
        $('#claim-uri').val(selectedClaim)
    }

    if(selectedDomain) {
        var j;
        $($("#domainSelector").children()).each(
                function(){
                      if($(this).attr('value') == selectedDomain)
                            j = $(this).index();
                }
          )
        $('#domainSelector')[0].selectedIndex = i;
        $('#domain-name').val(selectedDomain);
    }

} );

$(document).on("click", ".open-more-modal", function () {
    var username = $(this).data('id');
    var modalHeading = $("#groupModalLabel").html() + ' ' + username;
    $("#groupModalLabel").html(modalHeading);

    var listString = '';
    for (i = 0; i < moreGroups.length; i++) {
        listString = listString + '<li>' + moreGroups[i] + '</li>';
    }
    listString = '<ul>' + listString + '</ul>';
    $('.modal-body').html(listString);
});

buildColumnHeadings = function(columns) {
    var arrayString = '{ "data": null }';
    var columnCount = Object.keys(columns).length - 1;
    var groupIndex;
    var roleIndex;
    for (var i = 0; i < columnCount; i ++) {
        arrayString = arrayString + ',{"data": "' + columns[i] + '"}';
        if (columns[i] === "Groups") {
            groupIndex = i + 1;
        } else if (columns[i] === "Roles") {
            roleIndex = i + 1;
        }
    }
    var array = JSON.parse('[' + arrayString + ']');
    return [array, groupIndex, roleIndex];
}

buildDataArrays = function(aData, columns) {
    var columnCount = Object.keys(columns).length - 1;
    var data = [];
    data[0] = null;

    for (var i = 0; i < columnCount; i ++) {
        data[i + 1] = aData[columns[i.toString()]];
    }
    return data;
}

getClaimUri = function() {
    var claimUri = $('#claimSelector').val();
    $('#claim-uri').val(claimUri);
    var selectedIndex = $("#claimSelector")[0].selectedIndex;
    if (selectedIndex == 0) {
        $('#claim-filter').val("");
    }
}

getDomain = function() {
    var domain = $('#domainSelector').val();
    $('#domain-name').val(domain);
}
