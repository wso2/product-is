<%@page import="com.nimbusds.jwt.SignedJWT" %>
<%@page import="org.apache.commons.lang.StringUtils" %>
<%@page import="org.apache.oltu.oauth2.client.response.OAuthAuthzResponse" %>
<%@page import="org.wso2.sample.identity.oauth2.OAuth2Constants" %>
<%@ page import="java.security.MessageDigest" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="java.util.UUID" %>
<%@ page import="org.apache.commons.codec.binary.Base64" %>
<%@ page import="org.wso2.sample.identity.oauth2.OpenIDConnectConstants" %>
<%@ page import="org.wso2.sample.identity.oauth2.ApplicationConfig" %>
<%@ page import="java.util.logging.Logger" %>
<%@ page import="org.wso2.sample.identity.oauth2.ContextEventListener" %>
<%@ page import="java.util.logging.Level" %>
<%
    Logger LOGGER = Logger.getLogger(ContextEventListener.class.getName());

    String consumerKey = ApplicationConfig.getConsumerKey();
    String consumerSecret = ApplicationConfig.getConsumerSecret();
    String scopeName = ApplicationConfig.getScope();
    String callbackUrl = ApplicationConfig.getCallbackUrl();
    String accessTokenEndpoint = ApplicationConfig.getAccessTokenEndpointContext();
    String authorizeEndpoint = ApplicationConfig.getAuthorizeEndpointContext();
    String logoutEndpoint = ApplicationConfig.getLogoutEndpointContext();
    String userInfo = ApplicationConfig.getUserInforEndpointContext();
    String sessionIFrameEndpoint = ApplicationConfig.getSessionIframeEndpointContext();

    String code = null;
    String accessToken = null;
    String idToken = null;
    String name = null;
    String scope = null;
    String sessionState = null;
    String error = null;
    String grantType = null;
    String code_verifier = null;
    String code_challenge = null;
    String implicitResponseType = null;
    String acr_values = "";

    boolean isOIDCLogoutEnabled = false;
    boolean isOIDCSessionEnabled = false;

    OAuthAuthzResponse authzResponse = null;

    try {
        String reset = request.getParameter(OAuth2Constants.RESET_PARAM);
        if (reset != null && Boolean.parseBoolean(reset)) {
            session.removeAttribute(OAuth2Constants.OAUTH2_GRANT_TYPE);
            session.removeAttribute(OAuth2Constants.ACCESS_TOKEN);
            session.removeAttribute(OAuth2Constants.CODE);
            session.removeAttribute(OAuth2Constants.ID_TOKEN);
            session.removeAttribute(OAuth2Constants.RESULT);
            session.removeAttribute(OAuth2Constants.SESSION_STATE);
            session.removeAttribute(OAuth2Constants.SCOPE);
            session.removeAttribute(OAuth2Constants.OAUTH2_AUTHZ_ENDPOINT);
            session.removeAttribute(OAuth2Constants.OIDC_LOGOUT_ENDPOINT);
            session.removeAttribute(OAuth2Constants.OIDC_SESSION_IFRAME_ENDPOINT);
            session.removeAttribute(OAuth2Constants.OAUTH2_PKCE_CODE_VERIFIER);
            session.removeAttribute(OAuth2Constants.OAUTH2_USE_PKCE);
        }

        sessionState = request.getParameter(OAuth2Constants.SESSION_STATE);
        if (StringUtils.isNotBlank(sessionState)) {
            session.setAttribute(OAuth2Constants.SESSION_STATE, sessionState);
        }

        error = request.getParameter(OAuth2Constants.ERROR);
        grantType = (String) session.getAttribute(OAuth2Constants.OAUTH2_GRANT_TYPE);
        implicitResponseType = (String) session.getAttribute(OpenIDConnectConstants.IMPLICIT_RESPONSE_TYPE);
        if (StringUtils.isNotBlank(request.getHeader(OAuth2Constants.REFERER)) &&
                request.getHeader(OAuth2Constants.REFERER).contains("rpIFrame")) {
            /**
             * Here referer is being checked to identify that this is exactly is an response to the passive request
             * initiated by the session checking iframe.
             * In this sample, every error is forwarded back to this page. Thus, this condition is added to treat
             * error response coming for the passive request separately, and to identify that as a logout scenario.
             */
            if (StringUtils.isNotBlank(error)) { // User has been logged out
                session.invalidate();
                response.sendRedirect("index.jsp");
                return;
            } else {
                if (grantType != null && OAuth2Constants.OAUTH2_GRANT_TYPE_CODE.equals(grantType)) {
                    code = request.getParameter(OAuth2Constants.CODE);
                    session.setAttribute(OAuth2Constants.CODE, code);
                }
            }
        }

        if (grantType != null && OAuth2Constants.OAUTH2_GRANT_TYPE_CODE.equals(grantType)) {
            code = (String) session.getAttribute(OAuth2Constants.CODE);
            if (code == null) {
                authzResponse = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
                code = authzResponse.getCode();
                session.setAttribute(OAuth2Constants.CODE, code);
            } else {
                accessToken = (String) session.getAttribute(OAuth2Constants.ACCESS_TOKEN);
                idToken = (String) session.getAttribute(OAuth2Constants.ID_TOKEN);
            }
        } else if (grantType != null && OAuth2Constants.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS.equals(grantType)) {
            accessToken = (String) session.getAttribute(OAuth2Constants.ACCESS_TOKEN);
        } else if (grantType != null && OAuth2Constants.OAUTH2_GRANT_TYPE_RESOURCE_OWNER.equals(grantType)) {
            accessToken = (String) session.getAttribute(OAuth2Constants.ACCESS_TOKEN);
            idToken = (String) session.getAttribute(OAuth2Constants.ID_TOKEN);
        }

        scope = (String) session.getAttribute(OAuth2Constants.SCOPE);
        if (StringUtils.isNotBlank(scope) && scope.contains(OAuth2Constants.SCOPE_OPENID)) {
            if (StringUtils.isNotBlank((String) session.getAttribute(OAuth2Constants.OIDC_LOGOUT_ENDPOINT))) {
                isOIDCLogoutEnabled = true;
            }

            if (StringUtils.isNotBlank((String) session.getAttribute(OAuth2Constants.OIDC_SESSION_IFRAME_ENDPOINT))) {
                isOIDCSessionEnabled = true;
            }
        }

    } catch (Exception e) {
        error = e.getMessage();
    }
%>

<!DOCTYPE html>
<html>
<head>
    <title>WSO2 OAuth2 Playground</title>
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

    <script type="text/javascript">
        function setVisibility() {

            var grantType = document.getElementById("grantType").value;
            var scope = document.getElementById("scope").value;
            var implicitResponseType = document.getElementById("response_type").value;

            document.getElementById("logutep").style.display = "none";
            document.getElementById("sessionep").style.display = "none";

            if ('code' == grantType) {
                document.getElementById("clientsecret").style.display = "none";
                document.getElementById("callbackurltr").style.display = "";
                document.getElementById("authzep").style.display = "";
                document.getElementById("accessep").style.display = "none";
                document.getElementById("recownertr").style.display = "none";
                document.getElementById("recpasswordtr").style.display = "none";
                document.getElementById("formPost").style.display = "none";

                if (scope.indexOf("openid") > -1) {
                    document.getElementById("logutep").style.display = "";
                    document.getElementById("sessionep").style.display = "";
                }
            } else if ('token' == grantType) {
                document.getElementById("clientsecret").style.display = "none";
                document.getElementById("callbackurltr").style.display = "";
                document.getElementById("authzep").style.display = "";
                document.getElementById("accessep").style.display = "none";
                document.getElementById("recownertr").style.display = "none";
                document.getElementById("recpasswordtr").style.display = "none";
                document.getElementById("formPost").style.display = "";

                if (scope.indexOf("openid") > -1) {
                    document.getElementById("implicitRespType").style.display = "";
                }
            } else if ('password' == grantType) {
                document.getElementById("clientsecret").style.display = "";
                document.getElementById("callbackurltr").style.display = "none";
                document.getElementById("authzep").style.display = "none";
                document.getElementById("accessep").style.display = "";
                document.getElementById("recownertr").style.display = "";
                document.getElementById("recpasswordtr").style.display = "";
                document.getElementById("formPost").style.display = "none";
            } else if ('client_credentials' == grantType) {
                document.getElementById("clientsecret").style.display = "";
                document.getElementById("callbackurltr").style.display = "none";
                document.getElementById("authzep").style.display = "none";
                document.getElementById("accessep").style.display = "";
                document.getElementById("recownertr").style.display = "none";
                document.getElementById("recpasswordtr").style.display = "none";
                document.getElementById("formPost").style.display = "none";
            }

            return true;
        }

        function getAcceesToken() {
            var fragment = window.location.hash.substring(1);
            if (fragment.indexOf("&") > 0) {
                var arrParams = fragment.split("&");

                var i = 0;
                for (i = 0; i < arrParams.length; i++) {
                    var sParam = arrParams[i].split("=");

                    if (sParam[0] == "access_token") {
                        return sParam[1];
                    }
                }
            }
            return "";
        }

        function getIDtoken() {
            var fragment = window.location.hash.substring(1);
            var arrParams = fragment.split("&");
            for (var i = 0; i < arrParams.length; i++) {
                var urlParameters = arrParams[i].split("=");

                if (urlParameters[0] == "id_token") {
                    var idToken = urlParameters[1];
                    return idToken;
                }
            }
            return "";
        }

        function getDecodedIDToken() {
            var idToken = getIDtoken();
            if (idToken) {
                var decodedIdToken = atob(idToken.split(".")[1]);
                return decodedIdToken;
            }
            return "";
        }

        function makeList(data) {
            document.write('<tbody>');
            for (var i in data) {
                document.write('<div><tr><td><label id="idtokenList">')
                document.write(i);
                document.write('</label></td><td>');
                document.write(data[i]);
                document.write('</td></tr></div>');
            }
            document.write('</tbody>');
        }

        function decryptIdToken(element) {
            console.log($(element).parent().parent().parent().parent());

            // Remove error row if exists.
            var row = document.getElementById("errorRow");
            if (row != null ) row.parentNode.removeChild(row);

            // Remove previously added decrypted data if exists.
            $(".id_token_data_row").remove();

            // Get last row by going to parent table element from button click.
            var lastRow = $(element).parent().parent().parent().parent().find('tr:last');

            var idToken = $("#encryptedIdToken").val().trim();
            var privateKeyString = $("#clientPrivateKey").val().trim();
            var data = {
                "idToken": idToken,
                "privateKeyString": privateKeyString
            };

            // Send ajax request to decrypt the id token.
            $.ajax({
                type: 'POST',
                url: '/playground2/IDTokenDecrypterServlet',
                dataType: 'text',
                data: data,
                success: function (data) {
                    if (data != null || data !== "") {
                        var dataJson = JSON.parse(data);

                        // Add header dara row.
                        var prettyHeader = JSON.stringify(dataJson.header, undefined, 4);
                        var tokenHeaderHTML =
                            "<tr class='id_token_data_row'>" +
                                "<td>ID Token Header :</td><td>" +
                                    "<textarea style='width:450px;height:130px;line-height:13px;'>" + prettyHeader +
                                    "</textarea>" +
                                "</td>" +
                            "</tr>";
                        lastRow.prev().after(tokenHeaderHTML);

                        // Add claims data row.
                        var prettyClaims = JSON.stringify(dataJson.claims, undefined, 4);
                        var tokenClaimsHTML =
                            "<tr class='id_token_data_row'>" +
                                "<td>ID Token Claims :</td><td>" +
                                    "<textarea style='width:450px;line-height:13px;'>" + prettyClaims + "</textarea>" +
                                "</td>" +
                            "</tr>";
                        lastRow.prev().after(tokenClaimsHTML);
                    }
                },
                error: function (e) {
                    var errorHTML = $("<tr id=\"errorRow\"><td></td>" +
                        "<td style='width:100%;color:#cc0000;' colspan='2'>" + e.responseText + "</td></tr>");
                    lastRow.prev().after(errorHTML);
                }
            });
        }

        // Inject html in implicit flow id token only mode, depending on the type of the id token.
        function renderImplicitFlowIdTokenHTML(idToken) {
            var html = "";
            if (idToken.split(".").length == 5) {
                // It's a JWE.
                html =
                    "<tr>" +
                        "<td><label style=\"width: 130px;\">Encrypted ID Token :</label></td>" +
                        "<td>" +
                            "<textarea id=\"encryptedIdToken\" name=\"idToken\" style=\"width:450px\"></textarea>" +
                        "</td>"+
                    "</tr>" +
                    "<tr>" +
                        "<td><label>Client Private Key :</label></td>" +
                        "<td>" +
                            "<textarea id=\"clientPrivateKey\" name=\"idToken\" style=\"width:450px\"></textarea>" +
                        "</td>"+
                        "<td>" +
                            "<input type=\"submit\" class=\"button\" value=\"Decrypt\" " +
                                "onclick=\"decryptIdToken(this);return false;\">" +
                        "</td>" +
                    "</tr>";
                $(html).prependTo('#implicit-id-token');
                $("#encryptedIdToken").val(idToken);
            } else {
                html =
                    "<tr>" +
                        "<td><label style=\"width: 130px;\">ID Token :</label></td>" +
                        "<td>" +
                            "<textarea id=\"idToken\" name=\"idToken\" style=\"width:450px\"></textarea>" +
                        "</td>"+
                    "</tr>";
                $(html).prependTo('#implicit-id-token');
                $("#idToken").val(idToken);
            }
        }

        // Inject html in implicit flow id token and access token mode, depending on the type of the id token.
        function renderImplicitFlowIdTokenTokenHTML(idToken) {
            var html = "";
            if (idToken.split(".").length == 5) {
                // It's a JWE.
                html =
                    "<tr>" +
                        "<td><label style=\"width: 130px;\">Access Token :</label></td>" +
                        "<td>" +
                            "<input id=\"accessToken\" name=\"accessToken\" style=\"width:450px\"/>" +
                        "</td>"+
                    "</tr>" +
                    "<tr>" +
                        "<td><label>Encrypted ID Token :</label></td>" +
                        "<td>" +
                            "<textarea id=\"encryptedIdToken\" name=\"idToken\" style=\"width:450px\"></textarea>" +
                        "</td>"+
                    "</tr>" +
                    "<tr>" +
                        "<td><label>Client Private Key :</label></td>" +
                        "<td>" +
                            "<textarea id=\"clientPrivateKey\" name=\"idToken\" style=\"width:450px\"></textarea>" +
                        "</td>"+
                        "<td>" +
                            "<input type=\"submit\" class=\"button\" value=\"Decrypt\" " +
                                "onclick=\"decryptIdToken(this);return false;\">" +
                        "</td>" +
                    "</tr>";
                $(html).prependTo('#implicit-id-token-token');
                $("#encryptedIdToken").val(idToken);
                $("#accessToken").val(getAcceesToken());
            } else {
                html =
                    "<tr>" +
                        "<td><label style=\"width: 130px;\">Access Token :</label></td>" +
                        "<td>" +
                            "<input id=\"accessToken\" name=\"accessToken\" style=\"width:450px\"/>" +
                        "</td>"+
                    "</tr>" +
                    "<tr>" +
                        "<td><label>ID Token :</label></td>" +
                        "<td>" +
                            "<textarea id=\"idToken\" name=\"idToken\" style=\"width:450px\"></textarea>" +
                        "</td>"+
                    "</tr>";
                $(html).prependTo('#implicit-id-token-token');
                $("#idToken").val(idToken);
                $("#accessToken").val(getAcceesToken());
            }
        }
    </script>

</head>
<!-- ===================================== END HEADER ===================================== -->
<body><a id="top-of-page"></a>

<div id="wrap" class="clearfix"/>
<!-- Menu Horizontal -->
<ul class="menu">
    <li class="current"><a href="index.jsp">Home</a></li>
</ul>

<div class="col_12"/>
<div class="col_9"/>
<h3>WSO2 OAuth2 Playground</h3>

<table>
    <tr>
        <td>
            <% if (accessToken == null && code == null && grantType == null) {
                code_verifier = UUID.randomUUID().toString() + UUID.randomUUID().toString();
                code_verifier = code_verifier.replaceAll("-", "");

                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(code_verifier.getBytes(StandardCharsets.US_ASCII));
                //Base64 encoded string is trimmed to remove trailing CR LF
                code_challenge = new String(Base64.encodeBase64URLSafe(hash), StandardCharsets.UTF_8).trim();
                //set the generated code verifier to the current user session
                session.setAttribute(OAuth2Constants.OAUTH2_PKCE_CODE_VERIFIER, code_verifier);

            %>
            <div id="loginDiv" class="sign-in-box" width="100%">
                <% if (error != null && error.trim().length() > 0) {%>
                <table class="user_pass_table" width="100%">
                    <tr>
                        <td><font color="#CC0000"><%=error%>
                        </font></td>
                    </tr>
                </table>
                <%} %>

                <form action="oauth2-authorize-user.jsp" id="loginForm" method="post" name="oauthLoginForm">

                    <table class="user_pass_table" width="100%">
                        <tbody>

                        <tr>
                            <td>Authorization Grant Type :</td>
                            <td>
                                <select id="grantType" name="grantType" onchange="setVisibility();">
                                    <option value="<%=OAuth2Constants.OAUTH2_GRANT_TYPE_CODE%>" selected="selected">
                                        Authorization Code
                                    </option>
                                    <option value="<%=OAuth2Constants.OAUTH2_GRANT_TYPE_IMPLICIT%>">Implicit</option>
                                    <option value="<%=OAuth2Constants.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS%>">Client
                                        Credentials
                                    </option>
                                    <option value="<%=OAuth2Constants.OAUTH2_GRANT_TYPE_RESOURCE_OWNER%>">Resource
                                        Owner
                                    </option>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <td><label>Client Id : </label></td>
                            <td><input type="text" id="consumerKey" name="consumerKey" value="<%=consumerKey%>" style="width:450px"></td>
                        </tr>

                        <tr id="clientsecret" style="display:none">
                            <td><label>Client Secret : </label></td>
                            <td><input type="password" id="consumerSecret" name="consumerSecret"
                                       value="<%=consumerSecret%>" style="width:450px">
                            </td>
                        </tr>

                        <tr id="recownertr" style="display:none">
                            <td><label>Resource Owner User Name: </label></td>
                            <td><input type="text" id="recowner" name="recowner" style="width:450px"></td>
                        </tr>

                        <tr id="recpasswordtr" style="display:none">
                            <td><label>Resource Owner Password : </label></td>
                            <td><input type="password" id="recpassword" name="recpassword" style="width:450px">
                            </td>
                        </tr>

                        <tr>
                            <td><label>Scope : </label></td>
                            <td><input type="text" id="scope" name="scope" value="<%=scopeName%>"
                                       onchange="setVisibility();">
                            </td>
                        </tr>
                        <tr id="implicitRespType" style="display: none">
                            <td><label>Implicit Response Type: </label></td>
                            <td>
                                <select id="response_type" name="response_type">
                                    <option value="<%=OpenIDConnectConstants.ID_TOKEN%>" selected="selected">
                                        ID token Only
                                    </option>
                                    <option value="<%=OpenIDConnectConstants.ID_TOKEN_TOKEN%>">ID token &
                                        Access Token
                                    </option>
                                </select>
                            </td>
                        </tr>

                        <tr id="callbackurltr">
                            <td><label>Callback URL : </label></td>
                            <td><input type="text" id="callbackurl" name="callbackurl" value="<%=callbackUrl%>"
                                       style="width:450px">
                            </td>
                        </tr>

                        <tr id="authzep">
                            <td>Authorize Endpoint :</td>
                            <td><input type="text" id="authorizeEndpoint" name="authorizeEndpoint" value="<%=authorizeEndpoint%>"
                                       style="width:450px">
                            </td>
                        </tr>

                        <tr id="accessep" style="display:none">
                            <td>Access Token Endpoint :</td>
                            <td><input type="text" id="accessEndpoint" name="accessEndpoint"  value="<%=accessTokenEndpoint%>"
                                       style="width:450px"></td>
                        </tr>

                        <tr id="logutep" style="display:none">
                            <td>Logout Endpoint :</td>
                            <td><input type="text" id="logoutEndpoint" name="logoutEndpoint" value="<%=logoutEndpoint%>"
                                       style="width:450px">
                            </td>
                        </tr>

                        <tr id="sessionep" style="display:none">
                            <td>Session Iframe Endpoint :</td>
                            <td><input type="text" id="sessionIFrameEndpoint" name="sessionIFrameEndpoint" value="<%=sessionIFrameEndpoint%>"
                                       style="width:450px"></td>
                        </tr>

                        <tr id="pkceOption">
                            <td>Use PKCE</td>
                            <td><input type="radio" name="use_pkce" value="yes">Yes &nbsp;
                                <input type="radio" name="use_pkce" value="no" checked>No
                            </td>
                        </tr>
                        <tr id="pkceMethod">
                            <td>PKCE Challenge Method</td>
                            <td><input type="radio" name="code_challenge_method" onchange="togglePKCEMethod()"
                                       value="S256" checked>S256 &nbsp;
                                <input type="radio" name="code_challenge_method" onchange="togglePKCEMethod()"
                                       value="plain">plain
                            </td>
                        </tr>
                        <tr id="pkceChallenge">
                            <td>PKCE Code Challenge</td>
                            <td><input type="text" style="width: 450px" readonly name="code_challenge"
                                       value="<%=code_challenge%>"></td>
                        </tr>
                        <tr id="pkceVerifier">
                            <td>PKCE Code Verifier [length : <%=code_verifier.length()%>]</td>
                            <td><label><%=code_verifier%>
                            </label></td>
                        </tr>

                        <tr id="formPost" style="display:none">
                            <td>Enable Form Post</td>
                            <td><input type="radio" name="form_post" value="yes">Yes &nbsp;
                                <input type="radio" name="form_post" value="no" checked>No
                            </td>
                        </tr>

                        <tr id="acr">
                            <td>Authentication Context Class/LoA</td>
                            <td><input type="text" style="width: 450px" name="acr_values"
                                       value="<%=acr_values%>"></td>
                        </tr>

                        <tr>
                            <td colspan="2"><input type="submit" name="authorize" value="Authorize"></td>
                        </tr>
                        </tbody>
                    </table>

                </form>
            </div>

            <%} else if (code != null && accessToken == null) { %>
            <div>
                <form action="oauth2-get-access-token.jsp" id="loginForm" method="post">

                    <table class="user_pass_table">
                        <tbody>
                        <tr>
                            <td>Authorization Code :</td>
                            <td><%=code%></td>
                        </tr>
                        <tr>
                            <td>Callback URL :</td>
                            <td><input type="text" id="callbackurl" name="callbackurl" value="<%=callbackUrl%>" style="width:450px"></td>
                        </tr>
                        <tr>
                            <td>Access Token Endpoint :</td>
                            <td><input type="text" id="accessEndpoint" name="accessEndpoint" value="<%=accessTokenEndpoint%>" style="width:450px"></td>
                        </tr>
                        <tr>
                            <td><label>Client Secret : </label></td>
                            <td><input type="password" id="consumerSecret" name="consumerSecret" value="<%=consumerSecret%>" style="width:450px">
                            </td>
                        </tr>
                        <% if (session.getAttribute(OAuth2Constants.OAUTH2_USE_PKCE) != null) {%>
                        <tr>
                            <td><label>PKCE Verifier : </label></td>
                            <td><input type="text" id="pkce_verifier" name="code_verifier" style="width:450px"
                                       value="<%=(String)session.getAttribute(OAuth2Constants.OAUTH2_PKCE_CODE_VERIFIER)%>">
                            </td>
                        </tr>
                        <% }%>
                        <tr>
                            <td><input type="submit" name="authorize" value="Get Access Token"></td>
                            <%
                                if (isOIDCLogoutEnabled) {
                            %>
                            <td>
                                <button type="button" class="button"
                                        onclick="document.location.href='<%=(String)session.getAttribute(OAuth2Constants.OIDC_LOGOUT_ENDPOINT)%>';">
                                    Logout
                                </button>
                            </td>
                            <%
                                }
                            %>
                        </tr>
                        </tbody>
                    </table>

                </form>

            </div>
            <%
            } else if (accessToken != null) {

                if (idToken != null) {
                    // Check token for JWS or JWE by number of periods (.)
                    if (StringUtils.countMatches(idToken, ".") == 4) {
                        // It's a JWE.
            %>

            <div>
                <form action="oauth2-access-resource.jsp" id="loginForm" method="post">

                    <table class="user_pass_table">
                        <tbody>
                        <tr>
                            <td><label>Access Token :</label></td>
                            <td><input id="accessToken" name="accessToken" style="width:450px"
                                       value="<%=accessToken%>"/>
                        </tr>
                        <tr>
                            <td><label>UserInfo Endpoint :</label></td>
                            <td><input id="resource_url" name="resource_url" type="text" value="<%=userInfo%>"
                                       style="width:450px"/>
                        </tr>
                        <tr>
                            <td><label>Encrypted ID Token :</label></td>
                            <td><textarea id="encryptedIdToken" name="idToken" style="width:450px">
                                <%=idToken.trim()%>
                            </textarea>
                        </tr>
                        <tr>
                            <td><label>Client Private Key :</label></td>
                            <td><textarea id="clientPrivateKey" name="privateKey" style="width:450px"></textarea></td>
                            <td>
                                <input type="submit" class="button" value="Decrypt"
                                       onclick="decryptIdToken(this);return false;">
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <input type="submit" class="button" value="Get UserInfo">
                            </td>
                            <%
                                if (isOIDCLogoutEnabled) {
                            %>
                            <td>
                                <button type="button" class="button"
                                        onclick="document.location.href='<%=(String)session.getAttribute(OAuth2Constants.OIDC_LOGOUT_ENDPOINT)%>';">
                                    Logout
                                </button>
                            </td>
                            <%
                                }
                            %>
                        </tr>
                        </tbody>
                    </table>

                </form>
            </div>

            <%
                    } else {
                        try {
                            name = SignedJWT.parse(idToken).getJWTClaimsSet().getSubject();
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        }
            %>

            <div>
                <form action="oauth2-access-resource.jsp" id="loginForm" method="post">

                    <table class="user_pass_table" id="oauth-code-with-JWS">
                        <tbody>
                        <tr>
                            <td><label>Logged In User :</label></td>
                            <td><label id="loggedUser"><%=name%></label></td>
                        </tr>
                        <tr>
                            <td><label>Access Token :</label></td>
                            <td><input id="accessToken" name="accessToken" style="width:450px" value="<%=accessToken%>"/>
                        </tr>
                        <tr>
                            <td><label>UserInfo Endpoint :</label></td>
                            <td><input id="resource_url" name="resource_url" type="text" value="<%=userInfo%>"
                                       style="width:450px"/>
                        </tr>

                        <tr>
                            <td>
                                <input type="submit" class="button" value="Get UserInfo">
                            </td>
                            <%
                                if (isOIDCLogoutEnabled) {
                            %>
                            <td>
                                <button type="button" class="button"
                                        onclick="document.location.href='<%=(String)session.getAttribute(OAuth2Constants.OIDC_LOGOUT_ENDPOINT)%>';">
                                    Logout
                                </button>
                            </td>
                            <%
                                }
                            %>
                        </tr>
                        </tbody>
                    </table>

                </form>
            </div>

            <%
                    }
                } else {
            %>

            <div>
                <form action="oauth2-access-resource.jsp" id="loginForm" method="post">

                    <table class="user_pass_table">
                        <tbody>
                        <tr>
                            <td><label>Access Token :</label></td>
                            <td><input id="accessToken" name="accessToken" style="width:450px" value="<%=accessToken%>"/>
                        </tr>
                        <% if (application.getInitParameter("setup").equals("AM")) { %>
                        <tr>
                            <td><label>Resource URL :</label></td>
                            <td><input id="resource_url" name="resource_url" type="text" style="width:450px"/>
                        </tr>
                        <% } %>
                        <tr>
                            <td><label>Introspection Endpoint :</label></td>
                            <td><input id="resource_url" name="resource_url" type="text" style="width:450px"/>
                        </tr>
                        <tr>
                            <td>
                                <input type="submit" class="button" value="Get TokenInfo">
                            </td>
                        </tr>
                        </tbody>
                    </table>

                </form>
            </div>

            <%
                }
            } else if (OpenIDConnectConstants.ID_TOKEN.equals(implicitResponseType)) {
            %>
            <div>
                <table class="user_pass_table" id="implicit-id-token">
                    <tbody>
                    <tr>
                        <script type="text/javascript">
                            var idtoken = getIDtoken();
                            renderImplicitFlowIdTokenHTML(idtoken);
                            var decodedIdToken = JSON.parse(getDecodedIDToken());
                            makeList(decodedIdToken);
                        </script>
                    </tr>
                    </tbody>
                </table>
                <%session.invalidate();%>
            </div>

            <%
            } else if (OpenIDConnectConstants.ID_TOKEN_TOKEN.equals(implicitResponseType)) {
            %>
            <div>
                <table class="user_pass_table" id="implicit-id-token-token">
                    <tbody>
                    <tr>
                        <script type="text/javascript">
                            var idtoken = getIDtoken();
                            renderImplicitFlowIdTokenTokenHTML(idtoken);
                            var decodedIdToken = JSON.parse(getDecodedIDToken());
                            makeList(decodedIdToken);
                        </script>
                    </tr>
                    </tbody>
                </table>
            </div>
            <%
            } else if (grantType != null && OAuth2Constants.OAUTH2_GRANT_TYPE_IMPLICIT.equals(grantType)) {
            %>
            <div>
                <form action="oauth2-access-resource.jsp" id="loginForm" method="post">

                    <table class="user_pass_table">
                        <tbody>
                        <tr>
                            <td><label>Access Token :</label></td>
                            <td><input id="accessToken" name="accessToken" style="width:450px"/>
                                <script type="text/javascript">
                                    document.getElementById("accessToken").value = getAcceesToken();
                                </script>
                        </tr>
                        <% if (application.getInitParameter("setup").equals("AM")) { %>
                        <tr>
                            <td><label>Resource URL :</label></td>
                            <td><input id="resource_url" name="resource_url" type="text" style="width:450px"/>
                        </tr>
                        <% } %>
                        <tr>
                            <td><label>Introspection Endpoint :</label></td>
                            <td><input id="resource_url" name="resource_url" type="text" style="width:450px"/>
                        </tr>
                        <tr>
                            <td>
                                <input type="submit" class="button" value="Get TokenInfo">
                            </td>
                        </tr>
                        </tbody>
                    </table>

                </form>

            </div>
            <% } %>
        </td>
    </tr>
</table>
<script type="text/javascript">
    function togglePKCEMethod() {
        var radios = document.getElementsByName('code_challenge_method');
        var pkceMethod = "";
        for (var i = 0, length = radios.length; i < length; i++) {
            if (radios[i].checked) {
                pkceMethod = radios[i].value;
                break;
            }
        }
        var pkceChallenge = document.getElementsByName("code_challenge")[0];
        console.log(pkceMethod + " " + pkceChallenge.value);
        if (pkceMethod == "S256") {
            pkceChallenge.value = "<%=code_challenge%>";
        } else if (pkceMethod == "plain") {
            pkceChallenge.value = "<%=code_verifier%>";
        }
    }

    function pkceChangeVisibility(jQuery ) {
        if ($("#grantType").val() == "<%=OAuth2Constants.OAUTH2_GRANT_TYPE_CODE%>" &&
                $("input[name='use_pkce']:checked")[0].value == "yes") {
            $("#pkceMethod").show();
            $("#pkceChallenge").show();
            $("#pkceVerifier").show();


            $("input[name='code_challenge_method']")[0].removeAttribute('disabled');
            $("input[name='code_challenge_method']")[1].removeAttribute('disabled');
            $("input[name='code_challenge']")[0].removeAttribute('disabled');
        } else {
            $("#pkceMethod").hide();
            $("#pkceChallenge").hide();
            $("#pkceVerifier").hide();
            $("#pkceOption").hide();

            $("input[name='code_challenge_method']")[0].setAttribute('disabled', true);
            $("input[name='code_challenge_method']")[1].setAttribute('disabled', true);
            $("input[name='code_challenge']")[0].setAttribute('disabled', true);
        }
        if ($("#grantType").val() == "<%=OAuth2Constants.OAUTH2_GRANT_TYPE_CODE%>") {
            $("#pkceOption").show();
        }
    }

    $( document ).ready(pkceChangeVisibility);
    //set form change handler.
    $("form[name='oauthLoginForm']").change(pkceChangeVisibility)
</script>
<%
    if (isOIDCSessionEnabled) {
%>
<iframe id="rpIFrame" src="rpIFrame.jsp" frameborder="0" width="0" height="0"></iframe>
<%
    }
%>

</body>
</html>