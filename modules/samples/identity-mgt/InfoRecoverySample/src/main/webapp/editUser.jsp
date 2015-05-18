<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


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

<h3>Manage User Account</h3>
<br>

<form id="signupForm" method="POST"
      action="${pageContext.request.contextPath}/infoRecover/editUser?username=<%=username%>">
    <table cellpadding="5px">
        <c:forEach var="claim" items="${claims}" varStatus="Counter">
            <tr>
                <td><c:out value="${claim.displayName}"></c:out></td>
                <c:if test="${claim.claimValue != null }">
                    <td><input type="text" name="${claim.claimUri}" id="${claim.claimUri}"
                               value="${claim.claimValue}"/></td>
                </c:if>

                <c:if test="${claim.claimValue == null }">
                    <td><input type="text" name="${claim.claimUri}" id="${claim.claimUri}"/></td>
                </c:if>

            </tr>
        </c:forEach>
    </table>
    <br>
    <input type="submit" class="btn btn-primary" value="Update"/>
    <input type="button" class="btn btn-primary" value="Cancel"
           onclick="javascript:location.href='home.jsp'"/>
</form>


<jsp:include page="includes/footer.jsp"></jsp:include>



