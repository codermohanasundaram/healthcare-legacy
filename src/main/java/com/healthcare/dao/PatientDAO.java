package com.healthcare.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.healthcare.model.Patient;
import com.healthcare.util.DBConnection;
import com.healthcare.util.DateUtil;

/**
 * Data access for the PATIENTS table.
 */
public class PatientDAO {

    private static Logger log = Logger.getLogger(PatientDAO.class);

    public Patient getPatientById(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Patient patient = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT * FROM patients WHERE id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                patient = mapPatient(rs);
            }
        } catch (SQLException e) {
            log.error("Error loading patient id=" + id, e);
        } finally {
            DBConnection.close(conn, ps, rs);
        }
        return patient;
    }

    public Patient getPatientByMrn(String mrn) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Patient patient = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT * FROM patients WHERE mrn = ?");
            ps.setString(1, mrn);
            rs = ps.executeQuery();
            if (rs.next()) {
                patient = mapPatient(rs);
            }
        } catch (SQLException e) {
            log.error("Error loading patient mrn=" + mrn, e);
        } finally {
            DBConnection.close(conn, ps, rs);
        }
        return patient;
    }

    /**
     * Search by any combination of name, phone and MRN.
     */
    public List<Patient> searchPatients(String name, String phone, String mrn) {
        List<Patient> list = new ArrayList<Patient>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        StringBuffer sql = new StringBuffer("SELECT * FROM patients WHERE active = 1");
        if (name != null && !name.trim().equals("")) {
            sql.append(" AND (first_name LIKE '%" + name + "%' OR last_name LIKE '%" + name + "%')");
        }
        if (phone != null && !phone.trim().equals("")) {
            sql.append(" AND phone = '" + phone + "'");
        }
        if (mrn != null && !mrn.trim().equals("")) {
            sql.append(" AND mrn = '" + mrn + "'");
        }
        sql.append(" ORDER BY last_name, first_name");

        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            log.debug("Patient search SQL: " + sql);
            rs = stmt.executeQuery(sql.toString());
            while (rs.next()) {
                list.add(mapPatient(rs));
            }
        } catch (SQLException e) {
            log.error("Error searching patients", e);
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return list;
    }

    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<Patient>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM patients WHERE active = 1 ORDER BY last_name, first_name");
            while (rs.next()) {
                list.add(mapPatient(rs));
            }
        } catch (SQLException e) {
            log.error("Error loading patients", e);
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return list;
    }

    /**
     * Inserts the patient and returns the generated id (or -1 on failure).
     */
    public int addPatient(Patient p) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int newId = -1;
        String sql = "INSERT INTO patients (mrn, first_name, last_name, date_of_birth, gender, phone, email, "
                + "address, city, state, zip_code, ssn, insurance_provider, insurance_number, blood_group, "
                + "emergency_contact_name, emergency_contact_phone, created_date, created_by, active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, 1)";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getMrn());
            ps.setString(2, p.getFirstName());
            ps.setString(3, p.getLastName());
            ps.setDate(4, DateUtil.toSqlDate(p.getDateOfBirth()));
            ps.setString(5, p.getGender());
            ps.setString(6, p.getPhone());
            ps.setString(7, p.getEmail());
            ps.setString(8, p.getAddress());
            ps.setString(9, p.getCity());
            ps.setString(10, p.getState());
            ps.setString(11, p.getZipCode());
            ps.setString(12, p.getSsn());
            ps.setString(13, p.getInsuranceProvider());
            ps.setString(14, p.getInsuranceNumber());
            ps.setString(15, p.getBloodGroup());
            ps.setString(16, p.getEmergencyContactName());
            ps.setString(17, p.getEmergencyContactPhone());
            ps.setString(18, p.getCreatedBy());
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                newId = rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Error inserting patient " + p, e);
        } finally {
            DBConnection.close(conn, ps, rs);
        }
        return newId;
    }

    public boolean updatePatient(Patient p) {
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, date_of_birth = ?, gender = ?, "
                + "phone = ?, email = ?, address = ?, city = ?, state = ?, zip_code = ?, ssn = ?, "
                + "insurance_provider = ?, insurance_number = ?, blood_group = ?, "
                + "emergency_contact_name = ?, emergency_contact_phone = ? WHERE id = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, p.getFirstName());
            ps.setString(2, p.getLastName());
            ps.setDate(3, DateUtil.toSqlDate(p.getDateOfBirth()));
            ps.setString(4, p.getGender());
            ps.setString(5, p.getPhone());
            ps.setString(6, p.getEmail());
            ps.setString(7, p.getAddress());
            ps.setString(8, p.getCity());
            ps.setString(9, p.getState());
            ps.setString(10, p.getZipCode());
            ps.setString(11, p.getSsn());
            ps.setString(12, p.getInsuranceProvider());
            ps.setString(13, p.getInsuranceNumber());
            ps.setString(14, p.getBloodGroup());
            ps.setString(15, p.getEmergencyContactName());
            ps.setString(16, p.getEmergencyContactPhone());
            ps.setInt(17, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error updating patient " + p, e);
            return false;
        } finally {
            DBConnection.close(conn, ps);
        }
    }

    /** Soft delete - records must be retained for audit. */
    public boolean deletePatient(int id) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            return stmt.executeUpdate("UPDATE patients SET active = 0 WHERE id = " + id) > 0;
        } catch (SQLException e) {
            log.error("Error deleting patient id=" + id, e);
            return false;
        } finally {
            DBConnection.close(conn, stmt);
        }
    }

    public int getPatientCount() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM patients WHERE active = 1");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return 0;
    }

    private Patient mapPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getInt("id"));
        p.setMrn(rs.getString("mrn"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        p.setDateOfBirth(rs.getDate("date_of_birth"));
        p.setGender(rs.getString("gender"));
        p.setPhone(rs.getString("phone"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        p.setCity(rs.getString("city"));
        p.setState(rs.getString("state"));
        p.setZipCode(rs.getString("zip_code"));
        p.setSsn(rs.getString("ssn"));
        p.setInsuranceProvider(rs.getString("insurance_provider"));
        p.setInsuranceNumber(rs.getString("insurance_number"));
        p.setBloodGroup(rs.getString("blood_group"));
        p.setEmergencyContactName(rs.getString("emergency_contact_name"));
        p.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
        p.setCreatedDate(rs.getTimestamp("created_date"));
        p.setCreatedBy(rs.getString("created_by"));
        p.setActive(rs.getBoolean("active"));
        return p;
    }
}
