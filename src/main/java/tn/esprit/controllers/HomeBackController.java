package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeBackController {

    @FXML
    private Button beneficiairesButton;

    @FXML
    private Button donsButton;

    @FXML
    private Button logoutButton;

    @FXML
    private void handleBeneficiaires() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeBeneficiairesBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) beneficiairesButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Bénéficiaires");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la liste des bénéficiaires: " + e.getMessage());
        }
    }

    @FXML
    private void handleDons() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeDonsBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) donsButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Dons");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la liste des dons: " + e.getMessage());
        }
    }

    @FXML


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(HomeBackController.class.getResource("/HomeBack.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            primaryStage.setTitle("Back Office - Administration");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger l'interface d'administration: " + e.getMessage());
            alert.showAndWait();
        }
    }
} 