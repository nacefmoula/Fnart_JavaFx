package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import java.io.IOException;

public class Home{
    
    @FXML
    private void handleAddBeneficiaire() {
        try {
            // Charger la vue AddBeneficiaire
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddBeneficiaire.fxml"));
            Parent view = loader.load();
            
            // Créer une nouvelle scène
            Stage stage = new Stage();
            stage.setTitle("Ajouter un bénéficiaire");
            stage.setScene(new Scene(view));
            stage.show();
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la vue AddBeneficiaire", e);
        }
    }

    @FXML
    private void handleAddDons() {
        try {
            // Charger la vue AddDons
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddDons.fxml"));
            Parent view = loader.load();
            
            // Créer une nouvelle scène
            Stage stage = new Stage();
            stage.setTitle("Ajouter un don");
            stage.setScene(new Scene(view));
            stage.show();
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la vue AddDons", e);
        }
    }

    @FXML
    private void handleListeBeneficiaire() {
        try {
            // Charger la vue ListeBeneficiaires
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeBeneficiaires.fxml"));
            Parent view = loader.load();
            
            // Créer une nouvelle scène
            Stage stage = new Stage();
            stage.setTitle("Liste des bénéficiaires");
            stage.setScene(new Scene(view));
            stage.show();
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la vue ListeBeneficiaires", e);
        }
    }

    @FXML
    private void handleListeDons() {
        try {
            // Charger la vue ListeDons
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeDons.fxml"));
            Parent view = loader.load();
            
            // Créer une nouvelle scène
            Stage stage = new Stage();
            stage.setTitle("Liste des dons");
            stage.setScene(new Scene(view));
            stage.show();
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la vue ListeDons", e);
        }
    }

    private void showError(String title, String content, Exception e) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content + "\n" + e.getMessage());
        alert.showAndWait();
    }
} 