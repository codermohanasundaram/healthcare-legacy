package com.healthcare.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.healthcare.dao.UserDAO;
import com.healthcare.model.User;

public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(LoginServlet.class);

    private static final int MAX_ATTEMPTS = 5;

    private UserDAO userDAO = new UserDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        HttpSession session = request.getSession();

        Integer attempts = (Integer) session.getAttribute("loginAttempts");
        if (attempts == null) {
            attempts = new Integer(0);
        }
        if (attempts.intValue() >= MAX_ATTEMPTS) {
            request.setAttribute("error", "Too many failed login attempts. Please try again later.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        if (username == null || username.trim().equals("") || password == null || password.equals("")) {
            request.setAttribute("error", "Please enter username and password");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        log.debug("Login attempt: username=" + username + ", password=" + password);

        User user = userDAO.authenticate(username, password);
        if (user != null) {
            session.setAttribute("user", user);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            session.setAttribute("fullName", user.getFullName());
            if (user.getDoctorId() != null) {
                session.setAttribute("doctorId", user.getDoctorId());
            }
            session.removeAttribute("loginAttempts");
            userDAO.updateLastLogin(user.getId());

            log.info("User " + username + " logged in (role " + user.getRole() + ")");
            response.sendRedirect("home.jsp");
        } else {
            session.setAttribute("loginAttempts", new Integer(attempts.intValue() + 1));
            log.warn("Failed login for " + username);
            request.setAttribute("error", "Invalid username or password");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}
