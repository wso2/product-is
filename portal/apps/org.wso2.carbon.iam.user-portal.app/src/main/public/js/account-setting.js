/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

$(window).load(function(){

    var selector = '.settings-nav li';
    $(selector).not(".collapse-li").click(function () {
        $(selector).not(".collapse-li").removeClass('active');
    });

    // Show the relevant tab from url
    var url = document.location.toString();
    if (url.match('#')) {
        $('.settings-nav a[href="#' + url.split('#')[1] + '"]').tab('show');
    } else {
        $('.settings-content > .tab-pane:first-child').addClass('active');
        $('.settings-nav > ul > li:first-child a').removeClass('collapsed');
        $('#profile-menu li:first-child').addClass('active');
    }

    $( ".sub-menu" ).each(function( index ) {
        if ($(this).children('li').hasClass('active')) {
            $(this).addClass('in');
        }
    });

    // Change hash for select tab
    $('.settings-nav a, .sub-menu a').on('shown.bs.tab', function (e) {
        window.location.hash = e.target.hash;
        $(window).scrollTop(0);
        $('.settings-content').find('.alert').remove();
    });

    //remove readonly input focus 
    $('input[readonly]').on('focus', function () {
        this.blur();
    });

    // add edit indication on input fields
    var editField = $("input[type=text]:not(:read-only),input[type=email]:not(:read-only)");
    editField.closest('.form-group').addClass('has-feedback');
    editField.parent().append('<span class="form-control-feedback edit-icon" aria-hidden="true"></span>');
    editField.hover(function(){
        $(this).parent().find('.edit-icon').append('<i class="fw fw-edit"></i>')
    }, function(){
        $(this).parent().find('.edit-icon').find('i').remove();
    });
    editField.focus(function(){
        $(this).parent().find('.edit-icon').find('i').remove();
    });

});

