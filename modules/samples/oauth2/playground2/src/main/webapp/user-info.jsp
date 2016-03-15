<%@page import="org.apache.commons.lang.StringUtils" %>
<%@page import="org.json.simple.JSONObject" %>
<%@page import="org.json.simple.parser.JSONParser" %>
<%@page import="org.wso2.sample.identity.oauth2.OAuth2Constants" %>
<%@page import="java.util.Iterator" %>
<%@page import="java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <title>WSO2 OAuth2.0 Playground</title>
    <meta charset="UTF-8">
    <meta name="description" content=""/>
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"></script>
    <!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
    <script type="text/javascript" src="js/prettify.js"></script>
    <!-- PRETTIFY -->
    <script type="text/javascript" src="js/kickstart.js"></script>
    <!-- KICKSTART -->
    <link rel="stylesheet" type="text/css" href="css/kickstart.css" media="all"/>
    <!-- KICKSTART -->
    <link rel="stylesheet" type="text/css" href="style.css" media="all"/>
    <!-- CUSTOM STYLES -->


</head>
<!-- ===================================== END HEADER ===================================== -->
<body><a id="top-of-page"></a>

<div id="wrap" class="clearfix">

    <!-- Menu Horizontal -->
    <ul class="menu">
        <li class="current"><a href="index.jsp">Home</a></li>

    </ul>

    <br/>

    <h3 align="center">WSO2 OAuth2 Playground ~ User Info</h3>


    <table style="width:800px;margin-left: auto;margin-right: auto;" class="striped">

        <%

            String result = (String) session.getAttribute(OAuth2Constants.RESULT);

            if (result != null) {
                String json = new String(result);
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(json);
                JSONObject jsonObject = (JSONObject) obj;

                Iterator<?> ite = jsonObject.entrySet().iterator();

                while (ite.hasNext()) {
                    Map.Entry entry = (Map.Entry) ite.next();
        %>
        <tr>
            <td style="width:50%"><%=entry.getKey()%>
            </td>
            <td><%=entry.getValue()%>
            </td>
        </tr>
        <%
            }

        } else {
        %>
        <tr>
            <td>No data received</td>
        </tr>
        <%
            }

            boolean isOIDCLogoutEnabled = false;
            boolean isOIDCSessionEnabled = false;
            String scope = (String) session.getAttribute(OAuth2Constants.SCOPE);
            if (StringUtils.isNotBlank(scope) && scope.contains(OAuth2Constants.SCOPE_OPENID)) {
                if (StringUtils.isNotBlank((String) session.getAttribute(OAuth2Constants.OIDC_LOGOUT_ENDPOINT))) {
                    isOIDCLogoutEnabled = true;
                }

                if (StringUtils
                        .isNotBlank((String) session.getAttribute(OAuth2Constants.OIDC_SESSION_IFRAME_ENDPOINT))) {
                    isOIDCSessionEnabled = true;
                }
            }

        %>
        <tr>
            <%
                if (isOIDCLogoutEnabled) {
            %>
            <td colspan="2">
                <button type="button" class="button"
                        onclick="document.location.href='<%=(String)session.getAttribute(OAuth2Constants.OIDC_LOGOUT_ENDPOINT)%>';">
                    Logout
                </button>
            </td>
            <%
                }
            %>
        </tr>
    </table>
</div>

<%
    if (isOIDCSessionEnabled) {
%>
<iframe id="rpIFrame" src="rpIFrame.jsp" frameborder="0" width="0" height="0"></iframe>
<%
    }
%>
</body>
</html>
