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

// Create the common container object.
var CommonContainer = new osapi.container.Container({});

// Default the security token for the container. Using this example security
// token requires enabling the DefaultSecurityTokenCodec to let
// UrlParameterAuthenticationHandler create valid security token.
shindig.auth.updateSecurityToken('john.doe:john.doe:appid:cont:url:0:default');

// Wrapper function to set the gadget site/id and default width.
CommonContainer.renderGadget = function(gadgetURL, gadgetId) {
  // going to hardcode these values for width.
  var el = document.getElementById('gadget-site-' + gadgetId);
  var parms = {};
  parms[osapi.container.RenderParam.WIDTH] = '100%';
  var gadgetSite = CommonContainer.newGadgetSite(el);
  CommonContainer.navigateGadget(gadgetSite, gadgetURL, {}, parms);
  return gadgetSite;
};

// Function for pre-rendering gadgets.  Gadget pre-rendering
// occurs when an action contributed by a pre-loaded gadget
// is executed.
function preRenderGadget(gadgetUrl, opt_params) {
  var gadgetId = getGadgetId(gadgetUrl);
  var el = $('#gadget-site-' + gadgetId);
  var gadgetSite = CommonContainer.renderGadget(gadgetUrl, gadgetId);
  el.data('gadgetSite', gadgetSite);
  return gadgetSite;
}

// Common container init function.
CommonContainer.init = new function() {
  // Map needed for lazy loading of gadgets
  urlsToGadgetIdMap = {};

  // Register our rendering functions with the action service
  if (CommonContainer.actions) {
    // Called when an action should be displayed in the container
    CommonContainer.actions.registerShowActionsHandler(showActions);

    // Called when a action should be removed from the container
    CommonContainer.actions.registerHideActionsHandler(hideActions);

    // Called for actions contributed by pre-loaded gadgets (lazy load)
    CommonContainer.actions.registerNavigateGadgetHandler(preRenderGadget);
  }
}

// Support for lazy loading gadgets
function getGadgetId(url) {
  if (urlsToGadgetIdMap) {
    return urlsToGadgetIdMap[url];
  }
}

// Wrapper function to add gadgets to the page.
CommonContainer.addGadgetToPage = function(gadgetURL, lazyLoad) {
  addGadget(gadgetURL, lazyLoad);
};

// Wrapper function to expand a gadget
CommonContainer.navigateView = function(gadgetSite, gadgetURL, view) {
  var renderParms = {};
  if (view === null || view === '') {
    view = 'default';
  }
  renderParms[osapi.container.RenderParam.WIDTH] = '100%';
  renderParms['view'] = view;
  CommonContainer.navigateGadget(gadgetSite, gadgetURL, {}, renderParms);
};

// see peoplehelpers.js
osapi.people.getViewer = function(options) {
  options = options || {};
  options.userId = '@viewer';
  options.groupId = '@self';
  return osapi.people.get(options);
};

// see peoplehelpers.js
osapi.people.getViewerFriends = function(options) {
  options = options || {};
  options.userId = '@viewer';
  options.groupId = '@friends';
  return osapi.people.get(options);
};

// Function to display actions
function showActions(actions) {
  var itemObj = actions[0];
  if (!itemObj.path && !itemObj.dataType) {
    // object is invalid!
    return;
  }
  // bind the action to the specified data object type
  if (itemObj.dataType) {
    if (itemObj.dataType == 'opensocial.Person') {
      addPersonAction(itemObj);
    }
  }
  // bind the action to the specified path (container UI elements)
  if (itemObj.path && itemObj.path.length > 0) {
    addContainerAction(itemObj);
  }
}

// Adds the specified action to a person element.
function addPersonAction(itemObj) {
  // select all person elements
  var personActionDiv = $('.personActions');

  // create a link and append it to each person element
  var actionStr = '';
  if (itemObj.icon && itemObj.icon.length > 0) {
    actionStr += '<img src="' + itemObj.icon + '"/>';
  }
  actionStr += '<a name="person-action-' + itemObj.id + '" title="' +
               itemObj.tooltip + '" href="#">' + itemObj.label + '</a>';
  actionStr = '<span class="' + itemObj.id + '">' + actionStr + '</span>';
  var actionLink = $(actionStr);

  // add a separator if needed
  if (personActionDiv.children().length > 0) {
    personActionDiv.append(' | ');
  }

  // select all links that were added and set the click handler
  personActionDiv.append(actionLink);
  $('a[name="person-action-' + itemObj.id + '"]').each(function(i) {
    $(this).click(function() {
      CommonContainer.actions.runAction(itemObj.id);
    });
  });
}

// Adds an action to the container UI
function addContainerAction(itemObj) {
  var pathParts = itemObj.path.split('/');
  var pathType = pathParts.shift();
  var pathScope = pathParts.shift();
  var remainingPath = pathParts.join('/');
  var contributionBar;

  // right now we support contributing to the global menubar
  if (pathType == 'container' && pathScope == 'navigationLinks') {
    contributionBar = $('#globalMenubar');
    contributionBar.show();
  }

  // create the action element
  var actionStr = '';
  if (itemObj.icon && itemObj.icon.length > 0) {
    actionStr += '<img src="' + itemObj.icon + '"/>';
  }
  actionStr += '<a title="' + itemObj.tooltip + '" href="#">'
               + itemObj.label + '</a>';
  actionStr = '<span class="' + itemObj.id + '">' + actionStr + '</span>';
  var actionLink = $(actionStr);

  // add a separator if needed
  if (contributionBar.children().length > 0) {
    contributionBar.append(' | ');
  }

  // add the new action
  contributionBar.append(actionLink);
  actionLink.click(function() {
    CommonContainer.actions.runAction(itemObj.id);
  });
}

// Function to hide actions
function hideActions(actions) {
  var itemObj = actions[0];
  if (itemObj.path || itemObj.dataType) {
    // remove the action from the specified data object type
    if (itemObj.dataType && itemObj.dataType == 'opensocial.Person') {
      removePersonAction(itemObj);
    }
    // remove the action to the specified path (container UI elements)
    if (itemObj.path && itemObj.path.length > 0) {
      removeContainerAction(itemObj);
    }
  }
}

// Removes the specified action from a person element.
function removePersonAction(itemObj) {
  // hack - actions should be removed individually
  $('.personActions').empty();
}

// Removes the specified action from the container UI
function removeContainerAction(itemObj) {
  // hack - actions should be removed individually
  $('#globalMenubar').empty();
  $('#globalMenubar').hide();
}

// Runs the action specified in the action_id_field
function runAction() {
  id = document.getElementById('action_id_field').value;
    CommonContainer.actions.runAction(id, CommonContainer.selection.getSelection());
}
