<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<% String pageTitle = "Doctors"; %>
<%@ include file="/WEB-INF/includes/header.jspf" %>

<h2>Doctors</h2>

<form action="doctor" method="get">
    <input type="hidden" name="action" value="list">
    Specialization:
    <select name="specialization">
        <option value="">-- All --</option>
        <option value="General Medicine">General Medicine</option>
        <option value="Cardiology">Cardiology</option>
        <option value="Orthopedics">Orthopedics</option>
        <option value="Pediatrics">Pediatrics</option>
        <option value="Dermatology">Dermatology</option>
        <option value="Neurology">Neurology</option>
    </select>
    <input type="submit" value="Filter">
    &nbsp;&nbsp; <a href="doctor?action=new">+ Add Doctor</a>
</form>
<br/>

<c:choose>
    <c:when test="${empty doctors}">
        <p>No doctors found.</p>
    </c:when>
    <c:otherwise>
        <table class="grid">
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Specialization</th>
                <th>Phone</th>
                <th>Email</th>
                <th>Consultation Fee</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
            <c:forEach var="d" items="${doctors}" varStatus="st">
                <tr class="${st.index % 2 == 0 ? 'even' : 'odd'}">
                    <td>${d.id}</td>
                    <td>${d.fullName}</td>
                    <td>${d.specialization}</td>
                    <td>${d.phone}</td>
                    <td>${d.email}</td>
                    <td>$<fmt:formatNumber value="${d.consultationFee}" minFractionDigits="2" maxFractionDigits="2"/></td>
                    <td><c:if test="${d.active}">Active</c:if><c:if test="${!d.active}"><i>Inactive</i></c:if></td>
                    <td>
                        <a href="doctor?action=edit&id=${d.id}">Edit</a> |
                        <a href="appointment?action=list&doctorId=${d.id}">Schedule</a> |
                        <a href="doctor?action=delete&id=${d.id}" onclick="return confirmDelete()">Delete</a>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/includes/footer.jspf" %>
