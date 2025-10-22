$(document).ready(function() {
    UESContainer.renderGadget('store-gadget-div', portalGadgets.store);

	// counters when generating node IDs
	var nodeCounter = 0;
	var newCounter = 0;
	var tabId = 0;

	// form : http://stackoverflow.com/questions/979975/how-to-get-the-value-from-url-parameter {{##
	var queryString = function() {
		// This function is anonymous, is executed immediately and
		// the return value is assigned to QueryString!
		var query_string = {};
		var query = window.location.search.substring(1);
		var vars = query.split("&");
		for (var i = 0; i < vars.length; i++) {
			var pair = vars[i].split("=");
			// If first entry with this name
			if ( typeof query_string[pair[0]] === "undefined") {
				query_string[pair[0]] = pair[1];
				// If second entry with this name
			} else if ( typeof query_string[pair[0]] === "string") {
				var arr = [query_string[pair[0]], pair[1]];
				query_string[pair[0]] = arr;
				// If third or later entry with this name
			} else {
				query_string[pair[0]].push(pair[1]);
			}
		}
		return query_string;
	}();
	// ##}}

	// form http://stackoverflow.com/questions/246801/how-can-you-encode-to-base64-using-javascript#246813 {{##
	var _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

	var _utf8_encode = function(string) {
		string = string.replace(/\r\n/g, "\n");
		var utftext = "";

		for (var n = 0; n < string.length; n++) {

			var c = string.charCodeAt(n);

			if (c < 128) {
				utftext += String.fromCharCode(c);
			} else if ((c > 127) && (c < 2048)) {
				utftext += String.fromCharCode((c >> 6) | 192);
				utftext += String.fromCharCode((c & 63) | 128);
			} else {
				utftext += String.fromCharCode((c >> 12) | 224);
				utftext += String.fromCharCode(((c >> 6) & 63) | 128);
				utftext += String.fromCharCode((c & 63) | 128);
			}

		}

		return utftext;
	};

	var encode = function(input) {
		var output = "";
		var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
		var i = 0;

		input = _utf8_encode(input);

		while (i < input.length) {

			chr1 = input.charCodeAt(i++);
			chr2 = input.charCodeAt(i++);
			chr3 = input.charCodeAt(i++);

			enc1 = chr1 >> 2;
			enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
			enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
			enc4 = chr3 & 63;

			if (isNaN(chr2)) {
				enc3 = enc4 = 64;
			} else if (isNaN(chr3)) {
				enc4 = 64;
			}

			output = output + _keyStr.charAt(enc1) + _keyStr.charAt(enc2) + _keyStr.charAt(enc3) + _keyStr.charAt(enc4);

		}

		return output;
	};

	// ##}}

	var isFileLoaded;

	$('.store-left').height($('.store-right').height());

	$('.asset').hover(function() {
		$(this).children('.asset-details').animate({
			top : 1
		}, 200);
	}, function() {
		$(this).children('.asset-details').animate({
			top : 175
		}, 200);
	});

	$('#dash-pages > ul > li').hover(function() {
		$(this).children('div').show();
	}, function() {
		$(this).children('.dash-page-controls').hide();
	});

	localStorage['elfinder-lastdirelfinder'] = "";
	localStorage["elfinder-lastdirfile-tree"] = "l1_" + encode(editor.site);

	$.fn.elfindertoolbar = function(fm, opts) {
		this.not('.elfinder-toolbar').each(function() {
			var commands = fm._commands, self = $(this).addClass('ui-widget-header ui-corner-top elfinder-toolbar'), panels = opts || [], l = panels.length, i, cmd, panel, button;
			console.log(self.parent());
			self.prev().length && self.parent().find('.elfinder-workzone').append(this);

			while (l--) {
				if (panels[l]) {
					panel = $('<div class="ui-widget-content ui-corner-all elfinder-buttonset"/>');
					i = panels[l].length;
					while (i--) {

						if (( cmd = commands[panels[l][i]])) {
							button = 'elfinder' + cmd.options.ui;
							$.fn[button] && panel.prepend($('<div/>')[button](cmd).addClass('btn-' + panels[l][i]));
						}
					}

					panel.children().length && self.prepend(panel);
					panel.children(':gt(0)').before('<span class="ui-widget-content elfinder-toolbar-button-separator"/>');

				}
			}

			self.children().length && self.show();
		});

		return this;
	}
	//init elFinder.
	var elf = $('#file-tree').elfinder({
		url : caramel.context + '/apis/browser.jag?action=browse', // connector URL (REQUIRED)
		ui : ['toolbar', 'tree', 'stat', 'path'],

		showTreeFiles : true,
		uiOptions : {
			// directories tree options
			tree : {
				// expand current root on init
				openRootOnLoad : true,
				// auto load current dir parents
				syncTree : true
			},

			toolbar : [['savefile', 'previewapp'], ['addgadget'], ['bold', 'italic', 'link', 'img', 'ol', 'ul', 'table', 'indent', 'htmlstub']],

			// navbar options
			navbar : {
				minWidth : 150,
				maxWidth : 500
			}
		},
		contextmenu : {
			navbar : ['mkdir', 'mkfile']
		},
		allowShortcuts : false,

		resizable : false
	}).elfinder('instance');

	$(".elfinder-navbar").niceScroll();
	$('.elfinder-workzone').append('<div id="editor-page" style="float: left;"><div id="editor-start"><i class="icon-magic"></i>Click on a file to begin</div></div> ');

	var loadContent = function(hash, varsion, callback) {
		$.ajax({
			type : 'GET',
			url : caramel.context + "/apis/editor.jag",
			data : {
				operation : "get_content",
				page : editor.site,
				pathhash : hash,
				version : varsion || 'old'
			},
			success : async.apply(callback, null)
		});
	};
	var loadContentInfo = function(hash, callback) {
		$.ajax({
			type : 'GET',
			url : caramel.context + "/apis/autosave.jag",
			data : {
				"action" : "getStatus",
				"page" : editor.site,
				"pathhash" : hash
			},
			dataType : "json",
			success : async.apply(callback, null)
		});
	};

	var selectVersion = function(hash, version, callback) {
		$.ajax({
			type : 'GET',
			url : caramel.context + "/apis/autosave.jag",
			data : {
				"action" : "selectVersion",
				version : version,
				"page" : editor.site,
				"pathhash" : hash
			},
			dataType : "json",
			success : async.apply(callback, null)
		});
	};

	var addTab = function(name, hash, content, path) {
		tabId++;
		var tabName = name;
		$('.CodeMirrorTabs.tab-content div.active').removeClass('active').removeClass('in');
		$('#tabs-files li.active').removeClass('active');
		$('.CodeMirrorTabs.tab-content').append('<div class="tab-pane in active" id="tab-' + tabId + '"><p> Loading content ...</p></div>');
		$('#tabs-files').append('<li><a href="#" class="tab-close"><i class="icon-remove"></i></a><a id="tab-btn-' + tabId + '" href="#tab-' + tabId + '" data-toggle="tab" data-regpath="' + hash + '"><div class="autosave-wrapper"><i class="autosave"></div></i>' + tabName + '</a></li>');
		$('#tabs-files a:last').tab('show');
		var tab = $('#tab-' + tabId);
		tab.html('<textarea id="codetab-' + tabId + '" name="code' + tabId + '"></textarea>');
		tab.find('textarea').text(content);
		tab.attr('data-path', path);
		codeHightlight('tab-' + tabId, name, hash);
		codeMirrorResize();
	};

	var loadFile = function(file) {

		if ($('#tabs-files').length == 0) {
			$('#editor-start').remove();
			$('#editor-page').append('<div style="display: none" class="alert-bar"></div><ul class="nav nav-tabs" id="tabs-files">  </ul>  <div class="tab-content CodeMirrorTabs"></div>');
		}
		if (!isFileLoaded(file.hash)) {
			async.parallel([async.apply(loadContent, file.hash, 'old'), async.apply(loadContentInfo, file.hash)], function(e, r) {
				var addCurrentTab = async.apply(addTab, file.name, file.hash, r[0][0], r[1][0].path);
				if (r[1][0].hasVersions) {
					loadContent(file.hash, 'new', function(e, d) {
						var modal = $('#modal-resolve-versions');
						var oldButton = modal.find('#version-select-old');
						var newButton = modal.find('#version-select-new');
						modal.find('#preview-content-old').text(r[0][0]);
						modal.find('#preview-content-new').text(d);
						oldButton.unbind('click');
						newButton.unbind('click');
						oldButton.click(function() {
							selectVersion(file.hash, 'old', function() {
								addCurrentTab();
								modal.modal('hide');
							});
						});
						newButton.click(function() {
							selectVersion(file.hash, 'new', function() {
								addTab(file.name, file.hash, d, r[1][0].path);
								modal.modal('hide');
							});
						});
						modal.modal('show');
					});
				} else {
					addCurrentTab();
				}
			});

		} else {
			$('#tabs-files a[data-regpath="' + file.hash + '"]').tab('show');

		}

	};

	elf.bind('fileopen', function(event) {
		loadFile(event.data.file);
	});

	isFileLoaded = function(url) {
		var flag = false;

		$('#tabs-files li').each(function(i) {

			if ($(this).children('a').next().attr('data-regpath') == url) {
				flag = true;
				return false;
			}
		});

		return flag;

	};

	//multiplex the code completer between modes.
	CodeMirror.commands.autocomplete = function(cm) {
		var pos = cm.getCursor(), tok = cm.getTokenAt(pos);
		var inner = CodeMirror.innerMode(cm.getMode(), tok.state), state = inner.state;
		var modeName = inner.mode.name;
		if (modeName == "javascript") {
			CodeMirror.showHint(cm, CodeMirror.javascriptHint);
		} else if (modeName == "xml") {
			CodeMirror.showHint(cm, CodeMirror.htmlHint);
		}
	};

	//keep all the code mirror object mapped by the tab.
	var codeMirrorArr = {};

	var getCodeMirrorInfo = function() {
		var tab = $('#tabs-files li.active a[data-toggle=tab]').attr('href').replace('#', '');
		return codeMirrorArr[tab];
	};

	window.getCodeMirror = function() {
		return getCodeMirrorInfo().cm;
	};

	window.getCodeMirrorArr = function() {
		return codeMirrorArr;
	};

	setChangeStatus = function() {
		var cmInfo = getCodeMirrorInfo();
		var icon = cmInfo.tab.find('.autosave');
		cmInfo.changed = true;
		icon.addClass('icon-asterisk');
	};

	//inti code mirror.
	codeHightlight = function(id, name, hash) {
		function endsWith(str, suffix) {
			return str.indexOf(suffix, str.length - suffix.length) !== -1;
		}

		var mimes = {
			".jag" : "application/x-jaggery",
			".css" : "text/css",
			".conf" : "application/json",
			".xml" : "text/xml",
			".js" : "application/javascript",
			".html" : "text/html",
			".htm" : "text/html"
		};
		var getMime = function(s) {
			for (var key in mimes) {
				if (mimes.hasOwnProperty(key)) {
					if (endsWith(s, key)) {
						return mimes[key];
					}
				}
			}
			return "text/plain";
		};

		CodeMirror.defineExtension("reindent", function() {
			var cm = this;
			this.operation(function() {
				var lines = cm.lineCount();
				for (var i = 0; i <= lines; i++) {
					cm.indentLine(i);
				}
			});
		});

		var editor = CodeMirror.fromTextArea(document.getElementById("code" + id), {
			lineNumbers : true,
			mode : getMime(name),
			indentUnit : 4,
			indentWithTabs : true,
			extraKeys : {
				"Ctrl-Space" : "autocomplete"
			}
		});

		codeMirrorArr[id] = {
			tab : $('#tab-btn-' + id.substr(4)).parent(),
			cm : editor,
			changed : false,
			hash : hash,
			lastSaved : editor.getValue()
		};

		editor.on('change', setChangeStatus);

	};
	$('.btn-savefile').live('click', function() {
		var url = $('#tabs-files li.active').children('a').next().attr('data-regpath');
		var content = getCodeMirror().getValue();

		$.ajax({
			type : 'POST',
			url : caramel.context + "/apis/editor.jag",
			data : {
				"operation" : "put_content",
				"mode" : queryString["mode"],
				"pathhash" : url,
				"page" : editor.site,
				"data" : content
			},
			success : function(r) {
				showAlert('File saved');

			}
		});

	});

	$('.tab-close').live('click', function() {
		var rem = $(this).parents('li');
		var next = rem.siblings('li').first();
		var content = rem.children("a:not('.tab-close')").attr('href');
		var nextTab = next.children("a:not('.tab-close')");

		$(nextTab).tab('show');
		delete codeMirrorArr[content.substr(1)];
		rem.remove();
		$(content).remove();

	});

	$(window).resize(codeMirrorResize);
	$(window).load(codeMirrorResize);

	function codeMirrorResize() {
		var height = $(window).height();
		$('.tab-content .CodeMirror').height(height - 140);
		//$('.elfinder-workzone').height(height-150);
		$('#editor-page').width($(window).width() - $('.elfinder-navbar').width() - 12);

		$('#editor-start').height(height - 110);
		$('#editor-start i').css('padding-top', height - 660);
	}

	function includeGadget(editor, url) {
		var commentSearch = editor.getSearchCursor(includeHeader);
		if (commentSearch.findNext()) {
			console.log('already included');
		} else {
			var tagSearch = editor.getSearchCursor(includePoint);
			if (tagSearch.findNext()) {
				editor.replaceRange(includeBody, tagSearch.to());
				var includeBodyIndent = getIndentCount(includeBody);
				for (var i = 0; i < includeBodyIndent; i++) {
					editor.indentLine(tagSearch.to().line + i + 1);
				}
			} else {
				console.log("error: couldn't find " + includePoint);
			}
		}

		var divSearch = editor.getSearchCursor(includeDivId);
		var matchedId, maxId = -1;
		while ( matchedId = divSearch.findNext()) {
			maxId = Math.max(Number(matchedId[1]), maxId);
			console.log(matchedId[1]);
		}
		maxId++;
		var divCode = includeDiv(maxId, url);
		var cursor = editor.getCursor();
		editor.replaceRange(divCode, cursor);
		editor.indentLine(cursor.line + 1);
		var divIndent = getIndentCount(divCode) + 1;
		for (var i = 0; i < divIndent - 1; i++) {
			editor.indentLine(cursor.line + i + 1, "prev");
		}

	}

	var eventRegistered = false;
	$('.btn-addgadget').live('click', function() {
		var editor = getCodeMirror(), headerSearch = editor.getSearchCursor(includePoint);
		if (headerSearch.findNext()) {
			$('#modal-add-gadget').modal('show');
			if (!eventRegistered) {
				var cWindow;
				//setTimeout(function() {
					cWindow = $('iframe').get(0).contentWindow;
					cWindow.addListener(function(gadgetInfo) {
						$('#modal-add-gadget').modal('hide');

						includeGadget(editor, gadgetInfo.attributes.overview_url);
					});
			//	}, 2000);

				eventRegistered = true;
			}
		} else {
			showAlert('Please define &lt;head&gt; before inserting a Gadget', 'alert-error', '.alert-bar');
		}

	});

	$(function() {
		if (editor.fileName.indexOf('.') > 0) {
			loadFile({
				hash : editor.pathHash,
				name : editor.fileName
			});
		}
	});

	//initializing the common container
	CommonContainer.init();

});
