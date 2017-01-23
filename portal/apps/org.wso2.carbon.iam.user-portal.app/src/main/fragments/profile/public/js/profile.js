$(window).load(function () {

    $('#image-edit').click(function () {
        $('#image').click();
    });

    $('#image').change(function(){
        $('#image-uploader').submit();
    });
});