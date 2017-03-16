$(document).ready(function() {
    $('#users-sample').DataTable({
                wso2: true,
                data: users,
                columns: [
                    { "data": null },
                    { "data": "Username" },
                    { "data": "Status" },
                    { "data": "Groups" },
                    { "data": "Roles" },
                    { "data": null }
                ],
                "columnDefs": [
                    {
                        "targets": 0,
                        "render": function (data, type, full, meta) {
                            return '<div class="thumbnail icon">'+
                            '<i class="square-element text fw fw-'+data+'" style="font-size: 0px;"></i>'+
                            '</div>';
                        }
                    },
                    {
                        "targets": 5,
                            "render": function (data, type, full, meta) {
                            return   '<a href="#" class="btn btn-default">'+
                            '<span class="fw-stack">'+
                                                    '<i class="fw fw-circle-outline fw-stack-2x"></i>'+
                                                    '<i class="fw fw-view fw-stack-1x"></i>'+
                                                '</span>'+
                            '<span class="hidden-xs">View</span>'+
                            '</a>'+
                            '<a href="#" class="btn btn-default">'+
                            '<span class="fw-stack">'+
                                                    '<i class="fw fw-circle-outline fw-stack-2x"></i>'+
                                                    '<i class="fw fw-edit fw-stack-1x"></i>'+
                                                '</span>'+
                            '<span class="hidden-xs">Edit</span>'+
                            '</a>'+
                            '<a href="#" data-click-event="remove-form" class="btn btn-default">'+
                            '<span class="fw-stack">'+
                                                    '<i class="fw fw-circle-outline fw-stack-2x"></i>'+
                                                    '<i class="fw fw-delete fw-stack-1x"></i>'+
                                                '</span>'+
                            '<span class="hidden-xs">Delete</span>'+
                            '</a>';
                        }
                    }
                ],
                "fnCreatedRow": function(nRow, aData, iDataIndex) {

                    var columns = [
                        null,
                        aData.Username,
                        aData.Status,
                        aData.Groups,
                        aData.Roles,
                        null
                    ];
                    for (i = 1; i < 5; i++) {
                        $('td:eq('+i+')', nRow)
                            .attr('data-search', columns[i])
                            .attr('data-display', columns[i])
                            .attr('title', columns[i])
                            .attr('title', 'tooltip')
                            .attr('data-placement', 'bottom')
                            .addClass('fade-edge remove-padding-top');
                        }

                        $('td:eq(5)', nRow).addClass('text-right content-fill text-left-on-grid-view no-wrap');
                    },
                    initComplete: function (){
                    $('.random-thumbs .thumbnail.icon').random_background_color();
                }
            });
} );