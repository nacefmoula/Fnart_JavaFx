package tn.esprit.services;

import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class PasswordResetService {
    private Connection connection;
    private EmailService emailService;

    public PasswordResetService() {
        connection = MyDataBase.getInstance().getCnx();
        emailService = new EmailService();
    }

    public void initiatePasswordReset(String email) throws SQLException {
        String findUserQuery = "SELECT id FROM user WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(findUserQuery)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String token = generateToken();
                LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

                // Update user with reset token and expiration
                String updateTokenQuery = "UPDATE user SET reset_token = ?, reset_token_expiration = ? WHERE id = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateTokenQuery)) {
                    updateStmt.setString(1, token);
                    updateStmt.setTimestamp(2, Timestamp.valueOf(expiryDate));
                    updateStmt.setInt(3, rs.getInt("id"));
                    updateStmt.executeUpdate();
                }

                // Send reset email
                EmailService.sendPasswordResetEmail(email, token);
            }
        }
    }

    public boolean validateToken(String token) throws SQLException {
        String query = "SELECT reset_token_expiration FROM user WHERE reset_token = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Timestamp expiryDate = rs.getTimestamp("reset_token_expiration");
                return expiryDate != null && !LocalDateTime.now().isAfter(expiryDate.toLocalDateTime());
            }
        }
        return false;
    }

    public boolean resetPassword(String token, String newPassword) throws SQLException {
        if (!validateToken(token)) {
            return false;
        }

        connection.setAutoCommit(false);
        try {
            // Update password and clear reset token
            String updateQuery = "UPDATE user SET password = ?, reset_token = NULL, reset_token_expiration = NULL WHERE reset_token = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
                pstmt.setString(1, newPassword);
                pstmt.setString(2, token);
                int rowsAffected = pstmt.executeUpdate();

                connection.commit();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}