<%@page import="org.apache.commons.lang.StringUtils" %>
<%@page import="org.apache.oltu.oauth2.client.OAuthClient" %>
<%@page import="org.apache.oltu.oauth2.client.URLConnectionClient" %>
<%@page import="org.apache.oltu.oauth2.client.request.OAuthClientRequest" %>
<%@page import="org.apache.oltu.oauth2.client.response.OAuthClientResponse" %>
<%@page import="org.apache.oltu.oauth2.common.message.types.GrantType" %>
<%@page import="org.wso2.sample.identity.oauth2.OAuth2Constants" %>
<%@ page import="org.wso2.sample.identity.oauth2.OAuthPKCEAuthenticationRequestBuilder" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>

<%
    try {

        String consumerKey = request.getParameter(OAuth2Constants.CONSUMER_KEY);
        String consumerSecret = request.getParameter(OAuth2Constants.CONSUMER_SECRET);

        String authzEndpoint = request.getParameter(OAuth2Constants.OAUTH2_AUTHZ_ENDPOINT);
        String accessEndpoint = request.getParameter(OAuth2Constants.OAUTH2_ACCESS_ENDPOINT);
        String PKCECodeChallenge = request.getParameter(OAuth2Constants.OAUTH2_PKCE_CODE_CHALLENGE);
        String PKCECodeChallengeMethod = request.getParameter(OAuth2Constants.OAUTH2_PKCE_CODE_CHALLENGE_METHOD);
        String usePKCEParameter = request.getParameter(OAuth2Constants.OAUTH2_USE_PKCE);
        String logoutEndpoint = request.getParameter(OAuth2Constants.OIDC_LOGOUT_ENDPOINT);
        String sessionIFrameEndpoint = request.getParameter(OAuth2Constants.OIDC_SESSION_IFRAME_ENDPOINT);

        String recowner = request.getParameter(OAuth2Constants.RESOURCE_OWNER_PARAM);
        String recpassword = request.getParameter(OAuth2Constants.RESOURCE_OWNER_PASSWORD_PARAM);

        String authzGrantType = request.getParameter(OAuth2Constants.OAUTH2_GRANT_TYPE);
        String scope = request.getParameter(OAuth2Constants.SCOPE);
        String callBackUrl = request.getParameter(OAuth2Constants.CALL_BACK_URL);

        boolean usePKCE = usePKCEParameter != null && "yes".equals(usePKCEParameter);
        if(usePKCE) {
            session.setAttribute(OAuth2Constants.OAUTH2_USE_PKCE, usePKCE);
        }


        // By default IS does not validate scope. To validate we need to write a callback handler.
        if (scope == null || scope.trim().length() == 0) {
            scope = "default";
        }

        session.setAttribute(OAuth2Constants.OAUTH2_GRANT_TYPE, authzGrantType);
        session.setAttribute(OAuth2Constants.CONSUMER_KEY, consumerKey);
        session.setAttribute(OAuth2Constants.CONSUMER_SECRET, consumerSecret);
        session.setAttribute(OAuth2Constants.SCOPE, scope);
        session.setAttribute(OAuth2Constants.CALL_BACK_URL, callBackUrl);
        session.setAttribute(OAuth2Constants.OAUTH2_AUTHZ_ENDPOINT, authzEndpoint);
        session.setAttribute(OAuth2Constants.OIDC_LOGOUT_ENDPOINT, logoutEndpoint);
        session.setAttribute(OAuth2Constants.OIDC_SESSION_IFRAME_ENDPOINT, sessionIFrameEndpoint);

        if (authzGrantType.equals(OAuth2Constants.OAUTH2_GRANT_TYPE_CODE) ||
            authzGrantType.equals(OAuth2Constants.OAUTH2_GRANT_TYPE_IMPLICIT)) {
            // If the grant type is authorization code or implicit - then we need to send a request to the Authorization end point.

            if (StringUtils.isBlank(consumerKey) || StringUtils.isBlank(callBackUrl) ||
                StringUtils.isBlank(authzEndpoint)) {
%>

<script type="text/javascript">
    window.location = "oauth2.jsp?reset=true";
</script>

<%
        return;
    }

    OAuthPKCEAuthenticationRequestBuilder oAuthPKCEAuthenticationRequestBuilder = new OAuthPKCEAuthenticationRequestBuilder(authzEndpoint);
    if(authzGrantType.equals(OAuth2Constants.OAUTH2_GRANT_TYPE_CODE) && usePKCE) {
        oAuthPKCEAuthenticationRequestBuilder = oAuthPKCEAuthenticationRequestBuilder.setPKCECodeChallenge(PKCECodeChallenge, PKCECodeChallengeMethod);
    }

    oAuthPKCEAuthenticationRequestBuilder
            .setClientId(consumerKey)
            .setRedirectURI((String) session.getAttribute(OAuth2Constants.CALL_BACK_URL))
            .setResponseType(authzGrantType)
            .setScope(scope);

    OAuthClientRequest authzRequest = oAuthPKCEAuthenticationRequestBuilder.buildQueryMessage();
    response.sendRedirect(authzRequest.getLocationUri());
    return;

} else {

    // For any other grant type we need to send the request to the Access Token end point.
    OAuthClientRequest accessRequest = null;

    if (StringUtils.isBlank(recowner) || StringUtils.isBlank(recpassword)) {
        if (StringUtils.isBlank(consumerKey) || StringUtils.isBlank(consumerSecret) ||
            StringUtils.isBlank(accessEndpoint)) {
%>

<script type="text/javascript">
    window.location = "oauth2.jsp?reset=true";
</script>

<%
    }
    accessRequest = OAuthClientRequest.tokenLocation(accessEndpoint)
                                      .setGrantType(GrantType.CLIENT_CREDENTIALS)
                                      .setClientId(consumerKey)
                                      .setClientSecret(consumerSecret)
                                      .setScope(scope)
                                      .buildBodyMessage();

} else {
    if (StringUtils.isBlank(consumerKey) || StringUtils.isBlank(consumerSecret) || StringUtils.isBlank(recowner) ||
        StringUtils.isBlank(recpassword) || StringUtils.isBlank(accessEndpoint)) {
%>

<script type="text/javascript">
    window.location = "oauth2.jsp?reset=true";
</script>

<%
            }
            accessRequest = OAuthClientRequest.tokenLocation(accessEndpoint)
                                              .setGrantType(GrantType.PASSWORD)
                                              .setClientId(consumerKey)
                                              .setClientSecret(consumerSecret)
                                              .setScope(scope)
                                              .setUsername(recowner)
                                              .setPassword(recpassword)
                                              .buildBodyMessage();
        }

        // Creates OAuth client that uses custom http client under the hood
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthClientResponse oAuthResponse = oAuthClient.accessToken(accessRequest);
        String accessToken = oAuthResponse.getParam(OAuth2Constants.ACCESS_TOKEN);

        // For future use we store the access_token in session.
        session.setAttribute(OAuth2Constants.ACCESS_TOKEN, accessToken);
    }
} catch (Exception e) {
%>

<script type="text/javascript">
    window.location = "oauth2.jsp?reset=true&error=<%=e.getMessage()%>";
</script>

<%
    }
%>

<script type="text/javascript">
    window.location = "oauth2.jsp";
</script>


    