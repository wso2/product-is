<%--
  ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~    http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
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

    <h3 align="center">WSO2 OAuth2 Playground ~ Token Info</h3>


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
        %>
    </table>
</div>
</body>
</html>
