/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

//password strength meter logic
$(window).load(function () {

    //password strength meter logic
    $("#newPassword").on("focus keyup", function () {
        var score = 0;
        var a = $(this).val();
        var desc = new Array();

        // strength desc
        desc[0] = "Too short";
        desc[1] = "Weak";
        desc[2] = "Fair";
        desc[3] = "Good";
        desc[4] = "Strong";

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
        if (a.match(/.[!,@,#,$,%,^,&,*,?,_,~,-,(,)]/)) {
            $("#spchar").removeClass("invalid").addClass("valid");
            $("#spchar .status_icon").html(valid);
            score++;
        } else {
            $("#spchar").removeClass("valid").addClass("invalid");
            $("#spchar .status_icon").html(invalid);
        }

        if (a.length > 0) {
            //show strength text
            $("#passwordDescription").text(desc[score]);
            // show indicator
            $("#passwordStrength").removeClass().addClass("strength" + score);
        } else {
            $("#passwordDescription").text("Password not entered");
            $("#passwordStrength").removeClass().addClass("strength" + score);
        }
    });

    $("#newPassword").popover({
        html: true,
        content: $("#password_strength_wrap").html(),
        placement: 'top',
        trigger: 'focus keypress'
    });
    $("#newPassword").blur(function () {
        $(".password_strength_meter .popover").popover("hide");
    });

    jQuery.validator.addMethod("pwcheck", function (value) {
        return /[a-z]/.test(value) // has a lowercase letter
            && /[A-Z]/.test(value) // has a uppercase letter
            && /[^A-Za-z0-9]/.test(value) // has a special character
            && /\d/.test(value); // has a digit
    });

    //password update form validation
    $("#password-update-form").validate({
        rules: {
            confirmPassword: {
                equalTo: "#newPassword",
                required: {
                    depends: function (element) {
                        return $("#newPassword").is(":not(:blank)");
                    }
                }
            },
            newPassword: {
                pwcheck: true,
                minlength: 6,
                required: true
            }
        },
        messages: {
            confirmPassword: {
                equalTo: "These passwords do not match.",
                required: "Please re-enter the new password."
            },
            newPassword: {
                minlength: "Password should be at least {0} characters long.",
                pwcheck: "Password must have a minimum strength of Strong.",
                required: "Required to provide a new password."
            }
        }
    });

    //add show /hide option on password field
    $('input[type=password]').after('<span class="hide-pass" title="Show/Hide Password"><i class="fw fw-view"></i> </span>');
    var highPass = $('.hide-pass');
    $(highPass).click(function(){
        if($(this).find('i').hasClass("fw-hide")){
            $(this).parent().find('input[data-schemaformat=password]').attr('type', 'password');
            $(this).find('i').removeClass( "fw-hide" );
            $(this).find('i').addClass( "fw-view");
        }else{
            $(this).find('i').removeClass( "fw-view" );
            $(this).find('i').addClass( "fw-hide");
            $(this).parent().find('input[data-schemaformat=password]').attr('type', 'text');
        }
    });

});