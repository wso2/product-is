<%--
  ~ Copyright (c) 2018 WSO2 Inc. (http://wso2.com) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="org.wso2.is.hello.world.app.HelloWorldException"%>
<%@page import="org.wso2.is.hello.world.app.HelloWorldAppUtils"%>
<%
    Logger logger = Logger.getLogger(getClass().getName());
    if ("POST".equalsIgnoreCase(request.getMethod()) && request.getParameter("logout") != null) {
        HelloWorldAppUtils.logout(request, response);
        response.sendRedirect("index.jsp");
        return;
    }
	HttpSession currentSession = request.getSession(false);
	String accessToken = "";
	String idToken = "";
	
	try {
        HelloWorldAppUtils.getToken(request, response);
        currentSession = request.getSession(false);
        if (currentSession == null || currentSession.getAttribute("authenticated") == null) {
            currentSession.invalidate();
            response.sendRedirect("index.jsp");
        } else {
            accessToken = (String) currentSession.getAttribute("accessToken");
            idToken = (String) currentSession.getAttribute("idToken");
        }
	} catch (HelloWorldException e) {
	    logger.log(Level.SEVERE, "Error while getting access token.", e);
	    response.sendRedirect("index.jsp");
	}

%>
<html lang="en">
<head>
      <meta charset="utf-8">
      <meta http-equiv="X-UA-Compatible" content="IE=edge">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <meta name="description" content="HelloAuth Application">

      <title>HelloAuth</title>

      <!-- Bootstrap Core CSS -->
      <link href="libs/bootstrap_3.3.7/css/bootstrap.min.css" rel="stylesheet">
      <!-- Custom CSS -->
      <link href="css/custom.css" rel="stylesheet">
      <!-- Custom Fonts -->
      <link href="libs/font-awesome_4.3.0/css/font-awesome.min.css" rel="stylesheet" type="text/css">
      <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
      <!--[if lt IE 9]>
      <script src="libs/html5shiv_3.7.0/html5shiv.js"></script>
      <script src="libs/respond_1.4.2/respond.min.js"></script>
      <![endif]-->
</head>
<body class="app-home">
    <nav class="navbar navbar-inverse app-navbar">
        <div class="navbar-header">
            <a class="navbar-brand" href="#">HelloAuth</a>
        </div>
        <div class="container-fluid">
            <div class="collapse navbar-collapse">
                <ul class="nav navbar-nav navbar-right">
                    <li class="dropdown user-name">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <span class="user-name"></span><i class="fa fa-chevron-down"></i></span>
                        </a>
                        <ul class="dropdown-menu" role="menu">
                            <li><a onclick="$('#login-form-drop-down').submit();">
                                Logout</a>
                            </li>
                            <form method="post" id="login-form-drop-down">
                                <input id="logout" name="logout" type="hidden" value="logout">
                            </form>
                        </ul>
                    </li>
                </ul>
            </div>
        </div>
    </nav>
    <section>
        <div class="container">
            <div class="row">
                <div class="col-md-12">
                    <h2 class="text-center welcome-text">Welcome</h2>
                    <div class="app-icon center-block">
                        <i class="fa fa-user"></i>
                    </div>
                </div>
            </div>
        </div>
    </section>
    <div class="row">
        <div class="col-md-12">
            <br>
            <button href="#collapse-auth-container" class="btn btn-default nav-toggle center-block auth-button">Show Authentication
            Details</button>
        </div>
    </div>
    <div class="row">
        <div class="col-md-8 col-md-offset-2">
            <div id="collapse-auth-container">
                <h3 class="token-header">Access Token</h3>
                <div class="well">
                    <div class="access-token"><%=accessToken%></div>
                </div>
                <h3 class="token-header">ID Token</h3>
                <div class="col-md-6">
                    <div class="row">
                        <h4 class="sub-token-header">Encoded</h4>
                        <div class="well ">
                            <input id="id-token" type="hidden" value="<%=idToken%>"/>
                            <div class="id-token"></div>
                        </div>
                    </div>
                </div>
                <div class="col-md-6 remove-padding-right">
                        <h4 class="sub-token-header">Decoded</h4>
                        <div class="formatted-token"><pre></pre></div>
                </div>
            </div>
        </div>
    </div>

    <!-- jQuery -->
    <script src="libs/jquery_1.11.1/jquery.js"></script>
    <!-- Bootstrap Core JavaScript -->
    <script src="libs/bootstrap_3.3.7/js/bootstrap.min.js"></script>
    <script src="js/custom.js"></script>
    <script>

    </script>
<body>
</html>
