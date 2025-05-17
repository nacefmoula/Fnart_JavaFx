package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import tn.esprit.models.Artwork;
import tn.esprit.services.serviceartwork;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import javafx.geometry.Side;
import tn.esprit.utils.CartManager;

import java.io.*;
import java.nio.file.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

public class FrontArtworkController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private TilePane artworkGrid;

    @FXML
    private ImageView profileImage;

    @FXML private Label cartBadge;

    private final serviceartwork artworkService = new serviceartwork();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[DEBUG] FrontArtworkController.initialize() called");
        // Charger l'image de profil (remplacez le chemin par le vôtre)
        Image img = new Image("http://localhost/assets/1744.jpeg"); // Use HTTP for front-end profile image
        profileImage.setImage(img);
        setupSearch();
        loadArtworks();
        addFloatingAIButton(); // Ajout du bouton IA flottant
        updateCartBadge(CartManager.getCartCount());
        
        // Bind cart badge to cart count
        CartManager.cartCountProperty().addListener((obs, oldVal, newVal) -> {
            updateCartBadge(newVal.intValue());
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadArtworks();
        });
    }

    private void loadArtworks() {
        artworkGrid.getChildren().clear();
        List<Artwork> artworks = artworkService.getAll();

        // Debug: Print number of artworks loaded
        System.out.println("[DEBUG] Number of artworks loaded: " + artworks.size());
        for (Artwork a : artworks) {
            System.out.println("[DEBUG] To display: " + a.getTitre() + " | " + a.getImage());
        }

        // Filter by search
        String searchText = searchField.getText().toLowerCase();

        artworks = artworks.stream()
                .filter(artwork -> artwork.getTitre().toLowerCase().contains(searchText) ||
                        artwork.getDescription().toLowerCase().contains(searchText) ||
                        artwork.getArtistenom().toLowerCase().contains(searchText))
                .toList();

        // Configure the TilePane for Pinterest-like layout
        artworkGrid.setPrefColumns(4); // Adjust based on window width
        artworkGrid.setHgap(16);
        artworkGrid.setVgap(20);
        artworkGrid.setPadding(new Insets(16));

        // Create card for each artwork
        for (Artwork artwork : artworks) {
            VBox pinCard = createPinCard(artwork);
            artworkGrid.getChildren().add(pinCard);
        }
    }

    private VBox createPinCard(Artwork artwork) {
        VBox card = new VBox(0);
        card.getStyleClass().add("pin-card");
        card.setMaxWidth(236);
        card.setCache(true);
        card.setCacheShape(true);

        // Image container with overlay buttons
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("pin-image-container");

        // Image
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.getStyleClass().add("artwork-image");
        imageView.setFitWidth(236);
        imageView.setFitHeight(180); // Set a max height for the image
        imageView.setPickOnBounds(false); // Only image area is clickable

        try {
            String resourcePath = "/assets/artwork_images/" + artwork.getImage();
            URL imageUrl = getClass().getResource(resourcePath);
            if (imageUrl != null) {
                Image image = new Image(imageUrl.toExternalForm(), true);
                imageView.setImage(image);
                // Dynamically adjust fitHeight to image aspect ratio
                if (image.getWidth() > 0 && image.getHeight() > 0) {
                    double aspect = image.getWidth() / image.getHeight();
                    imageView.setFitHeight(236 / aspect > 180 ? 180 : 236 / aspect);
                }
            } else {
                System.err.println("[DEBUG] Image not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("[DEBUG] Erreur lors du chargement de l'image : " + e.getMessage());
            e.printStackTrace();
        }

        // Ajouter le bouton "Save" dans le coin supérieur droit
        Button saveButton = new Button("Save");
        saveButton.getStyleClass().addAll("save-button", "pin-save-button");
        StackPane.setAlignment(saveButton, Pos.TOP_RIGHT);
        StackPane.setMargin(saveButton, new Insets(10, 10, 0, 0));

        // Action pour le bouton Save
        saveButton.setOnAction(e -> {
            handleSave(artwork);
            e.consume(); // Empêcher la propagation du clic
        });

        // Conteneur pour les boutons en bas
        HBox bottomButtons = new HBox(4);
        bottomButtons.getStyleClass().add("bottom-buttons");
        bottomButtons.setAlignment(Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(bottomButtons, Pos.BOTTOM_RIGHT);

        // Bouton Share
        Button shareButton = new Button("↗");
        shareButton.getStyleClass().add("action-button");

        // Bouton More (trois points)
        Button moreButton = new Button("⋮");
        moreButton.getStyleClass().add("action-button");

        bottomButtons.getChildren().addAll(shareButton, moreButton);

        // Menu contextuel pour les options principales
        ContextMenu moreMenu = new ContextMenu();
        MenuItem hideItem = new MenuItem("Masquer");
        MenuItem downloadItem = new MenuItem("Télécharger");
        MenuItem reportItem = new MenuItem("Signaler");
        moreMenu.getItems().addAll(hideItem, downloadItem, reportItem);

        // Menu contextuel pour le partage
        ContextMenu shareMenu = new ContextMenu();
        MenuItem copyLinkItem = new MenuItem("Copier le lien");
        MenuItem whatsappItem = new MenuItem("WhatsApp");
        MenuItem messengerItem = new MenuItem("Messenger");
        MenuItem facebookItem = new MenuItem("Facebook");
        MenuItem xItem = new MenuItem("X");
        shareMenu.getItems().addAll(copyLinkItem, whatsappItem, messengerItem, facebookItem, xItem);

        // Actions des boutons
        moreButton.setOnAction(e -> {
            moreMenu.show(moreButton, Side.BOTTOM, -140, 5);
            e.consume(); // Empêcher la propagation du clic
        });

        shareButton.setOnAction(e -> {
            shareMenu.show(shareButton, Side.BOTTOM, -140, 5);
            e.consume(); // Empêcher la propagation du clic
        });

        // Actions des items du menu More
        hideItem.setOnAction(e -> handleHide(artwork));
        downloadItem.setOnAction(e -> handleDownload(artwork));
        reportItem.setOnAction(e -> handleReport(artwork));

        // Actions des items du menu Share
        copyLinkItem.setOnAction(e -> handleCopyLink(artwork));
        whatsappItem.setOnAction(e -> handleWhatsAppShare(artwork));
        messengerItem.setOnAction(e -> handleMessengerShare(artwork));
        facebookItem.setOnAction(e -> handleFacebookShare(artwork));
        xItem.setOnAction(e -> handleXShare(artwork));

        // Compact title and artist labels
        VBox infoBox = new VBox(4);  // Reduced spacing between labels
        infoBox.setPadding(new Insets(8, 8, 8, 8));  // Compact padding

        Label titleLabel = new Label(artwork.getTitre());
        titleLabel.getStyleClass().add("artwork-title");
        titleLabel.setWrapText(true);

        Label artistLabel = new Label(artwork.getArtistenom());
        artistLabel.getStyleClass().add("artwork-artist");

        Label descLabel = new Label(artwork.getDescription());
        descLabel.getStyleClass().add("artwork-description");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(220);

        infoBox.getChildren().setAll(titleLabel, artistLabel, descLabel);

        // Make sure all containers pick up mouse events
        card.setPickOnBounds(true);
        imageContainer.setPickOnBounds(true);
        infoBox.setPickOnBounds(true);
        card.setStyle("-fx-background-color: white;");
        imageContainer.setStyle("-fx-background-color: rgba(255,255,255,0.01);");

        // Add click handler to card and imageContainer
        javafx.event.EventHandler<javafx.scene.input.MouseEvent> openDetailsHandler = e -> {
            Node target = (Node) e.getTarget();
            if (!(target instanceof Button)) {
                showArtworkDetails(artwork);
                e.consume();
            }
        };
        card.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, openDetailsHandler);
        imageContainer.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, openDetailsHandler);
        infoBox.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, openDetailsHandler);

        // Ajoute tous les éléments à l'image container
        imageContainer.getChildren().addAll(imageView, saveButton, bottomButtons);

        // Add only necessary components to card
        card.getChildren().clear();
        card.getChildren().addAll(imageContainer, infoBox);
        return card;
    }

    private void handleSave(Artwork artwork) {
        // Implémenter la logique de sauvegarde
        showAlert("Succès", "Œuvre enregistrée dans votre collection");
    }

    private void showShareMenu(Artwork artwork) {
        Stage shareStage = new Stage();
        shareStage.initModality(Modality.APPLICATION_MODAL);
        shareStage.initStyle(StageStyle.UNDECORATED);

        VBox shareBox = new VBox(10);
        shareBox.setPadding(new Insets(15));
        shareBox.getStyleClass().add("share-menu");
        shareBox.setStyle("-fx-background-color: white; -fx-border-color: red; -fx-border-width: 2px;");

        // En-tête avec titre et bouton de fermeture
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPrefWidth(Double.MAX_VALUE);

        Label titleLabel = new Label("Share");
        titleLabel.getStyleClass().add("share-title");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setAlignment(Pos.CENTER);

        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("close-button");
        closeButton.setOnAction(e -> shareStage.close());

        header.getChildren().addAll(titleLabel, closeButton);

        // Options de partage
        HBox shareOptions = new HBox(15);
        shareOptions.setAlignment(Pos.CENTER);

        // Copier le lien
        VBox copyLink = createSimpleShareOption("Copy link", "ICON", e -> {
            handleCopyLink(artwork);
            shareStage.close();
        });

        // WhatsApp
        VBox whatsApp = createSimpleShareOption("WhatsApp", "ICON", e -> {
            handleWhatsAppShare(artwork);
            shareStage.close();
        });

        // Messenger
        VBox messenger = createSimpleShareOption("Messenger", "ICON", e -> {
            handleMessengerShare(artwork);
            shareStage.close();
        });

        // Facebook
        VBox facebook = createSimpleShareOption("Facebook", "ICON", e -> {
            handleFacebookShare(artwork);
            shareStage.close();
        });

        // X (Twitter)
        VBox twitter = createSimpleShareOption("X", "ICON", e -> {
            handleXShare(artwork);
            shareStage.close();
        });

        shareOptions.getChildren().addAll(copyLink, whatsApp, messenger, facebook, twitter);

        // Champ de recherche
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or email");
        searchField.getStyleClass().add("share-search");

        shareBox.getChildren().addAll(header, shareOptions, new Separator(), searchField);
        shareBox.getChildren().add(new Label("TEST SHARE"));

        Scene scene = new Scene(shareBox);
        // scene.getStylesheets().add(getClass().getResource("/styles/pinterest-style.css").toExternalForm());

        shareStage.setScene(scene);
        shareStage.show();
    }

    private void showArtworkDetails(Artwork artwork) {
        try {
            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Detailartwork.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène
            Scene scene = new Scene(root);
            
            // Créer une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Détails de l'œuvre");
            
            // Configurer la fenêtre
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setMaximized(true);

            // Récupérer et initialiser le contrôleur
            DetailartworkController controller = loader.getController();
            if (controller == null) {
                throw new IOException("Cannot get controller for DetailartworkController");
            }
            controller.setArtwork(artwork);

            // Afficher la fenêtre
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les détails de l'œuvre : " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleOeuvres() {
        // Déjà sur la page des œuvres
    }

    @FXML
    private void handleAtelier() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FrontendAtelier.fxml"));
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDons() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListeDons.fxml"));
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleForum() {
        // TODO: Implement forum navigation
        System.out.println("Forum navigation not implemented.");
    }
    @FXML
    private void handleCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Cart.fxml"));
            Parent root = loader.load();

            // Get the controller and refresh the cart
            // CartController controller = loader.getController();
            // if (controller != null) {
            //     controller.refreshCart();
            // }

            Stage stage = new Stage();
            stage.setTitle("Mon Panier");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le panier : " + e.getMessage());
        }
    }

    private void handleHide(Artwork artwork) {
        // Implémenter la logique pour masquer l'œuvre
        showAlert("Info", "Cette œuvre ne sera plus affichée dans votre flux");
    }

    private void handleDownload(Artwork artwork) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        
        VBox downloadMenu = new VBox(8);
        downloadMenu.getStyleClass().add("download-menu");
        downloadMenu.setPadding(new Insets(10));

        Label imageOption = new Label("Télécharger l'image");
        imageOption.getStyleClass().add("download-option");
        
        Label pdfOption = new Label("Télécharger en PDF");
        pdfOption.getStyleClass().add("download-option");

        imageOption.setOnMouseClicked(e -> {
            downloadImage(artwork);
            popupStage.close();
        });

        pdfOption.setOnMouseClicked(e -> {
            downloadPDF(artwork);
            popupStage.close();
        });

        downloadMenu.getChildren().addAll(imageOption, pdfOption);

        Scene scene = new Scene(downloadMenu);
        scene.getStylesheets().add(getClass().getResource("/styles/pinterest-style.css").toExternalForm());
        
        popupStage.setScene(scene);
        popupStage.show();
    }

    private void downloadImage(Artwork artwork) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'image");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        fileChooser.setInitialFileName(artwork.getTitre() + ".jpg");

        File destFile = fileChooser.showSaveDialog(null);
        if (destFile != null) {
            try {
                String sourcePath = "C:/xampp/htdocs/artwork_images/" + artwork.getImage();
                Files.copy(Paths.get(sourcePath), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert("Succès", "Image téléchargée avec succès !");
            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors du téléchargement de l'image : " + e.getMessage());
            }
        }
    }

    private void downloadPDF(Artwork artwork) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer en PDF");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF", "*.pdf")
        );
        fileChooser.setInitialFileName(artwork.getTitre() + ".pdf");

        File destFile = fileChooser.showSaveDialog(null);
        if (destFile != null) {
            try {
                createPDF(artwork, destFile);
                showAlert("Succès", "PDF créé avec succès !");
            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors de la création du PDF : " + e.getMessage());
            }
        }
    }

    private void createPDF(Artwork artwork, File destFile) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            String imagePath = "C:/xampp/htdocs/artwork_images/" + artwork.getImage();
            PDImageXObject image = PDImageXObject.createFromFile(imagePath, document);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float imageWidth = image.getWidth();
            float imageHeight = image.getHeight();

            float ratio = Math.min(pageWidth / imageWidth, pageHeight / imageHeight) * 0.8f;
            float scaledWidth = imageWidth * ratio;
            float scaledHeight = imageHeight * ratio;

            float x = (pageWidth - scaledWidth) / 2;
            float y = (pageHeight - scaledHeight) / 2;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(image, x, y, scaledWidth, scaledHeight);
            }

            document.save(destFile);
        }
    }

    private void handleReport(Artwork artwork) {
        // Implémenter la logique de signalement
        showAlert("Info", "Merci de votre signalement. Nous allons examiner cette œuvre.");
    }

    private void handleCopyLink(Artwork artwork) {
        // Simuler un lien pour l'exemple
        String link = "http://votre-site.com/artwork/" + artwork.getId();
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(link);
        clipboard.setContent(content);
        showAlert("Succès", "Lien copié dans le presse-papiers");
    }

    private void handleWhatsAppShare(Artwork artwork) {
        String url = "https://wa.me/?text=" + encodeUrl("Découvrez cette œuvre d'art : " + artwork.getTitre());
        openUrl(url);
    }

    private void handleMessengerShare(Artwork artwork) {
        String url = "https://www.messenger.com/share?link=" + encodeUrl("http://votre-site.com/artwork/" + artwork.getId());
        openUrl(url);
    }

    private void handleFacebookShare(Artwork artwork) {
        String url = "https://www.facebook.com/sharer/sharer.php?u=" + encodeUrl("http://votre-site.com/artwork/" + artwork.getId());
        openUrl(url);
    }

    private void handleXShare(Artwork artwork) {
        String url = "https://twitter.com/intent/tweet?text=" + encodeUrl("Découvrez cette œuvre d'art : " + artwork.getTitre());
        openUrl(url);
    }

    private String encodeUrl(String url) {
        try {
            return java.net.URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }

    private void openUrl(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le lien : " + e.getMessage());
        }
    }

    // Helper method to create a share option button
    private VBox createSimpleShareOption(String label, String icon, javafx.event.EventHandler<javafx.scene.input.MouseEvent> handler) {
        VBox optionBox = new VBox(2);
        optionBox.setAlignment(Pos.CENTER);
        optionBox.getStyleClass().add("share-option");

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("share-icon");
        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("share-label");

        optionBox.getChildren().addAll(iconLabel, textLabel);
        optionBox.setOnMouseClicked(handler);
        optionBox.setCursor(javafx.scene.Cursor.HAND);
        return optionBox;
    }

    private String generateAIImage(String prompt) {
        try {
            String url = "http://localhost:8080/api/images/generate";
            URL obj = new URL(url);
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String jsonInputString = "{\"prompt\": \"" + prompt + "\"}";
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = con.getResponseCode();
            if (code == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
                // Ici, il faut parser le JSON pour extraire l'URL de l'image
                String json = response.toString();
                int idx = json.indexOf("\"imageUrl\":\"");
                if (idx != -1) {
                    int start = idx + 12;
                    int end = json.indexOf("\"", start);
                    return json.substring(start, end);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showAIImage(String imageUrl, String prompt) {
        Stage stage = new Stage();
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);
        Label label = new Label("Image générée pour : " + prompt);
        ImageView imageView = new ImageView(new Image(imageUrl, 400, 400, true, true));
        box.getChildren().addAll(label, imageView);
        Scene scene = new Scene(box);
        stage.setScene(scene);
        stage.setTitle("Image IA générée");
        stage.show();
    }

    // Ajoute ce bouton flottant IA à la fenêtre principale
    private void addFloatingAIButton() {
        artworkGrid.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Scene scene = newScene;
                Parent oldRoot = scene.getRoot();
                // Si ce n'est pas déjà un StackPane, on encapsule
                StackPane newRoot;
                if (oldRoot instanceof StackPane) {
                    newRoot = (StackPane) oldRoot;
                } else {
                    newRoot = new StackPane();
                    newRoot.getChildren().add(oldRoot);
                    scene.setRoot(newRoot);
                }
                Button aiButton = new Button();
                aiButton.setStyle(
                    "-fx-background-radius: 50%; -fx-background-color: white; -fx-min-width: 56px; -fx-min-height: 56px; " +
                    "-fx-max-width: 56px; -fx-max-height: 56px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4,0,0,2);"
                );
                Label aiIcon = new Label("?");
                aiIcon.setStyle("-fx-font-size: 28px; -fx-text-fill: black;");
                aiButton.setGraphic(aiIcon);
                newRoot.getChildren().add(aiButton);
                StackPane.setAlignment(aiButton, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(aiButton, new Insets(0, 30, 30, 0));
                aiButton.setOnAction(e -> showAIGenerateDialog());
            }
        });
    }

    private void showAIGenerateDialog() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Generate AI Image");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/image_generation.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load the AI generation dialog: " + e.getMessage());
        }
    }

    private void updateCartBadge(int count) {
        if (cartBadge != null) {
            cartBadge.setText(String.valueOf(count));
            cartBadge.setVisible(count > 0);
        }
    }
}
