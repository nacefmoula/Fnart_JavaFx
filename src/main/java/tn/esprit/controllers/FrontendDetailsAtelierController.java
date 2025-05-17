package tn.esprit.controllers;

import tn.esprit.models.Atelier;
import tn.esprit.services.InscriptionAtelierService;
import tn.esprit.services.AtelierService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javafx.stage.FileChooser;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class FrontendDetailsAtelierController {

    @FXML
    private Text titreLabel;

    @FXML
    private Text lieuLabel;

    @FXML
    private Text descriptionLabel;

    @FXML
    private Text dateLabel;

    @FXML
    private Text placesLabel;

    @FXML
    private ImageView imageView;

    @FXML
    private ListView<String> inscriptionsListView;

    @FXML
    private ImageView qrCodeImageView;
    
    @FXML
    private WebView mapWebView;

    private Atelier atelier;

    private final InscriptionAtelierService inscriptionService = new InscriptionAtelierService();
    private final AtelierService atelierService = new AtelierService();
    
    @FXML
    public void initialize() {
        // Initialize WebView for map
        if (mapWebView != null) {
            WebEngine webEngine = mapWebView.getEngine();
            webEngine.setJavaScriptEnabled(true);
        }
    }

    public void setAtelier(Atelier atelier) {
        this.atelier = atelier;
        updateUI();
    }

    public void updateUI() {
        if (atelier != null) {
            titreLabel.setText(atelier.getTitre());
            lieuLabel.setText(atelier.getLieu());
            descriptionLabel.setText(atelier.getDescription());
            
            // Format la date avec SimpleDateFormat pour une meilleure présentation
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'à' HH'h'mm");
            dateLabel.setText(sdf.format(atelier.getDate()));
            
            placesLabel.setText(String.valueOf(atelier.getParticipantMax()));

            // Chargement de l'image avec gestion d'erreur améliorée
            if (atelier.getImage() != null && !atelier.getImage().isEmpty()) {
                try {
                    File file = new File(atelier.getImage());
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString());
                        imageView.setImage(image);
                    } else {
                        // Image par défaut si le fichier n'existe pas
                        imageView.setImage(new Image("/images/workshop-icon.png"));
                    }
                } catch (Exception e) {
                    System.err.println("Erreur chargement image: " + e.getMessage());
                    // Image par défaut en cas d'erreur
                    try {
                        imageView.setImage(new Image("/images/workshop-icon.png"));
                    } catch (Exception ex) {
                        System.err.println("Image par défaut non disponible: " + ex.getMessage());
                    }
                }
            } else {
                // Image par défaut si aucune image spécifiée
                try {
                    imageView.setImage(new Image("/images/workshop-icon.png"));
                } catch (Exception e) {
                    System.err.println("Image par défaut non disponible: " + e.getMessage());
                }
            }

            // Génération et affichage du QR code
            try {
                BufferedImage qrCode = atelierService.generateQRCode(atelier);
                qrCodeImageView.setImage(SwingFXUtils.toFXImage(qrCode, null));
            } catch (Exception e) {
                System.err.println("Erreur génération QR code: " + e.getMessage());
            }
            
            // Chargement des inscriptions s'il y a une ListView définie
            if (inscriptionsListView != null) {
                loadInscriptions();
            }
            
            // Affichage de la carte avec Leaflet
            loadMapWithLocation();
        }
    }
    
    private void loadInscriptions() {
        try {
            // Récupérer les inscriptions depuis la base de données
            List<String> inscriptions = inscriptionService.getByAtelier(atelier.getId())
                    .stream()
                    .map(i -> i.getNomTemporaire() + " (" + i.getEmailTemporaire() + ")")
                    .collect(Collectors.toList());
            
            // Afficher les inscriptions dans la ListView
            inscriptionsListView.getItems().clear();
            inscriptionsListView.getItems().addAll(inscriptions);
            
            // Afficher un message si aucune inscription
            if (inscriptions.isEmpty()) {
                inscriptionsListView.setPlaceholder(new Label("Aucune inscription pour le moment"));
            }
            
            // Améliorer l'apparence des éléments de la liste
            inscriptionsListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle("-fx-padding: 5px 0px; -fx-font-size: 13px;");
                    }
                }
            });
            
            System.out.println("Inscriptions chargées: " + inscriptions.size() + " participant(s)");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des inscriptions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadMapWithLocation() {
        if (mapWebView == null) {
            System.err.println("Erreur: mapWebView est null");
            return;
        }
        
        if (atelier == null) {
            System.err.println("Erreur: atelier est null");
            return;
        }
        
        System.out.println("Chargement de la carte pour: " + atelier.getTitre());
        System.out.println("Coordonnées: Latitude=" + atelier.getLatitude() + ", Longitude=" + atelier.getLongitude());
        
        // Vérification plus permissive des coordonnées (si l'une des coordonnées est définie, on considère qu'il y a une position)
        boolean hasCoordinates = !(Double.isNaN(atelier.getLatitude()) || Double.isNaN(atelier.getLongitude()));
        
        // Si les coordonnées sont à zéro, on vérifie si c'est parce qu'elles n'ont pas été définies
        if (atelier.getLatitude() == 0 && atelier.getLongitude() == 0) {
            // On vérifie dans le champ lieu s'il contient des coordonnées
            String lieu = atelier.getLieu();
            if (lieu != null && !lieu.isEmpty()) {
                try {
                    // Tentative d'extraire des coordonnées du format "lat, lng"
                    String[] parts = lieu.split(",");
                    if (parts.length == 2) {
                        double possibleLat = Double.parseDouble(parts[0].trim());
                        double possibleLng = Double.parseDouble(parts[1].trim());
                        
                        // Si on a pu parser les coordonnées, on les utilise
                        System.out.println("Coordonnées extraites du champ lieu: " + possibleLat + ", " + possibleLng);
                        showMapWithCoordinates(possibleLat, possibleLng);
                        return;
                    }
                } catch (NumberFormatException e) {
                    // Si le format n'est pas des coordonnées, on continue avec le comportement par défaut
                    System.out.println("Le champ lieu ne contient pas de coordonnées au format attendu");
                }
            }
            
            // Si on n'a pas pu extraire de coordonnées et qu'elles sont à zéro, on utilise des coordonnées par défaut (centre de la Tunisie)
            System.out.println("Utilisation des coordonnées par défaut (centre de la Tunisie)");
            showMapWithCoordinates(34.0, 9.0);
        } else {
            // Utiliser les coordonnées de l'atelier
            showMapWithCoordinates(atelier.getLatitude(), atelier.getLongitude());
        }
    }
    
    private void showMapWithCoordinates(double lat, double lng) {
        WebEngine webEngine = mapWebView.getEngine();
        
        try {
            // Charger la page HTML de la carte
            System.out.println("Chargement de la carte Leaflet...");
            if (getClass().getResource("/leaflet-viewer.html") == null) {
                System.err.println("Error: leaflet-viewer.html not found");
                return;
            }
            webEngine.load(getClass().getResource("/leaflet-viewer.html").toExternalForm());
            
            // Créer un bridge Java-JavaScript pour recevoir la notification quand la carte est prête
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    System.out.println("Page HTML chargée, attente d'initialisation de la carte...");
                    
                    try {
                        // Créer un object connecteur Java pour recevoir des callbacks de JavaScript
                        netscape.javascript.JSObject window = (netscape.javascript.JSObject) webEngine.executeScript("window");
                        MapBridge bridge = new MapBridge();
                        window.setMember("javaConnector", bridge);
                        
                        // Vérifier si la carte est déjà prête
                        Object result = webEngine.executeScript("isMapReady()");
                        boolean isReady = result instanceof Boolean && (Boolean) result;
                        
                        if (isReady) {
                            System.out.println("Carte déjà initialisée, affichage immédiat du marqueur");
                            // Appel de la fonction JavaScript pour afficher le lieu sur la carte
                            webEngine.executeScript(
                                String.format("showLocation(%f, %f)", lat, lng)
                            );
                        } else {
                            System.out.println("En attente de l'initialisation complète de la carte pour afficher le marqueur...");
                            // La carte n'est pas encore prête, les coordonnées seront utilisées automatiquement quand elle sera prête
                            webEngine.executeScript(
                                String.format("window.pendingLat = %f; window.pendingLng = %f;", lat, lng)
                            );
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur lors de l'interaction avec JavaScript: " + e.getMessage());
                        e.printStackTrace();
                        
                        // Tentative alternative après un délai
                        new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        javafx.application.Platform.runLater(() -> {
                                            try {
                                                System.out.println("Tentative alternative d'affichage du marqueur...");
                                                webEngine.executeScript(
                                                    String.format("showLocation(%f, %f);", lat, lng)
                                                );
                                            } catch (Exception e) {
                                                System.err.println("Échec de la tentative alternative: " + e.getMessage());
                                                e.printStackTrace();
                                            }
                                        });
                                    } catch (Exception e) {
                                        System.err.println("Erreur dans la tâche programmée: " + e.getMessage());
                                    }
                                }
                            },
                            2000 // Attendre 2 secondes
                        );
                    }
                } else if (newState == javafx.concurrent.Worker.State.FAILED) {
                    System.err.println("Échec du chargement de la carte");
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la carte: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Bridge pour communiquer avec JavaScript
    public class MapBridge {
        public void mapReady() {
            System.out.println("Notification reçue: la carte est prête");
        }
    }

    @FXML
    private void closeDetails() {
        Stage stage = (Stage) titreLabel.getScene().getWindow();
        stage.close();
    }


    @FXML
    private void downloadPdf() {
        if (atelier == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer les détails de l'atelier en PDF");
        fileChooser.setInitialFileName(atelier.getTitre().replaceAll("\\s+", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

        Stage stage = (Stage) titreLabel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    // En-tête avec titre de l'atelier
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                    contentStream.newLineAtOffset(50, 750);
                    contentStream.showText(atelier.getTitre());
                    contentStream.endText();
                    
                    // Ligne de séparation
                    contentStream.setLineWidth(1f);
                    contentStream.moveTo(50, 740);
                    contentStream.lineTo(550, 740);
                    contentStream.stroke();

                    // Informations détaillées
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    contentStream.newLineAtOffset(50, 710);
                    contentStream.showText("Détails de l'Atelier");
                    contentStream.endText();
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(50, 680);
                    contentStream.showText("Lieu: " + atelier.getLieu());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Date: " + sdf.format(atelier.getDate()));
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Participants max: " + atelier.getParticipantMax());
                    contentStream.endText();
                    
                    // Section description
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    contentStream.newLineAtOffset(50, 600);
                    contentStream.showText("Description:");
                    contentStream.endText();
                    
                    // Texte de description avec gestion du multi-lignes
                    String description = atelier.getDescription();
                    String[] descriptionLines = splitStringToLines(description, 80); // limite à 80 caractères par ligne
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(50, 580);
                    
                    for (String line : descriptionLines) {
                        contentStream.showText(line);
                        contentStream.newLineAtOffset(0, -15); // espacement entre les lignes
                    }
                    
                    contentStream.endText();
                    
                    // Pied de page avec coordonnées
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                    contentStream.newLineAtOffset(50, 50);
                    contentStream.showText("Document généré le " + new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()));
                    contentStream.endText();
                }

                document.save(file);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Le PDF a été enregistré avec succès.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec d'enregistrement du PDF: " + e.getMessage());
            }
        }
    }
    
    // Méthode pour fractionner une longue chaîne en lignes pour le PDF
    private String[] splitStringToLines(String text, int maxCharsInLine) {
        if (text == null || text.isEmpty()) {
            return new String[] {"Aucune description disponible."};
        }
        
        // Compte approximatif du nombre de lignes nécessaires
        int numLines = (text.length() + maxCharsInLine - 1) / maxCharsInLine;
        String[] lines = new String[numLines];
        
        int currentPos = 0;
        for (int i = 0; i < numLines; i++) {
            int endPos = Math.min(currentPos + maxCharsInLine, text.length());
            
            // Ajustement pour ne pas couper les mots
            if (endPos < text.length()) {
                int spacePos = text.lastIndexOf(' ', endPos);
                if (spacePos > currentPos) {
                    endPos = spacePos;
                }
            }
            
            lines[i] = text.substring(currentPos, endPos);
            currentPos = endPos;
            
            // Avancer au-delà de l'espace si nécessaire
            if (currentPos < text.length() && text.charAt(currentPos) == ' ') {
                currentPos++;
            }
            
            if (currentPos >= text.length()) {
                break;
            }
        }
        
        return lines;
    }
    
    @FXML
    private void handleInscription() {
        if (atelier == null) {
            return;
        }
        
        // Create a custom dialog for a better user experience
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Inscription à l'atelier");
        dialog.setHeaderText("Rejoignez l'atelier: " + atelier.getTitre());
        
        // Set the icon
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        try {
            stage.getIcons().add(new Image(getClass().getResource("/images/workshop-icon.png").toString()));
            // Added null check for resource
            if (getClass().getResource("/images/workshop-icon.png") == null) {
                System.err.println("Error: workshop-icon.png not found");
                return;
            }
        } catch (Exception e) {
            System.err.println("Impossible de charger l'icône: " + e.getMessage());
        }
        
        // Set the button types
        ButtonType inscriptionButtonType = new ButtonType("S'inscrire", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(inscriptionButtonType, cancelButtonType);
        
        // Create the form grid for the inputs
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 20, 20));
        
        // Create form fields with validation
        TextField nomField = new TextField();
        nomField.setPromptText("Votre nom");
        TextField emailField = new TextField();
        emailField.setPromptText("votre.email@exemple.com");
        TextField telephoneField = new TextField();
        telephoneField.setPromptText("Téléphone (optionnel)");
        TextArea commentaireArea = new TextArea();
        commentaireArea.setPromptText("Commentaires ou demandes particulières (optionnel)");
        commentaireArea.setPrefRowCount(3);
        commentaireArea.setWrapText(true);
        
        // Add fields to grid
        grid.add(new Label("Nom complet:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Téléphone:"), 0, 2);
        grid.add(telephoneField, 1, 2);
        grid.add(new Label("Commentaire:"), 0, 3);
        grid.add(commentaireArea, 1, 3);
        
        // Add workshop details
        Separator separator = new Separator();
        separator.setPadding(new javafx.geometry.Insets(10, 0, 10, 0));
        grid.add(separator, 0, 4, 2, 1);
        
        grid.add(new Label("Atelier:"), 0, 5);
        Label atelierLabel = new Label(atelier.getTitre());
        atelierLabel.setStyle("-fx-font-weight: bold;");
        grid.add(atelierLabel, 1, 5);
        
        grid.add(new Label("Date:"), 0, 6);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'à' HH'h'mm");
        grid.add(new Label(sdf.format(atelier.getDate())), 1, 6);
        
        grid.add(new Label("Lieu:"), 0, 7);
        grid.add(new Label(atelier.getLieu()), 1, 7);
        
        // Add error labels for validation feedback
        Label nomErrorLabel = new Label("");
        nomErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        Label emailErrorLabel = new Label("");
        emailErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        grid.add(nomErrorLabel, 1, 0, 1, 1);
        grid.add(emailErrorLabel, 1, 1, 1, 1);
        GridPane.setValignment(nomErrorLabel, javafx.geometry.VPos.BOTTOM);
        GridPane.setValignment(emailErrorLabel, javafx.geometry.VPos.BOTTOM);
        
        // Add validation
        final Button btInscription = (Button) dialog.getDialogPane().lookupButton(inscriptionButtonType);
        btInscription.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateInputs(nomField, emailField, nomErrorLabel, emailErrorLabel)) {
                event.consume();
            }
        });
        
        // Set the dialog content
        dialog.getDialogPane().setContent(grid);
        
        // Apply CSS
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/AdminDashboard.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        
        // Request focus on the nom field by default
        javafx.application.Platform.runLater(nomField::requestFocus);
        
        // Show the dialog and process the result
        Optional<ButtonType> result = dialog.showAndWait();
        
        result.ifPresent(buttonType -> {
            if (buttonType == inscriptionButtonType) {
                // Get the inputs
                String nom = nomField.getText().trim();
                String email = emailField.getText().trim();
                String telephone = telephoneField.getText().trim();
                String commentaire = commentaireArea.getText().trim();
                
                // Process the inscription
                try {
                    inscriptionService.inscrire(atelier, nom, email);
                    
                    // Show confirmation dialog with animation
                    showConfirmationDialog(nom);
                    
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur d'inscription", 
                            "Une erreur est survenue lors de l'inscription: " + e.getMessage());
                }
            }
        });
    }
    
    private boolean validateInputs(TextField nomField, TextField emailField, Label nomErrorLabel, Label emailErrorLabel) {
        boolean isValid = true;
        
        // Reset error messages
        nomErrorLabel.setText("");
        emailErrorLabel.setText("");
        
        // Validate name
        if (nomField.getText().trim().isEmpty()) {
            nomErrorLabel.setText("Le nom est requis");
            nomField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            nomField.setStyle("");
        }
        
        // Validate email with regex pattern
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (emailField.getText().trim().isEmpty()) {
            emailErrorLabel.setText("L'email est requis");
            emailField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (!emailField.getText().trim().matches(emailPattern)) {
            emailErrorLabel.setText("Format d'email invalide");
            emailField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            emailField.setStyle("");
        }
        
        return isValid;
    }
    
    private void showConfirmationDialog(String nom) {
        Dialog<ButtonType> confirmDialog = new Dialog<>();
        confirmDialog.setTitle("Inscription réussie");
        confirmDialog.setHeaderText(null);
        
        VBox content = new VBox(15);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setPadding(new javafx.geometry.Insets(20));
        
        // Success icon
        FontIcon checkIcon = new FontIcon();
        checkIcon.setIconLiteral("fas-check-circle");
        checkIcon.setIconSize(60);
        checkIcon.setIconColor(javafx.scene.paint.Color.GREEN);
        
        // Success message
        Label confirmLabel = new Label("Félicitations " + nom + " !");
        confirmLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label messageLabel = new Label("Votre inscription à l'atelier a été enregistrée avec succès.");
        messageLabel.setStyle("-fx-font-size: 14px;");
        messageLabel.setWrapText(true);
        
        // Add components to the dialog
        content.getChildren().addAll(checkIcon, confirmLabel, messageLabel);
        
        confirmDialog.getDialogPane().setContent(content);
        confirmDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        confirmDialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/AdminDashboard.css").toExternalForm());
        
        // Apply fade-in animation
        content.setOpacity(0);
        javafx.animation.FadeTransition fadeTransition = new javafx.animation.FadeTransition(javafx.util.Duration.seconds(1), content);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
        
        confirmDialog.showAndWait();
    }
    
    // Classe d'icône pour faciliter l'affichage d'icônes dans les boîtes de dialogue
    public static class FontIcon extends javafx.scene.layout.Region {
        private String iconLiteral = "fas-check-circle";
        private int iconSize = 16;
        private javafx.scene.paint.Color iconColor = javafx.scene.paint.Color.BLACK;
        
        public FontIcon() {
            updateIcon();
        }
        
        public String getIconLiteral() {
            return iconLiteral;
        }
        
        public void setIconLiteral(String iconLiteral) {
            this.iconLiteral = iconLiteral;
            updateIcon();
        }
        
        public int getIconSize() {
            return iconSize;
        }
        
        public void setIconSize(int iconSize) {
            this.iconSize = iconSize;
            updateIcon();
        }
        
        public javafx.scene.paint.Color getIconColor() {
            return iconColor;
        }
        
        public void setIconColor(javafx.scene.paint.Color iconColor) {
            this.iconColor = iconColor;
            updateIcon();
        }
        
        private void updateIcon() {
            // Use JavaFX text shape as a workaround for font icons
            setText("✓");
            setStyle(
                String.format(
                    "-fx-font-size: %dpx; -fx-text-fill: %s;",
                    iconSize, 
                    toRGBCode(iconColor)
                )
            );
        }
        
        private void setText(String text) {
            javafx.scene.text.Text textNode = new javafx.scene.text.Text(text);
            textNode.setFont(javafx.scene.text.Font.font(textNode.getFont().getFamily(), iconSize));
            getChildren().setAll(textNode);
        }
        
        private String toRGBCode(javafx.scene.paint.Color color) {
            return String.format(
                "#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
            );
        }
    }

    @FXML
    private void handleShare() {
        if (atelier == null) {
            return;
        }
        
        // Menu contextuel avec options de partage
        ContextMenu shareMenu = new ContextMenu();
        
        MenuItem emailItem = new MenuItem("Partager par e-mail");
        emailItem.setOnAction(event -> {
            try {
                // Construction de l'URL de l'e-mail
                String subject = "Découvrez cet atelier intéressant: " + atelier.getTitre();
                String body = "Bonjour,\n\nJ'ai découvert cet atelier qui pourrait t'intéresser: " +
                        atelier.getTitre() + " à " + atelier.getLieu() + " le " + 
                        new SimpleDateFormat("dd/MM/yyyy").format(atelier.getDate()) + ".\n\n" +
                        "Description: " + atelier.getDescription() + "\n\n" +
                        "À bientôt!";
                
                // Encodage pour URL
                body = java.net.URLEncoder.encode(body, "UTF-8").replace("+", "%20");
                subject = java.net.URLEncoder.encode(subject, "UTF-8").replace("+", "%20");
                
                // Ouverture du client email par défaut
                java.awt.Desktop.getDesktop().browse(
                        new java.net.URI("mailto:?subject=" + subject + "&body=" + body));
                
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Impossible d'ouvrir le client e-mail: " + e.getMessage());
            }
        });
        
        MenuItem copyItem = new MenuItem("Copier les informations");
        copyItem.setOnAction(event -> {
            String info = atelier.getTitre() + "\nLieu: " + atelier.getLieu() + 
                    "\nDate: " + new SimpleDateFormat("dd/MM/yyyy").format(atelier.getDate()) +
                    "\n\n" + atelier.getDescription();
            
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(info);
            clipboard.setContent(content);
            
            showAlert(Alert.AlertType.INFORMATION, "Copié", 
                    "Les informations de l'atelier ont été copiées dans le presse-papiers.");
        });
        
        // Ajout des options au menu
        shareMenu.getItems().addAll(emailItem, copyItem);
        
        // Affichage du menu à côté du bouton qui a déclenché l'action
        Button shareButton = (Button) titreLabel.getScene().lookup(".share-button");
        if (shareButton != null) {
            shareMenu.show(shareButton, javafx.geometry.Side.BOTTOM, 0, 0);
        } else {
            // Fallback si le bouton n'est pas trouvé
            shareMenu.show(titreLabel, javafx.geometry.Side.BOTTOM, 0, 0);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setAdminView(boolean isAdmin) {
        // Cette méthode peut être utilisée pour afficher/masquer certaines 
        // fonctionnalités en fonction du mode admin
    }
}
