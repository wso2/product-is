/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * overview Container implementation of view enhancement.
 */

CommonContainer['views'] = CommonContainer['views'] || {};

/**
 * Method will be called to create the DOM element to place the Gadget
 * Site in.
 *
 * @param {Object}
 *          metadata: Gadget meta data for the gadget being opened in
 *          this GadgetSite.
 * @param {Element}
 *          rel: The element to which opt_coordinates values are
 *          relative.
 * @param {string=}
 *          opt_view: Optional parameter, the view that indicates the
 *          type of GadgetSite.
 * @param {string=}
 *          opt_viewTarget: Optional parameter, the view target indicates
 *          where to open the gadget.
 * @param {Object=}
 *          opt_coordinates: Object containing the desired absolute
 *          positioning css parameters (top|bottom|left|right) with
 *          appropriate values. All values are relative to the calling
 *          gadget.
 * @return {Object} The DOM element to place the GadgetSite in.
 */
CommonContainer.views.createElementForGadget = function(metadata, rel, opt_view, opt_viewTarget,
        opt_coordinates) {

  var surfaceView = 'default';
  var viewTarget = 'default';

  if (typeof opt_view != 'undefined') {
    surfaceView = opt_view;
  }
  if (typeof opt_viewTarget != 'undefined') {
    viewTarget = opt_viewTarget;
  }

  switch (viewTarget) {
    case 'tab':
      return openInNewTab(metadata);
      break;
    case 'dialog':
      return openInDialog(false, surfaceView, true, metadata);
      break;
    case 'modalDialog':
      return openInDialog(true, surfaceView, true, metadata);
      break;
    default:
      return openInDialog(false, surfaceView, true, metadata);
  }
};



/**
 * Method will be called to create the DOM element to place the UrlSite
 * in.
 *
 * @param {Element}
 *          rel: The element to which opt_coordinates values are
 *          relative.
 * @param {string=}
 *          opt_view: Optional parameter, the view to open. If not
 *          included the container should use its default view.
 * @param {Object=}
 *          opt_coordinates: Object containing the desired absolute
 *          positioning css parameters (top|bottom|left|right) with
 *          appropriate values. All values are relative to the calling
 *          gadget.
 * @return {Object} The DOM element to place the UrlSite object in.
 */
CommonContainer.views.createElementForUrl = function(rel, opt_viewTarget, opt_coordinates) {
  var viewTarget = 'dialog';

  if (typeof opt_viewTarget != 'undefined') {
    viewTarget = opt_viewTarget;
  }

  switch (viewTarget) {
    case 'tab':
      return openInNewTab();
      break;
    case 'dialog':
      return openInDialog(false, 'canvas', false);
      break;
    case 'modalDialog':
      return openInDialog(true, 'canvas', false);
      break;
    default:
      return openInDialog(false, 'canvas', false);
  }
};


/**
 * Method will be called when a gadget wants to close itself or the parent
 * gadget wants to close a gadget or url site it has opened.
 *
 * @param {object=} site
 *          The id of the site to close.
 */
CommonContainer.views.destroyElement = function(site) {
  closeDialog(site);
};

var dialog_counter = 0;
var tab_counter = 2;
var newTabId;
var $tabs;


/**
 * private method will be called to create the dialog DOM element.
 * @private
 * @param {boolean} modaldialog
 *          true for modal dialog.
 * @param {string} view
 *          view type.
 * @param {boolean} isGadget
 *          true for gadget, false for url.
 * @param {string} opt_gadgetMetadata
 *          gadget metadate.
 * @return {Object} The DOM element to place the gadget or url site object in.
 */
function openInDialog(modaldialog, view, isGadget, opt_gadgetMetadata) {

  var dialog_width = 450; // default width
  if (view == 'canvas') {
    dialog_width = 675; // width for canvas
  }

  dialog_counter++;
  var dialog = document.createElement('div');
  dialog.id = 'dialog_' + dialog_counter;

  if (typeof opt_gadgetMetadata != 'undefined') {
    // open gadget, get the title from gadgetMetadata
    dialog.title = opt_gadgetMetadata['modulePrefs'].title;
  }

  document.getElementById('content').appendChild(dialog);
  if (isGadget) {
    var id = 'dialog_' + dialog_counter;
    // use jquery to create the dialog
    $('#' + id).dialog({
      resizable: false,
      width: dialog_width, // height will be auto
      modal: modaldialog, // set modal: true or false
      beforeClose: function(ev, ui) {
        var dialogDiv = $('#' + id + ' iframe');
        if(dialogDiv.length) {
          //Means the user most likely clicked the 'x' in the dialog chrome
          //If they clicked the OK or Cancel buttons in the dialog than the gadget
          //iFrame would have already been removed from the DOM
          var site = CommonContainer.getGadgetSiteByIframeId_(dialogDiv[0].id);
          CommonContainer.closeGadget(site);
        }
      }
    });
    return dialog;
  } else {
    var dialog_height = 350; // default height
    if (view == 'canvas') {
      dialog_height = 530; // height for canvas
    }
    $('#dialog_' + dialog_counter).dialog({
      resizable: false,
      width: dialog_width,
      height: dialog_height,
      modal: modaldialog, // set modal: true or false
      close: function(ev, ui) {
        $(this).remove();
      }
    });
    // Maybe an issue in jquery, we need to create another div, otherwise it
    // will show vertical scroll bar
    var dialog_content = document.createElement('div');
    dialog_content.style.height = '100%';
    dialog_content.style.width = '100%';
    dialog.appendChild(dialog_content);
    return dialog_content;
  }
}


/**
 * private method will be called to create the tab DOM element.
 * @private
 * @param {string} gadgetMetadata
 *          gadget metadate.
 * @return {Object} The DOM element to place the gadget or url site object in.
 */
function openInNewTab(gadgetMetadata) {

  var tabsNode;

  if (!document.getElementById('tabs')) {
    // add the new tab the first time, will create the default tab for the
    // current content, and add a new tab
    var controlPanelNode = document.getElementById('controlPanel');
    var testAreaNode = document.getElementById('testArea');
    var contentNode = document.getElementById('content');

    contentNode.removeChild(controlPanelNode);
    contentNode.removeChild(testAreaNode);

    tabsNode = document.createElement('div');
    tabsNode.id = 'tabs';

    tabsNode.innerHTML = "<ul><li><a href='#tabs-1'>Default</a></li></ul>";

    var tabs_1_Node = document.createElement('div');
    tabs_1_Node.id = 'tabs-1';

    // put the default content into the tabs-1
    tabs_1_Node.appendChild(controlPanelNode);
    tabs_1_Node.appendChild(testAreaNode);

    tabsNode.appendChild(tabs_1_Node);
    contentNode.appendChild(tabsNode);
    newTabId = 'tabs-2';

    // use jquery to create new tab
    $tabs = $('#tabs')
    .tabs(
            {
              tabTemplate: "<li><a href='#{href}'>#{label}</a>" +
              "<span class='ui-icon ui-icon-close'>Remove Tab</span></li>",
              add: function(event, ui) {
                var tab_content_id = 'tab_content' + tab_counter;
                var tab_content = document.createElement('div');
                tab_content.id = tab_content_id;
                // tab_content.className = "column";

                tab_content.style.height = '1000px';
                tab_content.style.width = '100%';

                $(ui.panel).append(tab_content);

                // set the focus to the new tab
                $('#tabs').tabs('select', '#' + newTabId);
              },

          remove: function(event, ui) {
            // If there is gadget inside, close the gadget
            var iframes = $(this).parent().get(0)
            .getElementsByTagName('iframe');
            if (iframes.lenth > 0 && (typeof iframes[0].id != 'undefined')) {
              var site = CommonContainer
              .getGadgetSiteByIframeId_(iframes[0].id);
              CommonContainer.closeGadget(site);
            }
          }
            });

    // close icon: removing the tab on click
    // note: closable tabs gonna be an option in the future - see
    // http://dev.jqueryui.com/ticket/3924
    $('#tabs span.ui-icon-close').live('click', function() {
      var index = $('li', $tabs).index($(this).parent());

      $tabs.tabs('remove', index);
      if ($tabs.tabs('length') < 2) {

        controlPanelNode = document.getElementById('controlPanel');
        testAreaNode = document.getElementById('testArea');
        contentNode = document.getElementById('content');
        tabsNode = document.getElementById('tabs');

        tabs_1_Node = document.getElementById('tabs-1');

        tabs_1_Node.removeChild(controlPanelNode);
        tabs_1_Node.removeChild(testAreaNode);

        contentNode.removeChild(tabsNode);

        tab_counter = 2;
        newTabId = null;
        $tabs = null;

        contentNode.appendChild(controlPanelNode);
        contentNode.appendChild(testAreaNode);

      }

    });
  }

  newTabId = 'tabs-' + tab_counter;
  var tab_content_id = 'tab_content' + tab_counter;

  // add new tab with new id and new title
  var tab_title = 'new tab ';
  if ((typeof gadgetMetadata != 'undefined') &&
          (typeof gadgetMetadata['modulePrefs'].title != 'undefined')) {
    // open gadget, get the title from gadgetMetadata
    tab_title = gadgetMetadata['modulePrefs'].title;
    if (tab_title.length > 7) {
      tab_title = tab_title.substring(0, 6) + '...';
    }
  }
  $tabs.tabs('add', '#tabs-' + tab_counter, tab_title);

  if (typeof gadgetMetadata != 'undefined') {
    // rendering gadget's header
    var gadgetSiteId = 'gadget-site-' + newTabId;
    var gadgetTemplate = '<div class="portlet">' +
        '<div class="portlet-header">sample to replace</div>' +
        '<div id=' + gadgetSiteId +
        ' class="portlet-content"></div>' + '</div>';

    $(gadgetTemplate).appendTo($('#' + tab_content_id)).addClass(
        'ui-widget ui-widget-content ui-helper-clearfix ui-corner-all')
    .find('.portlet-header')
    .addClass('ui-widget-header ui-corner-all')
    .text(gadgetMetadata['modulePrefs'].title)
    .append('<span id="remove" class="ui-icon ui-icon-closethick"></span>');
  }

  tab_counter++;

  // return the div
  if (typeof gadgetMetadata != 'undefined') {
    return document.getElementById('gadget-site-' + newTabId);
  } else {
    return document.getElementById(tab_content_id);
  }
}


/**
 * private method will be called to destroy dialog object.
 * @private
 * @param {object} site
 *          gadget site.
 */
function closeDialog(site) {
  // this is the site id, we also need to find the dojo dialog widget id
  var iframeId;
  var widgetId = 'dialog_' + dialog_counter; //default

  if (site && site.getActiveSiteHolder()) {
    // get iframe id
    iframeId = site.getActiveSiteHolder().getIframeId();
    if (typeof iframeId != 'undefined') {
      var iframeNode = document.getElementById(iframeId);
      // get dialog widget id
      widgetId = iframeNode.parentNode.id;
    }
  }
  // close the gadget
  CommonContainer.closeGadget(site);

  // close the widget
  $('#' + widgetId).dialog('close');

}
