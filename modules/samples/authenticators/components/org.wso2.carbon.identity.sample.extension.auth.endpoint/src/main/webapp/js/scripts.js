/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

$(document).ready(function(){
    $('#authorizeLink').click(function(){
        $('#loginForm').show('slow');
    });
	$('#loginBtn').click(function(){
			var error = "";
			if($('#oauth_user_name').val() == ""){
				error += '<div>Username field is empty.</div>';
			}
			if($('#oauth_user_password').val() == ""){
				error += '<div>Password field is empty.</div>';
			}
			if(error == ""){
				$('#errorMsg').hide('slow');
				$('#loginForm').submit();
				
			}else{				
				$('#errorMsg').html(error).show('slow');
			}
	});
	$('#denyLink').click(function(){
			$('#denyForm').submit();
	});
});
