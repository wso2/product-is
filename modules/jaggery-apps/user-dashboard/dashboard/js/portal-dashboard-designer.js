$(function() {

    var id = 1;

	function insertGadget(parentEl, url, pref,title) {
		id++;
		var gadgetDiv = parentEl.find('.add-gadget-item');
		var idStr = 'gadgetArea-d' + id;
		gadgetDiv.html('<div id="' + idStr + '">');
		UESContainer.renderGadget(idStr, url, pref || {}, function(gadgetInfo) {
            var visibleTitle = title || gadgetInfo.meta.modulePrefs.title;
            parentEl.find('h3').text(visibleTitle);
            parentEl.find('input').val(visibleTitle);
		});
	}

	function drawGadgets() {

		$.get('apis/ues/layout/', {}, function(result) {
			if (result) {

				var userWidgets = result.widgets;
				var elements = $('<div class="row row-gutter-30"></div>');

				$.each(userWidgets, function(i, w) {

                            var el = $('<div class="col-xs-12 col-sm-6 col-md-4 col-lg-4">' +
								'<div id="'+w.id+'" style="height:165px" class="layout_block gs_w" ' +
								'data-title="'+w.title+'" data-wid="'+w.wid+'" data-url="'+w.url+'" data-prefs="'+w.prefs+'"> ' +
								'<div class="grid_header"> <h3></h3>' +
								'<span class="grid_header_controls"> <a class="show-widget-pref" data-collapse="true"><i class="icon-cog"></i></a> ' +
								'<a class="expand-widget"><i class="icon-resize-full"></i></a> ' +
								'<a class="shrink-widget"><i class="icon-resize-small"></i></a> ' +
								'</span> ' +
								'</div> ' +
								'<div class="gadget-pref-cont"></div> <div class="add-gadget-item"></div></div></div>');

                        	el.find('.expand-widget').click(function(e) {
                        		e.preventDefault();
                        		var widget = $(this).closest('.gs_w');
                        		widget.addClass('maximized-view');
                        		var widgetEl = widget.get(0);
                        		$('.gs_w').each(function(i, el) {
                        			if (el != widgetEl) {
                        				$(el).hide();
                        			}
                        		});
                        		UESContainer.maximizeGadget(widget.find(".add-gadget-item > div").attr('id'));
                        	});


                            el.find('.shrink-widget').click(function(e) {
                            	e.preventDefault();
                            	var widget = $(this).closest('.gs_w');
                            	widget.removeClass('maximized-view');
                            	$('.gs_w').show();
                            	UESContainer.restoreGadget(widget.find(".add-gadget-item > div").attr('id'));
                            });


                             if((((i+1)%3)==0)&&(i!=0)){

                               elements.append(el);

                               $("#layouts_grid").append(elements);

                               elements = $('<div class="row row-gutter-30"></div>');

                             }else{

                                elements.append(el);

                             }

				});


				$('#dashboardName').find('span').text(result.title);

			}

			var widgets = $('#layouts_grid').find('.layout_block');

			$.each(widgets, function(i, widget) {
                var $w = $(widget);
                var wid = $w.attr('data-wid');

				var url = $w.attr('data-url');
				var prefs = JSON.parse($w.attr('data-prefs').replace(/'/g, '"'));
				var gadgetArea = $w.find('.add-gadget-item');
				if (url != '') {
                    $w.find('.designer-placeholder').remove();
                    $w.find('.btn-add-gadget').remove();
					insertGadget($w, url, {
						prefs : prefs
					},$w.attr('data-title'));
				}

			});

		}).error(function(error) {
			console.log(error);
		});

	}


	drawGadgets();

});
