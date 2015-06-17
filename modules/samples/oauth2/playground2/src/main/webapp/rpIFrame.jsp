<!--
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
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@ page import="org.wso2.sample.identity.oauth2.OAuth2Constants" %>
<%@ page import="org.wso2.sample.identity.oauth2.OAuth2ServiceClient" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% OAuth2ServiceClient oAuth2ServiceClient = new OAuth2ServiceClient();
    String clientId = oAuth2ServiceClient.getClientID();
    String endpoint = OAuth2Constants.OP_ENDPOINT_URL;
    String targetOrigin = OAuth2Constants.TARGET_URL;
    String statusCookie;
    statusCookie = request.getParameter("session");
%>
<html>
<iframe id="opIFrame" style='visibility: hidden;' src=endpoint>
</iframe>
<head>
    <script type="text/javascript">
        var sessionState = '<%=statusCookie%>'
    </script>
    <script src="http://crypto-js.googlecode.com/svn/tags/3.0.2/build/rollups/sha256.js"></script>
    <script type="text/javascript">
        var endPoint = '<%=endpoint%>';
        var targetOrigin = '<%=targetOrigin%>';
        var clientId = '<%=clientId%>';
        var sessionState = '<%=statusCookie%>';
        var origin = window.location.protocol.replace(':', '://') + window.location.host;
        var salt = Math.random().toString().slice(2);
        var mes = CryptoJS.SHA256(clientId + origin + sessionState + salt) + "." + salt + " " + clientId;
        console.log("session state at the RP side" + sessionState);
    </script>
    <script type="text/javascript" src="js/polling.js">
    </script>
</head>
<body>
</body>
</html>
