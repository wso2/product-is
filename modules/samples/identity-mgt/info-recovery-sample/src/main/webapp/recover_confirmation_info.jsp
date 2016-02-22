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

<%
    String captchaImageUrl = (String) request.getAttribute("captchaImageUrl");
    String captchaSecretKey = (String) request.getAttribute("captchaSecretKey");
    String captchaImagePath = (String) request.getAttribute("captchaImagePath");
    String recoveryMethod = (String) request.getAttribute("recoveryMethod");
    String validateAction = (String) request.getAttribute("validateAction");
%>
<link rel="stylesheet" type="text/css" href="css/table-styles.css">
<jsp:include page="includes/header_signup.jsp"></jsp:include>

<%
    String username = request.getParameter("username");
    String userstoredomain = request.getParameter("userstoredomain");
    String tenantdomain = request.getParameter("tenantdomain");
%>
<p>
    Enter below details to recover your password <br>
    <br>

<form method="POST" action="./<%=validateAction %>"
      id="recoverDetailsForm">
    <table style="width: 50%">
        <tbody>
        <tr class="spaceUnder">
            <td><input type="hidden" name="username" id="username" value="<%=username%>"/></td>
            <td><input type="hidden" name="userstoredomain" id="userstoredomain" value="<%=userstoredomain%>"/></td>
            <td><input type="hidden" name="tenantdomain" id="tenantdomain" value="<%=tenantdomain%>"/><</td>
        </tr>
        <tr class="spaceUnder">
            <td>Captcha</td>
            <td><img src="<%=captchaImageUrl%>"
                     alt='If you can not see the captcha " +
                        "image please refresh the page or click the link again.'/></td>
        </tr>
        <tr class="spaceUnder">
            <td>Enter Captcha Text</td>
            <td><input type="text" name="captchaAnswer" id="captchaAnswer" style="height: 25px"/>
            </td>
        </tr>
        <tr class="spaceUnder">
            <td><input type="hidden" name="captchaSecretKey"
                       value="<%=captchaSecretKey%>"/></td>
            <td><input type="hidden" name="captchaImagePath"
                       value="<%=captchaImagePath%>"/></td>
            <td><input type="hidden" name="recoveryMethod"
                       value="<%=recoveryMethod%>"/></td>
            <td><input type="hidden" name="action"
                       value="validateUserInfo"/></td>
        </tr>
        <tr id="buttonRow">
            <td><input class="btn btn-primary" type="submit" value="Submit"/></td>
        </tr>
        </tbody>
    </table>
</form>

<jsp:include page="includes/footer.jsp"></jsp:include>