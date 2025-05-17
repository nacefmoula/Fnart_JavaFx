package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.models.CommantairesF;
import tn.esprit.services.ServiceCommentaire_f;

import java.io.IOException;

public class ModifierCommentaireController {
    private ServiceCommentaire_f serviceCommentaire;
    private CommantairesF selectedCommentaire;

    @FXML
    private TextField TFtextemodif;

    @FXML
    private Button modifierCommentActionmodfi;

    @FXML
    private Button voirCommentsmodif;

    @FXML
    private Label min;

    @FXML
    private Label bien;

    public void initialize() {
        serviceCommentaire = new ServiceCommentaire_f();
    }

    public void setData(CommantairesF commentaire) {
        selectedCommentaire = commentaire;

        if (selectedCommentaire != null) {
            TFtextemodif.setText(selectedCommentaire.getTexte_c());
        }
    }

    @FXML
    private void handleTextInput() {
        String text = TFtextemodif.getText();
        if (text.length() < 3) {
            min.setVisible(true);
            bien.setVisible(false);
        } else {
            min.setVisible(false);
            bien.setVisible(true);
        }
    }

    @FXML
    public void modifierCommentActionmodfi(ActionEvent actionEvent) {
        if (selectedCommentaire != null) {
            String newText = TFtextemodif.getText();
            selectedCommentaire.setTexte_c(newText);

            try {
                // Update the comment in the database
                serviceCommentaire.modifier(selectedCommentaire);
                System.out.println("Commentaire modifié avec succès !");

                // Charger et afficher la vue des commentaires après modification
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherCommentaire.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Erreur lors de la modification du commentaire.");
            }
        } else {
            System.out.println("Veuillez sélectionner un commentaire à modifier.");
        }
    }

    @FXML
    public void voirCommentsmodif(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherCommentaires.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'affichage des commentaires.");
        }
    }
}