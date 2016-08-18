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
<jsp:include page="includes/header.jsp"></jsp:include>

<script type="text/javascript">
    function doSubmit() {
        var email = document.getElementById("http://wso2.org/claims/emailaddress").value;
        var pass = document.getElementById("password").value;
        var confPass = document.getElementById("confirmPassword").value;
        if (pass != confPass) {
            alert('Password do not match. Please correct');
        } else if (email == 'null' || email == "") {
            alert('Please provide the email address');
        } else {
            document.getElementById("signupForm").submit();
        }
    }

</script>

<%
    String username = request.getParameter("username");
%>

<h3>Account Overview</h3>
<br>

<%--<form id="signupForm" method="POST"--%>
<%--action="${pageContext.request.contextPath}/infoRecover/accountOverview?username=<%=username%>">--%>
<table>
    <tr>
        <td>
            <table cellpadding="5px">
                <c:forEach var="claim" items="${claims}" varStatus="Counter">
                    <tr>
                        <td><c:out value="${claim.displayName}"></c:out></td>
                        <c:if test="${claim.claimValue != null }">
                            <td><input type="text" name="${claim.claimUri}" readonly="true"
                                       id="${claim.claimUri}"
                                       value="${claim.claimValue}"/></td>
                        </c:if>

                        <c:if test="${claim.claimValue == null }">
                            <td><input type="text" name="${claim.claimUri}" readonly="true"
                                       id="${claim.claimUri}"/></td>
                        </c:if>

                    </tr>
                </c:forEach>
            </table>
        </td>
        <td class="top" style="padding-left: 20px;">
            <table>
                <tr>
                    <td style="border: 2px solid black; padding: 10px"><a
                            href="https://localhost:9443/samlsso?spEntityID=https://wso2.salesforce.com">Submit
                        a support ticket to Salesforce</a></td>
                </tr>
            </table>
        </td>
    </tr>
</table>


<h3>Product Subscriptions</h3>
<br>

<table cellpadding="20px">
    <tr>
        <td><img src="images/computer.jpg" alt="Computer Accessories"
                 style="width:200px;height:150px"></td>
        <td><img src="images/camera.png" alt="Digital Camera" style="width:200px;height:150px"></td>
    </tr>
    <tr>
        <td style="text-align: center"><b>Computer Accessories</b></td>
        <td style="text-align: center"><b>Digital Cameras</b></td>
    </tr>
</table>
<br>

<%--<input type="button" class="btn btn-primary" value="Cancel"--%>
<%--onclick="javascript:location.href='home.jsp'"/>--%>
<%--</form>--%>


<jsp:include page="includes/footer.jsp"></jsp:include>



