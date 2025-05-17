package tn.esprit.services;

import tn.esprit.models.CommantairesF;
import tn.esprit.models.User;
import tn.esprit.models.Forum;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCommentaire_f {

    private Connection conn;

    public ServiceCommentaire_f() {
        this.conn = MyDataBase.getInstance().getCnx();
    }

    // Add a Comment
    public void add(CommantairesF commentaire) {
        String query = "INSERT INTO commentaire_f (id_user_id, id_forum_id, date_c, texte_c) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, commentaire.getUser().getId());
            pst.setInt(2, commentaire.getForum().getId());
            pst.setDate(3, java.sql.Date.valueOf(commentaire.getDate_c()));
            pst.setString(4, commentaire.getTexte_c());

            pst.executeUpdate();
            System.out.println("Comment added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding comment: " + e.getMessage());
        }
    }

    // Modify a Comment
    public void modifier(CommantairesF commentaire) {
        String query = "UPDATE commentaire_f SET texte_c = ?, date_c = ? WHERE id = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, commentaire.getTexte_c());
            pst.setDate(2, java.sql.Date.valueOf(commentaire.getDate_c()));
            pst.setInt(3, commentaire.getId());

            pst.executeUpdate();
            System.out.println("Comment modified successfully!");
        } catch (SQLException e) {
            System.err.println("Error modifying comment: " + e.getMessage());
        }
    }

    // Delete a Comment
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM commentaire_f WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Comment deleted successfully!");
        } catch (SQLException e) {
            throw new SQLException("Error deleting comment with id " + id + ": " + e.getMessage(), e);
        }
    }

    // Retrieve all Comments
    public List<CommantairesF> afficher() {
        List<CommantairesF> commentaires = new ArrayList<>();
        String query = "SELECT * FROM commentaire_f";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                CommantairesF commentaire = new CommantairesF(
                        rs.getInt("id"),
                        getUserById(rs.getInt("id_user_id")),
                        getForumById(rs.getInt("id_forum_id")),
                        rs.getDate("date_c").toLocalDate(),
                        rs.getString("texte_c")
                );
                commentaires.add(commentaire);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching comments: " + e.getMessage());
        }
        return commentaires;
    }

    // Retrieve all Comments using getAll()
    public List<CommantairesF> getAll() {
        return afficher();
    }

    // Retrieve Comments by Forum ID
    public List<CommantairesF> getCommentairesByForumId(int forumId) {
        List<CommantairesF> commentaires = new ArrayList<>();
        String query = "SELECT * FROM commentaire_f WHERE id_forum_id = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, forumId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                CommantairesF commentaire = new CommantairesF(
                        rs.getInt("id"),
                        getUserById(rs.getInt("id_user_id")),
                        getForumById(rs.getInt("id_forum_id")),
                        rs.getDate("date_c").toLocalDate(),
                        rs.getString("texte_c")
                );
                commentaires.add(commentaire);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching comments by forum ID: " + e.getMessage());
        }
        return commentaires;
    }

    // Helper method to get User by ID
    private User getUserById(int userId) {
        User user = null;
        String query = "SELECT * FROM user WHERE id = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                user = new User(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return user;
    }

    // Helper method to get Forum by ID
    private Forum getForumById(int forumId) {
        Forum forum = null;
        String query = "SELECT * FROM forum WHERE id = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, forumId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                forum = new Forum(rs.getInt("id"), rs.getString("titre_f"), rs.getString("description_f"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching forum: " + e.getMessage());
        }
        return forum;
    }
}