package tn.esprit.controllers;

import tn.esprit.models.Atelier;
import tn.esprit.services.AtelierService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class CardViewController {

    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Button detailsButton;
    @FXML private Button modifyButton;
    @FXML private Button deleteButton;

    private Atelier atelier;
    private final AtelierService atelierService = new AtelierService();
    private AfficherAtelierController parentController;

    public void setData(Atelier atelier, AfficherAtelierController parentController) {
        this.atelier = atelier;
        this.parentController = parentController;

        titleLabel.setText(atelier.getTitre());
        descriptionLabel.setText(atelier.getDescription());

        detailsButton.setOnAction(event -> openDetails());
        modifyButton.setOnAction(event -> modifyAtelier());
        deleteButton.setOnAction(event -> deleteAtelier());
    }

    private void openDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsAtelier.fxml")); // Ensure correct path
            AnchorPane detailsPane = loader.load();

            DetailsAtelierController controller = loader.getController();
            controller.setAtelier(atelier);

            Stage stage = new Stage();
            stage.setTitle("Détails de l'Atelier");
            stage.setScene(new javafx.scene.Scene(detailsPane));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails de l'atelier.");
        }
    }

    private void modifyAtelier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierAtelier.fxml")); // Ensure correct path
            AnchorPane modifyPane = loader.load();

            ModifierAtelierController controller = loader.getController();
            controller.setAtelier(atelier);
            controller.setParentController(parentController);

            Stage stage = new Stage();
            stage.setTitle("Modifier Atelier");
            stage.setScene(new javafx.scene.Scene(modifyPane));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire de modification.");
        }
    }

    private void deleteAtelier() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cet atelier ?");
        if (confirmation.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            atelierService.supprimer(atelier.getId());
            parentController.refreshCards();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Atelier supprimé avec succès.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setAtelier(Atelier atelier) {
        this.atelier = atelier;
        
        // Update UI with atelier data
        if (titleLabel != null) {
            titleLabel.setText(atelier.getTitre());
        }
        
        if (descriptionLabel != null) {
            descriptionLabel.setText(atelier.getDescription());
        }
        
        // Setup button actions
        if (detailsButton != null) {
            detailsButton.setOnAction(event -> openDetails());
        }
        
        if (modifyButton != null) {
            modifyButton.setOnAction(event -> modifyAtelier());
        }
        
        if (deleteButton != null) {
            deleteButton.setOnAction(event -> deleteAtelier());
        }
    }

    public void setParentController(AfficherAtelierController afficherAtelierController) {
        this.parentController = afficherAtelierController;
    }
}
