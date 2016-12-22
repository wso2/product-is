/*This is a template for filling with handlebars*/
$(document).ready(function() {
    $(".profile").click(function() {
        $.get('/user-portal/public/components/root/base/templates/default-profile.hbs', function (templateData) {
            var template=Handlebars.compile(templateData);
            var context = { userName: 'indunil89', firstName: 'Indunil', lastName: 'Rathnayake', emailAdd: 'indunil@Wwso2.com', telephone: '0717834167'};
            var theCompiledHtml = template(context);
            $('#api-content').html(theCompiledHtml);
        }, 'html');
    });
});