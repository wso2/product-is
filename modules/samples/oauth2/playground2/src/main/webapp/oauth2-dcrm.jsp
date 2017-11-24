<%@ page import="org.wso2.sample.identity.oauth2.OAuth2Constants" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <head>
        <title>WSO2 OAuth2 DCRM </title>
        <meta charset="UTF-8">
        <meta name="description" content=""/>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"></script>
        <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
        <script type="text/javascript" src="js/prettify.js"></script>
        <script type="text/javascript" src="js/kickstart.js"></script>
        <link rel="stylesheet" type="text/css" href="css/kickstart.css" media="all"/>
        <link rel="stylesheet" type="text/css" href="style.css" media="all"/>

        <style type='text/css'>
            label {
                width: 100px;
            }
        </style>

        <script type="text/javascript">
            function setVisibility() {
                var dcrmMethod = document.getElementById("dcrmMethod").value;

                if (dcrmMethod == 'create') {
                    document.getElementById("clientNametr").style.display = "";
                    document.getElementById("clientIdtr").style.display = "none";
                    document.getElementById("clientSecrettr").style.display = "none";
                    document.getElementById("grantTypetr").style.display = "";
                    document.getElementById("redirectUritr").style.display = "";
                }
                if (dcrmMethod == 'read') {
                    document.getElementById("clientNametr").style.display = "none";
                    document.getElementById("clientIdtr").style.display = "";
                    document.getElementById("clientSecrettr").style.display = "none";
                    document.getElementById("grantTypetr").style.display = "none";
                    document.getElementById("redirectUritr").style.display = "none";
                } else if (dcrmMethod == 'update') {
                    document.getElementById("clientNametr").style.display = "";
                    document.getElementById("clientIdtr").style.display = "";
                    document.getElementById("clientSecrettr").style.display = "";
                    document.getElementById("grantTypetr").style.display = "";
                    document.getElementById("redirectUritr").style.display = "";
                } else if (dcrmMethod == 'delete') {
                    document.getElementById("clientNametr").style.display = "none";
                    document.getElementById("clientIdtr").style.display = "";
                    document.getElementById("clientSecrettr").style.display = "none";
                    document.getElementById("grantTypetr").style.display = "none";
                    document.getElementById("redirectUritr").style.display = "none";
                }
                return true;
            }
        </script>
    </head>
    <body>
        <a id="top-of-page"></a>

        <div id="wrap" class="clearfix"/>
        <ul class="menu">
            <li class="current"><a href="index.jsp">Home</a></li>
        </ul>

        <div class="col_12"/>
        <div class="col_9"/>
        <h3>WSO2 OAuth2 DCRM</h3>

        <form action="oauth2-dcrm-request.jsp" id="loginForm" >
            <table class="user_pass_table" width="100%">
                <tbody>
                    <tr>
                        <td>Method</td>
                        <td>
                            <select id="dcrmMethod" name="dcrmMethod" onchange="setVisibility();">
                                <option selected disabled hidden>Choose here</option>
                                <option value="<%=OAuth2Constants.CREATE%>">Create</option>
                                <option value="<%=OAuth2Constants.READ%>">Read</option>
                                <option value="<%=OAuth2Constants.UPDATE%>">Update</option>
                                <option value="<%=OAuth2Constants.DELETE%>">Delete</option>
                            </select>
                        </td>
                    </tr>
                    <tr id="clientNametr" style="...">
                        <td><label>Client Name</label></td>
                        <td><input type="text" id="clientName" name="clientName" style="width:350px"></td>
                    </tr>
                    <tr id="clientIdtr" style="...">
                        <td><label>Client Id</label></td>
                        <td><input type="text" id="clientId" name="clientId" style="width:350px"></td>
                    </tr>
                    <tr id="clientSecrettr" style="...">
                        <td><label>Client Secret</label></td>
                        <td><input type="text" id="clientSecret" name="clientSecret" style="width:350px"></td>
                    </tr>
                    <tr id="grantTypetr" style="...">
                        <td><label>Grant Types</label></td>
                        <td><input type="text" id="grantTypes" name="grantTypes" style="width:350px"></td>
                    </tr>
                    <tr id="redirectUritr" style="...">
                        <td><label>Redirect URIs</label></td>
                        <td><input type="text" id="redirectUris" name="redirectUris" style="width:350px"></td>
                    </tr>
                    <tr>
                        <td colspan="2"><input type="submit" name="request" value="Request"></td>
                    </tr>
                </tbody>
            </table>
        </form>
    </body>
</html>
