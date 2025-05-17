package tn.esprit.controllers;

import tn.esprit.models.Atelier;
import tn.esprit.services.AtelierService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Date;

public class AjouterAtelierController {

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
    private VBox mapContainer;

    @FXML
    private WebView mapWebView;

    @FXML
    private Label coordinatesLabel;

    private File selectedImageFile;

    private boolean mapVisible = false;
    private Double selectedLat = null;
    private Double selectedLng = null;

    private final AtelierService atelierService = new AtelierService();
    private BackAtelierController backController; // Reference to the parent controller

    public void setBackController(BackAtelierController backController) {
        this.backController = backController;
    }

    @FXML
    public void initialize() {
        // Initialize map-related components
        mapContainer.setVisible(false);
        mapContainer.setManaged(false);
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
        }
    }

    // Bridge pour recevoir les coordonnées depuis JavaScript
    public class MapLocationBridge {
        public void updateLocation(double lng, double lat) {
            System.out.println("Coordonnées reçues: lat=" + lat + ", lng=" + lng); // Ajout d'un log pour déboguer
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
                    
                    // Test si la connexion fonctionne
                    webEngine.executeScript(
                        "console.log('Bridge initialisé');" +
                        "setTimeout(function() {" +
                        "  if (window.javaConnector) {" +
                        "    console.log('Test de communication...');" +
                        "  }" +
                        "}, 500);"
                    );
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
    private void handleAjouter() {
        try {
            String titre = titreField.getText();
            String lieu = lieuField.getText();
            String description = descriptionArea.getText();
            String imagePath = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : null;

            if (titre.isEmpty() || lieu.isEmpty() || description.isEmpty() || datePicker.getValue() == null || participantMaxField.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            int participantMax = Integer.parseInt(participantMaxField.getText());
            Date date = Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Create a new Atelier object
            Atelier atelier = new Atelier();
            atelier.setTitre(titre);
            atelier.setLieu(lieu);
            atelier.setDate(date);
            atelier.setParticipantMax(participantMax);
            atelier.setDescription(description);
            atelier.setImage(imagePath);
            
            // Save the coordinates if selected
            if (selectedLat != null && selectedLng != null) {
                atelier.setLatitude(selectedLat);
                atelier.setLongitude(selectedLng);
            }

            // Save the atelier using the service
            boolean success = atelierService.ajouter(atelier);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Atelier ajouté avec succès.");

                // Refresh the list in the parent controller
                if (backController != null) {
                    backController.refreshTable();
                }

                // Redirect to the Admin Dashboard's Atelier Management section
                redirectToAtelierManagement();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'ajout de l'atelier.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nombre maximum de participants doit être un nombre entier.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }

    private void redirectToAtelierManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
            Parent root = loader.load();

            // Assuming AdminDashboardController has a method to switch to Atelier Management
            AdminDashboardController controller = loader.getController();
            controller.switchToAtelierManagement();

            Stage stage = (Stage) titreField.getScene().getWindow();
            System.out.println("Displaying stage: " + stage.getTitle());
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard - Atelier Management");
            stage.show();
            System.out.println("Stage displayed successfully: " + stage.getTitle());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de rediriger vers la gestion des ateliers : " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        redirectToAtelierManagement();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void resetForm(ActionEvent actionEvent) {

    }

    @FXML
    private void handleValidate() {
        // Perform validation logic for the form
        StringBuilder validationErrors = new StringBuilder();

        if (titreField.getText().isEmpty()) {
            validationErrors.append("Le titre est requis.\n");
        }
        if (lieuField.getText().isEmpty()) {
            validationErrors.append("Le lieu est requis.\n");
        }
        if (datePicker.getValue() == null) {
            validationErrors.append("La date est requise.\n");
        }
        if (participantMaxField.getText().isEmpty() || !participantMaxField.getText().matches("\\d+")) {
            validationErrors.append("Le nombre maximum de participants doit être un nombre valide.\n");
        }
        if (descriptionArea.getText().isEmpty()) {
            validationErrors.append("La description est requise.\n");
        }

        if (validationErrors.length() > 0) {
            // Show validation errors
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreurs de validation");
            alert.setHeaderText(null);
            alert.setContentText(validationErrors.toString());
            alert.showAndWait();
        } else {
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Validation réussie");
            alert.setHeaderText(null);
            alert.setContentText("Tous les champs sont valides.");
            alert.showAndWait();
        }
    }
}
