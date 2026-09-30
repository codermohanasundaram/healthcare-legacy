package com.healthcare.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.healthcare.dao.DoctorDAO;
import com.healthcare.model.Doctor;

/**
 * Doctor management.
 * URL: /doctor?action=list|new|edit|delete
 */
public class DoctorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private DoctorDAO doctorDAO = new DoctorDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) action = "list";

        if (action.equals("list")) {
            String specialization = request.getParameter("specialization");
            if (specialization != null && !specialization.equals("")) {
                request.setAttribute("doctors", doctorDAO.getDoctorsBySpecialization(specialization));
            } else {
                request.setAttribute("doctors", doctorDAO.getAllDoctors(false));
            }
            request.getRequestDispatcher("/doctor-list.jsp").forward(request, response);
        } else if (action.equals("new")) {
            request.getRequestDispatcher("/doctor-form.jsp").forward(request, response);
        } else if (action.equals("edit")) {
            int id = Integer.parseInt(request.getParameter("id"));
            request.setAttribute("doctor", doctorDAO.getDoctorById(id));
            request.getRequestDispatcher("/doctor-form.jsp").forward(request, response);
        } else if (action.equals("delete")) {
            int id = Integer.parseInt(request.getParameter("id"));
            if (doctorDAO.deleteDoctor(id)) {
                request.getSession().setAttribute("message", "Doctor deleted");
            } else {
                request.getSession().setAttribute("message", "Doctor could not be deleted");
            }
            response.sendRedirect("doctor?action=list");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Doctor d = new Doctor();
        d.setFirstName(request.getParameter("firstName"));
        d.setLastName(request.getParameter("lastName"));
        d.setSpecialization(request.getParameter("specialization"));
        d.setPhone(request.getParameter("phone"));
        d.setEmail(request.getParameter("email"));
        d.setActive("on".equals(request.getParameter("active")) || "1".equals(request.getParameter("active")));

        String fee = request.getParameter("consultationFee");
        try {
            d.setConsultationFee(Double.parseDouble(fee));
        } catch (Exception e) {
            request.setAttribute("error", "Consultation fee must be a number");
            request.setAttribute("doctor", d);
            request.getRequestDispatcher("/doctor-form.jsp").forward(request, response);
            return;
        }

        if (d.getFirstName() == null || d.getFirstName().equals("")
                || d.getLastName() == null || d.getLastName().equals("")) {
            request.setAttribute("error", "Doctor name is required");
            request.setAttribute("doctor", d);
            request.getRequestDispatcher("/doctor-form.jsp").forward(request, response);
            return;
        }

        String id = request.getParameter("id");
        if (id == null || id.equals("")) {
            doctorDAO.addDoctor(d);
            request.getSession().setAttribute("message", "Doctor added");
        } else {
            d.setId(Integer.parseInt(id));
            doctorDAO.updateDoctor(d);
            request.getSession().setAttribute("message", "Doctor updated");
        }
        response.sendRedirect("doctor?action=list");
    }
}
