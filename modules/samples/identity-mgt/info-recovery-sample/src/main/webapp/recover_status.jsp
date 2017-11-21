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

<jsp:include page="includes/header_signup.jsp"></jsp:include>

<%
    Boolean status = (Boolean) request.getAttribute("validationStatus");
    if (status) {
%>
<h2><font color='green'>
    Password recovery information has been sent to the email registered
    with the account
    <%=request.getParameter("username")%>.</font></h2>
<%
} else {
%>

<h2><font color='red'>
    Cannot verify the user with given username or captcha answer. Cannot proceed. </font></h2>
<%
    }
%>

<jsp:include page="includes/footer.jsp"></jsp:include>
