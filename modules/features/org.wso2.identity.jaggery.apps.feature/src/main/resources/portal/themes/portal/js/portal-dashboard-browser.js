var elf;
var createApp;

$(function() {
	//initialize elfinder plugin
	//TODO: fix back button and remove this
	localStorage['elfinder-lastdirelfinder'] = "";

	elf = $('#elfinder').elfinder({
		url : 'apis/browser.jag?action=browse', // connector URL (REQUIRED)
		uiOptions : {
			// toolbar configuration
			/*
			 toolbar : [['newsite'], ['publish'], ['back', 'forward'],
			 ['mkdir', 'mkfile', 'upload'], ['open', 'download', 'getfile'], ['info'], ['quicklook'], ['copy', 'cut', 'paste'], ['rm'], ['duplicate', 'rename', 'edit', 'resize'], ['extract', 'archive'], ['search'], ['view']],
			 */
			toolbar : [['newsite'], ['publish'], ['editor'], ['back', 'forward'], ['mkdir', 'mkfile'], ['copy', 'cut', 'paste'], ['rm'], ['duplicate', 'rename'], ['view']],

			// directories tree options
			tree : {
				// expand current root on init
				openRootOnLoad : true,
				// auto load current dir parents
				syncTree : true
			},

			// navbar options
			navbar : {
				minWidth : 150,
				maxWidth : 500
			},

			// current working directory options
			cwd : {
				// display parent directory in listing as ".."
				oldSchool : false
			}
		},

		handlers : {
			select : function(event, elfinderInstance) {
				var selected = event.data.selected;

				if (selected.length) {
					$('#inp-sel-length').val(selected.length);
				}

			}
		},

		//allowShortcuts: false,

		resizable : false
	}).elfinder('instance');

	var siteToHighlight;

	elf.bind('sync', function() {
		if (siteToHighlight) {
			var el = $('#' + siteToHighlight);
			el.addClass('site-just-created');
			setTimeout(function() {
				el.addClass('site-just-after-created');
			}, 100);
			setTimeout(function() {
				el.removeClass('site-just-created');
				el.removeClass('site-just-after-created');
			}, 5000);
		}
		siteToHighlight = null;
	});

	createApp = function() {
		if (!$('#btn-new-app').hasClass("disabled")) {
			var type = $('#inp-title').attr('data-type');
			var layout = $('#inp-layout').val();

			if (!$('#form-new-app').valid())
				return;

			$('#btn-new-app').text("Hold on...").addClass("disabled");

			var dashboard = $('#inp-title').val();

			$.post('apis/browser.jag', {
				action : "createSite",
				site : dashboard
			}, function(result) {
				if (result.created != true) {
					$('#new-app-alert').html('Another app called <strong>' + dashboard + '</strong> already exists. Please enter a different name').fadeIn();
					$('#btn-new-app').text("Create new app").removeClass("disabled");
					return;
				}
				$('.modal').modal('hide');
				$('#btn-new-app').text('Create new app').removeClass("disabled");
				elf.sync();
				siteToHighlight = result.id;
			});
			// opening window inside callback triggers popup blocker
			if (type == 'dashboard') {
				var win = window.open(caramel.context + '/designer.jag?dashboard=' + dashboard + '&layout=' + layout, '_blank');
				win.focus();
			}
		}
	};

	// adding niceScroll plugin to elfinder
	$(".elfinder-navbar, .elfinder-cwd-wrapper").niceScroll();
	//adding niceScroll plugin to modal
	$(".modal").on("shown", function() {
		$(this).find('div[id^="ascrail"]').show();
		$(".modal .modal-body").niceScroll();
	});

	$(".modal").on("hidden", function() {
		$(this).find('div[id^="ascrail"]').hide();
	});

	$('#elfinder').on('click', '.elfinder-cwd-file', function(e) {
		e.preventDefault();

	});

	// Create new site
	$('#btn-new-app').bind('click', createApp);

	var nameTaken = false;
	var input = $('#inp-title');
	input.bind('keyup', function(e) {
		if (e.keyCode === 13) {
			//	e.preventDefault();

		} else {
			$.post('apis/browser.jag', {
				action : 'checkSite',
				site : input.val()
			}, function(result) {
				if (result.hasSite) {
					$('#btn-new-app').removeClass("disabled");
					nameTaken = false;
				} else {
					$('#btn-new-app').addClass("disabled");
					nameTaken = true;
				}
				$('#form-new-app').valid();
			});
		}
	});

	$('#form-new-app').submit(function() {
		createApp();
		return false;
	});

	$('#menu-new-app li a').click(function() {
		var type = $(this).attr('data-app');
		$('#modal-layout-selection').html('');
		if (type == 'dashboard') {
			$("#form-new-app .form-app-title-label").text("Insert Dashboard Name");
			var layout = Handlebars.compile($('#layout-template').html());
			$('#modal-layout-selection').append(layout);
		}

		$('#modal-new-app').modal("show").on('shown', function() {
			$("#inp-title").focus().attr('data-type', type);
		});
		if (type == 'blank') {
			$("#form-new-app .form-app-title-label").text("Insert Microsite Name");
		}

	});

	$('#modal-new-app').on('hidden', function() {
		$('#form-new-app').validate().resetForm();
	});

	jQuery.validator.addMethod("appName", function(value, element) {
		return this.optional(element) || /^[a-zA-Z0-9\-]*$/.test(value);
	}, "Only alphanumeric characters and '-' allowed");

	jQuery.validator.addMethod("appUnique", function() {
		return !nameTaken;
	}, "Name has already been taken");

	$("#inp-title").keyup(function() {
		$('#form-new-app').valid();
		//var $th = $(this);
		//$th.val( $th.val().replace(/[^a-zA-Z0-9\-]/g, function(str) {  return ''; } ) );
	});

	$('.btn-newsite').live('click', function(e) {
		e.stopPropagation();
		$('#newSite').click();

	});

	$('.btn-publish').live('click', function() {

		if ($(this).hasClass('ui-state-disabled')) {
			return;
		}
		var selected, pathHash;

		var length = $('#inp-sel-length').val();

		if (length < 1) {
			$('#modal-alert').find('.alert-block').show().removeClass("alert-success").addClass("alert-error").html("<p class='alert-message'>Please select an asset to publish</p>");
			$('#modal-alert').modal("show");

		} else if (length > 1) {
			$('#modal-alert').find('.alert-block').show().removeClass("alert-success").addClass("alert-error").html("<p class='alert-message'>You can only publish one asset at a time</p>");
			$('#modal-alert').modal("show");
		} else {
			selected = $('.ui-selected');
			pathHash = selected.closest('tr').attr('id') ? selected.closest('tr').attr('id').substr(3) : selected.attr('id').substr(3);
			$('#asset-path-hash').val(pathHash);
			$('#modal-publish').modal('show');

		}
	});

	$('#modal-publish').on('shown', function() {
		$(".modal .modal-body").getNiceScroll().resize();
		$("#inp-publish-name").val(selectedSite);
		$('#inp-publish-url').val(selectedSiteUrl);
	});

	$('#chk-select-all').live('change', function() {

		$('.chk-asset').prop('checked', !$('.chk-asset').prop('checked'));

	});

	var selectedSite = function() {
		var selected;

		selected = $('.ui-selected');

		return $('.elfinder-cwd-filename', selected).html();
	};

	var selectedSiteUrl = function() {
		var selected;
		var siteUrl;

		selected = $('.ui-selected');
		siteUrl = "/" + $('.elfinder-cwd-filename', selected).html();

		return siteUrl;
	};

	$('#btn-publish-site').click(function() {
		if (!$('#form-publish-app').valid())
			return;
		var i, length, rolez = [], userz = [], site = selectedSite(), type = $('#sel-publish-type').val(), action = $('#sel-publish-permission').val(), name = $('#inp-publish-name').val(), version = "1.0.0", desc = $("#inp-publish-desc").val() || "No Description Available", url = $('#inp-publish-url').val(), thumbnail = $('#inp-publish-thumbnail').val() || "/store/themes/store/img/default_thumb.jpg", imageUrl = $("#inp-publish-imageurl").val() || "/store/themes/store/img/default_banner.jpg", roles = $('#inp-publish-roles').tokenInput("get"), users = $('#inp-publish-users').tokenInput("get");

		length = roles.length;
		for ( i = 0; i < length; i++) {
			rolez.push(roles[i].name);
		}
		length = users.length;
		for ( i = 0; i < length; i++) {
			userz.push(users[i].name);
		}
		caramel.post('/apis/asset/' + type + '/publish', JSON.stringify({
			site : site,
			action : action,
			name : name,
			version : version,
			description : desc,
			url : url,
			thumbnail : thumbnail,
			banner : imageUrl,
			roles : rolez,
			users : userz
		}), function(data) {
			var modal = $('#modal-alert');
			if (data.error) {
				modal.find('.alert-block').show().removeClass("alert-success").addClass("alert-error").html("<p class='alert-message'>Error publishing the " + type + " : " + name + "</p>");
			} else {
				modal.find('.alert-block').show().removeClass("alert-error").addClass("alert-success").html("<p class='alert-message'><i class='icon icon-ok-circle icon-large success-msg-icon'></i>" + type + " &quot;" + name + "&quot; successfully published.</p>");
			}
			modal.modal('show');
		});
		$('#modal-publish').modal("hide");
	});

	$('#inp-publish-users').tokenInput(caramel.context + "/apis/publisher/users", {
		theme : "facebook",
		noResultsText : "Username not found",
		hintText : "Type in a Username"
	});

	$('#inp-publish-roles').tokenInput(caramel.context + "/apis/publisher/roles", {
		theme : "facebook",
		noResultsText : "Role not found",
		hintText : "Type in a Role"
	});

	$('.designer-layouts').live('click', function() {
		var layoutType = $(this).attr('data-layout');
		$('.designer-layouts').removeClass('designer-layouts-selected');
		$(this).addClass('designer-layouts-selected');
		$('#inp-layout').val(layoutType);
	});

	$('.elfinder-cwd-file').live('click', function() {
		$(this).find('.chk-asset').attr("checked", checked);
	});

});

