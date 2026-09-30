<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.healthcare.model.*, com.healthcare.util.DateUtil" %>
<% String pageTitle = "Book Appointment"; %>
<%@ include file="/WEB-INF/includes/header.jspf" %>
<%
    List<Patient> patients = (List<Patient>) request.getAttribute("patients");
    List<Doctor> doctors = (List<Doctor>) request.getAttribute("doctors");
    String selectedPatientId = (String) request.getAttribute("selectedPatientId");
    String selectedDoctorId = (String) request.getAttribute("selectedDoctorId");
    String apptDate = (String) request.getAttribute("appointmentDate");
    String apptTime = (String) request.getAttribute("appointmentTime");
    String reason = (String) request.getAttribute("reason");
    if (apptDate == null) apptDate = DateUtil.formatDate(new Date());
%>

<h2>Book Appointment</h2>

<form action="appointment" method="post">
    <table class="form">
        <tr>
            <td class="label">Patient *:</td>
            <td>
                <select name="patientId">
                    <option value="">-- Select Patient --</option>
                    <% if (patients != null) {
                           for (Patient p : patients) { %>
                    <option value="<%= p.getId() %>" <%= String.valueOf(p.getId()).equals(selectedPatientId) ? "selected" : "" %>>
                        <%= p.getLastName() %>, <%= p.getFirstName() %> (<%= p.getMrn() %>)
                    </option>
                    <%     }
                       } %>
                </select>
            </td>
        </tr>
        <tr>
            <td class="label">Doctor *:</td>
            <td>
                <select name="doctorId">
                    <option value="">-- Select Doctor --</option>
                    <% if (doctors != null) {
                           for (Doctor d : doctors) { %>
                    <option value="<%= d.getId() %>" <%= String.valueOf(d.getId()).equals(selectedDoctorId) ? "selected" : "" %>>
                        <%= d.getFullName() %> - <%= d.getSpecialization() %>
                    </option>
                    <%     }
                       } %>
                </select>
            </td>
        </tr>
        <tr>
            <td class="label">Date * (yyyy-MM-dd):</td>
            <td><input type="text" name="appointmentDate" size="12" value="<%= apptDate %>"></td>
        </tr>
        <tr>
            <td class="label">Time *:</td>
            <td>
                <select name="appointmentTime">
                    <%
                        // 30 minute slots 09:00 - 16:30
                        for (int h = 9; h <= 16; h++) {
                            for (int m = 0; m < 60; m += 30) {
                                String slot = (h < 10 ? "0" + h : "" + h) + ":" + (m == 0 ? "00" : "30");
                    %>
                    <option value="<%= slot %>" <%= slot.equals(apptTime) ? "selected" : "" %>><%= slot %></option>
                    <%
                            }
                        }
                    %>
                </select>
            </td>
        </tr>
        <tr>
            <td class="label">Reason:</td>
            <td><input type="text" name="reason" size="50" value="<%= reason != null ? reason : "" %>"></td>
        </tr>
        <tr>
            <td class="label">Notes:</td>
            <td><textarea name="notes" rows="3" cols="50"></textarea></td>
        </tr>
        <tr>
            <td></td>
            <td><input type="submit" value="Book Appointment"></td>
        </tr>
    </table>
</form>

<%@ include file="/WEB-INF/includes/footer.jspf" %>
