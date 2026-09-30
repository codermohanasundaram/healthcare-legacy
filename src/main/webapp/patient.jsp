<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.healthcare.model.*, com.healthcare.util.DateUtil" %>
<% String pageTitle = "Patient Details"; %>
<%@ include file="/WEB-INF/includes/header.jspf" %>
<%
    Patient patient = (Patient) request.getAttribute("patient");
    Integer age = (Integer) request.getAttribute("age");
    List<Appointment> appointments = (List<Appointment>) request.getAttribute("appointments");
    List<MedicalRecord> records = (List<MedicalRecord>) request.getAttribute("records");
    List<Bill> bills = (List<Bill>) request.getAttribute("bills");
%>

<h2>Patient: <%= patient.getFullName() %> <small>(<%= patient.getMrn() %>)</small></h2>

<p class="noprint">
    <a href="patient?action=edit&patientId=<%= patient.getId() %>">Edit Details</a> |
    <a href="appointment?action=new&patientId=<%= patient.getId() %>">Book Appointment</a> |
    <a href="medicalRecord?patientId=<%= patient.getId() %>">Medical Records</a> |
    <a href="billing?action=new&patientId=<%= patient.getId() %>">New Bill</a> |
    <a href="javascript:window.print()">Print</a>
</p>

<table class="form" width="100%">
    <tr>
        <td class="label">MRN:</td><td><%= patient.getMrn() %></td>
        <td class="label">Registered:</td><td><%= DateUtil.displayDate(patient.getCreatedDate()) %> by <%= patient.getCreatedBy() %></td>
    </tr>
    <tr>
        <td class="label">Name:</td><td><%= patient.getFirstName() %> <%= patient.getLastName() %></td>
        <td class="label">Date of Birth:</td><td><%= DateUtil.displayDate(patient.getDateOfBirth()) %> (Age <%= age %>)</td>
    </tr>
    <tr>
        <td class="label">Gender:</td><td><%= patient.getGender() %></td>
        <td class="label">Blood Group:</td><td><%= patient.getBloodGroup() != null ? patient.getBloodGroup() : "" %></td>
    </tr>
    <tr>
        <td class="label">Phone:</td><td><%= patient.getPhone() %></td>
        <td class="label">Email:</td><td><%= patient.getEmail() != null ? patient.getEmail() : "" %></td>
    </tr>
    <tr>
        <td class="label">Address:</td>
        <td colspan="3"><%= patient.getAddress() %>, <%= patient.getCity() %>, <%= patient.getState() %> <%= patient.getZipCode() %></td>
    </tr>
    <tr>
        <td class="label">SSN:</td><td><%= patient.getSsn() %></td>
        <td class="label">Insurance:</td><td><%= patient.getInsuranceProvider() != null ? patient.getInsuranceProvider() : "None" %> <%= patient.getInsuranceNumber() != null ? patient.getInsuranceNumber() : "" %></td>
    </tr>
    <tr>
        <td class="label">Emergency Contact:</td>
        <td colspan="3"><%= patient.getEmergencyContactName() %> - <%= patient.getEmergencyContactPhone() %></td>
    </tr>
</table>

<h3>Appointments</h3>
<% if (appointments == null || appointments.isEmpty()) { %>
    <p>No appointments.</p>
<% } else { %>
<table class="grid">
    <tr><th>#</th><th>Date</th><th>Time</th><th>Doctor</th><th>Reason</th><th>Status</th></tr>
    <% for (Appointment a : appointments) { %>
    <tr>
        <td><%= a.getId() %></td>
        <td><%= DateUtil.displayDate(a.getAppointmentDate()) %></td>
        <td><%= a.getAppointmentTime() %></td>
        <td><%= a.getDoctorName() %></td>
        <td><%= a.getReason() %></td>
        <td class="status-<%= a.getStatus() %>"><%= a.getStatus() %></td>
    </tr>
    <% } %>
</table>
<% } %>

<h3>Recent Medical Records</h3>
<% if (records == null || records.isEmpty()) { %>
    <p>No medical records.</p>
<% } else { %>
<table class="grid">
    <tr><th>Visit Date</th><th>Doctor</th><th>Diagnosis</th><th>Prescription</th><th>BP</th></tr>
    <% for (int i = 0; i < records.size() && i < 5; i++) {
           MedicalRecord r = records.get(i); %>
    <tr>
        <td><%= DateUtil.displayDate(r.getVisitDate()) %></td>
        <td><%= r.getDoctorName() %></td>
        <td><%= r.getDiagnosis() %></td>
        <td><%= r.getPrescription() %></td>
        <td><%= r.getBloodPressure() %></td>
    </tr>
    <% } %>
</table>
<p><a href="medicalRecord?patientId=<%= patient.getId() %>">View all records</a></p>
<% } %>

<h3>Bills</h3>
<% if (bills == null || bills.isEmpty()) { %>
    <p>No bills.</p>
<% } else { %>
<table class="grid">
    <tr><th>Bill #</th><th>Date</th><th>Total</th><th>Status</th><th></th></tr>
    <% for (Bill b : bills) { %>
    <tr>
        <td><%= b.getId() %></td>
        <td><%= DateUtil.displayDate(b.getBillDate()) %></td>
        <td>$<%= String.format("%.2f", b.getTotalAmount()) %></td>
        <td class="status-<%= b.getStatus() %>"><%= b.getStatus() %></td>
        <td><a href="billing?action=view&billId=<%= b.getId() %>">View</a></td>
    </tr>
    <% } %>
</table>
<% } %>

<%@ include file="/WEB-INF/includes/footer.jspf" %>
