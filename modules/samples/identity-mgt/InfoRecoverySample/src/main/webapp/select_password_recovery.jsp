<%@ page contentType="text/html; charset=iso-8859-1" language="java" %>
<link rel="stylesheet" type="text/css" href="css/table-styles.css">
<jsp:include page="includes/header_signup.jsp"></jsp:include>

<h1>Recover Password</h1>

<p>Please select a method to recover your password</p>

<form method="POST" action="${pageContext.request.contextPath}/infoRecover/userInfoView"
      id="recoverMethodForm">

    <table>
        <tr class="spaceUnder">
            <td style="width: 10%">
                <input type="radio" name="recoveryMethod"
                       id="notificationRecover" value="notification" checked="checked"/>
            </td>
            <td style="width: 100%">
                Recover with Email
            </td>
        </tr>
        <tr class="spaceUnder">
            <td style="width: 10%">
                <input type="radio" name="recoveryMethod"
                       id="secretQuestionRecover" value="question"/></td>
            <td style="width: 100%">
                Recover with Secret Questions
            </td>
        </tr>
    </table>
    <input type="Submit" value="Recover" class="btn btn-primary"/>
</form>


<jsp:include page="includes/footer.jsp"></jsp:include>