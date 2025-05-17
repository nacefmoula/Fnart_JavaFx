package tn.esprit.services;
import tn.esprit.enumerations.Role;
import tn.esprit.interfaces.IService;
import tn.esprit.models.User;
import tn.esprit.utils.MyDataBase;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.utils.SessionManager;
import tn.esprit.services.EmailService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserService implements IService<User> {
    private Connection cnx;
    private EmailService emailService;

    public UserService() {
        try {
            cnx = MyDataBase.getInstance().getCnx();
            if (cnx == null) {
                System.err.println("Failed to obtain database connection in UserService constructor");
            }
        } catch (Exception e) {
            System.err.println("Error initializing UserService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean signUp(User user) {
        String checkEmailQuery = "SELECT COUNT(*) FROM user WHERE email = ?";
        String insertQuery = "INSERT INTO user (nom, email, password, roles, phone, gender, image, status, dateofbirth) VALUES (?,?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            // Check if email already exists
            PreparedStatement checkStmt = cnx.prepareStatement(checkEmailQuery);
            checkStmt.setString(1, user.getEmail());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Email already exists");
                return false;
            }

            // Hash the password
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

            // Format the role for database insertion
            String roleString = "[\"" + user.getRole().toString() + "\"]";

            // Set status based on role
            String status = "ACTIVE";
            if (user.getRole() == Role.ADMIN || user.getRole() == Role.THERAPIST || user.getRole() == Role.ARTIST) {
                status = "PENDING";
            }

            // Insert the new user
            PreparedStatement insertStmt = cnx.prepareStatement(insertQuery);
            insertStmt.setString(1, user.getNom());
            insertStmt.setString(2, user.getEmail());
            insertStmt.setString(3, hashedPassword);
            insertStmt.setString(4, roleString);
            insertStmt.setString(5, user.getPhone());
            insertStmt.setString(6, user.getGender());
            insertStmt.setString(7, user.getImagePath());
            insertStmt.setString(8, status);
            insertStmt.setDate(9, user.getDateOfBirth() != null ? new java.sql.Date(user.getDateOfBirth().getTime()) : null);

            int rowsAffected = insertStmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error during signup: " + e.getMessage());
            return false;
        }
    }

    public User login(String email, String password) {
        ensureConnection();
        String query = "SELECT * FROM user WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Verify the password against the hashed version
                String storedHash = resultSet.getString("password");
                if (BCrypt.checkpw(password, storedHash)) {
                    String status = resultSet.getString("status");
                    if (!"ACTIVE".equals(status)) {
                        System.out.println("Account is not active");
                        return null;
                    }

                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setNom(resultSet.getString("nom"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPassword(storedHash);
                    user.setPhone(resultSet.getString("phone"));
                    user.setGender(resultSet.getString("gender"));
                    user.setStatus(status);

                    // Parse the role
                    String rolesStr = resultSet.getString("roles");
                    if (rolesStr != null && !rolesStr.isEmpty()) {
                        String roleStr = rolesStr.replace("[", "").replace("]", "").replace("\"", "");
                        user.setRole(convertToRole(roleStr));
                    }

                    // Store the current user in the session
                    SessionManager.setCurrentUser(user);

                    return user;
                } else {
                    System.out.println("Password does not match");
                }
            } else {
                System.out.println("Email not found");
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void ensureConnection() {
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
                if (cnx == null) {
                    throw new SQLException("Failed to obtain database connection");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error establishing database connection: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database connection error", e);
        }
    }


    public boolean resetPassword(String verificationCode, String newPassword) {
        // First verify the code is valid and not expired
        String verifyQuery = "SELECT email FROM user WHERE reset_token = ? AND reset_token_expiration > NOW()";
        try {
            String userEmail = null;

            // Verify code and get user email
            try (PreparedStatement verifyStmt = cnx.prepareStatement(verifyQuery)) {
                verifyStmt.setString(1, verificationCode);
                ResultSet rs = verifyStmt.executeQuery();
                if (rs.next()) {
                    userEmail = rs.getString("email");
                } else {
                    return false; // Code invalid or expired
                }
            }

            // Update password and clear code
            String updateQuery = "UPDATE user SET password = ?, reset_token = NULL, reset_token_expiration = NULL WHERE reset_token = ?";
            try (PreparedStatement updateStmt = cnx.prepareStatement(updateQuery)) {
                // Hash the new password
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

                updateStmt.setString(1, hashedPassword);
                updateStmt.setString(2, verificationCode);

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    // Send confirmation email
                    emailService.sendPasswordChangedConfirmation(userEmail);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean resetPasswordByEmail(String email, String newPassword) {
        try {
            // Update password query
            String updateQuery = "UPDATE user SET password = ? WHERE email = ?";
            try (PreparedStatement updateStmt = cnx.prepareStatement(updateQuery)) {
                // Hash the new password
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

                updateStmt.setString(1, hashedPassword);
                updateStmt.setString(2, email);

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    // Send confirmation email
                    emailService.sendPasswordChangedConfirmation(email);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    //Gets the currently logged-in user
    public User getCurrentAdmin() {
        // Simply retrieve the current user from our session manager
        return SessionManager.getCurrentUser();
    }

    @Override
    public void add(User user) {
        String requete = "INSERT INTO user (nom, email, password, roles, phone, gender) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            // Format the role for database insertion
            String roleString = "[\"" + user.getRole().toString() + "\"]";

            PreparedStatement statement = cnx.prepareStatement(requete);
            statement.setString(1, user.getNom());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, roleString);
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getGender());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String requete = "SELECT * FROM user WHERE status = 'ACTIVE'"; // Only active users
        try {
            Statement statement = cnx.createStatement();
            ResultSet resultSet = statement.executeQuery(requete);
            while (resultSet.next()) {
                users.add(createUserFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return users;
    }


    @Override
    public void update(User user) {
        String checkQuery = "SELECT password FROM user WHERE id = ?";
        String updateQuery = "UPDATE user SET nom=?, email=?, password=?, roles=?, phone=?, gender=?, status=? WHERE id=?";

        try {
            // Format the role for database insertion
            String roleString = "[\"" + user.getRole().toString() + "\"]";

            // Check if the password is already hashed by retrieving the current password
            String currentPassword = null;
            boolean passwordChanged = false;

            try (PreparedStatement checkStmt = cnx.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, user.getId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    currentPassword = rs.getString("password");
                    // If the password in user object is different from the current hashed password
                    // and doesn't start with "$2a$" (BCrypt prefix), then it's a new plaintext password
                    if (!user.getPassword().equals(currentPassword) && !user.getPassword().startsWith("$2a$")) {
                        passwordChanged = true;
                    }
                }
            }

            // Prepare the update statement
            PreparedStatement statement = cnx.prepareStatement(updateQuery);
            statement.setString(1, user.getNom());
            statement.setString(2, user.getEmail());

            // If password changed, hash it; otherwise use the existing hash
            if (passwordChanged) {
                statement.setString(3, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            } else {
                statement.setString(3, user.getPassword());
            }

            statement.setString(4, roleString);
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getGender());
            statement.setString(7, user.getStatus());
            statement.setInt(8, user.getId());

            statement.executeUpdate();
            System.out.println("User updated successfully");
        } catch (SQLException e) {
            System.out.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(User user) {
        String requete = "DELETE FROM user WHERE id=?";
        try {
            PreparedStatement statement = cnx.prepareStatement(requete);
            statement.setInt(1, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public boolean ajouter(User user) throws SQLException {
        String requete = "INSERT INTO user (nom, email, password, roles, phone, gender) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            // Format the role for database insertion
            String roleString = "[\"" + user.getRole().toString() + "\"]";

            PreparedStatement statement = cnx.prepareStatement(requete);
            statement.setString(1, user.getNom());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, roleString);
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getGender());
            int rowsAffected = statement.executeUpdate();

            // Return true if at least one row was affected (user was added)
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false; // Return false if there was an exception during the operation
        }
    }
    @Override
    public void modifier(User user) throws SQLException {
        String checkQuery = "SELECT password FROM user WHERE id = ?";
        String updateQuery = "UPDATE user SET nom=?, email=?, password=?, roles=?, phone=?, gender=?, status=? WHERE id=?";

        try {
            // Format the role for database insertion
            String roleString = "[\"" + user.getRole().toString() + "\"]";

            // Check if the password is already hashed by retrieving the current password
            String currentPassword = null;
            boolean passwordChanged = false;

            try (PreparedStatement checkStmt = cnx.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, user.getId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    currentPassword = rs.getString("password");
                    // If the password in user object is different from the current hashed password
                    // and doesn't start with "$2a$" (BCrypt prefix), then it's a new plaintext password
                    if (!user.getPassword().equals(currentPassword) && !user.getPassword().startsWith("$2a$")) {
                        passwordChanged = true;
                    }
                }
            }

            // Prepare the update statement
            PreparedStatement statement = cnx.prepareStatement(updateQuery);
            statement.setString(1, user.getNom());
            statement.setString(2, user.getEmail());

            // If password changed, hash it; otherwise use the existing hash
            if (passwordChanged) {
                statement.setString(3, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            } else {
                statement.setString(3, user.getPassword());
            }

            statement.setString(4, roleString);
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getGender());
            statement.setString(7, user.getStatus());
            statement.setInt(8, user.getId());

            statement.executeUpdate();
            System.out.println("User updated successfully");
        } catch (SQLException e) {
            System.out.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(User user) throws SQLException {
        String requete = "DELETE FROM user WHERE id=?";
        try {
            PreparedStatement statement = cnx.prepareStatement(requete);
            statement.setInt(1, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<User> getall() {
        return List.of();
    }

    @Override
    public User getone() {
        return null;
    }

    // Helper method to convert database role string to Role enum
    private Role convertToRole(String roleStr) {
        switch (roleStr) {
            case "ROLE_USER":
                return Role.REGULARUSER;
            case "ROLE_ADMIN":
            case "ADMIN":
                return Role.ADMIN;
            case "ROLE_ARTIST":
                return Role.ARTIST;
            case "ROLE_THERAPIST":
                return Role.THERAPIST;
            default:
                return Role.REGULARUSER; // Default role
        }
    }

    public boolean createUser(User user){
        String query = "INSERT INTO user (name, email, password, phone, role, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getNom());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getPhone());

            // If the role is ADMIN, THERAPIST, or ARTIST, set status to PENDING
            if (user.getRole() == Role.ADMIN || user.getRole() == Role.THERAPIST || user.getRole() == Role.ARTIST) {
                statement.setString(5, user.getRole().name());
                statement.setString(6, "PENDING");
            } else {
                statement.setString(5, user.getRole().name());
                statement.setString(6, "ACTIVE");
            }

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<User> getPendingUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE status = 'PENDING'";

        try (Statement statement = cnx.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                users.add(createUserFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean approveUser(int userId) {
        String query = "UPDATE user SET status = 'ACTIVE' WHERE id = ?";

        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean rejectUser(int userId) {
        String query = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteUser(int userId) {
        String query = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<User> searchUsers(String searchTerm) {
        String loweredTerm = searchTerm.toLowerCase();
        return getAll().stream()
                .filter(u -> u.getNom().toLowerCase().contains(loweredTerm) ||
                        u.getEmail().toLowerCase().contains(loweredTerm))
                .toList();
    }
/*
    public List<User> searchUsers(String searchTerm) {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE LOWER(nom) LIKE ? OR LOWER(email) LIKE ?";

        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                users.add(createUserFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
}*/

    public List<User> searchPendingUsers(String searchTerm) {
        String loweredTerm = searchTerm.toLowerCase();
        return getPendingUsers().stream()
                .filter(u -> (u.getNom().toLowerCase().contains(loweredTerm) ||
                        u.getEmail().toLowerCase().contains(loweredTerm)) &&
                        "PENDING".equalsIgnoreCase(u.getStatus()))
                .toList();
    }
    /*
        public List<User> searchPendingUsers(String searchTerm) {
            /*String loweredTerm = searchTerm.toLowerCase();
            return getAll().stream()
                    .filter(u -> "PENDING".equalsIgnoreCase(u.getStatus()))
                    .filter(u -> u.getNom().toLowerCase().contains(loweredTerm) || u.getEmail().toLowerCase().contains(loweredTerm))
                    .toList();
            List<User> users = new ArrayList<>();
            String query = "SELECT * FROM user WHERE status = 'PENDING' AND " +
                    "(LOWER(nom) LIKE ? OR LOWER(email) LIKE ?)";

            try (PreparedStatement statement = cnx.prepareStatement(query)) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                statement.setString(1, searchPattern);
                statement.setString(2, searchPattern);

                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    users.add(createUserFromResultSet(resultSet));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return users;
        }
    */
    public int getUserCountByRole(Role role) {
        String query = "SELECT COUNT(*) FROM user WHERE roles LIKE ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, "%\"" + role.toString() + "\"%");
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalUsersCount() {
        String query = "SELECT COUNT(*) FROM user";

        try (Statement statement = cnx.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getPendingUsersCount() {
        String query = "SELECT COUNT(*) FROM user WHERE status = 'PENDING'";

        try (Statement statement = cnx.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getActiveUsersCount() {
        String query = "SELECT COUNT(*) FROM user WHERE status = 'ACTIVE'";

        try (Statement statement = cnx.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private User createUserFromResultSet(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setNom(resultSet.getString("nom"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        user.setPhone(resultSet.getString("phone"));
        user.setGender(resultSet.getString("gender"));
        user.setStatus(resultSet.getString("status"));

        // Parse the role
        String rolesStr = resultSet.getString("roles");
        if (rolesStr != null && !rolesStr.isEmpty()) {
            String roleStr = rolesStr.replace("[", "").replace("]", "").replace("\"", "");
            user.setRole(convertToRole(roleStr));
        }

        return user;
    }

    public boolean emailExists(String email) {
        try {
            String query = "SELECT COUNT(*) FROM user WHERE email = ?";
            PreparedStatement stmt = cnx.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String generateResetToken(String email) {
        String token = UUID.randomUUID().toString();
        String query = "UPDATE user SET reset_token = ?, reset_token_expiration = DATE_ADD(NOW(), INTERVAL 1 HOUR) WHERE email = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, token);
            stmt.setString(2, email);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Generate the reset link
                String resetLink = "http://localhost:8080/reset-password?token=" + token;
                // Send the reset email
                emailService.sendPasswordResetEmail(email, resetLink);
                return token;
            }
        } catch (SQLException e) {
            System.err.println("Error generating reset token: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean verifyPassword(String email, String password) {
        String query = "SELECT password FROM user WHERE email = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedHash = resultSet.getString("password");
                return BCrypt.checkpw(password, storedHash);
            }
        } catch (SQLException e) {
            System.err.println("Error verifying password: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public void saveResetToken(String email, String token) {
        String query = "UPDATE user SET reset_token = ?, reset_token_expiration = DATE_ADD(NOW(), INTERVAL 1 HOUR) WHERE email = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, token);
            stmt.setString(2, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving reset token: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean verifyResetToken(String email, String token) {
        String query = "SELECT reset_token FROM user WHERE email = ? AND reset_token = ? AND reset_token_expiration > NOW()";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, token);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a matching valid token is found
        } catch (SQLException e) {
            System.err.println("Error verifying reset token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean phoneExists(String phoneNumber) {
        try {
            String query = "SELECT COUNT(*) FROM user WHERE phone = ?";
            PreparedStatement stmt = cnx.prepareStatement(query);
            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void savePhoneResetToken(String phoneNumber, String token) {
        String query = "UPDATE user SET reset_token = ?, reset_token_expiration = DATE_ADD(NOW(), INTERVAL 1 HOUR) WHERE phone = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, token);
            stmt.setString(2, phoneNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving phone reset token: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean verifyPhoneResetToken(String phoneNumber, String token) {
        String query = "SELECT reset_token FROM user WHERE phone = ? AND reset_token = ? AND reset_token_expiration > NOW()";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, phoneNumber);
            stmt.setString(2, token);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a matching valid token is found
        } catch (SQLException e) {
            System.err.println("Error verifying phone reset token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetPasswordByPhone(String phoneNumber, String newPassword) {
        try {
            String updateQuery = "UPDATE user SET password = ?, reset_token = NULL, reset_token_expiration = NULL WHERE phone = ?";
            try (PreparedStatement updateStmt = cnx.prepareStatement(updateQuery)) {
                // Hash the new password
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

                updateStmt.setString(1, hashedPassword);
                updateStmt.setString(2, phoneNumber);

                int rowsAffected = updateStmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error resetting password by phone: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public User findByEmail(String email) {
        ensureConnection();
        String query = "SELECT * FROM user WHERE email = ?";
        try {
            validateConnection(); // Add this line
            PreparedStatement statement = cnx.prepareStatement(query);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return createUserFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean create(User user) {
        String insertQuery = "INSERT INTO user (nom, email, password, roles, phone, gender, image, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            // Hash the password
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

            // Format the role for database insertion
            String roleString = "[\"" + user.getRole().toString() + "\"]";

            // Insert the new user
            PreparedStatement insertStmt = cnx.prepareStatement(insertQuery);
            insertStmt.setString(1, user.getNom());
            insertStmt.setString(2, user.getEmail());
            insertStmt.setString(3, hashedPassword);
            insertStmt.setString(4, roleString);
            insertStmt.setString(5, user.getPhone());
            insertStmt.setString(6, user.getGender());
            insertStmt.setString(7, user.getImagePath());
            insertStmt.setString(8, "ACTIVE");

            int rowsAffected = insertStmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error creating user: " + e.getMessage());
            return false;
        }
    }

    private void validateConnection() throws SQLException {
        if (cnx == null || cnx.isClosed() || !cnx.isValid(2)) {
            cnx = MyDataBase.getInstance().getCnx(); // Re-establish connection
            if (cnx == null) {
                throw new SQLException("Failed to establish database connection");
            }
        }
    }


}
