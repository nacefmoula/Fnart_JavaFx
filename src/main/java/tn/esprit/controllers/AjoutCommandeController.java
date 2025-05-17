package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.models.Artwork;
import tn.esprit.models.Commande;
import tn.esprit.services.ServiceCommande;
import tn.esprit.interfaces.ParentControllerAware;
import tn.esprit.utils.CartManager;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import javafx.event.ActionEvent;

public class AjoutCommandeController implements ParentControllerAware {
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private Label totalLabel;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    private List<Artwork> cartItems;
    private double total;
    private final ServiceCommande serviceCommande = new ServiceCommande();
    private AdminDashboardController parentController;

    @FXML
    public void initialize() {
        confirmButton.setOnAction(this::handleConfirmation);
        cancelButton.setOnAction(e -> handleCancel());
    }

    @Override
    public void setParentController(Object controller) {
        if (controller instanceof AdminDashboardController) {
            this.parentController = (AdminDashboardController) controller;
        }
    }

    public void initData(Artwork artwork) {
        if (artwork != null) {
            this.cartItems = List.of(artwork);
            calculateTotal();
            // Afficher un message de debug
            System.out.println("Artwork loaded: " + artwork.getTitre() + " - Price: " + artwork.getPrix());
            System.out.println("Total calculated: " + total);
        } else {
            System.out.println("Warning: Null artwork passed to initData");
        }
    }

    public void setCartItems(List<Artwork> items) {
        this.cartItems = items;
        calculateTotal();
    }

    private void calculateTotal() {
        if (cartItems != null && !cartItems.isEmpty()) {
            total = cartItems.stream()
                    .mapToDouble(Artwork::getPrix)
                    .sum();
            if (totalLabel != null) {
                totalLabel.setText(String.format("Total: %.2f TND", total));
                System.out.println("Total label updated: " + String.format("%.2f TND", total));
            } else {
                System.out.println("Warning: totalLabel is null");
            }
        } else {
            System.out.println("Warning: Cart items is null or empty");
            total = 0.0;
            if (totalLabel != null) {
                totalLabel.setText("Total: 0.00 TND");
            }
        }
    }

    @FXML
    private void handleConfirmation(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        try {
            // Créer une nouvelle commande
            Commande commande = new Commande();
            commande.setNom(nomField.getText().trim() + " " + prenomField.getText().trim());
            commande.setAdress(addressField.getText().trim());
            commande.setTelephone(phoneField.getText().trim());
            commande.setEmail(emailField.getText().trim());
            commande.setTotale(total);
            commande.setDate(LocalDate.now().toString());
            commande.setStatus("En attente");
            
            // Pour chaque artwork dans le panier
            for (Artwork artwork : cartItems) {
                commande.setArtwork_id(artwork.getId());
                // Sauvegarder la commande
                serviceCommande.add(commande);
            }

            showAlert(Alert.AlertType.INFORMATION, "Commande Confirmée", 
                "Votre commande a été enregistrée avec succès!\n\n" +
                "Détails de la commande:\n" +
                "- Client: " + commande.getNom() + "\n" +
                "- Total: " + String.format("%.2f €", commande.getTotale()) + "\n" +
                "- Date: " + commande.getDate() + "\n\n" +
                "Un email de confirmation vous sera envoyé à " + commande.getEmail());
            
            // Rafraîchir la table des commandes si on est dans le dashboard admin

            // Vider le panier après une commande réussie
            CartManager.clearCart();
            
            closeWindow();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'enregistrement de la commande: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (nom.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le nom est requis.");
            return false;
        }

        if (prenom.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le prénom est requis.");
            return false;
        }

        if (address.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "L'adresse de livraison est requise.");
            return false;
        }

        if (phone.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le numéro de téléphone est requis.");
            return false;
        }

        if (!phone.matches("\\d{8}")) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le numéro de téléphone doit contenir 8 chiffres.");
            return false;
        }

        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Veuillez entrer une adresse email valide.");
            return false;
        }

        return true;
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 