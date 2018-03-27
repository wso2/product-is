<%--
  ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<%@ page language="java" session="true" %>
<%@ page import="java.util.Random" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils" %>
<%@page import="org.apache.log4j.Logger"%>

<%
    Logger logger = Logger.getLogger(this.getClass());
    String reqNumberStr = request.getParameter("timeBasedId");
    String callbackUrl = request.getParameter("callbackUrl");
    String oldNumStr = request.getParameter("oldNum");

    /* This is for demonstration purposes only.
    You need to use a SecureRandom generator to avoid security breaches. */
    Random randomGenerator = new Random();
    int nextNum = randomGenerator.nextInt(10000);

    if (reqNumberStr != null && oldNumStr != null) {
        try {
            int reqNumber = Integer.parseInt(reqNumberStr);
            int oldNum = Integer.parseInt(oldNumStr);
            if (oldNum == reqNumber) {
                if (callbackUrl != null) {
                    String cbURL = URLDecoder.decode(callbackUrl, StandardCharsets.UTF_8.name());
                    cbURL = FrameworkUtils.appendQueryParamsStringToUrl(cbURL, "success=true");
                    /* This is for demonstration purposes only.
                    You need to properly encode the parameters to avoid security breaches. */
                    response.sendRedirect(cbURL);
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid number.", e);
        }
    }
%>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WSO2 Identity Server Sample</title>

    <link rel="icon" href="images/favicon.png" type="image/x-icon"/>
    <link href="libs/bootstrap_3.3.5/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/Roboto.css" rel="stylesheet">
    <link href="css/custom-common.css" rel="stylesheet">

    <!--[if lt IE 9]>
    <script src="js/html5shiv.min.js"></script>
    <script src="js/respond.min.js"></script>
    <![endif]-->


</head>

<body>

<!-- page content -->
<div class="container-fluid body-wrapper">

    <div class="row">
        <div class="col-md-12">

            <!-- content -->
            <div class="container col-xs-10 col-sm-6 col-md-6 col-lg-3 col-centered wr-content wr-login col-centered">
                <div>
                    <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">Hardware
                        Key </h2>
                </div>
                <div class="boarder-all ">
                    <div class="clearfix"></div>
                    <div class="padding-double login-form">

                        <form action="" method="post" id="loginForm">
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <!--This is for demonstration purposes only.
                                You need to properly encode the parameters before adding to the page source to
                                avoid security breaches. -->
                                <input id="generatedNumber" name="generatedNumber" type="text" value="<%=nextNum%>"
                                       class="form-control" tabindex="0"
                                       placeholder="Key" disabled="disabled">
                                <input id="oldNum" name="oldNum" hidden="hidden" value="<%=nextNum%>"/>
                                <input id="callbackUrl" name="callbackUrl" hidden="hidden" value="<%=callbackUrl%>"/>
                                <input id="authenticator" name="authenticator" hidden="hidden"
                                       value="SampleHardwareKeyAuthenticator">
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <input id="timeBasedId" name="timeBasedId" type="password" class="form-control"
                                       placeholder="Password" autocomplete="off">
                            </div>

                            <br>

                            <div class="form-actions">
                                <button
                                        class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                                        type="submit">Sign in
                                </button>
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">

                                <br/>

                            </div>

                            <div class="clearfix"></div>
                        </form>

                        <div class="clearfix"></div>
                    </div>
                </div>
                <!-- /content -->
            </div>
        </div>
        <!-- /content/body -->

    </div>
</div>

<script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>

</body>
</html>