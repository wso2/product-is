/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

$(function () {
    var mode = "APPEND"; // Available modes [OVERWRITE,APPEND, PREPEND]
    var questions = {"questions" : [{"question" : "This is question 1?"}]};

    $( document ).ready(function() {
        if(result && result.option === 'security-question-recovery'){
            $('.email-recovery').parent().attr("disabled", "disabled");
            $('.security-question-recovery').closest('.recover-option-container').fadeIn();
            $("input[name=recover-option][value=email-recovery]").attr("disabled",true);
            $("input[name=recover-option][value=security-question-recovery]").prop("checked",true);
        }
    });

    $('input[name=recover-option]').click(function(){
        var button = this.value;
        if(button === "email-recovery"){
            $('.sec-question-answer').removeAttr("required");
        }
        if(button === "security-question-recovery"){
            $('.sec-question-answer').prop('required',true);;
        }
    });
});
