<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // already logged in?
    if (session.getAttribute("user") != null) {
        response.sendRedirect("home.jsp");
        return;
    }
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title><%= application.getInitParameter("hospitalName") %> - Login</title>
<link rel="stylesheet" type="text/css" href="css/style.css">
</head>
<body>
<div id="header">
    <span class="logo"><%= application.getInitParameter("hospitalName") %></span>
    <span class="subtitle">Patient Management System</span>
</div>

<div class="login-box">
    <h2>Staff Login</h2>

    <% if (request.getParameter("logout") != null) { %>
        <div class="message">You have been logged out.</div>
    <% } %>
    <% if (request.getAttribute("error") != null) { %>
        <div class="error"><%= request.getAttribute("error") %></div>
    <% } %>

    <form action="login" method="post" name="loginForm">
        <table class="form">
            <tr>
                <td class="label">Username:</td>
                <td><input type="text" name="username" size="20"
                           value="<%= request.getParameter("username") != null ? request.getParameter("username") : "" %>"></td>
            </tr>
            <tr>
                <td class="label">Password:</td>
                <td><input type="password" name="password" size="20"></td>
            </tr>
            <tr>
                <td></td>
                <td><input type="submit" value="Login"></td>
            </tr>
        </table>
    </form>
    <p style="font-size:10px;color:#666">Authorised hospital staff only. All access is logged.</p>
</div>
</body>
</html>
