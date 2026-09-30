package com.healthcare.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.healthcare.dao.AppointmentDAO;
import com.healthcare.dao.BillingDAO;
import com.healthcare.dao.DoctorDAO;
import com.healthcare.dao.PatientDAO;
import com.healthcare.model.Appointment;
import com.healthcare.model.Bill;
import com.healthcare.model.Doctor;
import com.healthcare.util.DateUtil;

/**
 * Appointment booking and status changes.
 * URL: /appointment?action=list|new|cancel|complete
 */
public class AppointmentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(AppointmentServlet.class);

    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private PatientDAO patientDAO = new PatientDAO();
    private DoctorDAO doctorDAO = new DoctorDAO();
    private BillingDAO billingDAO = new BillingDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) action = "list";

        if (action.equals("list")) {
            String date = request.getParameter("date");
            String doctorId = request.getParameter("doctorId");
            String patientId = request.getParameter("patientId");
            List<Appointment> appointments;

            if (date != null && !date.equals("")) {
                appointments = appointmentDAO.getAppointmentsByDate(DateUtil.parseDate(date));
            } else if (doctorId != null && !doctorId.equals("")) {
                appointments = appointmentDAO.getAppointmentsByDoctor(Integer.parseInt(doctorId));
            } else if (patientId != null && !patientId.equals("")) {
                appointments = appointmentDAO.getAppointmentsByPatient(Integer.parseInt(patientId));
            } else if ("DOCTOR".equals(request.getSession().getAttribute("role"))) {
                // doctors only see their own schedule by default
                Integer myId = (Integer) request.getSession().getAttribute("doctorId");
                appointments = appointmentDAO.getAppointmentsByDoctor(myId.intValue());
            } else {
                appointments = appointmentDAO.getAllAppointments();
            }

            request.setAttribute("appointments", appointments);
            request.setAttribute("doctors", doctorDAO.getAllDoctors(true));
            request.setAttribute("filterDate", date);
            request.getRequestDispatcher("/appointment-list.jsp").forward(request, response);

        } else if (action.equals("new")) {
            loadFormData(request);
            request.setAttribute("selectedPatientId", request.getParameter("patientId"));
            request.getRequestDispatcher("/appointment.jsp").forward(request, response);

        } else if (action.equals("cancel")) {
            int id = Integer.parseInt(request.getParameter("id"));
            appointmentDAO.updateStatus(id, "CANCELLED");
            log.info("Appointment " + id + " cancelled by " + request.getSession().getAttribute("username"));
            request.getSession().setAttribute("message", "Appointment #" + id + " cancelled");
            response.sendRedirect("appointment?action=list");

        } else if (action.equals("complete")) {
            int id = Integer.parseInt(request.getParameter("id"));
            Appointment appt = appointmentDAO.getAppointmentById(id);
            if (appt == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            appointmentDAO.updateStatus(id, "COMPLETED");

            // Auto-generate the consultation bill (billing integration, 2016).
            // Medicine / lab charges are added later from the billing screen.
            if (billingDAO.getBillByAppointment(id) == null) {
                Doctor doctor = doctorDAO.getDoctorById(appt.getDoctorId());
                double fee = doctor.getConsultationFee();
                double tax = fee * 0.18;

                Bill bill = new Bill();
                bill.setPatientId(appt.getPatientId());
                bill.setAppointmentId(new Integer(id));
                bill.setConsultationFee(fee);
                bill.setMedicineCharges(0);
                bill.setLabCharges(0);
                bill.setDiscount(0);
                bill.setTax(tax);
                bill.setTotalAmount(fee + tax);
                bill.setStatus("PENDING");
                billingDAO.createBill(bill);
                log.info("Auto bill generated for appointment " + id + " amount " + (fee + tax));
            }

            request.getSession().setAttribute("message", "Appointment #" + id + " marked completed and bill generated");
            response.sendRedirect("appointment?action=list");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String patientId = request.getParameter("patientId");
        String doctorId = request.getParameter("doctorId");
        String date = request.getParameter("appointmentDate");
        String time = request.getParameter("appointmentTime");
        String reason = request.getParameter("reason");
        String notes = request.getParameter("notes");

        List<String> errors = new ArrayList<String>();

        if (patientId == null || patientId.equals("")) {
            errors.add("Please select a patient");
        }
        if (doctorId == null || doctorId.equals("")) {
            errors.add("Please select a doctor");
        }

        Date apptDate = DateUtil.parseDate(date);
        if (apptDate == null) {
            errors.add("Appointment date is invalid");
        } else {
            if (apptDate.before(DateUtil.today())) {
                errors.add("Appointment date cannot be in the past");
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(apptDate);
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                errors.add("The clinic is closed on Sundays");
            }
        }

        // clinic hours 09:00 - 17:00, last slot 16:30
        if (time == null || time.compareTo("09:00") < 0 || time.compareTo("16:30") > 0) {
            errors.add("Appointment time must be between 09:00 and 16:30");
        }

        if (errors.isEmpty()) {
            if (!appointmentDAO.isSlotAvailable(Integer.parseInt(doctorId), apptDate, time)) {
                errors.add("The doctor already has an appointment at " + time + " on " + date);
            }
        }

        if (!errors.isEmpty()) {
            String msg = "";
            for (int i = 0; i < errors.size(); i++) {
                msg += errors.get(i) + "<br/>";
            }
            request.setAttribute("error", msg);
            request.setAttribute("selectedPatientId", patientId);
            request.setAttribute("selectedDoctorId", doctorId);
            request.setAttribute("appointmentDate", date);
            request.setAttribute("appointmentTime", time);
            request.setAttribute("reason", reason);
            loadFormData(request);
            request.getRequestDispatcher("/appointment.jsp").forward(request, response);
            return;
        }

        Appointment a = new Appointment();
        a.setPatientId(Integer.parseInt(patientId));
        a.setDoctorId(Integer.parseInt(doctorId));
        a.setAppointmentDate(apptDate);
        a.setAppointmentTime(time);
        a.setReason(reason);
        a.setNotes(notes);

        int id = appointmentDAO.bookAppointment(a);
        log.info("Appointment " + id + " booked for patient " + patientId + " with doctor " + doctorId);
        request.getSession().setAttribute("message", "Appointment #" + id + " booked for " + date + " " + time);
        response.sendRedirect("appointment?action=list");
    }

    private void loadFormData(HttpServletRequest request) {
        request.setAttribute("patients", patientDAO.getAllPatients());
        request.setAttribute("doctors", doctorDAO.getAllDoctors(true));
    }
}
