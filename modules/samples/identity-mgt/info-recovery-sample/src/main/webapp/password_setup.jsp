<%@ page contentType="text/html; charset=iso-8859-1" language="java" %>

<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<link rel="stylesheet" type="text/css" href="css/table-styles.css">

<script type="text/javascript">
    function doSubmit() {
        var pass = document.getElementById("password").value
        var confPass = document.getElementById("confirmPassword").value
        if (pass != confPass) {
            alert('Password do not match. Please correct');
        } else {
            document.getElementById("resetPasswordForm").submit();
        }
    }
</script>

<jsp:include page="includes/header_signup.jsp"></jsp:include>

<form method="POST" action='./passwordSetup'
      id="resetPasswordForm">
    <table>
        <tbody>
        <tr class="spaceUnder">
            <td>Enter new password</td>
            <td><input type="password" name="password" id="password"/></td>

        </tr>
        <tr class="spaceUnder">
            <td>Confirm password</td>
            <td><input type="password" name="confirmPassword" id="confirmPassword"/></td>
        </tr>
        <tr id="buttonRow">
            <td><input type="button" value="Submit" onclick="doSubmit()"/></td>
        </tr>
        </tbody>
    </table>
</form>

<jsp:include page="includes/footer.jsp"></jsp:include>