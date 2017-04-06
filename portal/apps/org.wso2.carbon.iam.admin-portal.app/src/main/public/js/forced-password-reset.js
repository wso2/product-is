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
$(window).load(function () {
    $('.login-form-wrapper').parents('body').addClass('background-grey');
    //setting offline Pass Code as the default
    $('#verificationSelector option[value=' + $('#verificationSelector').attr('data-primary') + ']').prop('selected', 'selected');
    var resetMethod = $('#verificationSelector').val();
    $("#verificationSelector").val(resetMethod);
});

$(document).ready(function () {
    $('.pw').hide();
    $("#generatePassword").click(function () {
        $('.pw').show();
        $(".hide-pass").show();

        $("#newPassword").prop('type', 'password');

        if ($('.hide-pass').length < 1) {
            $("#newPassword").after('<span class="hide-pass" title="Show/Hide Password">' +
            '<i class="fw fw-view"></i> </span>');
        }

        $("#reset").prop('disabled', false);
        $(this).prop('value', 'Re-generate Password');

        var password = $("#newPassword").val();
        $.ajax({
            type: "GET",
            url: "/admin-portal/root/apis/identityStore-micro-service/generatePassCode",
            success: function (result) {
                $("#newPassword").val(result);
            }
        });
    });
});


$('#verificationSelector').change(function () {
    var resetMethod = $(this).val();

});

//add show /hide option on password field
$('input[type=password], input[type=text]').after('<span class="hide-pass" title="Show/Hide Password"><i class="fw fw-view"></i> </span>');
var hidePass = $('.hide-pass');
$(document).on('click', '.hide-pass', function (e) {
    if ($(this).find('i').hasClass("fw-hide")) {
        $(e.originalEvent.srcElement).parents('.well').find("input[type='text']").prop('type', 'password');
        //$(this).parent().find('input[data-schemaformat=password]').prop('type', 'password');
        $(this).find('i').removeClass("fw-hide");
        $(this).find('i').addClass("fw-view");
    } else {
        $(this).find('i').removeClass("fw-view");
        $(this).find('i').addClass("fw-hide");
        $(e.originalEvent.srcElement).parents('.well').find("input[type='password']").prop('type', 'text');
        //$(this).parent().find("input[type='password']").prop('type', 'text');
    }
});