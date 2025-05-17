package tn.esprit.services;

import javafx.collections.ObservableList;
import tn.esprit.interfaces.IService;
import tn.esprit.models.Beneficiaires;
import tn.esprit.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServicesBeneficiaires implements IService<Beneficiaires> {

    private final Connection connection;

    public ServicesBeneficiaires() {
        this.connection = MyDataBase.getInstance().getCnx();
    }

    @Override
    public  void add(Beneficiaires b) {
        System.out.println("[DEBUG] Appel de ServicesBeneficiaires.add() avec : " + b);

        System.out.println("[DEBUG] Appel de ServicesBeneficiaires.add() avec : " + b);

        String sql = "INSERT INTO beneficiaires (nom, email, telephone, est_elle_association, cause, valeur_demande, status, description, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        System.out.println("[DEBUG] SQL: " + sql);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (b.getNom() == null || b.getNom().trim().isEmpty()) {
                throw new IllegalArgumentException("Le champ 'nom' est obligatoire.");
            }
            ps.setString(1, b.getNom());
            ps.setString(2, b.getEmail());
            ps.setString(3, b.getTelephone());
            ps.setString(4, b.getEstElleAssociation());
            ps.setString(5, b.getCause());
            if (b.getValeurDemande() != null) {
                ps.setDouble(6, b.getValeurDemande());
            } else {
                ps.setNull(6, java.sql.Types.DOUBLE);
            }
            ps.setString(7, b.getStatus());
            ps.setString(8, b.getDescription());
            // Handle the image path - use default if null or empty
            String imagePath = b.getImage();
            if (imagePath == null || imagePath.trim().isEmpty()) {
                imagePath = "default_profile.png";
            }
            ps.setString(9, imagePath);

            int rows = ps.executeUpdate();
            System.out.println("[DEBUG] Nombre de lignes insérées : " + rows);
            if (rows > 0) {
                System.out.println("Beneficiaire ajouté avec succès !");
            } else {
                System.out.println("Aucune ligne insérée !");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du bénéficiaire !");
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'ajout du bénéficiaire: " + e.getMessage());
        }
    }

    @Override
    public List<Beneficiaires> getAll() {
        List<Beneficiaires> beneficiairesList = new ArrayList<>();
        String sql = "SELECT * FROM beneficiaires";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            System.out.println("Exécution de la requête SQL: " + sql);
            
            while (rs.next()) {
                Beneficiaires b = new Beneficiaires();
                b.setId(rs.getLong("id"));
                b.setNom(rs.getString("nom"));
                b.setEmail(rs.getString("email"));
                b.setTelephone(rs.getString("telephone"));
                b.setEstElleAssociation(rs.getString("est_elle_association"));
                b.setCause(rs.getString("cause"));
                b.setValeurDemande(rs.getDouble("valeur_demande"));
                b.setStatus(rs.getString("status"));
                b.setDescription(rs.getString("description"));
                b.setImage(rs.getString("image"));
                
                beneficiairesList.add(b);
                System.out.println("Bénéficiaire chargé: " + b.getNom());
            }
            
            System.out.println("Nombre total de bénéficiaires chargés: " + beneficiairesList.size());
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des bénéficiaires !");
            System.err.println("Message d'erreur: " + e.getMessage());
            System.err.println("Code d'erreur SQL: " + e.getSQLState());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des bénéficiaires: " + e.getMessage());
        }
        
        return beneficiairesList;
    }

    @Override
    public void update(Beneficiaires beneficiaire) {
        String sql = "UPDATE beneficiaires SET nom = ?, email = ?, telephone = ?, est_elle_association = ?, cause = ?, valeur_demande = ?, description = ?, image = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, beneficiaire.getNom());
            ps.setString(2, beneficiaire.getEmail());
            ps.setString(3, beneficiaire.getTelephone());
            ps.setString(4, beneficiaire.getEstElleAssociation());
            ps.setString(5, beneficiaire.getCause());
            if (beneficiaire.getValeurDemande() != null) {
                ps.setDouble(6, beneficiaire.getValeurDemande());
            } else {
                ps.setNull(6, java.sql.Types.DOUBLE);
            }
            ps.setString(7, beneficiaire.getDescription());
            ps.setString(8, beneficiaire.getImage());
            ps.setLong(9, beneficiaire.getId());

            ps.executeUpdate();
            System.out.println("Bénéficiaire mis à jour avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour du bénéficiaire !");
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Beneficiaires beneficiaire) {
        String sql = "DELETE FROM beneficiaires WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, beneficiaire.getId());
            ps.executeUpdate();
            System.out.println("Bénéficiaire supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du bénéficiaire !");
            e.printStackTrace();
        }
    }

    @Override
    public boolean ajouter(Beneficiaires beneficiaires) throws SQLException {
        this.add(beneficiaires);
        return true;
    }

    @Override
    public void modifier(Beneficiaires beneficiaires) {

    }

    @Override
    public void supprimer(Beneficiaires beneficiaires) {

    }

    @Override
    public List<Beneficiaires> getall() {
        return null;
    }

    @Override
    public Beneficiaires getone() {
        String sql = "SELECT * FROM beneficiaires WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // Replace with the actual ID you want to fetch
            long id = 1; // Example ID
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Beneficiaires b = new Beneficiaires();
                    b.setId(rs.getLong("id"));
                    b.setNom(rs.getString("nom"));
                    b.setEmail(rs.getString("email"));
                    b.setTelephone(rs.getString("telephone"));
                    b.setEstElleAssociation(rs.getString("est_elle_association"));
                    b.setCause(rs.getString("cause"));
                    b.setValeurDemande(rs.getDouble("valeur_demande"));
                    b.setStatus(rs.getString("status"));
                    b.setDescription(rs.getString("description"));
                    b.setImage(rs.getString("image"));
                    return b;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération du bénéficiaire: " + e.getMessage());
        }
        return null;
    }

    public ObservableList<Beneficiaires> getByStatus(String enAttente) {
        return null;
    }

    public Beneficiaires search(String newValue) {
        return null;
    }
}
