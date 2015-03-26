
<%@ page import="org.wso2.sample.identity.oauth2.PollingClient" %>
<%@ page import="java.security.cert.X509Certificate" %>
<%@ page import="javax.net.ssl.SSLContext" %>
<%@ page import="javax.net.ssl.TrustManager" %>
<%@ page import="javax.net.ssl.X509TrustManager" %>
<%@ page import="java.security.SecureRandom" %>
<%@ page import="javax.net.ssl.HttpsURLConnection" %>

<%--
  Created by IntelliJ IDEA.
  User: hasanthi
  Date: 3/19/15
  Time: 9:38 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>

    <script>
        <%
         PollingClient pollingClient=new PollingClient();
         TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
         public X509Certificate[] getAcceptedIssuers(){return null;}
         public void checkClientTrusted(X509Certificate[] certs, String authType){}
         public void checkServerTrusted(X509Certificate[] certs, String authType){}
     }};

 // Install the all-trusting trust manager
     try {
         SSLContext sc = SSLContext.getInstance("TLS");
         sc.init(null, trustAllCerts, new SecureRandom());
         HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
     } catch (Exception e) {
        e.printStackTrace();
     }

 %>
        var set_delay = 5000,
                callout = function () {
                    $.ajax({

                    })
                            .done(function (response) {

                                <%
                                 String opResponse=
                                 pollingClient.getOPResponse();

          %>
      
                            })
                            .always(function () {
                                setTimeout(callout, set_delay);
                            });
                };
        callout();
    </script>

</head>
<body>

<iframe id="my_iframe" src="https://localhost:9443/oauth2/authorize" onload="callout();">
</iframe>
</body>
</html>

