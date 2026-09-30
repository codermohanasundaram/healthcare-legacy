<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.healthcare.model.*, com.healthcare.util.DateUtil" %>
<%!
    // null-safe output helper
    private String v(Object o) {
        return o == null ? "" : o.toString();
    }
    private String sel(String a, String b) {
        return (a != null && a.equals(b)) ? "selected" : "";
    }
%>
<%
    Patient patient = (Patient) request.getAttribute("patient");
    boolean isEdit = patient != null && patient.getId() > 0;
    if (patient == null) {
        patient = new Patient();
    }
    String pageTitle = isEdit ? "Edit Patient" : "Register Patient";
%>
<%@ include file="/WEB-INF/includes/header.jspf" %>

<h2><%= pageTitle %></h2>

<form action="patient" method="post" name="patientForm" onsubmit="return validateForm()">
    <input type="hidden" name="id" value="<%= isEdit ? String.valueOf(patient.getId()) : "" %>">
    <input type="hidden" name="mrn" value="<%= v(patient.getMrn()) %>">

    <table class="form">
        <% if (isEdit) { %>
        <tr><td class="label">MRN:</td><td><b><%= patient.getMrn() %></b></td></tr>
        <% } %>
        <tr>
            <td class="label">First Name *:</td>
            <td><input type="text" name="firstName" size="30" maxlength="50" value="<%= v(patient.getFirstName()) %>"></td>
        </tr>
        <tr>
            <td class="label">Last Name *:</td>
            <td><input type="text" name="lastName" size="30" maxlength="50" value="<%= v(patient.getLastName()) %>"></td>
        </tr>
        <tr>
            <td class="label">Date of Birth * (yyyy-MM-dd):</td>
            <td><input type="text" name="dateOfBirth" size="12" value="<%= DateUtil.formatDate(patient.getDateOfBirth()) %>"></td>
        </tr>
        <tr>
            <td class="label">Gender:</td>
            <td>
                <select name="gender">
                    <option value="M" <%= sel(patient.getGender(), "M") %>>Male</option>
                    <option value="F" <%= sel(patient.getGender(), "F") %>>Female</option>
                    <option value="O" <%= sel(patient.getGender(), "O") %>>Other</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="label">Blood Group:</td>
            <td>
                <select name="bloodGroup">
                    <option value="">-- Unknown --</option>
                    <%
                        String[] groups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
                        for (int i = 0; i < groups.length; i++) {
                    %>
                    <option value="<%= groups[i] %>" <%= sel(patient.getBloodGroup(), groups[i]) %>><%= groups[i] %></option>
                    <% } %>
                </select>
            </td>
        </tr>
        <tr>
            <td class="label">Phone *:</td>
            <td><input type="text" name="phone" size="20" value="<%= v(patient.getPhone()) %>"></td>
        </tr>
        <tr>
            <td class="label">Email:</td>
            <td><input type="text" name="email" size="30" value="<%= v(patient.getEmail()) %>"></td>
        </tr>
        <tr>
            <td class="label">Address:</td>
            <td><input type="text" name="address" size="50" value="<%= v(patient.getAddress()) %>"></td>
        </tr>
        <tr>
            <td class="label">City / State / Zip:</td>
            <td>
                <input type="text" name="city" size="20" value="<%= v(patient.getCity()) %>">
                <input type="text" name="state" size="4" maxlength="2" value="<%= v(patient.getState()) %>">
                <input type="text" name="zipCode" size="8" maxlength="10" value="<%= v(patient.getZipCode()) %>">
            </td>
        </tr>
        <tr>
            <td class="label">SSN (999-99-9999):</td>
            <td><input type="text" name="ssn" size="12" value="<%= v(patient.getSsn()) %>"></td>
        </tr>
        <tr>
            <td class="label">Insurance Provider:</td>
            <td><input type="text" name="insuranceProvider" size="30" value="<%= v(patient.getInsuranceProvider()) %>"></td>
        </tr>
        <tr>
            <td class="label">Insurance Number:</td>
            <td><input type="text" name="insuranceNumber" size="20" value="<%= v(patient.getInsuranceNumber()) %>"></td>
        </tr>
        <tr>
            <td class="label">Emergency Contact Name:</td>
            <td><input type="text" name="emergencyContactName" size="30" value="<%= v(patient.getEmergencyContactName()) %>"></td>
        </tr>
        <tr>
            <td class="label">Emergency Contact Phone:</td>
            <td><input type="text" name="emergencyContactPhone" size="20" value="<%= v(patient.getEmergencyContactPhone()) %>"></td>
        </tr>
        <tr>
            <td></td>
            <td>
                <input type="submit" value="<%= isEdit ? "Update Patient" : "Register Patient" %>">
                <input type="button" value="Cancel" onclick="history.back()">
            </td>
        </tr>
    </table>
</form>

<script type="text/javascript">
    function validateForm() {
        var f = document.patientForm;
        if (f.firstName.value == '' || f.lastName.value == '') {
            alert('First and last name are required');
            return false;
        }
        if (f.dateOfBirth.value == '') {
            alert('Date of birth is required');
            return false;
        }
        if (f.phone.value.length < 10) {
            alert('Please enter a valid phone number');
            return false;
        }
        return true;
    }
</script>

<%@ include file="/WEB-INF/includes/footer.jspf" %>
