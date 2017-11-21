<%--
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
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title>Credential Management Samples</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <meta name="layout" content="main"/>

    <script type="text/javascript" src="http://www.google.com/jsapi"></script>


    <link href="assets/css/font-awesome.min.css" type="text/css" rel="stylesheet"/>
    <link href="assets/css/customize-template.css" type="text/css" media="screen, projection"
          rel="stylesheet"/>
    <link href="assets/css/landing-page.css" type="text/css" rel="stylesheet"/>

    <style>
    </style>
</head>
<body>
<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">

            <!--a href="${pageContext.request.contextPath}/home.jsp" class="brand"><img src="assets/img/logo.jpg" /> </a-->
            <ul class="nav pull-right">
                <li style="text-align: left"><a
                        href="https://localhost/saml_login"><i class="fa fa-edit"></i><font
                        colour="blue">Purchase</font></a></li>
                <li><a href="${pageContext.request.contextPath}/samlsso"><i class="fa fa-edit"></i>Sign
                    in</a></li>
                <li><a href="${pageContext.request.contextPath}/infoRecover/signup"><i
                        class="fa fa-edit"></i>Account Registration</a></li>
                <li>
                    <a href="${pageContext.request.contextPath}/infoRecover/select_password_recovery.jsp"><i
                            class="fa fa-lock"></i> Recover Password</a></li>
            </ul>
        </div>
    </div>
</div>
