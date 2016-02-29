

<!DOCTYPE html>
+<%@ page import="org.wso2.sample.identity.oauth2.OAuth2Constants" %>
+<%@ page import="org.wso2.sample.identity.oauth2.OAuth2ServiceClient" %>
+<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%

        OAuth2ServiceClient oAuth2ServiceClient = new OAuth2ServiceClient();
        String clientId = request.getParameter("clientId");

    if (clientId!=null) {
        session.setAttribute("clientId", clientId);
    }

        String endpoint = OAuth2Constants.OP_ENDPOINT_URL;
        String statusCookie = request.getParameter("session");
%>
<html>
<iframe id="opIFrame" style='visibility: hidden;'  src=endpoint>
    </iframe>
<head>
    <script type="text/javascript">
                var sessionState = '<%=statusCookie%>'
                    </script>
        <script src="http://crypto-js.googlecode.com/svn/tags/3.0.2/build/rollups/sha256.js"></script>
        <script type="text/javascript">
            var stat = "unchanged";
            var clientId = '<%=session.getAttribute("clientId")%>';
            var sessionState = '<%=statusCookie%>';
            var targetOrigin = '<%=endpoint%>';
            var origin = "http://localhost:8080";
            var salt = Math.random().toString().slice(2);
            var mes = CryptoJS.SHA256(clientId + origin + sessionState + salt) + "." + salt + " " + clientId;
        </script>
        <script type="text/javascript" src="js/polling.js">
        </script>
    <title>WSO2 OAuth2 Playground</title>


</head><body>

</body>
</html>
