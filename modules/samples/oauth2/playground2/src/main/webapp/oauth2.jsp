<%@page import="org.wso2.sample.identity.oauth2.OAuth2Constants"%>
<%@page import="org.apache.amber.oauth2.client.response.OAuthAuthzResponse"%>
<%@page import="org.json.simple.parser.JSONParser"%>
<%@page import="org.json.simple.JSONObject"%>
<%@page import="org.apache.commons.codec.binary.Base64"%>
<%@page import="com.nimbusds.jwt.SignedJWT"%>
<%
String error = request.getParameter("error");    
String grantType = (String) session.getAttribute(OAuth2Constants.OAUTH2_GRANT_TYPE);

OAuthAuthzResponse authzResponse = null;
String code = null;
String accessToken = null;
String idToken = null;
String name = null;

try {
    
    String reset = request.getParameter("reset");

    if (reset!=null && "true".equals(reset)){
    	session.removeAttribute(OAuth2Constants.OAUTH2_GRANT_TYPE);
    	session.removeAttribute(OAuth2Constants.ACCESS_TOKEN);
    	session.removeAttribute(OAuth2Constants.CODE);
    	session.removeAttribute("id_token");
    	session.removeAttribute("result");
    }    
    
    if (grantType != null && OAuth2Constants.OAUTH2_GRANT_TYPE_CODE.equals(grantType)) {
    	code = (String) session.getAttribute(OAuth2Constants.CODE);
    	if (code==null) {
        	authzResponse = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
        	code = authzResponse.getCode();
        	session.setAttribute(OAuth2Constants.CODE,code);
    	} else {
        	accessToken = (String) session.getAttribute(OAuth2Constants.ACCESS_TOKEN);
        	idToken = (String) session.getAttribute("id_token");
    	}   
    } else if (grantType != null && OAuth2Constants.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS.equals(grantType)) {
    	accessToken = (String) session.getAttribute(OAuth2Constants.ACCESS_TOKEN);
    } else if (grantType != null && OAuth2Constants.OAUTH2_GRANT_TYPE_RESOURCE_OWNER.equals(grantType)) {
    	accessToken = (String) session.getAttribute(OAuth2Constants.ACCESS_TOKEN);
    }

  } catch (Exception e) {
%>
      <script type="text/javascript">
    	   window.location = "oauth2.jsp?reset=true&error=<%=e.getMessage()%>";
      </script>
<% 
  }        
%>

<!DOCTYPE html>
<html><head>
<title>WSO2 OAuth2 Playground</title>
<meta charset="UTF-8">
<meta name="description" content="" />
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"></script>
<!--[if lt IE 9]><script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
<script type="text/javascript" src="js/prettify.js"></script>                                   <!-- PRETTIFY -->
<script type="text/javascript" src="js/kickstart.js"></script>                                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="css/kickstart.css" media="all" />                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="style.css" media="all" />                          <!-- CUSTOM STYLES -->

<script type="text/javascript">
	     function setVisibility() {
 
    		var grantType = document.getElementById("grantType").value;
   
        	if ('code' == grantType) {
        		document.getElementById("clientsecret").style.display = "none";
                document.getElementById("callbackurltr").style.display = "";
        		document.getElementById("authzep").style.display = "";  
        		document.getElementById("accessep").style.display = "none";  
        		document.getElementById("recownertr").style.display = "none"; 
        		document.getElementById("recpasswordtr").style.display = "none"; 
        	} else if ("token" == grantType) {
        		document.getElementById("clientsecret").style.display = "none";
                document.getElementById("callbackurltr").style.display = "";
        		document.getElementById("authzep").style.display = "";  
        		document.getElementById("accessep").style.display = "none"; 
        		document.getElementById("recownertr").style.display = "none"; 
        		document.getElementById("recpasswordtr").style.display = "none"; 
            } else if ("password" == grantType) {
            	document.getElementById("clientsecret").style.display = "";
                document.getElementById("callbackurltr").style.display = "none";
        		document.getElementById("authzep").style.display = "none";  
        		document.getElementById("accessep").style.display = "";   
        		document.getElementById("recownertr").style.display = ""; 
        		document.getElementById("recpasswordtr").style.display = ""; 
            } else if ("client_credentials" == grantType) {
            	document.getElementById("clientsecret").style.display = "";
                document.getElementById("callbackurltr").style.display = "none";
        		document.getElementById("authzep").style.display = "none";  
        		document.getElementById("accessep").style.display = ""; 
        		document.getElementById("recownertr").style.display = "none"; 
        		document.getElementById("recpasswordtr").style.display = "none"; 
            }                    
        		
            return true;
   		 }
	     
	     function getAcceesToken() 
	     {
	        var fragment = window.location.hash.substring(1);  
	         if (fragment.indexOf("&") > 0)
	         {
	            var arrParams = fragment.split("&");         
    
	            var i = 0;
	            for (i=0;i<arrParams.length;i++)
	            {
	             	var sParam =  arrParams[i].split("=");

	             	if (sParam[0] == "access_token"){
	            	 return sParam[1];
	             	}
	          	}
	         }
	         return "";
	     }
</script>

</head><body><a id="top-of-page"></a><div id="wrap" class="clearfix"/>
<!-- ===================================== END HEADER ===================================== -->

<!-- 
	
		ADD YOU HTML ELEMENTS HERE
		
		Example: 2 Columns
	 -->
	 <!-- Menu Horizontal -->
	<ul class="menu">
	<li class="current"><a href="index.jsp">Home</a></li>
	
	</ul>
	 
<div class="col_12"/>
	<div class="col_9"/>
	<h3>WSO2 OAuth2 Playground</h3>
	
<table>
<tr>
<td>
      <% if (accessToken==null && code==null && grantType == null) { %>
              <div id="loginDiv" class="sign-in-box" width="100%">
                  <% if (error!=null && error.trim().length()>0) {%>
                    <table class="user_pass_table" width="100%">
                      <tr>
                          <td><font color="#CC0000"><%=error%></font></td>
                      </tr>
                    </table>
                   <%} %>
              
                    <form action="oauth2-authorize-user.jsp" id="loginForm" method="post">
                        <table class="user_pass_table" width="100%">
                            <tbody>
                          
                            <tr>
                                <td>Authorization Grant Type : </td>
                                <td>
                                   <select id="grantType" name="grantType"  onchange ="setVisibility();">
            			              <option value="<%=OAuth2Constants.OAUTH2_GRANT_TYPE_CODE%>" selected="selected">Authorization Code</option>
            			              <option value="<%=OAuth2Constants.OAUTH2_GRANT_TYPE_IMPLICIT%>">Implicit</option>
            		                  <option value="<%=OAuth2Constants.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS%>">Client Credentials</option>
                                      <option value="<%=OAuth2Constants.OAUTH2_GRANT_TYPE_RESOURCE_OWNER%>">Resource Owner</option>           			              
        			               </select>                                
                                </td>
                            </tr>
                          
                            <tr>
                                <td><label>Client Id : </label></td>
                                <td><input type="text" id="consumerKey" name="consumerKey" style= "width:350px"></td>
                            </tr>
                            
                            <tr id="clientsecret" style="display:none">
                                <td><label>Client Secret : </label></td>
                                <td><input type="password" id="consumerSecret" name="consumerSecret" style= "width:350px">
                                </td>
                            </tr>
                            
                            <tr id="recownertr" style="display:none">
                                <td><label>Resource Owner User Name: </label></td>
                                <td><input type="text" id="recowner" name="recowner" style= "width:350px"></td>
                            </tr>
                            
                            <tr id="recpasswordtr" style="display:none">
                                <td><label>Resource Owner Password : </label></td>
                                <td><input type="password" id="recpassword" name="recpassword" style= "width:350px">
                                </td>
                            </tr>
                           
                            <tr>
                                <td><label>Scope : </label></td>
                                <td><input type="text" id="scope" name="scope">
                                </td>
                            </tr>

                            <tr id="callbackurltr">
                                <td><label>Callback URL : </label></td>
                                <td><input type="text" id="callbackurl" name="callbackurl" style= "width:350px">
                                </td>
                            </tr>
                           
                            <tr id="authzep">
                                <td>Authorize Endpoint : </td>
                                <td><input type="text" id="authorizeEndpoint" name="authorizeEndpoint" style= "width:350px"></td>
                            </tr>
                            
                            <tr id="accessep" style="display:none">
                                <td>Access Token Endpoint : </td>
                                <td><input type="text" id="accessEndpoint" name="accessEndpoint" style= "width:350px"></td>
                            </tr>
                                     
                            <tr>
                                 <td colspan="2"><input type="submit" name="authorize" value="Authorize"></td>
                           </tr>                         
                           </tbody>
                        </table>

                    </form>
                </div>
                
               <%} else if (code!=null && accessToken==null) { %>
               <div>
                <form action="oauth2-get-access-token.jsp" id="loginForm" method="post">
              
                   <table class="user_pass_table">
                         <tbody>
                           <tr>
                              <td>Authorization Code :</td>
                              <td><%=code%></td>
                           </tr>
                           <tr>
                               <td>Callback URL :</td>
                               <td><input type="text" id="callbackurl" name="callbackurl" style= "width:350px">
                           </tr>
                           <tr>
                                <td>Access Token Endpoint : </td>
                                <td><input type="text" id="accessEndpoint" name="accessEndpoint" style= "width:350px"></td>
                           </tr>
                           <tr >
                                <td><label>Client Secret : </label></td>
                                <td><input type="password" id="consumerSecret" name="consumerSecret" style= "width:350px">
                                </td>
                           </tr>
                           <tr>
                                 <td colspan="2"><input type="submit" name="authorize" value="Get Access Token"></td>
                           </tr>
                         </tbody>
                   </table>
               </form>
                                      
              </div>
              <%
              	} else if (accessToken!=null) {
              		
                    if (idToken != null) { 
                    	try {
            				name = SignedJWT.parse(idToken).getJWTClaimsSet().getSubject();
            			} catch (Exception e) {
            				//ignore
            			}
                    %>
                    
                <div>
                <form action="oauth2-access-resource.jsp" id="loginForm" method="post">
 
                   <table class="user_pass_table">
                        <tbody>  
                          <tr>
                              <td><label>Logged In User :</label></td>
                              <td><label id="loggedUser"><%=name%></label></td>
                          </tr> 
                          <tr>
                              <td><label>Access Token :</label></td>
                              <td><input  id="accessToken" name="accessToken" style= "width:350px" value="<%=accessToken%>" />
                          </tr>
                          <tr>
                              <td><label>UserInfo Endpoint :</label></td>
                              <td><input  id="resource_url" name="resource_url" type="text" style= "width:350px"/>
                          </tr>
                 
                          <tr>
                              <td>
                                  <input type="submit" class="button" value="Get UserInfo">
                               </td>
                          </tr>     
                        </tbody>
                    </table>

               </form>                                    
              </div>
              			
              		<%} else { %>
              	<div>
                <form action="oauth2-access-resource.jsp" id="loginForm" method="post">
 
                   <table class="user_pass_table">
                        <tbody>                        
                          <tr>
                              <td><label>Access Token :</label></td>
                              <td><input  id="accessToken" name="accessToken" style= "width:350px" value="<%=accessToken%>" />
                          </tr>
                          <% if(application.getInitParameter("setup").equals("AM")){ %>
                              <tr>
                                  <td><label>Resource URL :</label></td>
                                  <td><input  id="resource_url" name="resource_url" type="text" style= "width:350px"/>
                              </tr>
                          <% } %>
                          <tr>
                              <td>
                                  <input type="submit" class="button" value="Get Photos">
                               </td>
                          </tr>     
                        </tbody>
                    </table>

               </form>                                    
              </div>
              		<%} %>
             
              <% } else if (grantType != null && OAuth2Constants.OAUTH2_GRANT_TYPE_IMPLICIT.equals(grantType)) {%>
              <div>
              <form action="oauth2-access-resource.jsp" id="loginForm" method="post">
            
                 <table class="user_pass_table">
                      <tbody>
                        <tr>
                            <td><label>Access Token :</label></td>
                            <td><input  id="accessToken" name="accessToken" style= "width:350px" />
                            <script type="text/javascript">
                                document.getElementById("accessToken").value = getAcceesToken();                        
                            </script>
                        </tr>
                        <% if(application.getInitParameter("setup").equals("AM")){ %>
                            <tr>
                                <td><label>Resource URL :</label></td>
                                <td><input  id="resource_url" name="resource_url" type="text" style= "width:350px"/>
                            </tr>
                        <% } %>
                        <tr>
                              <td>
                                  <input type="submit" class="button" value="Get Photos">  
                              </td>
                        </tr>                          
                     </tbody>
                 </table>
                 
             </form>
                                
            </div>
            <% } %>
</td>
</tr>
</table>

</body>
</html>
