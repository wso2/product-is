/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

$(window).load(function () {

    $('#image-edit').click(function () {
        $('#image').click();
    });

    $('#image').change(function () {
        $('#image-uploader').submit();
    });


    $.validator.addMethod(
        "regex",
        function (value, element, regexp) {
            var re = new RegExp(regexp);
            return this.optional(element) || re.test(value);
        },
        "Please enter a valid input."
    );

    $('#default-form').validate();
    $('#employee-form').validate();

    $('.profile-form input[type=text]').each(function () {
        var pattern = $(this).attr('pattern');

        if ((typeof pattern !== typeof undefined) && pattern !== ".*") {
            $("#" + $(this).attr('id')).rules("add", {regex: pattern.toString()});
        }
    })
});

window.setTimeout(function () {
    $(".alert-success").fadeTo(500, 0).slideUp(500, function () {
        $(this).remove();
    });
}, 5000);
