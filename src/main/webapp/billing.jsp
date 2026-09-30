<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.healthcare.model.*" %>
<% String pageTitle = "Generate Bill"; %>
<%@ include file="/WEB-INF/includes/header.jspf" %>
<%
    List<Patient> patients = (List<Patient>) request.getAttribute("patients");
    Appointment appointment = (Appointment) request.getAttribute("appointment");
    Double consultationFee = (Double) request.getAttribute("consultationFee");
    String selectedPatientId = (String) request.getAttribute("selectedPatientId");
%>

<h2>Generate Bill</h2>

<% if (appointment != null) { %>
<p>
    For appointment <b>#<%= appointment.getId() %></b> -
    <%= appointment.getPatientName() %> with <%= appointment.getDoctorName() %>
    (<%= appointment.getAppointmentDate() %> <%= appointment.getAppointmentTime() %>)
</p>
<% } %>

<form action="billing" method="post" onsubmit="return confirm('Generate this bill?')">
    <input type="hidden" name="appointmentId" value="<%= appointment != null ? String.valueOf(appointment.getId()) : "" %>">
    <table class="form">
        <tr>
            <td class="label">Patient *:</td>
            <td>
                <select name="patientId">
                    <% for (Patient p : patients) { %>
                    <option value="<%= p.getId() %>" <%= String.valueOf(p.getId()).equals(selectedPatientId) ? "selected" : "" %>>
                        <%= p.getLastName() %>, <%= p.getFirstName() %> (<%= p.getMrn() %>)
                    </option>
                    <% } %>
                </select>
            </td>
        </tr>
        <tr>
            <td class="label">Consultation Fee:</td>
            <td><input type="text" name="consultationFee" size="10"
                       value="<%= consultationFee != null ? String.valueOf(consultationFee) : "0" %>"></td>
        </tr>
        <tr>
            <td class="label">Medicine Charges:</td>
            <td><input type="text" name="medicineCharges" size="10" value="0"></td>
        </tr>
        <tr>
            <td class="label">Lab Charges:</td>
            <td><input type="text" name="labCharges" size="10" value="0"></td>
        </tr>
        <tr>
            <td></td>
            <td style="font-size:10px;color:#666">
                Senior citizen (60+) and insurance discounts are applied automatically.
                Tax @ <%= application.getInitParameter("taxRate") %>% is added.
            </td>
        </tr>
        <tr>
            <td></td>
            <td><input type="submit" value="Generate Bill"></td>
        </tr>
    </table>
</form>

<%@ include file="/WEB-INF/includes/footer.jspf" %>
