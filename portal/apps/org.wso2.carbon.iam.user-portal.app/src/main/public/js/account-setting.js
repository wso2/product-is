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
    });

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
    
});

