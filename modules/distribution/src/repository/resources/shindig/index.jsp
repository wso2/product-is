<%@ page import="java.io.*,java.util.*" %>
<%
    // New location to be redirected
    response.setStatus(response.SC_MOVED_TEMPORARILY);
    response.setHeader("Location", "/portal");
%>