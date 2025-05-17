package tn.esprit.controllers;

import javafx.scene.Parent;
import tn.esprit.models.Atelier;
import tn.esprit.services.AtelierService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Date;

public class ModifierAtelierController {

    @FXML
    private TextField titreField;

    @FXML
    private TextField lieuField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField participantMaxField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField imageField;
    
    @FXML
    private ImageView previewImage;
    
    @FXML
    private VBox mapContainer;

    @FXML
    private WebView mapWebView;

    @FXML
    private Label coordinatesLabel;

    private final AtelierService atelierService = new AtelierService();

    private Atelier atelier;
    private AfficherAtelierController parentController;
    private File selectedImageFile;
    private boolean mapVisible = false;
    private Double selectedLat = null;
    private Double selectedLng = null;

    @FXML
    public void initialize() {
        // Initialize map-related components
        if (mapContainer != null) {
            mapContainer.setVisible(false);
            mapContainer.setManaged(false);
        }
    }

    public void setAtelier(Atelier atelier) {
        this.atelier = atelier;
        populateFields();
    }

    public void setParentController(AfficherAtelierController parentController) {
        this.parentController = parentController;
    }

    private void populateFields() {
        if (atelier != null) {
            titreField.setText(atelier.getTitre());
            lieuField.setText(atelier.getLieu());
            datePicker.setValue(new java.sql.Date(atelier.getDate().getTime()).toLocalDate());
            participantMaxField.setText(String.valueOf(atelier.getParticipantMax()));
            descriptionArea.setText(atelier.getDescription());
            imageField.setText(atelier.getImage());
            
            // Load and display the image preview
            if (atelier.getImage() != null && !atelier.getImage().isEmpty()) {
                try {
                    File imageFile = new File(atelier.getImage());
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        previewImage.setImage(image);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading image: " + e.getMessage());
                }
            }
            
            // Store the coordinates from the atelier if they exist
            selectedLat = atelier.getLatitude();
            selectedLng = atelier.getLongitude();
            if (selectedLat != null && selectedLng != null) {
                coordinatesLabel.setText("Lieu sélectionné : Lat=" + selectedLat + ", Lng=" + selectedLng);
            }
        }
    }

    public void setBackController(BackAtelierController backAtelierController) {

    }

    // Bridge pour recevoir les coordonnées depuis JavaScript
    public class MapLocationBridge {
        public void updateLocation(double lng, double lat) {
            System.out.println("Coordonnées reçues: lat=" + lat + ", lng=" + lng);
            javafx.application.Platform.runLater(() -> {
                selectedLat = lat;
                selectedLng = lng;
                coordinatesLabel.setText("Lieu sélectionné : Lat=" + lat + ", Lng=" + lng);
            });
        }
    }

    @FXML
    private void toggleMapView() {
        mapVisible = !mapVisible;
        mapContainer.setVisible(mapVisible);
        mapContainer.setManaged(mapVisible);
        if (mapVisible) {
            WebEngine webEngine = mapWebView.getEngine();
            // Use Leaflet instead of Mapbox
            webEngine.load(getClass().getResource("/leaflet-selector.html").toExternalForm());
            webEngine.setJavaScriptEnabled(true);
            
            // Expose le bridge à JavaScript
            final MapLocationBridge javaBridge = new MapLocationBridge();
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    // Ajouter l'objet bridge au contexte JavaScript
                    netscape.javascript.JSObject window = (netscape.javascript.JSObject) webEngine.executeScript("window");
                    window.setMember("javaBridge", javaBridge);
                    
                    // Initialiser le bridge côté JavaScript
                    webEngine.executeScript(
                        "window.javaConnector = {" +
                        "  updateLocation: function(lng, lat) {" +
                        "    console.log('Envoi des coordonnées: lat=' + lat + ', lng=' + lng);" +
                        "    javaBridge.updateLocation(lng, lat);" +
                        "  }" +
                        "};"
                    );
                    
                    // Si on a déjà des coordonnées, centrer la carte sur ces coordonnées
                    if (selectedLat != null && selectedLng != null) {
                        webEngine.executeScript(
                            "if (typeof setMapCenter === 'function') {" +
                            "  setMapCenter(" + selectedLat + ", " + selectedLng + ");" +
                            "  addMarker(" + selectedLat + ", " + selectedLng + ");" +
                            "}"
                        );
                    }
                }
            });
        }
    }

    @FXML
    private void confirmLocation() {
        // Vérifie si les coordonnées sont déjà enregistrées via le bridge
        if (selectedLat == null || selectedLng == null) {
            // Si non, essaie de récupérer les coordonnées directement depuis JavaScript
            try {
                WebEngine webEngine = mapWebView.getEngine();
                
                // Récupère la latitude et la longitude via les fonctions JavaScript
                Object latObj = webEngine.executeScript("getSelectedLatitude()");
                Object lngObj = webEngine.executeScript("getSelectedLongitude()");
                
                if (latObj != null && lngObj != null) {
                    // Convertit en Double
                    if (latObj instanceof Number) {
                        selectedLat = ((Number) latObj).doubleValue();
                    }
                    if (lngObj instanceof Number) {
                        selectedLng = ((Number) lngObj).doubleValue();
                    }
                    
                    System.out.println("Coordonnées récupérées depuis JavaScript: lat=" + selectedLat + ", lng=" + selectedLng);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération des coordonnées: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Vérification finale des coordonnées
        if (selectedLat != null && selectedLng != null) {
            // Met à jour le champ lieuField et le label des coordonnées
            lieuField.setText(selectedLat + ", " + selectedLng);
            coordinatesLabel.setText("Lieu sélectionné : Lat=" + selectedLat + ", Lng=" + selectedLng);
            // Masque la carte après confirmation
            mapContainer.setVisible(false);
            mapContainer.setManaged(false);
            mapVisible = false;
        } else {
            showAlert(Alert.AlertType.WARNING, "Sélection du lieu", "Veuillez sélectionner un lieu sur la carte avant de confirmer.");
        }
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) titreField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedImageFile = file;
            imageField.setText(file.getAbsolutePath());
            
            // Update preview image
            try {
                Image image = new Image(file.toURI().toString());
                previewImage.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading image preview: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleModifier() {
        try {
            String titre = titreField.getText();
            String lieu = lieuField.getText();
            Date date = Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            int participantMax = Integer.parseInt(participantMaxField.getText());
            String description = descriptionArea.getText();
            String image = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : atelier.getImage();

            if (titre.isEmpty() || lieu.isEmpty() || description.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            atelier.setTitre(titre);
            atelier.setLieu(lieu);
            atelier.setDate(date);
            atelier.setParticipantMax(participantMax);
            atelier.setDescription(description);
            atelier.setImage(image);
            
            // Update coordinates if they were selected
            if (selectedLat != null && selectedLng != null) {
                atelier.setLatitude(selectedLat);
                atelier.setLongitude(selectedLng);
            }

            atelierService.modifier(atelier);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Atelier modifié avec succès.");

            if (parentController != null) {
                parentController.refreshAteliers();
            }

            switchScene("/AdminDashboard.fxml", "Liste des Ateliers");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nombre maximum de participants doit être un nombre entier.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification de l'atelier: " + e.getMessage());
        }
    }

    @FXML
    private void resetForm() {
        if (atelier != null) {
            populateFields(); // Reset to original values
        }
    }

    @FXML
    private void goBack() {
        if (parentController != null) {
            parentController.refreshAteliers();
        }
        switchScene("/AdminDashboard.fxml", "Liste des Ateliers");
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la scène: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    @FXML
    private void redirectToAtelierManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.handleAfficherAtelier();

            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard - Atelier Management");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de rediriger vers la gestion des ateliers: " + e.getMessage());
        }
    }
}
