package com.healthcare.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.healthcare.dao.AppointmentDAO;
import com.healthcare.dao.BillingDAO;
import com.healthcare.dao.DoctorDAO;
import com.healthcare.dao.PatientDAO;
import com.healthcare.model.Appointment;
import com.healthcare.model.Bill;
import com.healthcare.model.Doctor;
import com.healthcare.model.Patient;
import com.healthcare.service.PatientService;

/**
 * Billing: generate bills, record payments, view invoices.
 * URL: /billing?action=list|new|view|pay
 */
public class BillingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(BillingServlet.class);

    private BillingDAO billingDAO = new BillingDAO();
    private PatientDAO patientDAO = new PatientDAO();
    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private DoctorDAO doctorDAO = new DoctorDAO();

    private boolean canManageBilling(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return "ADMIN".equals(role) || "BILLING".equals(role);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) action = "list";

        // invoices can be printed by anyone at the front desk
        if (action.equals("view")) {
            int billId = Integer.parseInt(request.getParameter("billId"));
            Bill bill = billingDAO.getBillById(billId);
            request.setAttribute("bill", bill);
            if (bill != null) {
                request.setAttribute("patient", patientDAO.getPatientById(bill.getPatientId()));
            }
            request.getRequestDispatcher("/bill-view.jsp").forward(request, response);
            return;
        }

        if (!canManageBilling(request.getSession())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Billing access required");
            return;
        }

        if (action.equals("list")) {
            String status = request.getParameter("status");
            request.setAttribute("bills", billingDAO.getAllBills(status));
            request.setAttribute("status", status);
            request.getRequestDispatcher("/bill-list.jsp").forward(request, response);

        } else if (action.equals("new")) {
            String appointmentId = request.getParameter("appointmentId");
            if (appointmentId != null && !appointmentId.equals("")) {
                Appointment appt = appointmentDAO.getAppointmentById(Integer.parseInt(appointmentId));
                if (appt != null) {
                    Doctor doctor = doctorDAO.getDoctorById(appt.getDoctorId());
                    request.setAttribute("appointment", appt);
                    request.setAttribute("consultationFee", new Double(doctor.getConsultationFee()));
                    request.setAttribute("selectedPatientId", String.valueOf(appt.getPatientId()));
                }
            } else {
                request.setAttribute("selectedPatientId", request.getParameter("patientId"));
            }
            request.setAttribute("patients", patientDAO.getAllPatients());
            request.getRequestDispatcher("/billing.jsp").forward(request, response);

        } else if (action.equals("pay")) {
            int billId = Integer.parseInt(request.getParameter("billId"));
            String method = request.getParameter("method");
            if (method == null || method.equals("")) method = "CASH";
            billingDAO.markPaid(billId, method);
            log.info("Bill " + billId + " paid by " + method + ", recorded by "
                    + request.getSession().getAttribute("username"));
            request.getSession().setAttribute("message", "Payment recorded for bill #" + billId);
            response.sendRedirect("billing?action=view&billId=" + billId);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!canManageBilling(request.getSession())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Billing access required");
            return;
        }

        int patientId = Integer.parseInt(request.getParameter("patientId"));
        Patient patient = patientDAO.getPatientById(patientId);

        double consultationFee = parseAmount(request.getParameter("consultationFee"));
        double medicineCharges = parseAmount(request.getParameter("medicineCharges"));
        double labCharges = parseAmount(request.getParameter("labCharges"));

        // ---------------------------------------------------------------
        // Billing rules as per Finance dept (mail dated 14-Mar-2014).
        // DO NOT CHANGE without approval from finance.
        //   * senior citizens (60+) get 10% off the subtotal
        //   * insured patients: insurance covers 30% of lab charges
        //   * tax 18% on the amount after discount
        // ---------------------------------------------------------------
        double subtotal = consultationFee + medicineCharges + labCharges;
        double discount = 0;

        int age = PatientService.calculateAge(patient.getDateOfBirth());
        if (age >= 60) {
            discount = subtotal * 0.10;
        }
        if (patient.getInsuranceProvider() != null && !patient.getInsuranceProvider().trim().equals("")) {
            discount = discount + (labCharges * 0.30);
        }

        double tax = (subtotal - discount) * 0.18;
        double total = subtotal - discount + tax;
        total = Math.round(total * 100) / 100.0;

        Bill bill = new Bill();
        bill.setPatientId(patientId);
        String appointmentId = request.getParameter("appointmentId");
        if (appointmentId != null && !appointmentId.equals("")) {
            bill.setAppointmentId(Integer.valueOf(appointmentId));
        }
        bill.setConsultationFee(consultationFee);
        bill.setMedicineCharges(medicineCharges);
        bill.setLabCharges(labCharges);
        bill.setDiscount(discount);
        bill.setTax(tax);
        bill.setTotalAmount(total);
        bill.setStatus("PENDING");

        int billId = billingDAO.createBill(bill);
        log.info("Bill " + billId + " created for patient " + patient.getMrn() + " total=" + total);

        request.getSession().setAttribute("message", "Bill #" + billId + " generated");
        response.sendRedirect("billing?action=view&billId=" + billId);
    }

    private double parseAmount(String value) {
        if (value == null || value.trim().equals("")) return 0;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
