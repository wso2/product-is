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

$(window).load(function(){

   /* $("#default").show();*/

    $('#profileSelector').change(function () {
        var profile = $(this).val();

        $(this).find("#option-profile:selected").each(function(){
            var optionValue = $(this).attr("value");
            if(optionValue){
                $(".optionBox").not("." + optionValue).hide();
                $("." + optionValue).show();
            } else{
                $(".optionBox").hide();
            }
        });

    }).change();


});








