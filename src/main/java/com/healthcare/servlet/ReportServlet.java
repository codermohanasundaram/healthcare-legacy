package com.healthcare.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.healthcare.util.DBConnection;
import com.healthcare.util.DateUtil;

/**
 * Management reports. Queries are run directly here because the reports
 * change often and the DAOs were getting too big.
 *
 * URL: /report?type=summary|revenue|doctor|demographics|visits|export
 */
public class ReportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(ReportServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String type = request.getParameter("type");
        if (type == null || type.equals("")) type = "summary";

        if (type.equals("export")) {
            exportPatientsCsv(response);
            return;
        }

        String title;
        List<String[]> rows;

        try {
            if (type.equals("summary")) {
                title = "Hospital Summary";
                rows = runQuery("SELECT "
                        + "(SELECT COUNT(*) FROM patients WHERE active = 1) AS total_patients, "
                        + "(SELECT COUNT(*) FROM doctors WHERE active = 1) AS active_doctors, "
                        + "(SELECT COUNT(*) FROM appointments WHERE appointment_date = CURDATE() AND status <> 'CANCELLED') AS appointments_today, "
                        + "(SELECT IFNULL(SUM(total_amount), 0) FROM bills WHERE status = 'PAID') AS revenue_collected, "
                        + "(SELECT IFNULL(SUM(total_amount), 0) FROM bills WHERE status = 'PENDING') AS outstanding");

            } else if (type.equals("revenue")) {
                String month = request.getParameter("month");
                if (month == null || month.equals("")) {
                    month = DateUtil.currentMonth();
                }
                title = "Revenue for " + month;
                rows = runQuery("SELECT b.bill_date, b.id AS bill_no, p.mrn, "
                        + "CONCAT(p.first_name, ' ', p.last_name) AS patient, "
                        + "(b.consultation_fee + b.medicine_charges + b.lab_charges) AS subtotal, b.discount, b.tax, b.total_amount, b.status, b.payment_method "
                        + "FROM bills b JOIN patients p ON b.patient_id = p.id "
                        + "WHERE DATE_FORMAT(b.bill_date, '%Y-%m') = '" + month + "' "
                        + "ORDER BY b.bill_date, b.id");

            } else if (type.equals("doctor")) {
                title = "Appointments by Doctor";
                rows = runQuery("SELECT CONCAT('Dr. ', d.first_name, ' ', d.last_name) AS doctor, d.specialization, "
                        + "COUNT(a.id) AS total, "
                        + "SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed, "
                        + "SUM(CASE WHEN a.status = 'SCHEDULED' THEN 1 ELSE 0 END) AS scheduled, "
                        + "SUM(CASE WHEN a.status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled "
                        + "FROM doctors d LEFT JOIN appointments a ON a.doctor_id = d.id "
                        + "GROUP BY d.id, d.first_name, d.last_name, d.specialization ORDER BY total DESC");

            } else if (type.equals("demographics")) {
                title = "Patient Demographics";
                rows = runQuery("SELECT gender, "
                        + "CASE WHEN TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE()) < 18 THEN '0-17' "
                        + "WHEN TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE()) < 40 THEN '18-39' "
                        + "WHEN TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE()) < 60 THEN '40-59' "
                        + "ELSE '60+' END AS age_group, COUNT(*) AS patients "
                        + "FROM patients WHERE active = 1 GROUP BY gender, age_group ORDER BY gender, age_group");

            } else if (type.equals("visits")) {
                String from = request.getParameter("from");
                String to = request.getParameter("to");
                if (from == null || from.equals("")) from = "2000-01-01";
                if (to == null || to.equals("")) to = DateUtil.formatDate(new java.util.Date());
                title = "Patient Visit Summary " + from + " to " + to;
                rows = runVisitSummary(from, to);

            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown report " + type);
                return;
            }
        } catch (SQLException e) {
            log.error("Report " + type + " failed", e);
            throw new ServletException("Report failed: " + e.getMessage(), e);
        }

        request.setAttribute("reportType", type);
        request.setAttribute("reportTitle", title);
        request.setAttribute("reportRows", rows);
        request.getRequestDispatcher("/reports.jsp").forward(request, response);
    }

    /** First row of the result is the column headers. */
    private List<String[]> runQuery(String sql) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            log.debug("Report SQL: " + sql);
            rs = stmt.executeQuery(sql);
            return toRows(rs);
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
    }

    private List<String[]> runVisitSummary(String from, String to) throws SQLException {
        Connection conn = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            cs = conn.prepareCall("{call sp_patient_visit_summary(?, ?)}");
            cs.setString(1, from);
            cs.setString(2, to);
            rs = cs.executeQuery();
            return toRows(rs);
        } finally {
            DBConnection.close(conn, cs, rs);
        }
    }

    private List<String[]> toRows(ResultSet rs) throws SQLException {
        List<String[]> rows = new ArrayList<String[]>();
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        String[] header = new String[cols];
        for (int i = 0; i < cols; i++) {
            header[i] = md.getColumnLabel(i + 1);
        }
        rows.add(header);
        while (rs.next()) {
            String[] row = new String[cols];
            for (int i = 0; i < cols; i++) {
                row[i] = rs.getString(i + 1);
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * Full patient extract for the insurance reconciliation team.
     */
    private void exportPatientsCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=patients.csv");
        PrintWriter out = response.getWriter();
        out.println("MRN,First Name,Last Name,DOB,Gender,Phone,Email,SSN,Insurance Provider,Insurance Number");

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM patients WHERE active = 1 ORDER BY mrn");
            while (rs.next()) {
                out.println(rs.getString("mrn") + "," + rs.getString("first_name") + ","
                        + rs.getString("last_name") + "," + rs.getString("date_of_birth") + ","
                        + rs.getString("gender") + "," + rs.getString("phone") + ","
                        + rs.getString("email") + "," + rs.getString("ssn") + ","
                        + rs.getString("insurance_provider") + "," + rs.getString("insurance_number"));
            }
        } catch (SQLException e) {
            log.error("Patient export failed", e);
            out.println("ERROR: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        out.flush();
    }
}
