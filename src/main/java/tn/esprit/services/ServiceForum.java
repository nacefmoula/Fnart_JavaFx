package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Forum;
import tn.esprit.models.User;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServiceForum implements IService<Forum> {

    private Connection cnx;

    public ServiceForum() {
        this.cnx = MyDataBase.getInstance().getCnx(); // Initialize connection
    }

    @Override
    public void add(Forum forum) {
        try {
            ajouter(forum);
        } catch (SQLException e) {
            System.err.println("Error adding forum: " + e.getMessage());
        }
    }

    @Override
    public boolean ajouter(Forum forum) throws SQLException {
        String query = "INSERT INTO forum (date_f, titre_f, id_user_id, categorie_f, description_f, image_f, rating) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setDate(1, new java.sql.Date(forum.getDate_f().getTime()));
            ps.setString(2, forum.getTitre_f());
            ps.setInt(3, forum.getUser() != null ? forum.getUser().getId() : 1); // Default user ID if null
            ps.setString(4, forum.getCategorie_f());
            ps.setString(5, forum.getDescription_f());
            ps.setString(6, forum.getImage_f());
            ps.setFloat(7, forum.getRating());

            int rowsAffected = ps.executeUpdate();
            System.out.println("Forum added successfully!");
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new SQLException("Error adding forum: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Forum forum) throws SQLException {
        modifier(forum);
    }

    @Override
    public void modifier(Forum forum) throws SQLException {
        String query = "UPDATE forum SET date_f = ?, titre_f = ?, id_user_id = ?, categorie_f = ?, description_f = ?, image_f = ?, rating = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setDate(1, new java.sql.Date(forum.getDate_f().getTime()));
            ps.setString(2, forum.getTitre_f());
            ps.setInt(3, forum.getUser() != null ? forum.getUser().getId() : 1); // Default user ID if null
            ps.setString(4, forum.getCategorie_f());
            ps.setString(5, forum.getDescription_f());
            ps.setString(6, forum.getImage_f());
            ps.setFloat(7, forum.getRating());
            ps.setInt(8, forum.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Forum updated successfully!");
            } else {
                System.out.println("No forum found with ID: " + forum.getId());
            }
        } catch (SQLException e) {
            throw new SQLException("Error updating forum: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Forum forum) {
        try {
            supprimer(forum.getId());
        } catch (SQLException e) {
            System.err.println("Error deleting forum: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(Forum forum) throws SQLException {
        supprimer(forum.getId());
    }

    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM forum WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Forum deleted successfully!");
            } else {
                System.out.println("No forum found with ID: " + id);
            }
        } catch (SQLException e) {
            throw new SQLException("Error deleting forum with ID " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Forum> getAll() {
        return (List<Forum>) getall();
    }

    @Override
    public List<Forum> getall() {
        Set<Forum> forums = new HashSet<>();
        String query = "SELECT * FROM forum";
        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Forum forum = new Forum(
                        rs.getInt("id"),
                        rs.getDate("date_f"),
                        rs.getString("titre_f"),
                        getUserById(rs.getInt("id_user_id")),
                        rs.getString("categorie_f"),
                        rs.getString("description_f"),
                        rs.getString("image_f"),
                        rs.getFloat("rating")
                );
                forums.add(forum);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching forums: " + e.getMessage());
        }
        return new ArrayList<>(forums); // Convert HashSet to ArrayList
    }

    @Override
    public Forum getone() {
        String query = "SELECT * FROM forum LIMIT 1";
        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return new Forum(
                        rs.getInt("id"),
                        rs.getDate("date_f"),
                        rs.getString("titre_f"),
                        getUserById(rs.getInt("id_user_id")),
                        rs.getString("categorie_f"),
                        rs.getString("description_f"),
                        rs.getString("image_f"),
                        rs.getFloat("rating")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching a single forum: " + e.getMessage());
        }
        return null;
    }

    public Forum getOneByTitle(String title) {
        String query = "SELECT * FROM forum WHERE titre_f = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Forum(
                        rs.getInt("id"),
                        rs.getDate("date_f"),
                        rs.getString("titre_f"),
                        getUserById(rs.getInt("id_user_id")),
                        rs.getString("categorie_f"),
                        rs.getString("description_f"),
                        rs.getString("image_f"),
                        rs.getFloat("rating")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching forum by title: " + e.getMessage());
        }
        return null;
    }

    public Set<String> getAllTitles() {
        Set<String> titles = new HashSet<>();
        String query = "SELECT titre_f FROM forum";
        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                titles.add(rs.getString("titre_f"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching forum titles: " + e.getMessage());
        }
        return titles;
    }

    public void updateRating(int forumId, float newRating) throws SQLException {
        String query = "UPDATE forum SET rating = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setFloat(1, newRating);
            ps.setInt(2, forumId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Rating updated for forum ID: " + forumId);
            } else {
                System.out.println("No forum found with ID: " + forumId);
            }
        } catch (SQLException e) {
            throw new SQLException("Error updating rating for forum ID " + forumId + ": " + e.getMessage(), e);
        }
    }

    private User getUserById(int userId) {
        String query = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
        }
        return new User(1); // Default user if not found
    }
}