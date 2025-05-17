package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.models.Beneficiaires;
import tn.esprit.services.ServicesBeneficiaires;

import java.io.File;

public class DetailBeneficiaireBackController {
    @FXML
    private Label nomLabel;
    
    @FXML
    private Label emailLabel;
    
    @FXML
    private Label telephoneLabel;
    
    @FXML
    private Label causeLabel;
    
    @FXML
    private Label associationLabel;
    
    @FXML
    private Label valeurLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private Button closeButton;
    
    @FXML
    private ImageView imageView;
    
    private Beneficiaires currentBeneficiaire;
    private ServicesBeneficiaires servicesBeneficiaire;
    
    @FXML
    public void initialize() {
        servicesBeneficiaire = new ServicesBeneficiaires();
    }
    
    public void setBeneficiaire(Beneficiaires beneficiaire) {
        this.currentBeneficiaire = beneficiaire;
        displayBeneficiaireDetails();
    }
    
    private void displayBeneficiaireDetails() {
        if (currentBeneficiaire != null) {
            nomLabel.setText(currentBeneficiaire.getNom());
            emailLabel.setText(currentBeneficiaire.getEmail());
            telephoneLabel.setText(currentBeneficiaire.getTelephone());
            causeLabel.setText(currentBeneficiaire.getCause());
            associationLabel.setText(currentBeneficiaire.getEstElleAssociation());
            valeurLabel.setText(currentBeneficiaire.getValeurDemande() != null ? currentBeneficiaire.getValeurDemande().toString() + " DT" : "Non spécifié");
            statusLabel.setText(currentBeneficiaire.getStatus() != null ? currentBeneficiaire.getStatus() : "En attente");
            descriptionArea.setText(currentBeneficiaire.getDescription());

            // Load and display the image
            if (currentBeneficiaire.getImage() != null && !currentBeneficiaire.getImage().isEmpty()) {
                try {
                    File imageFile = new File(currentBeneficiaire.getImage());
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        imageView.setImage(image);
                    } else {
                        loadDefaultImage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    loadDefaultImage();
                }
            } else {
                loadDefaultImage();
            }
        }
    }
    
    private void loadDefaultImage() {
        try {
            File defaultImage = new File("src/main/resources/images/placehoolder.png");
            if (defaultImage.exists()) {
                imageView.setImage(new Image(defaultImage.toURI().toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
} 