$(function () {
    $('#dash-pages > ul > li').hover(function () {
        $(this).children('div').show();
    }, function () {
        $(this).children('.dash-page-controls').hide();
    });
});