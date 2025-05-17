package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.control.Separator;
import tn.esprit.utils.CartManager;
import tn.esprit.models.Artwork;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class CartController {
    @FXML private VBox cartItemsContainer;
    @FXML private Label totalPriceLabel;
    @FXML private Button continueButton;
    @FXML private Button orderButton;

    @FXML
    public void initialize() {
        refreshCart();
        orderButton.setOnAction(e -> handleOrder(e));
    }

    private void refreshCart() {
        cartItemsContainer.getChildren().clear();
        double total = 0;
        java.util.Iterator<Artwork> iterator = CartManager.getCartItems().iterator();
        while (iterator.hasNext()) {
            Artwork artwork = iterator.next();
            HBox itemBox = new HBox(10);
            Label itemLabel = new Label(artwork.getTitre() + " - " + artwork.getPrix() + " DT");
            Button removeBtn = new Button("Supprimer");
            removeBtn.setOnAction(ev -> {
                CartManager.removeFromCart(artwork);
                refreshCart();
            });
            itemBox.getChildren().addAll(itemLabel, removeBtn);
            cartItemsContainer.getChildren().add(itemBox);
            cartItemsContainer.getChildren().add(new Separator());
            total += artwork.getPrix();
        }
        totalPriceLabel.setText("Total TTC: " + total + " DT");
    }

    @FXML
    public void handleOrder(ActionEvent event) {
        if (CartManager.getCartItems().isEmpty()) {
            showAlert("Erreur", "Votre panier est vide.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutCommande.fxml"));
            Parent root = loader.load();
            // Pass cart items to the controller
            AjoutCommandeController ajoutCommandeController = loader.getController();
            ajoutCommandeController.setCartItems(new java.util.ArrayList<>(CartManager.getCartItems()));
            Stage stage = new Stage();
            stage.setTitle("Finaliser la commande");
            stage.setScene(new Scene(root));
            stage.show();
            // Optionally, close the cart window:
            Stage cartStage = (Stage) orderButton.getScene().getWindow();
            cartStage.close();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de commande: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
