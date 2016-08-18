<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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
<jsp:include page="includes/header_signup.jsp"></jsp:include>

<style type="text/css">
    .tooltip {
        display: inline;
        position: relative;
        text-decoration: none;
        top: 0px;
        left: 4px;
    }

    .tooltip:hover:after {
        background: #333;
        background: rgba(0, 0, 0, .8);
        border-radius: 5px;
        top: -5px;
        color: #fff;
        content: attr(alt);
        left: 160px;
        padding: 5px 15px;
        position: absolute;
        z-index: 98;
        width: 150px;
    }

    .tooltip:hover:before {
        border: solid;
        border-color: transparent black;
        border-width: 6px 6px 6px 0;
        bottom: 20px;
        content: "";
        left: 155px;
        position: absolute;
        z-index: 99;
        top: 3px;
    }

</style>
<script type="text/javascript">
    function doSubmit() {
        var username = document.getElementById("username").value;
        var pass = document.getElementById("password").value;
        var confPass = document.getElementById("confirmPassword").value;
        if (pass != confPass) {
            alert('Password do not match. Please correct');
        } else if (username == 'null' || username == "") {
            alert('Please provide the User Name');
        } else {
            document.getElementById("signupForm").submit();
        }
    }

</script>


<h3>Account Registration</h3>
<br>

<form id="signupForm" method="POST" action="signup">
    <table>
        <tr>
            <td>Username<font
                    color='red'>*</font></td>
            <td><input id="username" type="text" name="username"/></td>
        </tr>
        <tr>
            <td>Password<font
                    color='red'>*</font></td>
            <td><input id="password" type="password" name="password"
                       placeholder="Length should be btwn 5-12"/></td>
        </tr>
        <tr>
            <td>Confirm Password<font
                    color='red'>*</font></td>
            <td><input id="confirmPassword" type="password"
                       name="confirmPassword"/></td>
        </tr>

        <c:forEach var="claim" items="${claims}" varStatus="Counter">
            <tr>
                <td><c:out value="${claim.displayName}"></c:out><font
                        color='red'>*</font></td>
                <td><input type="text" name="${claim.claimUri}" id="${claim.claimUri}"/></td>
            </tr>
        </c:forEach>

    </table>
    <br>
    <input type="button" class="btn btn-primary" value="Register" onclick="doSubmit()"/>
</form>


<jsp:include page="includes/footer.jsp"></jsp:include>



