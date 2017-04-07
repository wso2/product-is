$(document).ready(function () {
    var uriPath = window.location.pathname;
    $('#sidebar-portal .pages a').attr('href');
    
    var currentPage = $('#sidebar-portal .pages a[href="' + uriPath + '"]');
    currentPage.parents('li').addClass('active');
});