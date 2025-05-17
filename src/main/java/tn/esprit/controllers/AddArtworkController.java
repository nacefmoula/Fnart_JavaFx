package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.interfaces.ParentControllerAware;
import tn.esprit.models.Artwork;
import tn.esprit.services.serviceartwork;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

public class AddArtworkController implements Initializable, ParentControllerAware {
    @FXML private TextField titleField;
    @FXML private TextField artistField;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionArea;
    @FXML private ImageView imagePreview;
    @FXML private Button uploadButton;
    @FXML private Button saveButton;
    
    private String imagePath;
    private serviceartwork artworkService;
    private Object parentController;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        artworkService = new serviceartwork();
        setupValidation();
    }
    
    @Override
    public void setParentController(Object parentController) {
        this.parentController = parentController;
    }
    
    private void setupValidation() {
        // Add validation listeners for the fields
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                priceField.setText(oldValue);
            }
        });
    }
    
    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Artwork Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (selectedFile != null) {
            File destDir = new File("C:/xampp/htdocs/artwork_images");
            if (!destDir.exists()) destDir.mkdirs();
            File destFile = new File(destDir, selectedFile.getName());
            try {
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                imagePath = selectedFile.getName(); // Only save the filename
                imagePreview.setImage(new Image(destFile.toURI().toString()));
            } catch (IOException e) {
                showAlert("Erreur", "Impossible de copier l'image: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleSave() {
        if (validateForm()) {
            try {
                Artwork artwork = new Artwork();
                artwork.setTitre(titleField.getText());
                artwork.setArtistenom(artistField.getText());
                artwork.setPrix(Integer.parseInt(priceField.getText()));
                artwork.setDescription(descriptionArea.getText());
                artwork.setImage(imagePath); // Always save the filename
                artworkService.add(artwork); // Actually add to DB/service
                // Refresh the parent controller's artwork list
                if (parentController instanceof AdminDashboardController) {
                    ((AdminDashboardController) parentController).loadArtworks();
                }
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            } catch (Exception e) {
                System.err.println("[ERROR] Impossible d'ajouter l'œuvre : " + e.getMessage());
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'ajouter l'œuvre: " + e.getMessage());
            }
        }
    }
    
    private boolean validateForm() {
        if (titleField.getText().isEmpty() ||
            artistField.getText().isEmpty() ||
            priceField.getText().isEmpty() ||
            descriptionArea.getText().isEmpty() ||
            imagePath == null) {
            showAlert("Validation Error", "Please fill in all fields and upload an image");
            return false;
        }
        return true;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 