function initScript() {
    $("#accountVerification").ready(function () {
        $("#length .status-text").text("At least " + result.passwordMinLength + " characters");
        if (result.isNumbersIncluded) {
            $("#pnum .status-text").text("At least one number");
        }
        if (result.isUpperCaseNeeded && result.isLowerCaseNeeded) {
            $("#capital .status-text").text("At least one lowercase & one uppercase letter");
        }
        if (result.isSpecialCharacterNeeded) {
            $("#spchar .status-text").text("At least one special character");
        }
    });
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

        var valid = '<i class="fw fw-success"></i>';
        var invalid = '<i class="fw fw-error"></i>';

        if (a.length >= result.passwordMinLength) {

            $("#length").removeClass("invalid").addClass("valid");
            $("#length .status_icon").html(valid);
            score++;
        } else {
            $("#length").removeClass("valid").addClass("invalid");
            $("#length .status_icon").html(invalid);
        }

        // at least 1 digit in password
        if (result.isNumbersIncluded) {
            if (a.match(/\d/)) {
                $("#pnum").removeClass("invalid").addClass("valid");
                $("#pnum .status_icon").html(valid);
                score++;
            } else {
                $("#pnum").removeClass("valid").addClass("invalid");
                $("#pnum .status_icon").html(invalid);
            }
        }

        // at least 1 capital & lower letter in password
        if (result.isUpperCaseNeeded && result.isLowerCaseNeeded) {
            if (a.match(/[A-Z]/) && a.match(/[a-z]/)) {
                $("#capital").removeClass("invalid").addClass("valid");
                $("#capital .status_icon").html(valid);
                score++;
            } else {
                $("#capital").removeClass("valid").addClass("invalid");
                $("#capital .status_icon").html(invalid);
            }
        }

        // at least 1 special character in password {
        if (result.isSpecialCharacterNeeded) {
            if (a.match(/.[!,@,#,$,%,^,&,*,?,_,~,-,(,)]/)) {
                $("#spchar").removeClass("invalid").addClass("valid");
                $("#spchar .status_icon").html(valid);
                score++;
            } else {
                $("#spchar").removeClass("valid").addClass("invalid");
                $("#spchar .status_icon").html(invalid);
            }
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
    $('input[type=password]').after('<span class="hide-pass" title="Show/Hide Password"><i class="fw fw-view"></i>' +
        '</span>');
    var highPass = $('.hide-pass');
    $(highPass).click(function () {
        if ($(this).find('i').hasClass("fw-hide")) {
            $(this).parent().find('input[data-schemaformat=password]').attr('type', 'password');
            $(this).find('i').removeClass("fw-hide");
            $(this).find('i').addClass("fw-view");
        } else {
            $(this).find('i').removeClass("fw-view");
            $(this).find('i').addClass("fw-hide");
            $(this).parent().find('input[data-schemaformat=password]').attr('type', 'text');
        }
    });
}

