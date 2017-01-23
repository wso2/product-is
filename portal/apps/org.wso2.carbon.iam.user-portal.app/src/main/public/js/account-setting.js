<<<<<<< HEAD
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

    window.location.hash = '';

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
    });

    //remove readonly input focus 
    $('input[readonly]').on('focus', function () {
        this.blur();
    });

    // add edit indication on input fields
    var editField = $("input[type=text]:not(:read-only),input[type=email]:not(:read-only)");
    editField.closest('.form-group').addClass('has-feedback')
    editField.parent().append('<span class="form-control-feedback edit-icon" aria-hidden="true"></span>');
    editField.hover(function(){
        $(this).parent().find('.edit-icon').append('<i class="fw fw-edit"></i>')
    }, function(){
        $(this).parent().find('.edit-icon').find('i').remove();
    });
    editField.focus(function(){
        $(this).parent().find('.edit-icon').find('i').remove();
    });

=======
$(window).load(function(){

    var selector = '.settings-nav li';
    $(selector).not(".collapse-li").click(function () {
        $(selector).not(".collapse-li").removeClass('active');
    });
<<<<<<< HEAD
>>>>>>> c06d2f5... Adding Account settings template
=======

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
    });
<<<<<<< HEAD

<<<<<<< HEAD
>>>>>>> e4b1725... Account settings - Append selected tab to the url
=======
    //password strength meter logic
    $("#newPassword").on("focus keyup", function () {
        var score = 0;
        var a = $(this).val();
        var desc = new Array();

        // strength desc
        desc[0] = "Too short";
        desc[1] = "Weak";
        desc[2] = "Good";
        desc[3] = "Strong";
        desc[4] = "Best";

        // password length
        var valid = '<i class="fw fw-success"></i>';
        var invalid = '<i class="fw fw-error"></i>';

        if (a.length >= 6) {
            $("#length").removeClass("invalid").addClass("valid");
            $("#length .status_icon").html(valid);
            score++;
        } else {
            $("#length").removeClass("valid").addClass("invalid");
            $("#length .status_icon").html(invalid);
        }

        // at least 1 digit in password
        if (a.match(/\d/)) {
            $("#pnum").removeClass("invalid").addClass("valid");
            $("#pnum .status_icon").html(valid);
            score++;
        } else {
            $("#pnum").removeClass("valid").addClass("invalid");
            $("#pnum .status_icon").html(invalid);
        }

        // at least 1 capital & lower letter in password
        if (a.match(/[A-Z]/) && a.match(/[a-z]/)) {
            $("#capital").removeClass("invalid").addClass("valid");
            $("#capital .status_icon").html(valid);
            score++;
        } else {
            $("#capital").removeClass("valid").addClass("invalid");
            $("#capital .status_icon").html(invalid);
        }

        // at least 1 special character in password {
        if ( a.match(/.[!,@,#,$,%,^,&,*,?,_,~,-,(,)]/) ) {
            $("#spchar").removeClass("invalid").addClass("valid");
            $("#spchar .status_icon").html(valid);
            score++;
        } else {
            $("#spchar").removeClass("valid").addClass("invalid");
            $("#spchar .status_icon").html(invalid);
        }

        if(a.length > 0) {
            //show strength text
            $("#passwordDescription").text(desc[score]);
            // show indicator
            $("#passwordStrength").removeClass().addClass("strength"+score);
        } else {
            $("#passwordDescription").text("Password not entered");
            $("#passwordStrength").removeClass().addClass("strength"+score);
        }
    });

    $("#newPassword").popover({ html:true, content: $("#password_strength_wrap").html(), placement: 'top', trigger:'focus keypress' });
    $("#newPassword").blur(function () {
        $(".password_strength_meter .popover").popover("hide");
    });
<<<<<<< HEAD
    
>>>>>>> ec888d6... Adding password strength to password input
=======

    //confirm password validation
    $( "#password-update-form" ).validate({
        rules: {
            confirmPassword: {
                equalTo: "#newPassword"
            }
        }
    });
>>>>>>> bb876d9... Adding confirm password validation
=======
>>>>>>> 4520c3c... Reset password code structuring
});

