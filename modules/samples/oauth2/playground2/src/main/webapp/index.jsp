<%@page import="org.wso2.sample.identity.oauth2.OAuth2Constants"%>
<%
    String clientId=null;
    String endpoint=OAuth2Constants.OP_ENDPOINT_URL;
    String targetOrigin=OAuth2Constants.TARGET_URL;
    String opStatus=OAuth2Constants.OP_STATUS_LOGGED;
    session.removeAttribute(OAuth2Constants.OAUTH2_GRANT_TYPE);
    session.removeAttribute(OAuth2Constants.ACCESS_TOKEN);
    session.removeAttribute(OAuth2Constants.CODE);

 %>

<!DOCTYPE html>
<html><head>
<title>WSO2 OAuth2 Playground</title>
<meta charset="UTF-8">
<meta name="description" content="" />
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"></script>
<!--[if lt IE 9]><script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
<script type="text/javascript" src="js/prettify.js"></script>                                   <!-- PRETTIFY -->
<script type="text/javascript" src="js/kickstart.js"></script> 
<script type="text/javascript">
        var opStatus='<%=opStatus%>';
        var endPoint='<%=endpoint%>';
        var targetOrigin='<%=targetOrigin%>';
        var clientId ='<%=clientId%>';
<iframe id="opIFrame" style='visibility: hidden;' src=endpoint >
</iframe>
</script>
 <script type="text/javascript" src="js/polling.js"> </script>                                 <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="css/kickstart.css" media="all" />                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="style.css" media="all" />                          <!-- CUSTOM STYLES -->


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
	 
<br/>
	<h3 align="center">WSO2 OAuth2 Playground</h3>
	
<table style="width:100%;text-align:center;'">
<tr>
<td style="text-align:center;width:100%">        						
<a href="oauth2.jsp?reset=true"><img src="images/import.png" /></a>
</td>
</tr>

</table>


</body>
</html>
