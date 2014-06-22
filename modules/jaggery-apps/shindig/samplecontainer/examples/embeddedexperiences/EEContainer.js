/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

//When the document is ready kick off the request so we can render the activity stream
$(document).ready(function() {
  osapi.activitystreams.get({userId: 'john.doe'}).execute(function(response){
    renderAS(response);
  });
});



//Initiate the common container code and register any RPC listeners for embedded experiences
var config = {};
config[osapi.container.ContainerConfig.RENDER_DEBUG] = '1';
var CommonContainer = new osapi.container.Container(config);
CommonContainer.init = new function() {

  CommonContainer.views.createElementForEmbeddedExperience = function(rel, opt_gadgetInfo,
          opt_viewTarget, opt_coordinates){
    if(opt_gadgetInfo && !opt_gadgetInfo.error) {
      var title = opt_gadgetInfo.modulePrefs.title;
      if(title) {
        $('#title').html(opt_gadgetInfo.modulePrefs.title);
      }
    }
    var top = $(rel).offset().top,
      left =  $(rel).offset().left;
    if(opt_viewTarget == 'FLOAT' && opt_coordinates) {
      $('#preview').css({
        top: (top + opt_coordinates.top) + 'px',
        left: (left + opt_coordinates.left) + 'px',
        'margin-top': '0px',
        position: 'absolute'
      });
    }
    else {
      $('#preview').css({
        'margin-top': top +'px',
        top: 'auto',
        left: 'auto',
        position: 'static'
      });
    }

    return $('#previewSite').get(0);
  };

  CommonContainer.views.destroyElement = function(site) {
    CommonContainer.ee.close(site);
    $('#title').html('');
  };
}

/**
 * Renders the activity stream on the page
 * @param stream the activity stream json.
 * @return void.
 */
function renderAS(stream) {
  jQuery.each(stream.list, createAccordianEntry);
  $('#accordion').accordion({
    clearStyle: true,
    active: false,
    change: function(event, ui) {
      closeCurrentGadget();
      onAccordionChange(stream, event, ui);
    }

  });
}

/**
 * Closes the current gadget when a new accordian is selected.
 */
var currentEESite;
function closeCurrentGadget() {
  if (currentEESite)
    CommonContainer.ee.close(currentEESite);

  var previewSite = $('#previewSite').get(0);
  var previewChildren = previewSite.childNodes;
  if (previewChildren.length > 0) {
    var iframe = previewChildren[0];
    var iframeId = iframe.getAttribute('id');
    var site = CommonContainer.getGadgetSiteByIframeId_(iframeId);
    CommonContainer.ee.close(site);
  }
  $('#title').html('');
}

/**
 * Called when a new accordian pane is opened.
 * @param stream the activity stream for the accordian.
 * @param event the event that occurred.
 * @param ui the ui elements changing.
 * @return void.
 */

function onAccordionChange(stream, event, ui) {
  var id = ui.newHeader.context.id;
  var localStream = stream;
  var entry = localStream.list[id];
  var extensions = entry.openSocial;
  if (extensions) {
    var embed = extensions.embed;
    if (embed) {
      var eeElement = $('#ee' + id).get(0);
      var urlRenderingParams = {
          'height' : 400,
          'width' : 650
      };

      // Add additional container context
      var containerContext = {};
      containerContext.associatedContext = {"id" : entry.id , "type" : "opensocial.ActivityEntry",
          "objectReference" : entry};

      CommonContainer.ee.navigate(eeElement, embed, {'urlRenderParams' : urlRenderingParams},
         function(site, metaData) {
           console.log('Embedded Experiences callback called');
           console.log(gadgets.json.stringify(metaData));
           currentEESite = site;
         }, containerContext);
    }
  }
}

/**
 * Called for each activity entry and adds the necessary HTML to the page.
 * Check if preferredExperience is added to the EE model and set title accordingly.
 * @param i the item in the activity stream we are currently rendering.
 * @param entry the activity stream entry json.
 * @return void.
 */
function createAccordianEntry(i, entry) {
  var title = entry.title;

  // Lets try to see if the activity entry has preferredExperience extension.
  var extensions = entry.openSocial;
  if(extensions) {
    var embed = extensions.embed;
    if(embed && embed.preferredExperience) {
      var linkText = getPreferredExperienceLinkText(embed[osapi.container.ee.DataModel.PREFERRED_EXPERIENCE]);
      if(linkText) {
        title = linkText;
      }
    }
  }
  var result = '<h3 id=' + i + '><a href="#">' + title + '</a></h3><div>';
  if (entry.body)
    result = result + '<p>' + entry.body + '</p>';
  result = result + '<div id="ee' + i + '"></div></div>';

  $('#accordion').append(result);
}

function getPreferredExperienceLinkText(preferredExperience) {
  if(preferredExperience && preferredExperience.display) {
    if(preferredExperience.display.type !== osapi.container.ee.DisplayType.TEXT) {
      return null;
    }
    var linkText = preferredExperience.display.label;
    return linkText;
  }
}
