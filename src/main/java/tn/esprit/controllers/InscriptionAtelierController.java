package tn.esprit.controllers;

import tn.esprit.models.Atelier;
import tn.esprit.models.InscriptionAtelier;
import tn.esprit.services.EmailService;
import tn.esprit.services.InscriptionAtelierService;
import tn.esprit.utils.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class InscriptionAtelierController {

    @FXML
    private Label atelierLabel;

    @FXML
    private TextField nomField;

    @FXML
    private TextField emailField;

    @FXML
    private TableView<InscriptionAtelier> inscriptionTable;

    private Atelier atelier;
    private Object parentController;
    private final InscriptionAtelierService inscriptionService = new InscriptionAtelierService();

    public void setAtelier(Atelier atelier) {
        this.atelier = atelier;
        // Update UI components if necessary
        if (atelier != null) {
            // Example: Update a label with the atelier title
            atelierLabel.setText("Inscription à l'atelier: " + atelier.getTitre());
        }
    }

    @FXML
    public void initialize() {
        // Initialize any UI components here
        // If atelier is already set before FXML is loaded, update the label
        if (atelier != null && atelierLabel != null) {
            atelierLabel.setText("Inscription à l'atelier: " + atelier.getTitre());
        }
    }

    public void setParentController(Object parentController) {
        this.parentController = parentController;
    }

    public void openInNewWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/InscriptionAtelier.fxml")); // Ensure correct path
            Parent root = loader.load();

            InscriptionAtelierController controller = loader.getController();
            controller.setAtelier(this.atelier);
            controller.setParentController(this.parentController);

            Stage stage = new Stage();
            stage.setTitle("Inscription à l'Atelier");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre d'inscription.");
        }
    }

    @FXML
    private void validerInscription() {
        // Validation des champs
        if (!validateFields()) {
            return;
        }

        try {
            // Vérifier si l'atelier est complet
            int nombreInscriptions = inscriptionService.getNombreInscriptions(atelier.getId());
            if (nombreInscriptions >= atelier.getParticipantMax()) {
                showAlert(Alert.AlertType.WARNING, "Atelier complet", 
                    "Désolé, cet atelier a atteint sa capacité maximale de " + 
                    atelier.getParticipantMax() + " participants.");
                return;
            }

            // Vérifier si l'email est déjà inscrit
            if (inscriptionService.existeDeja(emailField.getText(), atelier.getId())) {
                showAlert(Alert.AlertType.WARNING, "Inscription existante", 
                    "Cet email est déjà inscrit à cet atelier.");
                return;
            }

            InscriptionAtelier inscription = new InscriptionAtelier(
                atelier.getId(),
                nomField.getText(),
                emailField.getText()
            );

            if (inscriptionService.ajouter(inscription)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    "Votre inscription a été enregistrée avec succès !");
                
                // Rafraîchir la vue parente
                if (parentController instanceof FrontendAtelierController) {
                    FrontendAtelierController frontController = (FrontendAtelierController) parentController;
                    frontController.loadData();
                    
                    // Rafraîchir la vue des détails si elle est ouverte
                    if (frontController.getDetailsStage() != null) {
                        DetailsAtelierController detailsController = frontController.getDetailsController();
                        if (detailsController != null) {
                            detailsController.updateUI();
                        }
                    }
                }
                
                closeWindow();
                redirectToAtelierList();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Une erreur est survenue lors de l'inscription. Veuillez réessayer.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Une erreur inattendue est survenue : " + e.getMessage());
        }
    }

    private void redirectToAtelierList() {
        // Instead of opening a new window, just close the current inscription window
        // and refresh the parent controller's data if available
        if (parentController instanceof FrontendAtelierController) {
            FrontendAtelierController frontController = (FrontendAtelierController) parentController;
            frontController.loadData();
        }
        closeWindow();
    }

    private boolean validateFields() {
        // Validation du nom
        if (!ValidationUtils.validateTextField(nomField, "Nom", true)) {
            return false;
        }

        // Validation de l'email
        if (!ValidationUtils.validateTextField(emailField, "Email", true)) {
            return false;
        }
        if (!ValidationUtils.validateEmail(emailField)) {
            return false;
        }

        return true;
    }

    @FXML
    private void annulerInscription() {
        closeWindow();
    }

    @FXML
    private void acceptInscription() {
        InscriptionAtelier selectedInscription = inscriptionTable.getSelectionModel().getSelectedItem();
        if (selectedInscription == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une inscription à accepter.");
            return;
        }

        try {
            inscriptionService.updateInscriptionStatus(selectedInscription.getId(), "accepted");
            EmailService.sendEmail(selectedInscription.getEmailTemporaire(), "Inscription Acceptée", "Votre inscription a été acceptée.");
            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'inscription a été acceptée et un email a été envoyé.");
            refreshTable();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'acceptation de l'inscription : " + e.getMessage());
        }
    }

    @FXML
    private void refuseInscription() {
        InscriptionAtelier selectedInscription = inscriptionTable.getSelectionModel().getSelectedItem();
        if (selectedInscription == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une inscription à refuser.");
            return;
        }

        try {
            inscriptionService.updateInscriptionStatus(selectedInscription.getId(), "refused");
            EmailService.sendEmail(selectedInscription.getEmailTemporaire(), "Inscription Refusée", "Votre inscription a été refusée.");
            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'inscription a été refusée et un email a été envoyé.");
            refreshTable();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors du refus de l'inscription : " + e.getMessage());
        }
    }

    private void refreshTable() {
        // Logic to refresh the inscription table
        inscriptionTable.setItems(FXCollections.observableList(inscriptionService.getByAtelier(atelier.getId())));
    }

    private void closeWindow() {
        Stage stage = (Stage) atelierLabel.getScene().getWindow();
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

