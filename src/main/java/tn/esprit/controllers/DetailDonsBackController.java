package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import tn.esprit.models.Dons;
import tn.esprit.services.ServicesDons;

public class DetailDonsBackController {
    @FXML
    private Label typeLabel;
    
    @FXML
    private Label valeurLabel;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private Label beneficiaireLabel;
    
    @FXML
    private Button closeButton;
    
    private Dons currentDon;
    private ServicesDons servicesDons;
    
    @FXML
    public void initialize() {
        servicesDons = new ServicesDons();
    }
    
    public void setdons(Dons don) {
        this.currentDon = don;
        displayDonDetails();
    }
    
    private void displayDonDetails() {
        if (currentDon != null) {
            typeLabel.setText(currentDon.getType());
            valeurLabel.setText(String.valueOf(currentDon.getValeur()));
            descriptionArea.setText(currentDon.getDescription());
            beneficiaireLabel.setText(currentDon.getBeneficiaire() != null ? 
                currentDon.getBeneficiaire().getNom() : "Non spécifié");
        }
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
} 