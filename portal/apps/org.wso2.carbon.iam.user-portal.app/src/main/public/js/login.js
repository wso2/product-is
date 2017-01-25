$(window).load(function () {
    $('.login-form-wrapper').parents('body').addClass('background-grey');
});

$('#domainSelector').change(function () {
    var domain = document.getElementById('domainSelector').value;
    if (domain != "default") {
        document.getElementById("domain").value = domain;
    }
});
