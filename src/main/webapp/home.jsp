<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.healthcare.dao.*, com.healthcare.model.*, com.healthcare.util.DateUtil" %>
<% String pageTitle = "Home"; %>
<%@ include file="/WEB-INF/includes/header.jspf" %>
<%
    // dashboard figures - loaded directly here for speed
    PatientDAO patientDAO = new PatientDAO();
    DoctorDAO doctorDAO = new DoctorDAO();
    AppointmentDAO appointmentDAO = new AppointmentDAO();

    int patientCount = patientDAO.getPatientCount();
    int doctorCount = doctorDAO.getDoctorCount();
    int todayCount = appointmentDAO.getTodayAppointmentCount();
    List<Appointment> todays = appointmentDAO.getAppointmentsByDate(new Date());
%>

<h2>Dashboard</h2>
<p>Welcome, <b><%= session.getAttribute("fullName") %></b>. Today is <%= DateUtil.displayDate(new Date()) %>.</p>

<div class="dashboard-box">
    <div class="number"><%= patientCount %></div>
    Registered Patients
</div>
<div class="dashboard-box">
    <div class="number"><%= doctorCount %></div>
    Active Doctors
</div>
<div class="dashboard-box">
    <div class="number"><%= todayCount %></div>
    Appointments Today
</div>
<div class="clear"></div>

<h3>Today's Appointments</h3>
<% if (todays.size() == 0) { %>
    <p>No appointments scheduled for today.</p>
<% } else { %>
<table class="grid">
    <tr><th>Time</th><th>Patient</th><th>MRN</th><th>Doctor</th><th>Reason</th><th>Status</th><th></th></tr>
    <% for (int i = 0; i < todays.size(); i++) {
           Appointment a = todays.get(i); %>
    <tr class="<%= i % 2 == 0 ? "even" : "odd" %>">
        <td><%= a.getAppointmentTime() %></td>
        <td><a href="patient?action=view&patientId=<%= a.getPatientId() %>"><%= a.getPatientName() %></a></td>
        <td><%= a.getPatientMrn() %></td>
        <td><%= a.getDoctorName() %></td>
        <td><%= a.getReason() %></td>
        <td class="status-<%= a.getStatus() %>"><%= a.getStatus() %></td>
        <td>
            <% if ("SCHEDULED".equals(a.getStatus())) { %>
                <a href="appointment?action=complete&id=<%= a.getId() %>">Complete</a> |
                <a href="appointment?action=cancel&id=<%= a.getId() %>">Cancel</a>
            <% } %>
        </td>
    </tr>
    <% } %>
</table>
<% } %>

<h3>Quick Links</h3>
<ul>
    <li><a href="patient?action=new">Register a new patient</a></li>
    <li><a href="patient?action=list">Search patients</a></li>
    <li><a href="appointment?action=new">Book an appointment</a></li>
</ul>

<%@ include file="/WEB-INF/includes/footer.jspf" %>
