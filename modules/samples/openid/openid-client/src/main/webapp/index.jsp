<html>
<head>
<title>WSO2 OAuth2 Playground</title>
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

<div id="colOne">
	
	<form action="OpenIDRelyingPartyServlet" method="post">
		<div class="col_2 column"></div>
		<div class="col_8 column">
			<h3>WSO2 Identity Server OpenID SSO</h3>
			<div class="login-box">
				<label>Please login:</label>
				<input type="text" name="claimed_id"
					class="openid_identifier" size="60" /> <input class="small green" type="submit"
					name="login" value="Login" />
			</div>
		</div>
		<div class="col_2 column"></div>
	</form>
</div>
</body>
</html>