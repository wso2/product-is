$(function () {

/*    setInterval(function () {
        $.get('/portal/apis/session-check.jag');
    }, 10000 * 6);*/

    var login = function () {
        if (!$("#form-login").valid()) return;

        $('#btn-login').addClass('disabled').text('Signing in');


        var username = $('#inp-login-username').val();
        var password = $('#inp-login-password').val();
        var path = $('#inp-login-path').val();

        if (path == 'null')
            path = caramel.context + '/dashboard.jag';

        caramel.post('/apis/user/login', JSON.stringify({
            username: username,
            password: password
        }), function (data) {
            if (!data.error) {
                window.location = path;
            } else {
                var msg = data.message.replace(/[0-9a-z.+]+:\s/i, '');

                $('#login-alert').html(msg).fadeIn('fast');
                $('#btn-login').text('Sign in').removeClass('disabled');
            }
        }, "json");

    };

    var register = function () {
        if (!$("#form-register").valid()) return;

        $('#btn-register-submit').text('Register').addClass('disabled');

        caramel.post('/apis/user/register', JSON.stringify({
            username: $('#inp-reg-username').val(),
            password: $('#inp-reg-password').val()
        }), function (data) {
            if (!data.error) {
                location.reload();
            } else {
                var msg = data.message.replace(/[0-9a-z.+]+:\s/i, '');
                $('#register-alert').html(msg).fadeIn('fast');
                $('#btn-register-submit').text('Register').removeClass('disabled');
            }
        }, "json");
    };

    $('a[data-toggle=modal]').live('click', function () {
        $($(this).attr('data-target')).modal("show").on('shown', function () {
            $('input[type=text]:first').focus();
        });
    });

    $('#btn-signout').live('click', function () {
        caramel.post("/apis/user/logout", function (data) {
            window.location = caramel.context;
        }, "json");
    });

    $('#btn-login').bind('click', login);

    $('#modal-login input').bind('keypress', function (e) {
        if (e.keyCode === 13) {
            login();
        }
    });

    $('#inp-reg-username').change(function () {
        var username = $(this).val();
        caramel.post('/apis/user/exists', JSON.stringify({
            username: $('#inp-reg-username').val()
        }), function (data) {
            if (data.error || data.exists) {
                $('#register-alert').html(data.message).fadeIn('fast');
            } else {
                $('#register-alert').fadeOut('slow');
            }
        }, "json");
    });

    $('#btn-register-submit').click(register);

    $('#modal-register input').keypress(function (e) {
        if (e.keyCode === 13) {
            register();
        }
    });

    $('.modal').on('hidden', function () {
        $(this).find('form').find('input[type=text], input[type=password], input[type=number], input[type=email], textarea').val('');
        $(this).find('.alert').html('').hide();
    })
    /*
     $('#btn-register').bind('click', register);

     $('#modal-register input').bind('keypress', function(e) {
     if (e.keyCode === 13) {
     register();
     }
     });
     */
    $('#sso-login').click(function () {
        $('#sso-login-form').submit();

    });

    $('.dropdown-menu input, .dropdown-menu label').click(function (e) {
        e.stopPropagation();
    });

    $("[data-toggle='tooltip']").tooltip();

});
