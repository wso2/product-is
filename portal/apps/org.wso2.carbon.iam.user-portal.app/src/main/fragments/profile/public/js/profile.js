$(window).load(function () {

    $('#image-edit').click(function () {
        $('#image').click();
    });

    $('#image').change(function(){
        $('#image-uploader').submit();
    });


    $.validator.addMethod(
        "regex",
        function(value, element, regexp) {
            var re = new RegExp(regexp);
            return this.optional(element) || re.test(value);
        },
        "Please check your input."
    );

    $('#default-form').validate();
    $('#employee-form').validate();
    
    $('.profile-form input[type=text], .profile-form input[type=email]').each(function(){
        var pattern = $(this).attr('pattern');

        if ((typeof pattern !== typeof undefined) && pattern !==".*" ) {
            $("#"+$(this).attr('id')).rules("add", { regex: pattern.toString()});
        }
    })

});