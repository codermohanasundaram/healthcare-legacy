package com.healthcare.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.healthcare.model.MedicalRecord;
import com.healthcare.util.DBConnection;
import com.healthcare.util.DateUtil;

public class MedicalRecordDAO {

    public List<MedicalRecord> getRecordsByPatient(int patientId) {
        List<MedicalRecord> list = new ArrayList<MedicalRecord>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT m.*, CONCAT('Dr. ', d.first_name, ' ', d.last_name) AS doctor_name "
                    + "FROM medical_records m LEFT JOIN doctors d ON m.doctor_id = d.id "
                    + "WHERE m.patient_id = ? ORDER BY m.visit_date DESC");
            ps.setInt(1, patientId);
            rs = ps.executeQuery();
            while (rs.next()) {
                MedicalRecord r = new MedicalRecord();
                r.setId(rs.getInt("id"));
                r.setPatientId(rs.getInt("patient_id"));
                r.setDoctorId(rs.getInt("doctor_id"));
                int apptId = rs.getInt("appointment_id");
                if (!rs.wasNull()) r.setAppointmentId(new Integer(apptId));
                r.setVisitDate(rs.getDate("visit_date"));
                r.setDiagnosis(rs.getString("diagnosis"));
                r.setPrescription(rs.getString("prescription"));
                r.setNotes(rs.getString("notes"));
                r.setBloodPressure(rs.getString("blood_pressure"));
                r.setTemperature(rs.getDouble("temperature"));
                r.setWeight(rs.getDouble("weight"));
                r.setCreatedDate(rs.getTimestamp("created_date"));
                r.setDoctorName(rs.getString("doctor_name"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(conn, ps, rs);
        }
        return list;
    }

    public boolean addRecord(MedicalRecord r) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("INSERT INTO medical_records (patient_id, doctor_id, appointment_id, "
                    + "visit_date, diagnosis, prescription, notes, blood_pressure, temperature, weight, created_date) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())");
            ps.setInt(1, r.getPatientId());
            ps.setInt(2, r.getDoctorId());
            if (r.getAppointmentId() != null) {
                ps.setInt(3, r.getAppointmentId().intValue());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setDate(4, DateUtil.toSqlDate(r.getVisitDate()));
            ps.setString(5, r.getDiagnosis());
            ps.setString(6, r.getPrescription());
            ps.setString(7, r.getNotes());
            ps.setString(8, r.getBloodPressure());
            ps.setDouble(9, r.getTemperature());
            ps.setDouble(10, r.getWeight());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnection.close(conn, ps);
        }
    }
}
