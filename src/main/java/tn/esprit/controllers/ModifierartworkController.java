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

public class ModifierartworkController implements Initializable, ParentControllerAware {
    @FXML private TextField titreTextField;
    @FXML private TextField descriprtionTextField;
    @FXML private TextField prixTextField;
    @FXML private TextField artistenomTextField;
    @FXML private TextField statusTextField;
    @FXML private ImageView imagePreview;
    @FXML private Button uploadButton;
    @FXML private Button saveButton;
    
    private String imagePath;
    private serviceartwork artworkService;
    private Artwork artwork;
    private AdminDashboardController parentController;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        artworkService = new serviceartwork();
        
        // Add validation for price field
        prixTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                prixTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
    
    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
        titreTextField.setText(artwork.getTitre());
        artistenomTextField.setText(artwork.getArtistenom());
        prixTextField.setText(String.valueOf(artwork.getPrix()));
        descriprtionTextField.setText(artwork.getDescription());
        statusTextField.setText(artwork.getStatus());
    
        if (artwork.getImage() != null && !artwork.getImage().isEmpty()) {
            String imgPath = artwork.getImage();
            if (imgPath.startsWith("http://") || imgPath.startsWith("https://")) {
                imagePreview.setImage(new Image(imgPath, true));
            } else {
                File imgFile = new File(imgPath);
                if (!imgFile.isAbsolute()) {
                    imgFile = new File(System.getProperty("user.dir"), imgPath);
                }
                if (imgFile.exists()) {
                    imagePreview.setImage(new Image(imgFile.toURI().toString()));
                } else {
                    imagePreview.setImage(null);
                }
            }
        } else {
            imagePreview.setImage(null);
        }
    }
    
    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (selectedFile != null) {
            File destDir = new File("images");
            if (!destDir.exists()) destDir.mkdirs();
            File destFile = new File(destDir, selectedFile.getName());
            try {
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                imagePath = "images/" + selectedFile.getName(); // Save relative path only
                imagePreview.setImage(new Image(destFile.toURI().toString()));
            } catch (IOException e) {
                showAlert("Erreur", "Impossible de copier l'image: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleSave() {
        if (validateForm()) {
            artwork.setTitre(titreTextField.getText());
            artwork.setArtistenom(artistenomTextField.getText());
            artwork.setPrix(Integer.parseInt(prixTextField.getText()));
            artwork.setDescription(descriprtionTextField.getText());
            artwork.setStatus(statusTextField.getText());
            if (imagePath != null) {
                artwork.setImage(imagePath); // Always save the full path
            }
            artworkService.update(artwork); // Actually update in DB/service
            if (parentController != null) {
                parentController.loadArtworks();
            }
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        }
    }
    
    @FXML
    private void handleUpdateOrder() {
        // TODO: Implement update order logic here
        // This is a placeholder to resolve FXML loading errors.
    }
    
    private boolean validateForm() {
        if (titreTextField.getText().isEmpty()) {
            showAlert("Validation Error", "Title is required");
            return false;
        }
        if (artistenomTextField.getText().isEmpty()) {
            showAlert("Validation Error", "Artist name is required");
            return false;
        }
        if (prixTextField.getText().isEmpty()) {
            showAlert("Validation Error", "Price is required");
            return false;
        }
        return true;
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @Override
    public void setParentController(Object parentController) {
        if (parentController instanceof AdminDashboardController) {
            this.parentController = (AdminDashboardController) parentController;
        }
    }
}