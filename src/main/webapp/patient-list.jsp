<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.healthcare.model.*, com.healthcare.util.DateUtil" %>
<% String pageTitle = "Patients"; %>
<%@ include file="/WEB-INF/includes/header.jspf" %>

<h2>Patient Search</h2>

<form action="patient" method="get">
    <input type="hidden" name="action" value="search">
    <table class="form">
        <tr>
            <td class="label">Name:</td>
            <td><input type="text" name="name" value="<%= request.getAttribute("searchName") != null ? request.getAttribute("searchName") : "" %>"></td>
            <td class="label">Phone:</td>
            <td><input type="text" name="phone" value="<%= request.getAttribute("searchPhone") != null ? request.getAttribute("searchPhone") : "" %>"></td>
            <td class="label">MRN:</td>
            <td><input type="text" name="mrn" value="<%= request.getAttribute("searchMrn") != null ? request.getAttribute("searchMrn") : "" %>"></td>
            <td>
                <input type="submit" value="Search">
                <a href="patient?action=list">Show All</a>
            </td>
        </tr>
    </table>
</form>

<%
    List<Patient> patients = (List<Patient>) request.getAttribute("patients");
    if (request.getAttribute("searchName") != null && !"".equals(request.getAttribute("searchName"))) {
%>
    <p>Search results for: <b><%= request.getAttribute("searchName") %></b></p>
<%
    }
    if (patients == null || patients.size() == 0) {
%>
    <p>No patients found. <a href="patient?action=new">Register a new patient</a></p>
<% } else { %>
    <p><%= patients.size() %> patient(s) found</p>
    <table class="grid">
        <tr>
            <th>MRN</th>
            <th>Name</th>
            <th>Date of Birth</th>
            <th>Gender</th>
            <th>Phone</th>
            <th>SSN</th>
            <th>Insurance</th>
            <th>Actions</th>
        </tr>
        <% for (int i = 0; i < patients.size(); i++) {
               Patient p = patients.get(i); %>
        <tr class="<%= i % 2 == 0 ? "even" : "odd" %>">
            <td><%= p.getMrn() %></td>
            <td><a href="patient?action=view&patientId=<%= p.getId() %>"><%= p.getLastName() %>, <%= p.getFirstName() %></a></td>
            <td><%= DateUtil.displayDate(p.getDateOfBirth()) %></td>
            <td><%= p.getGender() %></td>
            <td><%= p.getPhone() %></td>
            <td><%= p.getSsn() != null ? p.getSsn() : "" %></td>
            <td><%= p.getInsuranceProvider() != null ? p.getInsuranceProvider() : "-" %></td>
            <td>
                <a href="patient?action=edit&patientId=<%= p.getId() %>">Edit</a> |
                <a href="appointment?action=new&patientId=<%= p.getId() %>">Book</a> |
                <a href="medicalRecord?patientId=<%= p.getId() %>">Records</a>
                <% if ("ADMIN".equals(_role)) { %>
                | <a href="patient?action=delete&patientId=<%= p.getId() %>" onclick="return confirmDelete()">Delete</a>
                <% } %>
            </td>
        </tr>
        <% } %>
    </table>
<% } %>

<%@ include file="/WEB-INF/includes/footer.jspf" %>
