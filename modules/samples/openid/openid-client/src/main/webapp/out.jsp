<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
Design by Free CSS Templates
http://www.freecsstemplates.org
Released for free under a Creative Commons Attribution 2.5 License
-->
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>OpenID SSO</title>
<meta charset="UTF-8">
<meta name="description" content="" />
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"></script>
<!--[if lt IE 9]><script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
<script type="text/javascript" src="js/prettify.js"></script>                                   <!-- PRETTIFY -->
<script type="text/javascript" src="js/kickstart.js"></script>                                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="css/kickstart.css" media="all" />                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="style.css" media="all" />                          <!-- CUSTOM STYLES -->
</head>
<body>
<a id="top-of-page"></a><div id="wrap" class="clearfix">
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
<div id="content">
		<div id="colOne">
		<div class="col_2 column"></div>
		<div class="col_8 column">
			<h3>WSO2 Identity Server OpenID SSO</h3>
			
			<div class="login-box">
				<h4>Welcome</h4>
		
					
					<table class="striped" style="border:solid 1px #ccc">
						<tr>
							<td><%=request.getParameter("openid")%></td>
							<td></td>
						</tr>
						<tr>
							<td>Email</td>
							<td><%=request.getParameter("email")%></td>
						</tr>
						<tr>
							<td>First Name</td>
							<td><%=request.getParameter("firstname")%></td>
						</tr>
						<tr>
							<td>Last Name</td>
							<td><%=request.getParameter("lastname")%></td>
						</tr>
						<tr>
							<td>Country</td>
							<td><%=request.getParameter("country")%></td>
						</tr>
					</table>
				
			</div>
		</div>
		<div class="col_2 column"></div>
	</div>
</div>
		

</body>
</html>
