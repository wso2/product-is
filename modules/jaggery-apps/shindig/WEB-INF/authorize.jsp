<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<%@ page import="org.apache.shiro.SecurityUtils" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags" %>

<%@ page import="net.oauth.OAuthConsumer" %>
<%@ page import="org.apache.shindig.social.opensocial.oauth.OAuthEntry" %>
<%@ page import="org.apache.shindig.social.opensocial.oauth.OAuthDataStore" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%
  // Gather data passed in to us.
  OAuthConsumer consumer = (OAuthConsumer)request.getAttribute("CONSUMER");
  OAuthEntry entry = (OAuthEntry) request.getAttribute("OAUTH_ENTRY");
  OAuthDataStore dataStore = (OAuthDataStore) request.getAttribute("OAUTH_DATASTORE");
  String token = (String)request.getAttribute("TOKEN");
  String callback = (String)request.getAttribute("CALLBACK");

  // Check if the user already authorized
  // TODO - this is a bit hard since we cannot get at the jsondb here...

  // If user clicked on the Authorize button then we're good.
  if (request.getParameter("Authorize") != null) {
    // If the user clicked the Authorize button we authorize the token and redirect back.
    dataStore.authorizeToken(entry, SecurityUtils.getSubject().getPrincipal().toString());

    // Bounce back to the servlet to handle redirecting to the callback URL
    request.getRequestDispatcher("/oauth/authorize?oauth_token=" + token + "&oauth_callback=" + callback)
            .forward(request,response);
  } else if (request.getParameter("Deny") != null) {
    dataStore.removeToken(entry);
  }
  // Gather some data
  pageContext.setAttribute("appTitle", consumer.getProperty("title") , PageContext.PAGE_SCOPE);
  pageContext.setAttribute("appDesc", consumer.getProperty("description"), PageContext.PAGE_SCOPE);
    
  pageContext.setAttribute("appIcon", consumer.getProperty("icon"));
  pageContext.setAttribute("appThumbnail", consumer.getProperty("thumbnail"));
%>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Your Friendly OAuth Provider</title>
</head>
<body>
Greetings <shiro:principal/>,<br/><br/>

The following application wants to access your account information<br/><br/>

<h3><img src="${appIcon}"/> <b><c:out value="${appTitle}"/></b> is trying to access your information.</h3>
<img src="${appThumbnail}" align="left" width="120" height="60"/>
<c:out value="${appDesc}" default=""/>
<br/>

<form id="authorize_form" name="authZForm" action="authorize" method="POST">
  <input type="hidden" id="authorize_oauth_token" name="oauth_token" value="<%= token %>"/>
  <input type="submit" id="authroize_deny" name="Authorize" value="Deny"/>
  <input type="submit" id="authorize_authorize" name="Authorize" value="Authorize"/>
</form>

</body>
</html>
