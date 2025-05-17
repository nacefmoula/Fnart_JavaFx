package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.esprit.models.Artwork;
import tn.esprit.services.serviceartwork;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ArtworkBackController implements Initializable {
    // Example FXML fields (add/rename as needed to match your artworkback.fxml)
    @FXML private TextField searchField;
    @FXML private Button refreshButton;
    @FXML private Button ajoutButton;
    @FXML private ListView<?> artworkListView; // Replace ? with your Artwork model
    @FXML private VBox profileContainer;
    @FXML private ImageView profileImage;
    @FXML private AnchorPane profilePanel;
    @FXML private ImageView profileImageLarge;
    @FXML private Label adminNameLabel;
    @FXML private Label adminEmailLabel;
    @FXML private Button approveButton;
    @FXML private Button updateButton;
    @FXML private Button editProfileButton;
    @FXML private ComboBox<?> sortComboBox;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private Button filterByPriceButton;
    @FXML private Button addArtworkButton;
    @FXML private Button exportToPDFButton;
    @FXML private ListView<Artwork> artworksListView;
    private final serviceartwork artworkService = new serviceartwork();
    private ObservableList<Artwork> artworkObservableList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadArtworks();
        artworksListView.setCellFactory(param -> new ArtworkListCell());
        // TODO: Initialize artwork list, set up listeners, etc.
    }

    private void loadArtworks() {
        artworkObservableList = FXCollections.observableArrayList(artworkService.getAll());
        artworksListView.setItems(artworkObservableList);
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        // TODO: Refresh artwork list
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        // TODO: Implement search logic
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // TODO: Implement logout logic
    }

    @FXML
    private void toggleProfilePanel() {
        // TODO: Show/hide profile panel
    }

    @FXML
    private void handleAjout(ActionEvent event) {
        // TODO: Open add artwork dialog/page
    }

    @FXML
    private void handleEditProfile(ActionEvent event) {
        // TODO: Implement profile editing logic
    }

    @FXML
    private void approveArtwork(ActionEvent event) {
        // TODO: Implement artwork approval logic
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        // TODO: Implement artwork update logic
    }

    @FXML
    private void filterByPrice(ActionEvent event) {
        // TODO: Implement price filter logic
    }

    @FXML
    private void handleAddArtwork(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddArtwork.fxml"));
            Parent root = loader.load();
            AddArtworkController controller = loader.getController();
            controller.setParentController(this);
            Stage stage = new Stage();
            stage.setTitle("Add New Artwork");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadArtworks(); // Refresh after closing
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not open Add Artwork dialog: " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    private void exportToPDF(ActionEvent event) {
        // TODO: Implement export to PDF logic
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        // TODO: Implement artwork delete logic
    }
}
