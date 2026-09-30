<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.healthcare.util.DateUtil" %>
<% String pageTitle = "Reports"; %>
<%@ include file="/WEB-INF/includes/header.jspf" %>
<%
    String reportType = (String) request.getAttribute("reportType");
    String reportTitle = (String) request.getAttribute("reportTitle");
    List<String[]> rows = (List<String[]>) request.getAttribute("reportRows");
%>

<h2>Reports</h2>

<table class="form">
    <tr>
        <td><a href="report?type=summary">Hospital Summary</a></td>
        <td><a href="report?type=doctor">Appointments by Doctor</a></td>
        <td><a href="report?type=demographics">Patient Demographics</a></td>
        <td><a href="report?type=export">Export Patients (CSV)</a></td>
    </tr>
</table>

<form action="report" method="get" style="display:inline">
    <input type="hidden" name="type" value="revenue">
    Monthly revenue (yyyy-MM): <input type="text" name="month" size="8" value="<%= DateUtil.currentMonth() %>">
    <input type="submit" value="Run">
</form>
&nbsp;&nbsp;
<form action="report" method="get" style="display:inline">
    <input type="hidden" name="type" value="visits">
    Visit summary from <input type="text" name="from" size="10">
    to <input type="text" name="to" size="10">
    <input type="submit" value="Run">
</form>

<% if (rows != null) { %>
<h3><%= reportTitle %></h3>
<table class="grid">
    <tr>
        <% String[] header = rows.get(0);
           for (int i = 0; i < header.length; i++) { %>
        <th><%= header[i].replace('_', ' ').toUpperCase() %></th>
        <% } %>
    </tr>
    <% for (int r = 1; r < rows.size(); r++) {
           String[] row = rows.get(r); %>
    <tr class="<%= r % 2 == 0 ? "even" : "odd" %>">
        <% for (int c = 0; c < row.length; c++) { %>
        <td><%= row[c] != null ? row[c] : "" %></td>
        <% } %>
    </tr>
    <% } %>
</table>
<p><%= rows.size() - 1 %> row(s)</p>
<% } %>

<%@ include file="/WEB-INF/includes/footer.jspf" %>
