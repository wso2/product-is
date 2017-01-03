$(window).load(function(){
    var selector = '.settings-nav li';

    $(selector).click(function () {
        $(selector).not(".collapse-li").removeClass('active');
    });
});

