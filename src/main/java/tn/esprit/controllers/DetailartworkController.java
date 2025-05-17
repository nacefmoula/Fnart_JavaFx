package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.models.Artwork;
import tn.esprit.utils.CartManager;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class DetailartworkController implements Initializable {
    @FXML private Label titleLabel;
    @FXML private Label artistLabel;
    @FXML private Label priceLabel;
    @FXML private Label descriptionLabel;
    @FXML private ImageView artworkImage;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button addToCartButton;
    @FXML private Button backButton;
    
    private Artwork artwork;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int maxQty = 99;
        // If artwork has stock or status, adjust max quantity and disable if out of stock
        if (artwork != null && artwork.getStatus() != null && artwork.getStatus().equalsIgnoreCase("out_of_stock")) {
            if (addToCartButton != null) addToCartButton.setDisable(true);
            if (quantitySpinner != null) quantitySpinner.setDisable(true);
        } else {
            if (quantitySpinner != null) {
                quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxQty, 1));
            }
            if (addToCartButton != null) {
                addToCartButton.setDisable(false);
                addToCartButton.setOnAction(e -> handleAddToCart());
            }
        }
        if (backButton != null) {
            backButton.setOnAction(e -> handleBack());
        }
    }
    
    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
        updateUI();
    }
    
    private void updateUI() {
        if (artwork != null) {
            titleLabel.setText(artwork.getTitre());
            artistLabel.setText(artwork.getArtistenom());
            priceLabel.setText(String.valueOf(artwork.getPrix()));
            descriptionLabel.setText(artwork.getDescription());

            String imgPath = artwork.getImage();
            Image image = null;
            if (imgPath != null && !imgPath.isEmpty()) {
                try {
                    // Try as resource first
                    URL resourceUrl = getClass().getResource("/assets/artwork_images/" + imgPath);
                    if (resourceUrl != null) {
                        image = new Image(resourceUrl.toExternalForm());
                    } else if (imgPath.startsWith("http")) {
                        image = new Image(imgPath);
                    } else {
                        File file = new File(imgPath);
                        if (file.exists()) {
                            image = new Image(file.toURI().toString());
                        } else {
                            System.err.println("Image not found: " + imgPath);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error loading image: " + imgPath + " - " + e.getMessage());
                }
            }
            if (image != null) artworkImage.setImage(image);
        }
    }
    
    private void handleAddToCart() {
        if (artwork != null) {
            int qty = 1;
            if (quantitySpinner != null && quantitySpinner.getValue() != null) {
                qty = quantitySpinner.getValue();
            }
            for (int i = 0; i < qty; i++) {
                CartManager.addToCart(artwork);
            }
            showAlert("Succès", "L'œuvre a été ajoutée au panier !");
            // Optional: close after add
            // backButton.getScene().getWindow().hide();
        }
    }

    private void handleBack() {
        // Close the current window/dialog
        backButton.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 