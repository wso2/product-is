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

<script type="text/javascript">
    function doSubmit() {
        var email = document
                .getElementById("http://wso2.org/claims/emailaddress").value;
        var firstname = document
                .getElementById("http://wso2.org/claims/givenname").value;
        var lastname = document
                .getElementById("http://wso2.org/claims/lastname").value;

        if (firstname == 'null' || firstname == "") {
            alert('Please provide the first name');
        } else if (email == 'null' || email == "") {
            alert('Please provide the email address');
        } else if (lastname == 'null' || lastname == "") {
            alert('Please provide the last name');
        } else {
            document.getElementById("accountRecoverForm").submit();
        }
    }
</script>

<jsp:include page="includes/header_signup.jsp"></jsp:include>

<br>
<br>

<h3>Recover My Account Name</h3>
<br>

<p>Enter following details to recover your account name</p>
<br>

<form id="accountRecoverForm" method="POST"
      action="${pageContext.request.contextPath}/infoRecover/recoverAccount">
    <table>
        <c:forEach var="claim" items="${claims}" varStatus="Counter">
            <tr>
                <td><c:out value="${claim.displayName}"></c:out></td>
                <td><input type="text" name="${claim.claimUri}"
                           id="${claim.claimUri}" value="${claim.claimValue}"/></td>
            </tr>
        </c:forEach>
        <tr>
            <td>Captcha</td>
            <td><img src="${captchaImageUrl}"
                     alt='If you can not see the captcha " +
                        "image please refresh the page or click the link again.'/></td>
        </tr>
        <tr>
            <td>Enter Captcha text</td>
            <td><input type="text" name="captchaAnswer" id="captchaAnswer"
                       value="${captcha.userAnswer}"/></td>
        </tr>
        <tr>
            <td><input type="hidden" name="captchaSecretKey"
                       value="${captcha.secretKey}"/></td>
            <td><input type="hidden" name="captchaImagePath"
                       value="${captcha.imagePath}"/></td>
        </tr>
    </table>
    <br>
    <input type="button" value="Recover" onclick="doSubmit()"/>
</form>
<jsp:include page="includes/footer.jsp"></jsp:include>