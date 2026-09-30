package com.healthcare.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.healthcare.dao.DoctorDAO;
import com.healthcare.dao.MedicalRecordDAO;
import com.healthcare.dao.PatientDAO;
import com.healthcare.model.MedicalRecord;
import com.healthcare.model.Patient;
import com.healthcare.util.DateUtil;

/**
 * View and add medical records (visit notes) for a patient.
 * URL: /medicalRecord?patientId=
 */
public class MedicalRecordServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(MedicalRecordServlet.class);

    private MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAO();
    private PatientDAO patientDAO = new PatientDAO();
    private DoctorDAO doctorDAO = new DoctorDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String patientId = request.getParameter("patientId");
        if (patientId == null || patientId.equals("")) {
            response.sendRedirect("patient?action=list");
            return;
        }
        int id = Integer.parseInt(patientId);
        Patient patient = patientDAO.getPatientById(id);

        log.info("Medical records for patient " + id + " viewed by " + request.getSession().getAttribute("username"));

        request.setAttribute("patient", patient);
        request.setAttribute("records", medicalRecordDAO.getRecordsByPatient(id));
        request.setAttribute("doctors", doctorDAO.getAllDoctors(true));
        request.getRequestDispatcher("/medical-record.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role");
        if (!"DOCTOR".equals(role) && !"ADMIN".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only doctors can add medical records");
            return;
        }

        int patientId = Integer.parseInt(request.getParameter("patientId"));

        MedicalRecord r = new MedicalRecord();
        r.setPatientId(patientId);

        // doctors always record against themselves; admin picks a doctor
        if ("DOCTOR".equals(role)) {
            r.setDoctorId(((Integer) session.getAttribute("doctorId")).intValue());
        } else {
            r.setDoctorId(Integer.parseInt(request.getParameter("doctorId")));
        }

        String apptId = request.getParameter("appointmentId");
        if (apptId != null && !apptId.equals("")) {
            r.setAppointmentId(Integer.valueOf(apptId));
        }

        r.setVisitDate(DateUtil.parseDate(request.getParameter("visitDate")));
        if (r.getVisitDate() == null) {
            r.setVisitDate(new java.util.Date());
        }
        r.setDiagnosis(request.getParameter("diagnosis"));
        r.setPrescription(request.getParameter("prescription"));
        r.setNotes(request.getParameter("notes"));
        r.setBloodPressure(request.getParameter("bloodPressure"));
        try {
            r.setTemperature(Double.parseDouble(request.getParameter("temperature")));
        } catch (Exception e) {
            r.setTemperature(0);
        }
        try {
            r.setWeight(Double.parseDouble(request.getParameter("weight")));
        } catch (Exception e) {
            r.setWeight(0);
        }

        if (r.getDiagnosis() == null || r.getDiagnosis().trim().equals("")) {
            session.setAttribute("message", "Diagnosis is required - record not saved");
        } else if (medicalRecordDAO.addRecord(r)) {
            session.setAttribute("message", "Medical record added");
        } else {
            session.setAttribute("message", "Error saving medical record");
        }
        response.sendRedirect("medicalRecord?patientId=" + patientId);
    }
}
