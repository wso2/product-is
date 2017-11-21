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

<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.wso2.carbon.identity.sso.agent.bean.SSOAgentSessionBean" %>
<%@ page import="org.wso2.carbon.identity.sso.agent.util.SSOAgentConfigs" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<jsp:include page="includes/header.jsp"></jsp:include>

<%
    String claimedId = null;
    String subject = null;
    Map<String, List<String>> openIdAttributes = null;
    Map<String, String> samlSSOAttributes = null;
    SSOAgentSessionBean.AccessTokenResponseBean accessTokenResponseBean = null;

    if (session.getAttribute(SSOAgentConfigs.getSessionBeanName()) != null) {
        if (((SSOAgentSessionBean) session.getAttribute(
                SSOAgentConfigs.getSessionBeanName())).getOpenIDSessionBean() != null) {
            claimedId = ((SSOAgentSessionBean) session.getAttribute(
                    SSOAgentConfigs.getSessionBeanName())).getOpenIDSessionBean().getClaimedId();
            openIdAttributes = ((SSOAgentSessionBean) session.getAttribute(
                    SSOAgentConfigs.getSessionBeanName())).getOpenIDSessionBean().getOpenIdAttributes();
        } else if (((SSOAgentSessionBean) session.getAttribute(
                SSOAgentConfigs.getSessionBeanName())).getSAMLSSOSessionBean() != null) {
            subject = ((SSOAgentSessionBean) session.getAttribute(
                    SSOAgentConfigs.getSessionBeanName())).getSAMLSSOSessionBean().getSubjectId();
            samlSSOAttributes = ((SSOAgentSessionBean) session.getAttribute(
                    SSOAgentConfigs.getSessionBeanName())).getSAMLSSOSessionBean().getSAMLSSOAttributes();
            accessTokenResponseBean = ((SSOAgentSessionBean) session.getAttribute(
                    SSOAgentConfigs.getSessionBeanName())).getSAMLSSOSessionBean()
                    .getAccessTokenResponseBean();
        } else {
%>
<%--<script type="text/javascript">--%>
<%--location.href = "index.jsp";--%>
<%--</script>--%>
<%
        return;
    }
} else {
%>
<%--<script type="text/javascript">--%>
<%--location.href = "index.jsp";--%>
<%--</script>--%>


<%
        return;
    }
%>
<body>

<div class="product-box">
    <%
        if (subject != null) {
    %>
    <h2> You are logged in as <%=subject%>
    </h2>

    <%--<div>--%>
    <%--<div>--%>
    <%--<a href="${pageContext.request.contextPath}/editUser?username=<%=subject%>"><br>Edit User Profile</a> <br>--%>
    <%--</div>--%>
    <%--</div>--%>
    <%
    } else if (claimedId != null) {
    %>
    <h2> You are logged in as <%=claimedId%>
    </h2>
    <%
        }
    %>
    <table>
        <%
            if (samlSSOAttributes != null) {
                for (Map.Entry<String, String> entry : samlSSOAttributes.entrySet()) {
        %>
        <tr>
            <td><%=entry.getKey()%>
            </td>
            <td><%=entry.getValue()%>
            </td>
        </tr>
        <%
            }
        } else if (openIdAttributes != null) {
            for (Map.Entry<String, List<String>> entry : openIdAttributes.entrySet()) {
        %>
        <tr>
            <td><%=entry.getKey()%>
            </td>
            <td>
                <%
                    Iterator it = entry.getValue().iterator();
                    if (it.hasNext()) {
                %>
                <%=it.next().toString()%>
                <%
                    }
                %>
            </td>
        </tr>
        <%
                }
            }
        %>
    </table>
    <%
        if (subject != null) {
            if (accessTokenResponseBean != null) {
    %>
    <u><b>Your OAuth2 Access Token details</b></u>

    <div style="text-indent: 50px">Token Type: <%=accessTokenResponseBean.getToken_type()%> <br/>
    </div>
    <div style="text-indent: 50px">Access Token: <%=accessTokenResponseBean.getAccess_token()%>
        <br/></div>
    <div style="text-indent: 50px">Refresh Token: <%=accessTokenResponseBean.getRefresh_token()%>
        <br/></div>
    <div style="text-indent: 50px">Expiry In: <%=accessTokenResponseBean.getExpires_in()%> <br/>
    </div>
    <%
    } else {
        if (SSOAgentConfigs.isSAML2GrantEnabled()) {
    %>
    <a href="token">Request OAuth2 Access Token</a><br/>
    <%

                }
            }
        }
    %>
    <hr/>
    <%--<%--%>
    <%--if(subject != null && SSOAgentConfigs.isSLOEnabled()){--%>
    <%--%>--%>
    <%--<form action="logout">--%>
    <%--<input type="submit" value="Logout">--%>
    <%--</form>--%>
    <%--<%--%>
    <%--}--%>
    <%--%>--%>
</div>
<jsp:include page="includes/footer.jsp"></jsp:include>
