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
    if ("POST".equalsIgnoreCase(request.getMethod()) && request.getParameter("singIn") != null) {
	    try {
	        String authzRequest = HelloWorldAppUtils.getAuthzRequest(request, response);
	        response.sendRedirect(authzRequest);
	    } catch (HelloWorldException e) {
		    logger.log(Level.SEVERE, "Error while building authorization request.", e);
	    }
    }
%>

<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="HelloAuth Login">

    <title>Login | Hello Auth</title>

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
<body class="app-login">
    <section class="login-block">
        <div class="container">
            <div class="row">
                <div class="col-md-12 login-sec">
                    <div class="app-icon center-block">
                        <i class="fa fa-key"></i>
                    </div>
                    <h2 class="text-center">HelloAuth</h2>
                    <form class="app-login-form" method="post">
                        <input id="singIn" name="singIn" type="hidden" value="singIn">
                        <button type="submit" id="btn-login" class="btn btn-login">LOGIN</button>
                    </form>
                </div>
            </div>
        </div>
    </section>
    <!-- jQuery -->
    <script src="libs/jquery_1.11.1/jquery.js"></script>
    <!-- Bootstrap Core JavaScript -->
    <script src="libs/bootstrap_3.3.7/js/bootstrap.min.js"></script>
</body>

</html>
