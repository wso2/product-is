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

//  Setup the base container objects for managing layout, gadget, and container configuration
$(function() {
  // TODO: enable drag and drop with the portlet at some point
  $('.column').sortable({
    connectWith: '.column',
    update: function(event, ui) {
      // TODO: There is an issue with drag & drop
    }
  });

  $('.portlet').addClass(
      'ui-widget ui-widget-content ui-helper-clearfix ui-corner-all').find(
      '.portlet-header').addClass('ui-widget-header ui-corner-all').prepend(
      '<span class="ui-icon ui-icon-minusthick"></span>').end().find(
      '.portlet-content');
  $('.portlet-header .ui-icon').click(function() {
    $(this).toggleClass('ui-icon-minusthick').toggleClass('ui-icon-plusthick');
    $(this).parents('.portlet:first').find('.portlet-content').toggle();
  });

  $('.column').disableSelection();
});
