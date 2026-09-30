<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<% String pageTitle = "Billing"; %>
<%@ include file="/WEB-INF/includes/header.jspf" %>

<h2>Billing</h2>

<form action="billing" method="get">
    <input type="hidden" name="action" value="list">
    Status:
    <select name="status">
        <option value="ALL">All</option>
        <option value="PENDING" ${status == 'PENDING' ? 'selected' : ''}>Pending</option>
        <option value="PAID" ${status == 'PAID' ? 'selected' : ''}>Paid</option>
    </select>
    <input type="submit" value="Filter">
    &nbsp;&nbsp; <a href="billing?action=new">+ New Bill</a>
</form>
<br/>

<c:set var="grandTotal" value="0"/>
<table class="grid">
    <tr>
        <th>Bill #</th>
        <th>Date</th>
        <th>Patient</th>
        <th>MRN</th>
        <th>Appt #</th>
        <th>Subtotal</th>
        <th>Discount</th>
        <th>Tax</th>
        <th>Total</th>
        <th>Status</th>
        <th>Actions</th>
    </tr>
    <c:forEach var="b" items="${bills}" varStatus="st">
    <c:set var="grandTotal" value="${grandTotal + b.totalAmount}"/>
    <tr class="${st.index % 2 == 0 ? 'even' : 'odd'}">
        <td>${b.id}</td>
        <td><fmt:formatDate value="${b.billDate}" pattern="dd-MMM-yyyy"/></td>
        <td>${b.patientName}</td>
        <td>${b.patientMrn}</td>
        <td>${b.appointmentId}</td>
        <td><fmt:formatNumber value="${b.subtotal}" minFractionDigits="2" maxFractionDigits="2"/></td>
        <td><fmt:formatNumber value="${b.discount}" minFractionDigits="2" maxFractionDigits="2"/></td>
        <td><fmt:formatNumber value="${b.tax}" minFractionDigits="2" maxFractionDigits="2"/></td>
        <td><b>$<fmt:formatNumber value="${b.totalAmount}" minFractionDigits="2" maxFractionDigits="2"/></b></td>
        <td class="status-${b.status}">${b.status}</td>
        <td>
            <a href="billing?action=view&billId=${b.id}">View</a>
            <c:if test="${b.status == 'PENDING'}">
                | <a href="billing?action=pay&billId=${b.id}&method=CASH">Paid (Cash)</a>
                | <a href="billing?action=pay&billId=${b.id}&method=CARD">Paid (Card)</a>
            </c:if>
        </td>
    </tr>
    </c:forEach>
    <tr>
        <td colspan="8" align="right"><b>Total:</b></td>
        <td colspan="3"><b>$<fmt:formatNumber value="${grandTotal}" minFractionDigits="2" maxFractionDigits="2"/></b></td>
    </tr>
</table>

<%@ include file="/WEB-INF/includes/footer.jspf" %>
