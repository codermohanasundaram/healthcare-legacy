package com.healthcare.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.healthcare.model.Appointment;
import com.healthcare.util.DBConnection;
import com.healthcare.util.DateUtil;

public class AppointmentDAO {

    private static Logger log = Logger.getLogger(AppointmentDAO.class);

    private static final String BASE_SELECT =
            "SELECT a.*, CONCAT(p.first_name, ' ', p.last_name) AS patient_name, p.mrn AS patient_mrn, "
            + "CONCAT('Dr. ', d.first_name, ' ', d.last_name) AS doctor_name "
            + "FROM appointments a "
            + "LEFT JOIN patients p ON a.patient_id = p.id "
            + "LEFT JOIN doctors d ON a.doctor_id = d.id ";

    public Appointment getAppointmentById(int id) {
        List<Appointment> list = query(BASE_SELECT + "WHERE a.id = " + id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Appointment> getAllAppointments() {
        return query(BASE_SELECT + "ORDER BY a.appointment_date DESC, a.appointment_time");
    }

    public List<Appointment> getAppointmentsByDate(Date date) {
        return query(BASE_SELECT + "WHERE a.appointment_date = '" + DateUtil.formatDate(date)
                + "' ORDER BY a.appointment_time");
    }

    public List<Appointment> getAppointmentsByPatient(int patientId) {
        return query(BASE_SELECT + "WHERE a.patient_id = " + patientId
                + " ORDER BY a.appointment_date DESC, a.appointment_time");
    }

    public List<Appointment> getAppointmentsByDoctor(int doctorId) {
        return query(BASE_SELECT + "WHERE a.doctor_id = " + doctorId
                + " ORDER BY a.appointment_date DESC, a.appointment_time");
    }

    /**
     * Checks whether the doctor already has a (non cancelled) appointment in the slot.
     */
    public boolean isSlotAvailable(int doctorId, Date date, String time) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT COUNT(*) FROM appointments WHERE doctor_id = ? "
                    + "AND appointment_date = ? AND appointment_time = ? AND status <> 'CANCELLED'");
            ps.setInt(1, doctorId);
            ps.setDate(2, DateUtil.toSqlDate(date));
            ps.setString(3, time);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            log.error("Error checking slot", e);
        } finally {
            DBConnection.close(conn, ps, rs);
        }
        return false;
    }

    public int bookAppointment(Appointment a) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int id = -1;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("INSERT INTO appointments (patient_id, doctor_id, appointment_date, "
                    + "appointment_time, reason, status, notes, created_date) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, a.getPatientId());
            ps.setInt(2, a.getDoctorId());
            ps.setDate(3, DateUtil.toSqlDate(a.getAppointmentDate()));
            ps.setString(4, a.getAppointmentTime());
            ps.setString(5, a.getReason());
            ps.setString(6, "SCHEDULED");
            ps.setString(7, a.getNotes());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) id = rs.getInt(1);
        } catch (SQLException e) {
            log.error("Error booking appointment", e);
        } finally {
            DBConnection.close(conn, ps, rs);
        }
        return id;
    }

    public boolean updateStatus(int id, String status) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            return stmt.executeUpdate("UPDATE appointments SET status = '" + status + "' WHERE id = " + id) > 0;
        } catch (SQLException e) {
            log.error("Error updating appointment status", e);
            return false;
        } finally {
            DBConnection.close(conn, stmt);
        }
    }

    public int getTodayAppointmentCount() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM appointments WHERE appointment_date = CURDATE() "
                    + "AND status <> 'CANCELLED'");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return 0;
    }

    private List<Appointment> query(String sql) {
        List<Appointment> list = new ArrayList<Appointment>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Appointment a = new Appointment();
                a.setId(rs.getInt("id"));
                a.setPatientId(rs.getInt("patient_id"));
                a.setDoctorId(rs.getInt("doctor_id"));
                a.setAppointmentDate(rs.getDate("appointment_date"));
                a.setAppointmentTime(rs.getString("appointment_time"));
                a.setReason(rs.getString("reason"));
                a.setStatus(rs.getString("status"));
                a.setNotes(rs.getString("notes"));
                a.setCreatedDate(rs.getTimestamp("created_date"));
                a.setPatientName(rs.getString("patient_name"));
                a.setPatientMrn(rs.getString("patient_mrn"));
                a.setDoctorName(rs.getString("doctor_name"));
                list.add(a);
            }
        } catch (SQLException e) {
            log.error("Error running appointment query: " + sql, e);
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return list;
    }
}
