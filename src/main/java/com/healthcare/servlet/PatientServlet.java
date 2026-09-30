package com.healthcare.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.healthcare.dao.AppointmentDAO;
import com.healthcare.dao.BillingDAO;
import com.healthcare.dao.MedicalRecordDAO;
import com.healthcare.dao.PatientDAO;
import com.healthcare.model.Patient;
import com.healthcare.service.PatientService;
import com.healthcare.util.DateUtil;

/**
 * Handles patient registration, search, view, edit and delete.
 *
 * URL: /patient?action=list|search|view|new|edit|delete
 */
public class PatientServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(PatientServlet.class);

    private PatientService patientService = new PatientService();
    private PatientDAO patientDAO = new PatientDAO();
    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAO();
    private BillingDAO billingDAO = new BillingDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        try {
            if (action.equals("list")) {
                List<Patient> patients = patientService.getAllPatients();
                request.setAttribute("patients", patients);
                request.getRequestDispatcher("/patient-list.jsp").forward(request, response);

            } else if (action.equals("search")) {
                String name = request.getParameter("name");
                String phone = request.getParameter("phone");
                String mrn = request.getParameter("mrn");
                List<Patient> patients = patientService.searchPatients(name, phone, mrn);
                request.setAttribute("patients", patients);
                request.setAttribute("searchName", name);
                request.setAttribute("searchPhone", phone);
                request.setAttribute("searchMrn", mrn);
                request.getRequestDispatcher("/patient-list.jsp").forward(request, response);

            } else if (action.equals("view")) {
                String patientId = request.getParameter("patientId");
                Patient patient = patientService.getPatient(patientId);
                if (patient == null) {
                    request.setAttribute("error", "Patient not found: " + patientId);
                    request.setAttribute("patients", patientService.getAllPatients());
                    request.getRequestDispatcher("/patient-list.jsp").forward(request, response);
                    return;
                }
                request.setAttribute("patient", patient);
                request.setAttribute("age", new Integer(PatientService.calculateAge(patient.getDateOfBirth())));
                request.setAttribute("appointments", appointmentDAO.getAppointmentsByPatient(patient.getId()));
                request.setAttribute("records", medicalRecordDAO.getRecordsByPatient(patient.getId()));
                request.setAttribute("bills", billingDAO.getBillsByPatient(patient.getId()));
                request.getRequestDispatcher("/patient.jsp").forward(request, response);

            } else if (action.equals("new")) {
                request.getRequestDispatcher("/patient-form.jsp").forward(request, response);

            } else if (action.equals("edit")) {
                Patient patient = patientService.getPatient(request.getParameter("patientId"));
                request.setAttribute("patient", patient);
                request.getRequestDispatcher("/patient-form.jsp").forward(request, response);

            } else if (action.equals("delete")) {
                String role = (String) request.getSession().getAttribute("role");
                if (!"ADMIN".equals(role)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only administrators can delete patients");
                    return;
                }
                int id = Integer.parseInt(request.getParameter("patientId"));
                patientDAO.deletePatient(id);
                log.info("Patient " + id + " deleted by " + request.getSession().getAttribute("username"));
                request.getSession().setAttribute("message", "Patient deleted");
                response.sendRedirect("patient?action=list");

            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action: " + action);
            }
        } catch (NumberFormatException e) {
            log.error("Invalid patient id", e);
            request.setAttribute("error", "Invalid patient id");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");

        Patient p = new Patient();
        p.setFirstName(request.getParameter("firstName"));
        p.setLastName(request.getParameter("lastName"));
        p.setDateOfBirth(DateUtil.parseDate(request.getParameter("dateOfBirth")));
        p.setGender(request.getParameter("gender"));
        p.setPhone(request.getParameter("phone"));
        p.setEmail(request.getParameter("email"));
        p.setAddress(request.getParameter("address"));
        p.setCity(request.getParameter("city"));
        p.setState(request.getParameter("state"));
        p.setZipCode(request.getParameter("zipCode"));
        p.setSsn(request.getParameter("ssn"));
        p.setInsuranceProvider(request.getParameter("insuranceProvider"));
        p.setInsuranceNumber(request.getParameter("insuranceNumber"));
        p.setBloodGroup(request.getParameter("bloodGroup"));
        p.setEmergencyContactName(request.getParameter("emergencyContactName"));
        p.setEmergencyContactPhone(request.getParameter("emergencyContactPhone"));

        String id = request.getParameter("id");
        try {
            if (id == null || id.equals("")) {
                int newId = patientService.registerPatient(p, username);
                session.setAttribute("message", "Patient registered successfully. MRN: " + p.getMrn());
                response.sendRedirect("patient?action=view&patientId=" + newId);
            } else {
                p.setId(Integer.parseInt(id));
                p.setMrn(request.getParameter("mrn"));
                patientService.updatePatient(p);
                session.setAttribute("message", "Patient details updated");
                response.sendRedirect("patient?action=view&patientId=" + id);
            }
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("patient", p);
            request.getRequestDispatcher("/patient-form.jsp").forward(request, response);
        }
    }
}
