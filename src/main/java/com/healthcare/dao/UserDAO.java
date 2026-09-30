package com.healthcare.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.healthcare.model.User;
import com.healthcare.util.DBConnection;
import com.healthcare.util.PasswordUtil;

public class UserDAO {

    private static Logger log = Logger.getLogger(UserDAO.class);

    /**
     * Returns the user if the credentials are valid, otherwise null.
     */
    public User authenticate(String username, String password) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        User user = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();

            String hashed = PasswordUtil.md5(password);
            // TODO: some accounts created before v1.4 still have plain text
            // passwords, so check both until they are all migrated
            String sql = "SELECT * FROM users WHERE username = '" + username + "'"
                    + " AND (password = '" + hashed + "' OR password = '" + password + "')"
                    + " AND active = 1";
            log.debug("Auth SQL: " + sql);

            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setFullName(rs.getString("full_name"));
                int doctorId = rs.getInt("doctor_id");
                if (!rs.wasNull()) {
                    user.setDoctorId(new Integer(doctorId));
                }
                user.setActive(rs.getBoolean("active"));
                user.setLastLogin(rs.getTimestamp("last_login"));
            }
        } catch (SQLException e) {
            log.error("Error authenticating user " + username, e);
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return user;
    }

    public void updateLastLogin(int userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("UPDATE users SET last_login = NOW() WHERE id = ?");
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(conn, ps);
        }
    }
}
