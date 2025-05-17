package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Atelier;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.awt.image.BufferedImage;

public class AtelierService implements IService<Atelier> {

    private final Connection cnx;
    private final InscriptionAtelierService inscriptionService;

    public AtelierService() {
        cnx = MyDataBase.getInstance().getCnx();
        inscriptionService = new InscriptionAtelierService();
    }

    @Override
    public void add(Atelier atelier) {

    }

    @Override
    public List<Atelier> getAll() {
        return null;
    }

    @Override
    public void update(Atelier atelier) {

    }

    @Override
    public void delete(Atelier atelier) {

    }

    @Override
    public boolean ajouter(Atelier atelier) throws SQLException {
        if (atelier == null) {
            throw new IllegalArgumentException("Atelier cannot be null");
        }

        String req = "INSERT INTO atelier (titre, lieu, date, participants_max, description, image) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setString(1, atelier.getTitre());
            stm.setString(2, atelier.getLieu());
            stm.setDate(3, new java.sql.Date(atelier.getDate().getTime()));
            stm.setInt(4, atelier.getParticipantMax());
            stm.setString(5, atelier.getDescription());
            stm.setString(6, atelier.getImage());
            int rowsAffected = stm.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding atelier: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void modifier(Atelier atelier) {
        if (atelier == null) {
            throw new IllegalArgumentException("Atelier cannot be null");
        }

        String req = "UPDATE atelier SET titre = ?, lieu = ?, date = ?, participants_max = ?, description = ?, image = ? WHERE id = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setString(1, atelier.getTitre());
            stm.setString(2, atelier.getLieu());
            stm.setDate(3, new java.sql.Date(atelier.getDate().getTime()));
            stm.setInt(4, atelier.getParticipantMax());
            stm.setString(5, atelier.getDescription());
            stm.setString(6, atelier.getImage());
            stm.setInt(7, atelier.getId());
            int rowsUpdated = stm.executeUpdate();

            if (rowsUpdated == 0) {
                throw new SQLException("No rows updated. Atelier with ID " + atelier.getId() + " may not exist.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating atelier: " + e.getMessage());
            throw new RuntimeException("Failed to update atelier", e);
        }
    }

    @Override
    public void supprimer(Atelier atelier) {
        if (atelier == null) {
            throw new IllegalArgumentException("Atelier cannot be null");
        }

        // D'abord supprimer toutes les inscriptions associées
        inscriptionService.supprimerParAtelier(atelier.getId());

        // Ensuite supprimer l'atelier
        String req = "DELETE FROM atelier WHERE id = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, atelier.getId());
            int rowsDeleted = stm.executeUpdate();

            if (rowsDeleted == 0) {
                throw new SQLException("No rows deleted. Atelier with ID " + atelier.getId() + " may not exist.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting atelier: " + e.getMessage());
            throw new RuntimeException("Failed to delete atelier", e);
        }
    }

    public List<Atelier> getall() {
        List<Atelier> ateliers = new ArrayList<>();
        String req = "SELECT a.*, COUNT(i.id) AS inscription_count FROM atelier a LEFT JOIN inscription_atelier i ON a.id = i.atelier_id GROUP BY a.id";
        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(req)) {
            while (rs.next()) {
                Atelier atelier = new Atelier();
                atelier.setId(rs.getInt("id"));
                atelier.setTitre(rs.getString("titre"));
                atelier.setLieu(rs.getString("lieu"));
                atelier.setDate(rs.getDate("date"));
                atelier.setParticipantMax(rs.getInt("participants_max"));
                atelier.setDescription(rs.getString("description"));
                atelier.setImage(rs.getString("image"));
                // Add inscription count as a new field or use a setter if available
                atelier.setInscriptionCount(rs.getInt("inscription_count"));
                ateliers.add(atelier);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching ateliers: " + e.getMessage());
            throw new RuntimeException("Failed to fetch ateliers", e);
        }
        return ateliers;
    }

    @Override
    public Atelier getone() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public void supprimer(int id) {
        // D'abord supprimer toutes les inscriptions associées
        inscriptionService.supprimerParAtelier(id);

        // Ensuite supprimer l'atelier
        String req = "DELETE FROM atelier WHERE id = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, id);
            int rowsDeleted = stm.executeUpdate();

            if (rowsDeleted == 0) {
                throw new SQLException("No rows deleted. Atelier with ID " + id + " may not exist.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting atelier: " + e.getMessage());
            throw new RuntimeException("Failed to delete atelier", e);
        }
    }

    public BufferedImage generateQRCode(Atelier atelier) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        String qrContent = "Atelier: " + atelier.getTitre() + "\n" +
                           "Lieu: " + atelier.getLieu() + "\n" +
                           "Date: " + atelier.getDate();
        try {
            var bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    /**
     * Récupère une liste paginée d'ateliers avec filtrage par date optionnel
     * @param page Numéro de page (commençant à 0)
     * @param pageSize Nombre d'éléments par page
     * @param fromDate Date de début du filtre (peut être null)
     * @param toDate Date de fin du filtre (peut être null)
     * @param sortBy Champ pour trier (titre, date, etc.)
     * @param ascending Ordre de tri (true pour ascendant, false pour descendant)
     * @return Liste des ateliers correspondant aux critères
     */
    public List<Atelier> getAteliersPaginated(int page, int pageSize, Date fromDate, Date toDate, String sortBy, boolean ascending) {
        List<Atelier> ateliers = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();
        
        queryBuilder.append("SELECT a.*, COUNT(i.id) AS inscription_count FROM atelier a ")
                   .append("LEFT JOIN inscription_atelier i ON a.id = i.atelier_id ");
        
        // Ajouter le filtre de date s'il est spécifié
        if (fromDate != null || toDate != null) {
            queryBuilder.append("WHERE ");
            if (fromDate != null) {
                queryBuilder.append("a.date >= ? ");
                if (toDate != null) {
                    queryBuilder.append("AND ");
                }
            }
            if (toDate != null) {
                queryBuilder.append("a.date <= ? ");
            }
        }
        
        queryBuilder.append("GROUP BY a.id ");
        
        // Ajouter le tri
        if (sortBy != null && !sortBy.isEmpty()) {
            queryBuilder.append("ORDER BY a.").append(sortBy).append(" ").append(ascending ? "ASC" : "DESC");
        } else {
            queryBuilder.append("ORDER BY a.date ASC");
        }
        
        // Ajouter la pagination
        queryBuilder.append(" LIMIT ? OFFSET ?");
        
        try (PreparedStatement stm = cnx.prepareStatement(queryBuilder.toString())) {
            int paramIndex = 1;
            
            // Définir les paramètres de filtre de date
            if (fromDate != null) {
                stm.setDate(paramIndex++, new java.sql.Date(fromDate.getTime()));
            }
            if (toDate != null) {
                stm.setDate(paramIndex++, new java.sql.Date(toDate.getTime()));
            }
            
            // Définir les paramètres de pagination
            stm.setInt(paramIndex++, pageSize);
            stm.setInt(paramIndex, page * pageSize);
            
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    Atelier atelier = new Atelier();
                    atelier.setId(rs.getInt("id"));
                    atelier.setTitre(rs.getString("titre"));
                    atelier.setLieu(rs.getString("lieu"));
                    atelier.setDate(rs.getDate("date"));
                    atelier.setParticipantMax(rs.getInt("participants_max"));
                    atelier.setDescription(rs.getString("description"));
                    atelier.setImage(rs.getString("image"));
                    atelier.setInscriptionCount(rs.getInt("inscription_count"));
                    ateliers.add(atelier);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching paginated ateliers: " + e.getMessage());
            throw new RuntimeException("Failed to fetch paginated ateliers", e);
        }
        return ateliers;
    }
    
    /**
     * Récupère le nombre total d'ateliers avec filtrage par date optionnel
     * @param fromDate Date de début du filtre (peut être null)
     * @param toDate Date de fin du filtre (peut être null)
     * @return Nombre total d'ateliers correspondant aux critères
     */
    public int getTotalAteliers(Date fromDate, Date toDate) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT COUNT(*) FROM atelier ");
        
        // Ajouter le filtre de date s'il est spécifié
        if (fromDate != null || toDate != null) {
            queryBuilder.append("WHERE ");
            if (fromDate != null) {
                queryBuilder.append("date >= ? ");
                if (toDate != null) {
                    queryBuilder.append("AND ");
                }
            }
            if (toDate != null) {
                queryBuilder.append("date <= ? ");
            }
        }
        
        try (PreparedStatement stm = cnx.prepareStatement(queryBuilder.toString())) {
            int paramIndex = 1;
            
            // Définir les paramètres de filtre de date
            if (fromDate != null) {
                stm.setDate(paramIndex++, new java.sql.Date(fromDate.getTime()));
            }
            if (toDate != null) {
                stm.setDate(paramIndex, new java.sql.Date(toDate.getTime()));
            }
            
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting ateliers: " + e.getMessage());
            throw new RuntimeException("Failed to count ateliers", e);
        }
        return 0;
    }
    
    /**
     * Récupère les ateliers filtrés par date
     * @param fromDate Date de début du filtre (peut être null)
     * @param toDate Date de fin du filtre (peut être null)
     * @return Liste des ateliers correspondant aux critères de date
     */
    public List<Atelier> getAteliersByDateRange(Date fromDate, Date toDate) {
        List<Atelier> ateliers = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();
        
        queryBuilder.append("SELECT a.*, COUNT(i.id) AS inscription_count FROM atelier a ")
                   .append("LEFT JOIN inscription_atelier i ON a.id = i.atelier_id ");
        
        // Ajouter le filtre de date s'il est spécifié
        if (fromDate != null || toDate != null) {
            queryBuilder.append("WHERE ");
            if (fromDate != null) {
                queryBuilder.append("a.date >= ? ");
                if (toDate != null) {
                    queryBuilder.append("AND ");
                }
            }
            if (toDate != null) {
                queryBuilder.append("a.date <= ? ");
            }
        }
        
        queryBuilder.append("GROUP BY a.id ORDER BY a.date ASC");
        
        try (PreparedStatement stm = cnx.prepareStatement(queryBuilder.toString())) {
            int paramIndex = 1;
            
            // Définir les paramètres de filtre de date
            if (fromDate != null) {
                stm.setDate(paramIndex++, new java.sql.Date(fromDate.getTime()));
            }
            if (toDate != null) {
                stm.setDate(paramIndex, new java.sql.Date(toDate.getTime()));
            }
            
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    Atelier atelier = new Atelier();
                    atelier.setId(rs.getInt("id"));
                    atelier.setTitre(rs.getString("titre"));
                    atelier.setLieu(rs.getString("lieu"));
                    atelier.setDate(rs.getDate("date"));
                    atelier.setParticipantMax(rs.getInt("participants_max"));
                    atelier.setDescription(rs.getString("description"));
                    atelier.setImage(rs.getString("image"));
                    atelier.setInscriptionCount(rs.getInt("inscription_count"));
                    ateliers.add(atelier);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching ateliers by date range: " + e.getMessage());
            throw new RuntimeException("Failed to fetch ateliers by date range", e);
        }
        return ateliers;
    }
}