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

$(document).ready(function () {
    $('.panel-extended').on('shown.bs.collapse', function (e,f) {
        var elem = $(this).children().children('.in');

        if($(elem).hasClass('in')){
            $(elem).prev().find('input[type=radio]').prop('checked',true);
        }
    });

    $('.pw').hide();
    if(passcode){

        $("#newPassword").prop('type', 'password');
        $('#newPassword').val(passcode);
        $('#accordion2').collapse("show");
        $('.pw').show();
        $(".hide-pass").show();

        if ($('.hide-pass').length < 1) {
            $("#newPassword").after('<span class="hide-pass" title="Show/Hide Password">' +
            '<i class="fw fw-view"></i> </span>');
        }
        $('#genbtn').prop('value', 'Re-generate Password');
    }
});

//add show /hide option on password field
$('input[type=password], input[type=text]').after('<span class="hide-pass" title="Show/Hide Password"><i class="fw fw-view"></i> </span>');
var hidePass = $('.hide-pass');
$(document).on('click', '.hide-pass', function (e) {
    if ($(this).find('i').hasClass("fw-hide")) {
        $(this).parent().find('input[data-schemaformat=password]').prop('type', 'password');
        $(this).find('i').removeClass("fw-hide");
        $(this).find('i').addClass("fw-view");
    } else {
        $(this).find('i').removeClass("fw-view");
        $(this).find('i').addClass("fw-hide");
        $(this).parent().find("input[type='password']").prop('type', 'text');
    }
});

window.setTimeout(function() {
    $(".alert-success").fadeTo(500, 0).slideUp(500, function(){
        $(this).remove();
    });
}, 5000);

window.setTimeout(function() {
    $(".alert-danger").fadeTo(500, 0).slideUp(500, function(){
        $(this).remove();
    });
}, 5000);