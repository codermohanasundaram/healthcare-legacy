<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<% String pageTitle = "Appointments"; %>
<%@ include file="/WEB-INF/includes/header.jspf" %>

<h2>Appointments</h2>

<form action="appointment" method="get">
    <input type="hidden" name="action" value="list">
    Date (yyyy-MM-dd): <input type="text" name="date" size="12" value="${filterDate}">
    Doctor:
    <select name="doctorId">
        <option value="">-- All --</option>
        <c:forEach var="d" items="${doctors}">
            <option value="${d.id}" ${param.doctorId == d.id ? 'selected' : ''}>${d.fullName}</option>
        </c:forEach>
    </select>
    <input type="submit" value="Filter">
    <a href="appointment?action=list">Clear</a>
    &nbsp;&nbsp; <a href="appointment?action=new">+ Book Appointment</a>
</form>
<br/>

<c:if test="${empty appointments}">
    <p>No appointments found.</p>
</c:if>

<c:if test="${not empty appointments}">
<table class="grid">
    <tr>
        <th>#</th>
        <th>Date</th>
        <th>Time</th>
        <th>Patient</th>
        <th>MRN</th>
        <th>Doctor</th>
        <th>Reason</th>
        <th>Status</th>
        <th>Actions</th>
    </tr>
    <c:forEach var="a" items="${appointments}" varStatus="st">
    <tr class="${st.index % 2 == 0 ? 'even' : 'odd'}">
        <td>${a.id}</td>
        <td><fmt:formatDate value="${a.appointmentDate}" pattern="dd-MMM-yyyy"/></td>
        <td>${a.appointmentTime}</td>
        <td><a href="patient?action=view&patientId=${a.patientId}">${a.patientName}</a></td>
        <td>${a.patientMrn}</td>
        <td>${a.doctorName}</td>
        <td>${a.reason}</td>
        <td class="status-${a.status}">${a.status}</td>
        <td>
            <c:if test="${a.status == 'SCHEDULED'}">
                <a href="appointment?action=complete&id=${a.id}">Complete</a> |
                <a href="appointment?action=cancel&id=${a.id}" onclick="return confirm('Cancel this appointment?')">Cancel</a>
            </c:if>
            <c:if test="${a.status == 'COMPLETED'}">
                <a href="medicalRecord?patientId=${a.patientId}">Record</a> |
                <a href="billing?action=new&appointmentId=${a.id}">Bill</a>
            </c:if>
        </td>
    </tr>
    </c:forEach>
</table>
</c:if>

<%@ include file="/WEB-INF/includes/footer.jspf" %>
