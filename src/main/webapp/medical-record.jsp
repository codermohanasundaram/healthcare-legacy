<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.healthcare.model.*, com.healthcare.util.DateUtil" %>
<% String pageTitle = "Medical Records"; %>
<%@ include file="/WEB-INF/includes/header.jspf" %>
<%
    Patient patient = (Patient) request.getAttribute("patient");
    List<MedicalRecord> records = (List<MedicalRecord>) request.getAttribute("records");
    List<Doctor> doctors = (List<Doctor>) request.getAttribute("doctors");
%>

<h2>Medical Records - <%= patient.getFullName() %> (<%= patient.getMrn() %>)</h2>
<p>
    DOB: <%= DateUtil.displayDate(patient.getDateOfBirth()) %> |
    Blood Group: <%= patient.getBloodGroup() %> |
    <a href="patient?action=view&patientId=<%= patient.getId() %>">Back to patient</a>
</p>

<% if (records == null || records.size() == 0) { %>
    <p>No medical records found for this patient.</p>
<% } else { %>
<table class="grid">
    <tr>
        <th>Visit Date</th>
        <th>Doctor</th>
        <th>Diagnosis</th>
        <th>Prescription</th>
        <th>BP</th>
        <th>Temp (F)</th>
        <th>Weight (kg)</th>
        <th>Notes</th>
    </tr>
    <% for (int i = 0; i < records.size(); i++) {
           MedicalRecord r = records.get(i); %>
    <tr class="<%= i % 2 == 0 ? "even" : "odd" %>">
        <td><%= DateUtil.displayDate(r.getVisitDate()) %></td>
        <td><%= r.getDoctorName() %></td>
        <td><%= r.getDiagnosis() %></td>
        <td><%= r.getPrescription() %></td>
        <td><%= r.getBloodPressure() %></td>
        <td><%= r.getTemperature() > 0 ? String.valueOf(r.getTemperature()) : "" %></td>
        <td><%= r.getWeight() > 0 ? String.valueOf(r.getWeight()) : "" %></td>
        <td><%= r.getNotes() != null ? r.getNotes() : "" %></td>
    </tr>
    <% } %>
</table>
<% } %>

<% if ("DOCTOR".equals(_role) || "ADMIN".equals(_role)) { %>
<h3>Add Visit Record</h3>
<form action="medicalRecord" method="post">
    <input type="hidden" name="patientId" value="<%= patient.getId() %>">
    <table class="form">
        <% if ("ADMIN".equals(_role)) { %>
        <tr>
            <td class="label">Doctor:</td>
            <td>
                <select name="doctorId">
                    <% for (Doctor d : doctors) { %>
                    <option value="<%= d.getId() %>"><%= d.getFullName() %></option>
                    <% } %>
                </select>
            </td>
        </tr>
        <% } %>
        <tr>
            <td class="label">Visit Date:</td>
            <td><input type="text" name="visitDate" size="12" value="<%= DateUtil.formatDate(new Date()) %>"></td>
        </tr>
        <tr>
            <td class="label">Appointment #:</td>
            <td><input type="text" name="appointmentId" size="6"> (optional)</td>
        </tr>
        <tr>
            <td class="label">BP / Temp / Weight:</td>
            <td>
                <input type="text" name="bloodPressure" size="8" placeholder="120/80">
                <input type="text" name="temperature" size="6" placeholder="98.6">
                <input type="text" name="weight" size="6" placeholder="70">
            </td>
        </tr>
        <tr>
            <td class="label">Diagnosis *:</td>
            <td><textarea name="diagnosis" rows="2" cols="60"></textarea></td>
        </tr>
        <tr>
            <td class="label">Prescription:</td>
            <td><textarea name="prescription" rows="3" cols="60"></textarea></td>
        </tr>
        <tr>
            <td class="label">Notes:</td>
            <td><textarea name="notes" rows="3" cols="60"></textarea></td>
        </tr>
        <tr>
            <td></td>
            <td><input type="submit" value="Save Record"></td>
        </tr>
    </table>
</form>
<% } %>

<%@ include file="/WEB-INF/includes/footer.jspf" %>
