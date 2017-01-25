<<<<<<< HEAD
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
    $('.login-form-wrapper').parents('body').addClass('background-grey');
});

$('#domainSelector').change(function () {
    var domain = document.getElementById('domainSelector').value;
    if (domain != "default") {
        document.getElementById("domain").value = domain;
    }
});
<<<<<<< HEAD
=======
$(window).load(function () {
    $('.login-form-wrapper').parents('body').addClass('background-grey');
});
>>>>>>> c317e55... login signup pages UI fixes
=======
>>>>>>> d30cce6... Add domain selector configurations in app
