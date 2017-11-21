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

    String question = (String) session.getAttribute("question");
%>
<script type="text/javascript">
    function doSubmit() {
        var answer = document.getElementById("answer").value

        if (answer == null) {
            alert('Please answer the secret question properly');
        } else {
            document.getElementById("secretQuestionForm").submit();
        }
    }
</script>
<jsp:include page="includes/header_signup.jsp"></jsp:include>

<a href="${pageContext.request.contextPath}">Home</a><br><br>

<form method="POST" action='qnaProcessor' id="secretQuestionForm">
    <table>
        <tbody>
        <tr>
            <td>Question</td>
            <td><% out.println(question); %></td>

        </tr>
        <tr>
            <td>Answer</td>
            <td><input type="text" name="answer" id="answer"/></td>
        </tr>
        <tr id="buttonRow">
            <td><input type="button" value="Submit" onclick="doSubmit()"/></td>
        </tr>
        </tbody>
    </table>
</form>

<jsp:include page="includes/footer.jsp"></jsp:include>