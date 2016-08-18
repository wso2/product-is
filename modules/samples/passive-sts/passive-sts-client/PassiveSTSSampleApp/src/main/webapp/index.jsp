<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
        "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="java.util.HashMap" %>
<%@page import="java.util.Iterator" %>
<%@page import="java.util.Map" %>
<html>
<head>
<link rel="stylesheet" type="text/css" href="css/cart-styles.css">
<title>WSO2</title>
</head>
<body>
    <%
        String rstr = (String) request.getSession().getAttribute("RSTR");
        Map<String, String> claimMap = (HashMap<String, String>) request.getSession().getAttribute("claimMap");
        String message = (String) request.getSession().getAttribute("message");
        String displayFullResponse = (String)request.getSession().getAttribute("displayFullResponse");
        String logouturl = (String) request.getSession().getAttribute("logouturl");

    %>
    <div id="container">
        <div id="header-area">
            <img src="images/cart-logo.gif" alt="Logo" vspace="10" />

        </div>
        <!-- Header-are end -->

        <div id="content-area">
            <div class="cart-tabs">
                <table cellpadding="0" cellspacing="0" border="0">
                    <tr>
                        <td class="cart-tab-left"><img src="images/cart-tab-left.gif"
                            alt="-"></td>
                        <td class="cart-tab-mid"><a>Home</a></td>
                        <td class="cart-tab-right"><img
                            src="images/cart-tab-right.gif" alt="-"></td>
                    </tr>
                </table>
            </div>
            <table cellpadding="0" cellspacing="0" border="0" class="cart-expbox">
                <tr>
                    <td><img src="images/cart-expbox-01.gif" alt="-"></td>
                    <td class="cart-expbox-02">&nbsp</td>
                    <td><img src="images/cart-expbox-03.gif" alt="-"></td>
                </tr>
                <tr>
                    <td class="cart-expbox-08">&nbsp</td>
                    <td class="cart-expbox-09">
                        <!--all content for cart and links goes here-->
                    </td>
                    <td class="cart-expbox-04">&nbsp</td>
                </tr>
                <tr>
                    <td><img src="images/cart-expbox-07.gif" alt="-"></td>
                    <td class="cart-expbox-06">&nbsp</td>
                    <td><img src="images/cart-expbox-05.gif" alt="-"></td>
                </tr>

            </table>
            <hr />
            <div class="product-box">
                <h2><%=message%></h2>
                <% if(logouturl != null){ %>
                <a href='<%=logouturl%>'>Logout</a>
                <%}%>
            </div>
            
            <% if(claimMap != null && !claimMap.isEmpty()){ %>
            <table border="1" align="center">
                <thead>
                    <tr>
	                    <th>Claim URI</th>
	                    <th>Claim Value</th>
                    </tr>
                </thead>
                <tbody>
	            <%
	                for (Map.Entry<String, String> entry : claimMap.entrySet()) {
	            %>
                   <tr>
                       <td><%=entry.getKey()%></td>
                       <td><%=entry.getValue()%></td>
                   </tr>
	            <%}%>
                </tbody>
            </table>
            <%}%>
            
            <%if("true".equals(displayFullResponse)){ %>
                <pre><%=rstr%></pre>
            <%}%>    
        </div>
        <!-- content-area end -->
    </div>
</body>
</html>