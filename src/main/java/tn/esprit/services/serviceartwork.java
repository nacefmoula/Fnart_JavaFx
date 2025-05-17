package tn.esprit.services;

import tn.esprit.models.Artwork;
import tn.esprit.utils.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class serviceartwork {
    private Connection connection;

    public serviceartwork() {
        connection = DataSource.getInstance().getConnection();
    }

    public List<Artwork> getAll() {
        List<Artwork> artworks = new ArrayList<>();
        String sql = "SELECT * FROM artwork";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Artwork artwork = new Artwork();
                artwork.setId(rs.getInt("id"));
                artwork.setTitre(rs.getString("titre"));
                artwork.setDescription(rs.getString("description"));
                artwork.setPrix(rs.getInt("prix"));
                artwork.setImage(rs.getString("image"));
                artwork.setArtistenom(rs.getString("artistenom"));
                // Only retrieve columns that actually exist in your database table
                // Remove or comment out all the following lines if the columns don't exist
                // artwork.setTechnique(rs.getString("technique"));
                // artwork.setDimensions(rs.getString("dimensions"));
                // artwork.setCondition(rs.getString("condition"));
                // artwork.setLocation(rs.getString("location"));
                // artwork.setInventoryNumber(rs.getString("inventoryNumber"));
                // artwork.setYear(rs.getString("year"));
                System.out.println("[DEBUG] Artwork loaded: " + artwork.getTitre() + " | " + artwork.getImage());
                artworks.add(artwork);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return artworks;
    }

    public void add(Artwork artwork) {
        String sql = "INSERT INTO artwork (titre, description, prix, image, artistenom) VALUES ('"
                + artwork.getTitre().replace("'", "''") + "', '"
                + artwork.getDescription().replace("'", "''") + "', "
                + artwork.getPrix() + ", '"
                + artwork.getImage().replace("'", "''") + "', '"
                + artwork.getArtistenom().replace("'", "''") + "')";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Artwork artwork) {
        String sql = "UPDATE artwork SET titre = '" + artwork.getTitre().replace("'", "''") +
                "', description = '" + artwork.getDescription().replace("'", "''") +
                "', prix = " + artwork.getPrix() +
                ", image = '" + artwork.getImage().replace("'", "''") +
                "', artistenom = '" + artwork.getArtistenom().replace("'", "''") +
                "' WHERE id = " + artwork.getId();
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Artwork artwork) {
        System.out.println("[DEBUG] serviceartwork.delete() called for ID: " + artwork.getId());
        try (Statement stmt = connection.createStatement()) {
            // First delete commandes referencing this artwork
            String sqlDeleteCommandes = "DELETE FROM commande WHERE artwork_id = " + artwork.getId();
            int commandesDeleted = stmt.executeUpdate(sqlDeleteCommandes);
            System.out.println("[DEBUG] Commandes deleted: " + commandesDeleted);

            // Now delete the artwork
            String sql = "DELETE FROM artwork WHERE id = " + artwork.getId();
            int rows = stmt.executeUpdate(sql);
            System.out.println("[DEBUG] Rows affected by delete: " + rows);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}