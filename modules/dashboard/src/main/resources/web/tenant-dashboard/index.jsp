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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../lcm/js/lcm.js"></script>
<script type="text/javascript" src="../tenant-dashboard/js/dashboard.js"></script>

<link href="../tenant-dashboard/css/dashboard-common.css" rel="stylesheet" type="text/css" media="all"/>


<style type="text/css">
    .tip-table td.user-profile {
        background-image: url(../../carbon/tenant-dashboard/images/user-profile.png);
    }

    .tip-table td.openid {
        background-image: url(../../carbon/tenant-dashboard/images/openid.png);
    }
    .tip-table td.information-card {
        background-image: url(../../carbon/tenant-dashboard/images/information-card.png);
    }
   .tip-table td.scim {
        background-image: url(../../carbon/tenant-dashboard/images/scim.png);
    }

    .tip-table td.oauth {
        background-image: url(../../carbon/tenant-dashboard/images/oauth.png);
    }
    .tip-table td.user-roles {
        background-image: url(../../carbon/tenant-dashboard/images/user-roles.png);
    }
    .tip-table td.relying-parties {
        background-image: url(../../carbon/tenant-dashboard/images/relying-parties.png);
    }
    .tip-table td.key-stores {
        background-image: url(../../carbon/tenant-dashboard/images/key-stores.png);
    }
</style>
<%
        Object param = session.getAttribute("authenticated");
        String passwordExpires = (String) session.getAttribute(ServerConstants.PASSWORD_EXPIRATION);
        boolean hasPermissionManage = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage");

        boolean hasPermissionLogin = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/login");

        boolean hasPermissionConfigure = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/configure");
        
        boolean hasPermissionAdmin = CarbonUIUtil.isUserAuthorized(request,
                "/permission/admin");

        boolean hasPermissionForMySCIMProviders = CarbonUIUtil.isUserAuthorized(request,
                "/permission/admin/configure/security/usermgt/provisioning");
       

        boolean loggedIn = false;
        if (param != null) {
            loggedIn = (Boolean) param;             
        } 
%>
  
<div id="passwordExpire">
         <%
         if (loggedIn && passwordExpires != null) {
         %>
              <div class="info-box"><p>Your password expires at <%=passwordExpires%>. Please change by visiting <a href="../user/change-passwd.jsp?isUserChange=true&returnPath=../admin/index.jsp">here</a></p></div>
         <%
             }
         %>
</div>

 <!-- <h2 class="dashboard-title">Identity & Entitlement quick start dashboard</h2> -->
        <%-- <table class="tip-table">
            <tr>
                <td class="tip-top user-profile"></td>
                <td class="tip-empty"></td>
                <td class="tip-top openid"></td>
                <td class="tip-empty "></td>
                <td class="tip-top information-card"></td>
                <td class="tip-empty "></td>
                <td class="tip-top scim"></td>
            </tr>
            <tr>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
                            if (hasPermissionLogin) {
                        %>
                        <a href="../userprofile/index.jsp?region=region5&item=userprofiles_menu" class="tip-title" >User Profile</a> <br/>
                        <%
                        } else {
                        %>
                        <h3 class="tip-title">User Profile</h3><br/>
                        <%
                            }
                        %>
                        <p>Update your user profile or add multiple user profiles.The value from your profile will be used to populate your Information card and OpenID profiles.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
                            if (hasPermissionLogin) {
                        %>
                        <a href="../identity-provider/index.jsp?region=region5&item=openid_infocard_menu" class="tip-title" >OpenID</a> <br/>
                        <%
                        } else {
                        %>
                        <h3 class="tip-title">OpenID</h3><br/>
                        <%
                            }
                        %>

				<p>This OpenID can be used with any OpenID relying party which trusts your domain for login.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
                            if (hasPermissionManage) {
                        %>
                        <a  href="../sso-saml/manage_service_providers.jsp?region=region1&item=manage_saml_sso" class="tip-title">SAML 2.0 Single Sign-On</a> <br/>
                        <%
                        } else {
                        %>
                        <font size="3px" color="#333333">SAML 2.0 Single Sign-On</font></br></br>
                        <%
                            }
                        %>

                        <p>Use WSO2 Identity Server for Single Sign-on configuration. Service Providers can be added and removed.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
                            if (hasPermissionForMySCIMProviders) {
                        %>
                        <a  href="../scim/my-scim-index.jsp?region=region5&item=my_providers_menu" class="tip-title">My SCIM Providers</a> <br/>
                        <%
                        } else {
                        %>
                        <font size="3px" color="#333333">My SCIM Providers</font> <br/> <br/>
                        <%
                            }
                        %>


                        <p>Use WSO2 Identity Server to provision users and roles across heterogeneous identity management systems in a standard way</p>

                    </div>
                </td>

            </tr>
            <tr>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
            </tr>
        </table> --%>
	<!-- <div class="tip-table-div"></div> -->
       <%--  <table class="tip-table">
            <tr>
                <td class="tip-top oauth"></td>
                <td class="tip-empty"></td>
                <td class="tip-top user-roles"></td>
                <td class="tip-empty "></td>
                <td class="tip-top relying-parties"></td>
                <td class="tip-empty "></td>
                <td class="tip-top key-stores"></td>
            </tr>
            <tr>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
                            if (hasPermissionManage) {
                        %>
                        <a  href="../oauth/index.jsp?region=region1&item=oauth_menu" class="tip-title">OAuth</a> <br/>
                        <%
                        } else {
                        %>
                        <font size="3px" color="#333333">OAuth</font><br/>
                        <%
                            }
                        %>


                        <p>Use WSO2 Identity Server to facilitate identity delegation with OAuth 1.0 and OAuth 2.0</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
                            if (hasPermissionConfigure) {
                        %>
                        <a  href="../userstore/index.jsp?region=region1&item=userstores_menu" class="tip-title">Users and Roles</a><br/>
                        <%
                        } else {
                        %>
                        <font size="3px" color="#333333">Users and Roles</font></br></br>
                        <%
                            }
                        %>

                        <p>WSO2 Identity Server enables you to manage users and roles in your system.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
                        if (hasPermissionAdmin) {
                        %>
                        <a href="../idpmgt/idp-mgt-list.jsp" class="tip-title">Trusted Identity Providers</a> <br/>
                        <%
                            } else {
                        %>
                        <font size="3px" color="#333333">Trusted Identity Providers</font> <br/>
                        <%
                            }
                        %>

                        <p>Manage your Trusted Identity Providers with WSO2 Identity Server.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
                            if (hasPermissionConfigure) {
                        %>
                        <a href="../keystoremgt/keystore-mgt.jsp?region=region1&item=keystores_menu" class="tip-title" >Key Stores</a> <br/>
                        <%
                        } else {
                        %>
                        <font size="3px" color="#333333">Key Stores</font></br></br>
                        <%
                            }
                        %>

                        <p>Manage your Key Stores with WSO2 Identity Server</p>

                    </div>
                </td>
            </tr>
            <tr>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
            </tr>
        </table> --%>
<p>
    <br/>
</p> </div>
</div>
