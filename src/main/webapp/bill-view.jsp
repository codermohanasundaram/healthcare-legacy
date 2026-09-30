<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.healthcare.model.*, com.healthcare.util.DateUtil" %>
<% String pageTitle = "Invoice"; %>
<%@ include file="/WEB-INF/includes/header.jspf" %>
<%
    Bill bill = (Bill) request.getAttribute("bill");
    Patient patient = (Patient) request.getAttribute("patient");
    if (bill == null) {
%>
    <p>Bill not found.</p>
<%
    } else {
%>
<div class="invoice">
    <table width="100%">
        <tr>
            <td>
                <h2 style="border:none"><%= application.getInitParameter("hospitalName") %></h2>
                <%= application.getInitParameter("hospitalAddress") %>
            </td>
            <td align="right">
                <h3>INVOICE #<%= bill.getId() %></h3>
                Date: <%= DateUtil.displayDate(bill.getBillDate()) %><br/>
                Status: <span class="status-<%= bill.getStatus() %>"><%= bill.getStatus() %></span>
            </td>
        </tr>
    </table>
    <hr/>
    <p>
        <b>Bill To:</b> <%= patient.getFullName() %> (MRN <%= patient.getMrn() %>)<br/>
        <%= patient.getAddress() %>, <%= patient.getCity() %>, <%= patient.getState() %> <%= patient.getZipCode() %><br/>
        Insurance: <%= patient.getInsuranceProvider() != null ? patient.getInsuranceProvider() + " / " + patient.getInsuranceNumber() : "Self pay" %>
    </p>

    <table class="grid">
        <tr><th>Description</th><th style="text-align:right">Amount</th></tr>
        <tr><td>Consultation Fee<%= bill.getAppointmentId() != null ? " (Appointment #" + bill.getAppointmentId() + ")" : "" %></td>
            <td align="right"><%= String.format("%.2f", bill.getConsultationFee()) %></td></tr>
        <tr><td>Medicine Charges</td><td align="right"><%= String.format("%.2f", bill.getMedicineCharges()) %></td></tr>
        <tr><td>Lab Charges</td><td align="right"><%= String.format("%.2f", bill.getLabCharges()) %></td></tr>
        <tr><td><b>Subtotal</b></td><td align="right"><b><%= String.format("%.2f", bill.getSubtotal()) %></b></td></tr>
        <tr><td>Less: Discount</td><td align="right">-<%= String.format("%.2f", bill.getDiscount()) %></td></tr>
        <tr><td>Tax (<%= application.getInitParameter("taxRate") %>%)</td><td align="right"><%= String.format("%.2f", bill.getTax()) %></td></tr>
        <tr><td><b>TOTAL</b></td><td align="right"><b>$<%= String.format("%.2f", bill.getTotalAmount()) %></b></td></tr>
    </table>

    <% if ("PAID".equals(bill.getStatus())) { %>
        <p>Paid by <b><%= bill.getPaymentMethod() %></b> on <%= DateUtil.displayDate(bill.getPaidDate()) %>. Thank you.</p>
    <% } else { %>
        <p>Payment due on receipt.</p>
    <% } %>
</div>

<p class="noprint">
    <a href="javascript:window.print()">Print Invoice</a>
    <% if ("PENDING".equals(bill.getStatus()) && ("ADMIN".equals(_role) || "BILLING".equals(_role))) { %>
    | <a href="billing?action=pay&billId=<%= bill.getId() %>&method=CASH">Record Cash Payment</a>
    | <a href="billing?action=pay&billId=<%= bill.getId() %>&method=CARD">Record Card Payment</a>
    | <a href="billing?action=pay&billId=<%= bill.getId() %>&method=INSURANCE">Record Insurance Payment</a>
    <% } %>
    | <a href="patient?action=view&patientId=<%= bill.getPatientId() %>">Back to Patient</a>
</p>
<% } %>

<%@ include file="/WEB-INF/includes/footer.jspf" %>
