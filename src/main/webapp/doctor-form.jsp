<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% String pageTitle = "Doctor"; %>
<%@ include file="/WEB-INF/includes/header.jspf" %>

<h2><c:choose><c:when test="${doctor != null && doctor.id > 0}">Edit Doctor</c:when><c:otherwise>Add Doctor</c:otherwise></c:choose></h2>

<form action="doctor" method="post">
    <input type="hidden" name="id" value="${doctor.id > 0 ? doctor.id : ''}">
    <table class="form">
        <tr>
            <td class="label">First Name *:</td>
            <td><input type="text" name="firstName" value="${doctor.firstName}"></td>
        </tr>
        <tr>
            <td class="label">Last Name *:</td>
            <td><input type="text" name="lastName" value="${doctor.lastName}"></td>
        </tr>
        <tr>
            <td class="label">Specialization:</td>
            <td><input type="text" name="specialization" value="${doctor.specialization}"></td>
        </tr>
        <tr>
            <td class="label">Phone:</td>
            <td><input type="text" name="phone" value="${doctor.phone}"></td>
        </tr>
        <tr>
            <td class="label">Email:</td>
            <td><input type="text" name="email" value="${doctor.email}"></td>
        </tr>
        <tr>
            <td class="label">Consultation Fee *:</td>
            <td><input type="text" name="consultationFee" value="${doctor.consultationFee}"></td>
        </tr>
        <tr>
            <td class="label">Active:</td>
            <td><input type="checkbox" name="active" <c:if test="${doctor == null || doctor.active}">checked</c:if>></td>
        </tr>
        <tr>
            <td></td>
            <td>
                <input type="submit" value="Save">
                <input type="button" value="Cancel" onclick="location.href='doctor?action=list'">
            </td>
        </tr>
    </table>
</form>

<%@ include file="/WEB-INF/includes/footer.jspf" %>
