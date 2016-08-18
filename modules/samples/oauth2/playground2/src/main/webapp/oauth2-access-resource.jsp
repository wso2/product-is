<%@page import="org.wso2.sample.identity.oauth2.OAuth2ServiceClient"%>
<%@page import="org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO" %>
<%@page import="org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLConnection"%>
<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.io.DataOutputStream" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken" %>
<%@ page import="org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_TokenValidationContextParam" %>
<%@ page import="org.wso2.sample.identity.oauth2.OAuth2Constants" %>

<%
    OAuth2ServiceClient client = new OAuth2ServiceClient();
    String accessTokenIdentifier = request.getParameter("accessToken");
    String resource_url = request.getParameter("resource_url");

    if (accessTokenIdentifier ==null || accessTokenIdentifier.trim().length()==0) {
%>
<script type="text/javascript">
    window.location = "oauth2.jsp?reset=true";
</script>
<%
    }

    if(application.getInitParameter("setup").equals("IS")){
        OAuth2TokenValidationRequestDTO  oauthReq = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessToken =  new OAuth2TokenValidationRequestDTO_OAuth2AccessToken();

        accessToken.setTokenType("bearer");
        accessToken.setIdentifier(accessTokenIdentifier);
        oauthReq.setAccessToken(accessToken);

        try {
            // Validate the OAuth access token.
            OAuth2TokenValidationResponseDTO oauthResponse = client.validateAuthenticationRequest(oauthReq);
            session.setAttribute(OAuth2Constants.TOKEN_VALIDATION, Boolean.toString(oauthResponse.getValid()));

            if (!oauthResponse.getValid()) {
                String errorMsg = oauthResponse.getErrorMsg();
                if(errorMsg == null){
                    errorMsg = "";
                }
                errorMsg = "Invalid Access Attempt : " + errorMsg;
%>
<script type="text/javascript">
    window.location = "oauth2.jsp?reset=true&error=<%=errorMsg%>";
</script>
<%
            }
            else if(resource_url != null && resource_url.contains("userinfo")) {
            	String result = executeGet(resource_url, "", accessTokenIdentifier);
            	session.setAttribute("result", result);
                response.sendRedirect("user-info.jsp");
            } else if (resource_url != null && resource_url.contains("introspect")) {
                String result = executePost(resource_url, "token=" + accessTokenIdentifier, accessTokenIdentifier);
                session.setAttribute("result", result);
                response.sendRedirect("token-info.jsp");
            } else {
                RequestDispatcher view = request.getRequestDispatcher("my-photos.jsp");
                view.forward(request, response);
            }
        } catch(Exception e) {
%>
<script type="text/javascript">
    window.location = "oauth2.jsp?reset=true&error=<%=e.getMessage()%>";
</script>
<%
        }
    }else if(getServletConfig().getServletContext().getInitParameter("setup").equals("AM")){
        String result = executePost(resource_url,"", accessTokenIdentifier);
        out.print(result);
    }else{
%>
<script type="text/javascript">
    window.location = "oauth2.jsp?reset=true&error='Invalid Setup value'";
</script>
<%
    }
%>

<%!
    public static String executePost(String targetURL, String urlParameters,String accessTokenIdentifier)
    {
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.setRequestProperty("Authorization","Bearer " + accessTokenIdentifier);

            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches (false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream ());
            wr.writeBytes (urlParameters);
            wr.flush ();
            wr.close ();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
    }

public static String executeGet(String targetURL, String urlParameters,String accessTokenIdentifier){
	try {
	    URL myURL = new URL(targetURL);
	    URLConnection myURLConnection = myURL.openConnection();
	    myURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        myURLConnection.setRequestProperty("Authorization","Bearer " + accessTokenIdentifier);
        myURLConnection.setRequestProperty("Content-Language", "en-US");
        myURLConnection.setUseCaches (false);
        myURLConnection.setDoInput(true);
        myURLConnection.setDoOutput(true);
        
	    BufferedReader br = new BufferedReader(
	                                           new InputStreamReader(myURLConnection.getInputStream()));
	    String line;
        StringBuffer response = new StringBuffer();
        while((line = br.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        br.close();
        return response.toString();
	} 
	catch (Exception e) { 
	    // new URL() failed
	    // ...
	} 
	return "";
}
%>