<%@ page import="org.wso2.sample.identity.oauth2.OAuth2Constants" %>
<%@ page import="org.apache.http.HttpHeaders" %>
<%@ page import="org.apache.http.HttpResponse" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="org.json.simple.JSONValue" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="java.util.Objects" %>
<%@ page import="org.json.simple.JSONArray" %>
<%@ page import="org.apache.http.entity.StringEntity" %>
<%@ page import="org.apache.http.client.HttpClient" %>
<%@ page import="org.apache.http.impl.client.DefaultHttpClient" %>
<%@ page import="org.apache.http.client.methods.HttpDelete" %>
<%@ page import="org.apache.http.client.methods.HttpGet" %>
<%@ page import="org.apache.http.client.methods.HttpPut" %>
<%@ page import="org.apache.http.client.methods.HttpPost" %>
<%
    String method = request.getParameter(OAuth2Constants.DCRM_METHOD);
    String clientId = request.getParameter(OAuth2Constants.CLIENT_ID);
    String clientSecret = request.getParameter(OAuth2Constants.CLIENT_SECRET);
    String clientName = request.getParameter(OAuth2Constants.CLIENT_NAME);
    String grantTypes = request.getParameter(OAuth2Constants.GRANT_TYPES);
    String redirectUris = request.getParameter(OAuth2Constants.REDIRECT_URIS);
    String deleteConfirmation = null;

    HttpClient client = new DefaultHttpClient();
    BufferedReader reader;
    JSONObject jsonObject = null;

    try {
        if (Objects.equals(method, OAuth2Constants.DELETE)) {
            HttpDelete httpDelete = new HttpDelete(OAuth2Constants.CLIENT_CONFIGURATION_ENDPOINT + clientId);
            httpDelete.addHeader(HttpHeaders.AUTHORIZATION, OAuth2Constants.AUTHORIZATION);

            HttpResponse deleteResponse = client.execute(httpDelete);

            int statusCode = deleteResponse.getStatusLine().getStatusCode();

            if (statusCode == 204) {
                deleteConfirmation = "Application has been successfully deleted.";
            } else {
                deleteConfirmation = "Application has not been successfully deleted.";
            }
        } else {
            if (Objects.equals(method, OAuth2Constants.CREATE)) {
                HttpPost httpPost = new HttpPost(OAuth2Constants.CLIENT_CONFIGURATION_ENDPOINT);
                httpPost.addHeader(HttpHeaders.AUTHORIZATION, OAuth2Constants.AUTHORIZATION);
                httpPost.addHeader(HttpHeaders.CONTENT_TYPE, OAuth2Constants.CONTENT_TYPE);

                JSONObject object = new JSONObject();
                JSONArray grantTypeArray = new JSONArray();
                JSONArray redirectUriArray = new JSONArray();

                String[] gtList = grantTypes.split(",");
                for (String str : gtList) {
                    grantTypeArray.add(str);
                }
                String[] rList = redirectUris.split(",");
                for (String str : rList) {
                    redirectUriArray.add(str);
                }

                object.put(OAuth2Constants.DCRMMetaData.CLIENT_NAME, clientName);
                object.put(OAuth2Constants.DCRMMetaData.GRANT_TYPES, grantTypeArray);
                object.put(OAuth2Constants.DCRMMetaData.REDIRECT_URIS, redirectUriArray);

                StringEntity entity = new StringEntity(object.toJSONString());
                httpPost.setEntity(entity);

                HttpResponse createResponse = client.execute(httpPost);
                reader = new BufferedReader(new InputStreamReader(createResponse.getEntity().getContent()));
                Object obj = JSONValue.parse(reader);
                reader.close();
                jsonObject = (JSONObject) obj;

            } else if (Objects.equals(method, OAuth2Constants.READ)) {
                HttpGet httpGet = new HttpGet(OAuth2Constants.CLIENT_CONFIGURATION_ENDPOINT + clientId);
                httpGet.addHeader(HttpHeaders.AUTHORIZATION, OAuth2Constants.AUTHORIZATION);
                HttpResponse readResponse = client.execute(httpGet);

                reader = new BufferedReader(new InputStreamReader(readResponse.getEntity().getContent()));
                Object obj = JSONValue.parse(reader);
                reader.close();
                jsonObject = (JSONObject) obj;

            } else if (Objects.equals(method, OAuth2Constants.UPDATE)) {
                HttpPut httpPut = new HttpPut(OAuth2Constants.CLIENT_CONFIGURATION_ENDPOINT + clientId);
                httpPut.addHeader(HttpHeaders.AUTHORIZATION, OAuth2Constants.AUTHORIZATION);
                httpPut.addHeader(HttpHeaders.CONTENT_TYPE, OAuth2Constants.CONTENT_TYPE);

                JSONObject object = new JSONObject();
                JSONArray grantTypeArray = new JSONArray();
                JSONArray rUriArray = new JSONArray();

                String[] gtList = grantTypes.split(",");
                for (String str : gtList) {
                    grantTypeArray.add(str);
                }
                String[] rList = redirectUris.split(",");
                for (String str : rList) {
                    rUriArray.add(str);
                }
                object.put(OAuth2Constants.DCRMMetaData.CLIENT_ID, clientId);
                object.put(OAuth2Constants.DCRMMetaData.CLIENT_SECRET, clientSecret);
                object.put(OAuth2Constants.DCRMMetaData.CLIENT_NAME, clientName);
                object.put(OAuth2Constants.DCRMMetaData.GRANT_TYPES, grantTypeArray);
                object.put(OAuth2Constants.DCRMMetaData.REDIRECT_URIS, rUriArray);

                StringEntity entity = new StringEntity(object.toJSONString());

                httpPut.setEntity(entity);

                HttpResponse updateResponse = client.execute(httpPut);

                reader = new BufferedReader(new InputStreamReader(updateResponse.getEntity().getContent()));
                Object obj = JSONValue.parse(reader);
                reader.close();
                jsonObject = (JSONObject) obj;
            }

            clientName = (String) jsonObject.get(OAuth2Constants.DCRMMetaData.CLIENT_NAME);
            clientId = (String) jsonObject.get(OAuth2Constants.DCRMMetaData.CLIENT_ID);
            clientSecret = (String) jsonObject.get(OAuth2Constants.DCRMMetaData.CLIENT_SECRET);

            JSONArray gtArray = (JSONArray) jsonObject.get(OAuth2Constants.DCRMMetaData.GRANT_TYPES);
            StringBuilder sb1 = new StringBuilder();
            for (Object gt : gtArray) {
                sb1.append(gt).append(",");
            }
            grantTypes = sb1.deleteCharAt(sb1.length() - 1).toString();

            JSONArray ruriArray = (JSONArray) jsonObject.get(OAuth2Constants.DCRMMetaData.REDIRECT_URIS);
            StringBuilder sb2 = new StringBuilder();
            for (Object ruri : ruriArray) {
                sb2.append(ruri).append(",");
            }
            redirectUris = sb2.deleteCharAt(sb2.length() - 1).toString();
        }
    } catch (Exception e) {
    %>
    <script type="text/javascript">
        window.location = "oauth2-dcrm.jsp?reset=true&error=<%=e.getMessage()%>";
    </script>
    <%
    }
%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <head>
        <title>WSO2 OAuth2 DCRM Response</title>
        <meta charset="UTF-8">
        <meta name="description" content=""/>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"></script>
        <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
        <script type="text/javascript" src="js/prettify.js"></script>
        <script type="text/javascript" src="js/kickstart.js"></script>
        <link rel="stylesheet" type="text/css" href="css/kickstart.css" media="all"/>
        <link rel="stylesheet" type="text/css" href="style.css" media="all"/>
    </head>
    <body>
        <a id="top-of-page"></a>
        <div id="wrap" class="clearfix"/>
        <ul class="menu">
            <li class="current"><a href="index.jsp">Home</a></li>
            <li class="current"><a href="oauth2-dcrm.jsp">Manage</a> </li>
        </ul>

        <div class="col_12"/>
        <div class="col_9"/>
        <h3>WSO2 OAuth2 DCRM Response</h3>

        <form>
            <table class="user_pass_table" width="100%">
                <%if (Objects.equals(method, OAuth2Constants.DELETE)) {%>
                    <tbody>
                        <tr>
                            <td><p><%=deleteConfirmation%></p></td>
                        </tr>
                    </tbody>
                <%} else {%>
                    <tbody>
                        <tr>
                            <td><label>Client Name </label></td>
                            <td><p><%=clientName%></p></td>
                        </tr>
                        <tr>
                            <td><label>Client ID </label></td>
                            <td><p><%=clientId%></p></td>
                        </tr>
                        <tr>
                            <td><label>Client Secret </label></td>
                            <td><p><%=clientSecret%></p></td>
                        </tr>
                        <tr>
                            <td><label>Grant Types </label></td>
                            <td><p><%=grantTypes%></p></td>
                        </tr>
                        <tr>
                            <td><label>Redirect URIs </label></td>
                            <td><p><%=redirectUris%></p></td>
                        </tr>
                    </tbody>
                <%}%>
            </table>
        </form>
    </body>
</html>
