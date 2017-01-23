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
    });
});

