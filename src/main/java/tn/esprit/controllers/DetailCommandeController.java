package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import tn.esprit.models.Commande;

import java.net.URL;
import java.util.ResourceBundle;

public class DetailCommandeController implements Initializable {
    @FXML private Label nameLabel;
    @FXML private Label addressLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label totalLabel;
    @FXML private Label statusLabel;
    
    private Commande commande;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize any necessary components
    }
    
    public void setCommande(Commande commande) {
        this.commande = commande;
        updateUI();
    }
    
    private void updateUI() {
        if (commande != null) {
            nameLabel.setText(commande.getNom());
            addressLabel.setText(commande.getAdress());
            phoneLabel.setText(commande.getTelephone());
            emailLabel.setText(commande.getEmail());
            totalLabel.setText(String.valueOf(commande.getTotale()));
            statusLabel.setText(commande.getStatus());
        }
    }
} 