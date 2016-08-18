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
<%@ page import="org.wso2.carbon.identity.sso.agent.bean.SSOAgentSessionBean" %>
<%@ page import="org.wso2.carbon.identity.sso.agent.util.SSOAgentConfigs" %>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title>Credential Management Samples</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <meta name="layout" content="main"/>

    <script src="assets/js/jquery.js"></script>
    <script src="assets/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="http://www.google.com/jsapi"></script>

    <link href="assets/css/font-awesome.min.css" type="text/css" rel="stylesheet"/>
    <link href="assets/css/customize-template.css" type="text/css" media="screen, projection"
          rel="stylesheet"/>

    <style>
    </style>
</head>
<body>
    <%
    String dupSubject = "Anonymous";
    SSOAgentSessionBean agentSessionBean = (SSOAgentSessionBean)session.getAttribute(
                    SSOAgentConfigs.getSessionBeanName());
                    if (agentSessionBean == null) {
                         %>
<script type="text/javascript">
    location.href = "../index.jsp";
</script>
    <%
                    } else {
                        if (agentSessionBean.getSAMLSSOSessionBean() == null) {
                             %>
<script type="text/javascript">
    location.href = "../index.jsp";
</script>
    <%
                        } else {
                            dupSubject = agentSessionBean.getSAMLSSOSessionBean().getSubjectId();
                        }
                    }


%>
<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">

            <!--a href="${pageContext.request.contextPath}/home.jsp" class="brand"><img src="assets/img/logo.jpg" /> </a-->

            <ul class="nav pull-right">
                <li class="dropdown">
                    <a data-toggle="dropdown" class="dropdown-toggle" href="#"><i
                            class="icon icon-user"></i>Welcome <%=dupSubject%> <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li>
                            <a href="${pageContext.request.contextPath}/accountOverview?username=<%=dupSubject%>"><i
                                    class="fa fa-arrow-circle-left"></i> Account Overview</a></li>
                        <li>
                            <a href="${pageContext.request.contextPath}/editUser?username=<%=dupSubject%>"><i
                                    class="fa fa-wrench"></i> Manage Account</a></li>
                    </ul>
                </li>
                <%--<li><a href="${pageContext.request.contextPath}/editUser?username=<%=dupSubject%>"><i class="fa fa-edit"></i> Update Profile</a></li>--%>
                <li><a href="${pageContext.request.contextPath}/logout"><i class="fa fa-edit"></i>
                    Sign out</a></li>

            </ul>

            <%--<ul class="nav pull-right">--%>

            <%--<li class="dropdown">--%>
            <%--<a data-toggle="dropdown" class="dropdown-toggle" href="#"><i class="icon icon-user"></i>Welcome <%=dupSubject%> <b class="caret"></b></a>--%>
            <%--<ul class="dropdown-menu">--%>
            <%--<li><a><i class="fa fa-wrench"></i> Setting</a></li>--%>
            <%--<li><a href="samlsso"><i class="fa fa-arrow-circle-left"></i> Sign Out</a></li>--%>
            <%--</ul>--%>
            <%--</li>--%>
            <%--<li><a href="logout"><i class="fa fa-edit"></i> Sign out</a></li>--%>
            <%--</ul>--%>
        </div>
    </div>
</div>

<div id="body-container">
    <div id="body-content">

        <div class="body-nav body-nav-horizontal body-nav-fixed">
            <div class="container">
                <ul>
                    <li>
                        <a href="${pageContext.request.contextPath}/index.jsp">
                            <i class="icon-lock icon-large"></i> Home
                        </a>
                    </li>
                </ul>
            </div>
        </div>

        <section class="page container">
            <div class="row">
                <div class="span12">
