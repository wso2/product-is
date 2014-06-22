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

$(function() {

  // Base html template that is used for the gadget wrapper and site
  var gadgetTemplate = '<div class="portlet">' +
                '<div class="portlet-header" id="portlet-id">' +
                'sample to replace</div>' +
                '<div id="gadget-site" class="portlet-content"></div>' +
                '</div>';

  // Variable to keep track of gadget current view
  // for collapse and expand gadget actions.
  var currentView = 'default';

  // ID used to associate gadget site
  var curId = 0;

  // Navigate to the new view and save it as current view
  navigateView = function(gadgetSite, gadgetURL, toView) {
    // Save the current view for collapse, expand gadget
    currentView = toView;
    CommonContainer.navigateView(gadgetSite, gadgetURL, toView);
  };

  // Handle gadget collapse, expand, and remove gadget actions
  handleNavigateAction = function(portlet, gadgetSite, gadgetURL, actionId) {
    // Remove button was click, remove the portlet/gadget
    if (actionId === 'remove') {
      if (confirm('This gadget will be removed, ok?')) {
        if (gadgetSite) {
          CommonContainer.closeGadget(gadgetSite);
        }
        if (gadgetURL) {
          CommonContainer.unloadGadget(gadgetURL);
          urlsToGadgetIdMap[gadgetURL] = null;
        }
        portlet.remove();
      }
    } else if (actionId === 'expand') {
      // Navigate to currentView prior to collapse gadget
      if (gadgetSite) {
        CommonContainer.navigateView(gadgetSite, gadgetURL, currentView);
      }
      else {
        preRenderGadget(gadgetURL);
      }
    } else if (actionId === 'collapse') {
      CommonContainer.closeGadget(gadgetSite);
    }
  };

  // Create a gadget with navigation tool bar header
  // enabling gadget collapse, expand and remove.
  window.buildGadget = function(result,gadgetURL,lazyload) {
    var gadgetSiteString = "$(this).closest(\'.portlet\')." +
      "find(\'.portlet-content\').data(\'gadgetSite\')";
    var newGadgetSite = gadgetTemplate;
    newGadgetSite = newGadgetSite.replace(/(portlet-id)/g, '$1-' + curId);
    newGadgetSite = newGadgetSite.replace(/(gadget-site)/g, '$1-' + curId);
    var gadgetSiteData = null;
    $(newGadgetSite).appendTo($('#gadgetArea')).addClass(
        'ui-widget ui-widget-content ui-helper-clearfix ui-corner-all')
        .find('.portlet-header')
        .addClass('ui-widget-header ui-corner-all')
        .text(result[gadgetURL]['modulePrefs'].title)
    .append('<span id="remove" class="ui-icon ui-icon-closethick"></span>')
    .append('<span id="expand" class="ui-icon ui-icon-plusthick"></span>')
    .append('<span id="collapse" class="ui-icon ui-icon-minusthick"></span>')
    .end()
      .find('.portlet-content')
      .data('gadgetSite', lazyload ? null :
        CommonContainer.renderGadget(gadgetURL, curId));

    // determine which button was clicked and handle the appropriate event.
    $('#portlet-id-'+ curId + ' > .ui-icon').click(
        function() {
          handleNavigateAction(
              $(this).closest('.portlet'),
              $(this).closest('.portlet').find('.portlet-content')
                .data('gadgetSite'),
              gadgetURL, this.id);
        }
    );

    var gadgetId = urlsToGadgetIdMap[gadgetURL];
    if (!gadgetId) {
      urlsToGadgetIdMap[gadgetURL] = curId;
    }
  };

  // Add single gadget
  addGadget = function(gadgetUrl, lazyload) {
    CommonContainer.preloadGadget(gadgetUrl, function(result) {
      for (var gadgetURL in result) {
       window.buildGadget(result, gadgetURL, lazyload);
       curId++;
      }
    });
    return true;
  };

});
