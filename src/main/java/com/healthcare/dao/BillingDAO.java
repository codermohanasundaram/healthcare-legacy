package com.healthcare.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.healthcare.model.Bill;
import com.healthcare.util.DBConnection;

public class BillingDAO {

    private static Logger log = Logger.getLogger(BillingDAO.class);

    private static final String BASE_SELECT =
            "SELECT b.*, CONCAT(p.first_name, ' ', p.last_name) AS patient_name, p.mrn AS patient_mrn "
            + "FROM bills b LEFT JOIN patients p ON b.patient_id = p.id ";

    public List<Bill> getAllBills(String status) {
        String sql = BASE_SELECT;
        if (status != null && !status.equals("") && !status.equals("ALL")) {
            sql += "WHERE b.status = '" + status + "' ";
        }
        sql += "ORDER BY b.bill_date DESC, b.id DESC";
        return query(sql);
    }

    public Bill getBillById(int id) {
        List<Bill> list = query(BASE_SELECT + "WHERE b.id = " + id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Bill getBillByAppointment(int appointmentId) {
        List<Bill> list = query(BASE_SELECT + "WHERE b.appointment_id = " + appointmentId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Bill> getBillsByPatient(int patientId) {
        return query(BASE_SELECT + "WHERE b.patient_id = " + patientId + " ORDER BY b.bill_date DESC");
    }

    public int createBill(Bill b) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int id = -1;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("INSERT INTO bills (patient_id, appointment_id, bill_date, consultation_fee, "
                    + "medicine_charges, lab_charges, discount, tax, total_amount, status) "
                    + "VALUES (?, ?, CURDATE(), ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, b.getPatientId());
            if (b.getAppointmentId() != null) {
                ps.setInt(2, b.getAppointmentId().intValue());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setDouble(3, b.getConsultationFee());
            ps.setDouble(4, b.getMedicineCharges());
            ps.setDouble(5, b.getLabCharges());
            ps.setDouble(6, b.getDiscount());
            ps.setDouble(7, b.getTax());
            ps.setDouble(8, b.getTotalAmount());
            ps.setString(9, b.getStatus());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) id = rs.getInt(1);
        } catch (SQLException e) {
            log.error("Error creating bill", e);
        } finally {
            DBConnection.close(conn, ps, rs);
        }
        return id;
    }

    public boolean markPaid(int billId, String paymentMethod) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("UPDATE bills SET status = 'PAID', payment_method = ?, paid_date = NOW() "
                    + "WHERE id = ?");
            ps.setString(1, paymentMethod);
            ps.setInt(2, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error marking bill paid " + billId, e);
            return false;
        } finally {
            DBConnection.close(conn, ps);
        }
    }

    private List<Bill> query(String sql) {
        List<Bill> list = new ArrayList<Bill>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Bill b = new Bill();
                b.setId(rs.getInt("id"));
                b.setPatientId(rs.getInt("patient_id"));
                int apptId = rs.getInt("appointment_id");
                if (!rs.wasNull()) b.setAppointmentId(new Integer(apptId));
                b.setBillDate(rs.getDate("bill_date"));
                b.setConsultationFee(rs.getDouble("consultation_fee"));
                b.setMedicineCharges(rs.getDouble("medicine_charges"));
                b.setLabCharges(rs.getDouble("lab_charges"));
                b.setDiscount(rs.getDouble("discount"));
                b.setTax(rs.getDouble("tax"));
                b.setTotalAmount(rs.getDouble("total_amount"));
                b.setStatus(rs.getString("status"));
                b.setPaymentMethod(rs.getString("payment_method"));
                b.setPaidDate(rs.getTimestamp("paid_date"));
                b.setPatientName(rs.getString("patient_name"));
                b.setPatientMrn(rs.getString("patient_mrn"));
                list.add(b);
            }
        } catch (SQLException e) {
            log.error("Error running bill query: " + sql, e);
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return list;
    }
}
