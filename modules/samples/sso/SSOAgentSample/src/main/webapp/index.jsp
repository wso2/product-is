<%@ page import="org.wso2.carbon.identity.sso.agent.util.SSOAgentConfigs" %>
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
<html>
<head>
    <link rel="stylesheet" type="text/css" href="css/cart-styles.css">
    <title>User Logged out</title>
</head>
<body>
<div id="container">
    <div id="header-area">
        <img src="images/cart-logo.gif" alt="Logo" vspace="10" />
    </div>
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
        <h1>Travelocity.COM</h1>
        <hr />
        <a href="../avis.com"> Avis.COM </a>
        <div class="product-box">
            <h2>Click <a href="samlsso">here</a> to login with <img src="images/saml2-logo.png" height="22" width="60"/> from <img src="images/identity_logo.gif" height="20" width="120"/></h2>
            <hr/>
            <h2>Click <a href="openid?claimed_id=<%=SSOAgentConfigs.getOpenIdProviderUrl()%>">here</a> to login with <img src="images/openid-logo.svg" height="20" width="55"/> from <img src="images/identity_logo.gif" height="20" width="120"/></h2>
            <hr/>
            <h2>Click <a href="openid?claimed_id=https://www.google.com/accounts/o8/id">here</a> to login with <img src="images/google-openid.png" height="20" width="75"/></h2>
            <hr/>
            <h2>Click <a href="openid?claimed_id=http://myopenid.com">here</a> to login with <img src="images/myopenid-logo.png" height="20" width="75"/></h2>
            <hr/>
            <fieldset>
                <legend>Login with your Claimed OpenID URI</legend>
                <form action="openid" method="post">
                    <div align="center">
                        <input type="text" name="claimed_id" size="30"/> <input type="submit" name="login" value="Login"/>
                    </div>
                </form>
            </fieldset>
        </div>
    </div>
    <div id="footer-area">
        <p>©2013 WSO2</p>
    </div>
</div>
</body>
</html>