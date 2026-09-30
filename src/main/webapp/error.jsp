<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ page import="java.io.*" %>
<html>
<head>
<title>Error</title>
<link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div id="header">
    <span class="logo"><%= application.getInitParameter("hospitalName") %></span>
</div>
<div id="content">
    <h2>An error has occurred</h2>
    <p>Status: <%= request.getAttribute("javax.servlet.error.status_code") %></p>
    <p>URL: <%= request.getAttribute("javax.servlet.error.request_uri") %></p>
    <% if (request.getAttribute("error") != null) { %>
        <div class="error"><%= request.getAttribute("error") %></div>
    <% } %>
    <%
        if (exception != null) {
            // show details to help the helpdesk diagnose problems
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
    %>
        <h3>Details</h3>
        <pre style="font-size:10px;background:#eee;padding:10px"><%= sw.toString() %></pre>
    <% } %>
    <p><a href="<%= request.getContextPath() %>/home.jsp">Return to Home</a></p>
</div>
</body>
</html>
