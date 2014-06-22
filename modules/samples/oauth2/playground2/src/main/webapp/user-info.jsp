<%@page import="org.json.simple.parser.JSONParser"%>
<%@page import="org.json.simple.JSONObject"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>
<!DOCTYPE html>
<html><head>
<title>WSO2 OAuth2.0 Playground</title>
<meta charset="UTF-8">
<meta name="description" content="" />
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"></script>
<!--[if lt IE 9]><script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
<script type="text/javascript" src="js/prettify.js"></script>                                   <!-- PRETTIFY -->
<script type="text/javascript" src="js/kickstart.js"></script>                                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="css/kickstart.css" media="all" />                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="style.css" media="all" />                          <!-- CUSTOM STYLES -->



</head><body><a id="top-of-page"></a><div id="wrap" class="clearfix">
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
	<h3 align="center">WSO2 OAuth2 Playground ~ User Info</h3>
	

<table  style="width:800px;" class="striped">
     						
<%

String result = (String) session.getAttribute("result");

if(result != null) {
	String json = new String(result);
	JSONParser parser = new JSONParser();
	Object obj = parser.parse(json);
	JSONObject jsonObject = (JSONObject) obj;
	
	Iterator<?> ite = jsonObject.entrySet().iterator();
	
	while(ite.hasNext()) {
		Map.Entry entry = (Map.Entry)ite.next();
		%>
		<tr>
             <td style="width:50%"><%=entry.getKey()%> </td>
             <td><%=entry.getValue()%></td>
        </tr>
		<%
	}
	
} else {
	 out.print("No data received ");
}

%>

   
</table>
</div>

</body>
</html>
