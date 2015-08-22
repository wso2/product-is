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

<jsp:include page="includes/header.jsp"></jsp:include>

<c:if test="${updatedStatus == true }">
    <h2><font color='green'>Successfully updated user account information.</font></h2>
</c:if>

<c:if test="${updatedStatus == false }">
    <h2><font color='red'>Failed to update account information. Please contact your system
        administrator</font></h2>
</c:if>

<jsp:include page="includes/footer.jsp"></jsp:include>