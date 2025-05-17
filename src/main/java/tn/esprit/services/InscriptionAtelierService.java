package tn.esprit.services;

import tn.esprit.models.Atelier;
import tn.esprit.models.InscriptionAtelier;
import tn.esprit.utils.MyDataBase;
import tn.esprit.services.EmailService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InscriptionAtelierService {

    private final Connection cnx;
    private final List<InscriptionAtelier> inscriptions = new ArrayList<>();

    public InscriptionAtelierService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    // Retrieve all inscriptions for a specific atelier by its ID
    public List<InscriptionAtelier> getByAtelier(int atelierId) {
        List<InscriptionAtelier> result = new ArrayList<>();
        String req = "SELECT * FROM inscription_atelier WHERE atelier_id = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, atelierId);
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                InscriptionAtelier inscription = new InscriptionAtelier(
                    rs.getInt("atelier_id"),
                    rs.getString("nom_temporaire"),
                    rs.getString("email_temporaire")
                );
                result.add(inscription);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching inscriptions for atelier ID " + atelierId + ": " + e.getMessage());
        }
        return result;
    }

    public void supprimerParAtelier(int atelierId) {
        String req = "DELETE FROM inscription_atelier WHERE atelier_id = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, atelierId);
            stm.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting inscriptions for atelier ID " + atelierId + ": " + e.getMessage());
            throw new RuntimeException("Failed to delete inscriptions for atelier", e);
        }
    }

    public void supprimerParEmail(String email) {
        String req = "DELETE FROM inscription_atelier WHERE email_temporaire = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setString(1, email);
            stm.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting inscription with email " + email + ": " + e.getMessage());
            throw new RuntimeException("Failed to delete inscription", e);
        }
    }

    // Get the number of inscriptions for a specific atelier
    public int getNombreInscriptions(int atelierId) {
        String req = "SELECT COUNT(*) FROM inscription_atelier WHERE atelier_id = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, atelierId);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching number of inscriptions for atelier ID " + atelierId + ": " + e.getMessage());
        }
        return 0;
    }

    // Check if an email is already registered for a specific atelier
    public boolean existeDeja(String email, int atelierId) {
        String req = "SELECT COUNT(*) FROM inscription_atelier WHERE email_temporaire = ? AND atelier_id = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setString(1, email);
            stm.setInt(2, atelierId);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if email exists for atelier ID " + atelierId + ": " + e.getMessage());
        }
        return false;
    }

    // Add a new inscription
    public boolean ajouter(InscriptionAtelier inscription) {
        String req = "INSERT INTO inscription_atelier (atelier_id, nom_temporaire, email_temporaire, statut) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, inscription.getIdAtelier());
            stm.setString(2, inscription.getNomTemporaire());
            stm.setString(3, inscription.getEmailTemporaire());
            stm.setString(4, "en attente"); // Default value for 'statut'
            boolean isAdded = stm.executeUpdate() > 0;

            if (isAdded) {
                // Send confirmation email
                String subject = "Inscription Confirmation";
                String content = "Dear " + inscription.getNomTemporaire() + ",\n\n" +
                                 "Your inscription to the atelier has been successfully registered.\n\n" +
                                 "Best regards,\nFnart";
                EmailService.sendEmail(inscription.getEmailTemporaire(), subject, content);
            }

            return isAdded;
        } catch (SQLException e) {
            System.err.println("Error adding new inscription: " + e.getMessage());
        }
        return false;
    }

    public void updateInscriptionStatus(int inscriptionId, String status) {
        String req = "UPDATE inscription_atelier SET status = ? WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement stm = conn.prepareStatement(req)) {
            stm.setString(1, status);
            stm.setInt(2, inscriptionId);
            stm.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating inscription status: " + e.getMessage());
            throw new RuntimeException("Failed to update inscription status", e);
        }
    }

    public void updateInscriptionStatusByEmail(String email, String status) {
        String req = "UPDATE inscription_atelier SET statut = ? WHERE email_temporaire = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setString(1, status);
            stm.setString(2, email);
            int rowsUpdated = stm.executeUpdate();
            
            if (rowsUpdated == 0) {
                throw new RuntimeException("Aucune inscription trouvée avec l'email: " + email);
            }
            
            System.out.println("Statut d'inscription mis à jour pour " + email + " : " + status);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut d'inscription: " + e.getMessage());
            throw new RuntimeException("Échec de la mise à jour du statut d'inscription", e);
        }
    }

    public void inscrire(Atelier atelier, String nom, String email) {
        if (atelier == null || nom == null || email == null || nom.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("L'atelier, le nom et l'email sont requis");
        }
        
        // Vérifier si l'utilisateur est déjà inscrit à cet atelier
        if (existeDeja(email, atelier.getId())) {
            throw new RuntimeException("Vous êtes déjà inscrit à cet atelier");
        }
        
        // Vérifier si l'atelier n'est pas complet
        int nombreInscrits = getNombreInscriptions(atelier.getId());
        if (nombreInscrits >= atelier.getParticipantMax()) {
            throw new RuntimeException("Cet atelier est complet");
        }
        
        // Créer et ajouter l'inscription
        InscriptionAtelier inscription = new InscriptionAtelier(atelier.getId(), nom, email);
        boolean success = ajouter(inscription);
        
        if (!success) {
            throw new RuntimeException("Échec de l'inscription");
        }
        
        System.out.println("Inscription réussie pour " + nom + " à l'atelier " + atelier.getTitre());
    }
}
