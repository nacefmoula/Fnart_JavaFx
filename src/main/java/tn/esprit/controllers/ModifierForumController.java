package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.Forum;
import tn.esprit.services.ServiceForum;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ModifierForumController implements Initializable {

    public void setForum(Forum forum) {
        this.forum = forum;
        // Pré-remplir les champs si forum non nul
        if (forum != null) {
            TFtitre.setText(forum.getTitre_f());
            TFDescriptionModif.setText(forum.getDescription_f());
            TFCategorieModif.setValue(forum.getCategorie_f());
            // Gérer l'image si besoin
        }
    }


    @FXML
    private TextField TFtitre;

    @FXML
    private TextField TFDescriptionModif;

    @FXML
    private ComboBox<String> TFCategorieModif; // Specify type of ComboBox to String

    @FXML
    private Button modifierbutton;

    @FXML
    private Button uploadbutton;

    @FXML
    private Button uploadbuttonmodif;

    @FXML
    private Label warningtitle;

    @FXML
    private Label goodtitle;

    @FXML
    private Label warningDescription;

    @FXML
    private Label gooddescription;

    @FXML
    private ImageView imgView_reclamationmodifffffff;

    private ServiceForum serviceForum;
    private Forum forum;
    private String imagePath;

    @FXML
    void modifierbutton(ActionEvent event) {
        if (forum != null && serviceForum != null) {
            String nouveauTitre = TFtitre.getText();
            String nouvelleDescription = TFDescriptionModif.getText();
            String nouvelleCategorie = TFCategorieModif.getValue();

            // Validate title and description
            if (nouveauTitre.length() >= 4 && nouvelleDescription.length() >= 12) {
                forum.setTitre_f(nouveauTitre);
                forum.setDescription_f(nouvelleDescription);
                forum.setCategorie_f(nouvelleCategorie);

                // Update image if selected
                if (imagePath != null) {
                    forum.setImage_f(imagePath);
                } else {
                    // If no image is selected, retain the old image (if any)
                    String currentImage = forum.getImage_f();
                    if (currentImage != null && !currentImage.isEmpty()) {
                        forum.setImage_f(currentImage);
                    }
                }

                try {
                    serviceForum.modifier(forum);

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setContentText("Forum modifié avec succès !");
                    successAlert.setTitle("Modification réussie");
                    successAlert.show();
                } catch (Exception e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setContentText("Erreur lors de la modification du forum : " + e.getMessage());
                    errorAlert.setTitle("Erreur de modification");
                    errorAlert.show();
                }
            } else {
                // Show validation errors
                if (nouveauTitre.length() < 4) {
                    warningtitle.setVisible(true);
                    goodtitle.setVisible(false);
                } else {
                    warningtitle.setVisible(false);
                    goodtitle.setVisible(true);
                }

                if (nouvelleDescription.length() < 12) {
                    warningDescription.setVisible(true);
                    gooddescription.setVisible(false);
                } else {
                    warningDescription.setVisible(false);
                    gooddescription.setVisible(true);
                }
            }
        } else {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setContentText("Impossible de modifier le forum, aucune donnée n'est sélectionnée.");
            errorAlert.setTitle("Erreur de modification");
            errorAlert.show();
        }
    }

    @FXML
    void uploadimg(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPEG Image", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG Image", "*.png"),
                new FileChooser.ExtensionFilter("All image files", "*.jpg", "*.png")
        );
        Stage stage = (Stage) uploadbutton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();
            Image image = new Image(selectedFile.toURI().toString());
            imgView_reclamationmodifffffff.setImage(image);
        }
    }

    @FXML
    void uploadimgmodif(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPEG Image", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG Image", "*.png"),
                new FileChooser.ExtensionFilter("All image files", "*.jpg", "*.png")
        );
        Stage stage = (Stage) uploadbuttonmodif.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();
            Image image = new Image(selectedFile.toURI().toString());
            imgView_reclamationmodifffffff.setImage(image);
        }
    }

    public void setData(Forum forum) {
        this.forum = forum;
        TFtitre.setText(forum.getTitre_f());
        TFDescriptionModif.setText(forum.getDescription_f());
        TFCategorieModif.setValue(forum.getCategorie_f());
        
        // Set the image if it exists
        if (forum.getImage_f() != null && !forum.getImage_f().isEmpty()) {
            try {
                Image image = new Image("file:" + forum.getImage_f());
                imgView_reclamationmodifffffff.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
    }

    public void setServiceForum(ServiceForum serviceForum) {
        this.serviceForum = serviceForum;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize ComboBox with choices
        TFCategorieModif.getItems().addAll(
                "Art-Thérapie et Bien-Être",
                "Techniques Artistiques",
                "Témoignages et Inspirations",
                "Ressources et Outils",
                "Questions et Conseils"
        );
    }

    public void goafficherAction(ActionEvent actionEvent) {
        try {
            // Load the FXML file
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherForum.fxml")); // Adjust the path if necessary

            // Set the new scene
            Stage stage = (Stage) TFtitre.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Erreur lors du chargement de la vue.");
            alert.setTitle("Erreur");
            alert.show();
        }
    }

    public void setAfficherForumController(AfficherForumController afficherForumController) {
        
    }
}
