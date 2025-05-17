package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import tn.esprit.models.Artwork;
import tn.esprit.models.Forum;
import tn.esprit.models.CommantairesF;
import tn.esprit.models.User;
import tn.esprit.services.ServiceCommentaire_f;
import tn.esprit.services.ServiceForum;
import tn.esprit.services.serviceartwork;
import tn.esprit.utils.CartManager;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private TilePane artworkGrid;
    @FXML
    private TextField searchField;
    @FXML
    private ImageView profileImage;
    @FXML
    private Label cartBadge;
    @FXML
    private HBox accountSection;
    @FXML
    private Button loginButton;
    @FXML
    private TableView<User> userTable;

    private User currentUser;
    private final serviceartwork artworkService = new serviceartwork();
    private final ServiceForum serviceForum = new ServiceForum();
    private final ServiceCommentaire_f serviceComment = new ServiceCommentaire_f();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            String cssPath = getClass().getResource("/styles/pinterest-style.css").toExternalForm();
            System.out.println("CSS file found at: " + cssPath);
            System.out.println("[DEBUG] FrontArtworkController.initialize() called");
            
            Image img = new Image("http://localhost/assets/1744.jpeg");
            profileImage.setImage(img);
            setupSearch();
            loadArtworks();
            addFloatingAIButton();
            updateCartBadge(CartManager.getCartCount());

            // Bind cart badge to cart count
            CartManager.cartCountProperty().addListener((obs, oldVal, newVal) -> {
                updateCartBadge(newVal.intValue());
            });

            // Add responsiveness to the dashboard only after the scene is set
            artworkGrid.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    makeDashboardResponsive();
                }
            });

        } catch (Exception e) {
            System.err.println("Error loading CSS file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void makeDashboardResponsive() {
        // Bind the width and height of the main container to the scene's dimensions
        if (artworkGrid != null) {
            artworkGrid.prefWidthProperty().bind(artworkGrid.getScene().widthProperty().multiply(0.9));
            artworkGrid.prefHeightProperty().bind(artworkGrid.getScene().heightProperty().multiply(0.9));
        }

        if (profileImage != null) {
            profileImage.fitWidthProperty().bind(profileImage.getScene().widthProperty().multiply(0.1));
            profileImage.fitHeightProperty().bind(profileImage.getScene().heightProperty().multiply(0.1));
        }

        if (cartBadge != null) {
            StackPane.setAlignment(cartBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(cartBadge, new Insets(0, 8, 0, 0));
        }

        // Add listeners to adjust layout dynamically
        if (accountSection != null) {
            accountSection.widthProperty().addListener((obs, oldVal, newVal) -> {
                accountSection.setSpacing(newVal.doubleValue() / 50);
            });
        }
    }

    //just added
    public void setUser(User user) {
        this.currentUser = user;
        updateUserUI();
    }

    private void updateUserUI() {
        Platform.runLater(() -> {
            if (currentUser != null) {
                // User is logged in
                accountSection.setVisible(true);
                loginButton.setVisible(false);

                // Load profile image if available
                if (currentUser.getImage() != null && !currentUser.getImage().isEmpty()) {
                    try {
                        Image img = new Image(currentUser.getImage());
                        profileImage.setImage(img);
                    } catch (Exception e) {
                        System.err.println("Error loading profile image: " + e.getMessage());
                    }
                }
            } else {
                // User is not logged in
                accountSection.setVisible(false);
                loginButton.setVisible(true);
            }
        });
    }

    @FXML
    private void handleLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la page de connexion", e);
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Clear user session
            tn.esprit.utils.SessionManager.clearSession();

            // Load login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            // Get current stage and set login scene
            Stage stage = (Stage) profileImage.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la déconnexion : " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    private void handleProfile() {
        if (currentUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile.fxml"));
                Parent root = loader.load();

                // Pass the user to the profile controller
                if (loader.getController() instanceof UserAwareController) {
                    ((UserAwareController) loader.getController()).setUser(currentUser);
                }

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Mon Profil");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de charger le profil");
            }
        }
    }

    @FXML
    private void showAccountsMenu(MouseEvent event) {
        ContextMenu accountsMenu = new ContextMenu();

        MenuItem profileItem = new MenuItem("Mon profil");
        profileItem.setOnAction(e -> handleProfile());

        MenuItem logoutItem = new MenuItem("Déconnexion");
        logoutItem.setOnAction(e -> handleLogout());

        accountsMenu.getItems().addAll(profileItem, logoutItem);
        
        accountsMenu.show(((Node) event.getSource()), event.getScreenX(), event.getScreenY());
    }

    @FXML
    private void handleAddBeneficiaire() {
        loadForm("/AddBeneficiaire.fxml");
    }

    @FXML
    private void handleAddDons() {
        loadForm("/AddDons.fxml");
    }

    @FXML
    private void handleAtelier() {
        // Revert to loading FrontendAtelier.fxml
        loadForm("/FrontendAtelier.fxml");
    }

    private void loadForm(String fxmlFile) {
        try {
            System.out.println("Attempting to load FXML file: " + fxmlFile);
            URL resource = getClass().getResource(fxmlFile);
            if (resource == null) {
                throw new IOException("FXML file not found: " + fxmlFile);
            }
            System.out.println("FXML file found at: " + resource.toExternalForm());
            Node form = FXMLLoader.load(resource);
            artworkGrid.getChildren().clear(); // Clear existing content
            artworkGrid.getChildren().add(form); // Add the new form
            System.out.println("Successfully loaded and displayed FXML file: " + fxmlFile);
        } catch (IOException e) {
            System.err.println("Error loading FXML file: " + fxmlFile);
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la vue " + fxmlFile, e);
        }
    }

    private void showError(String title, String content, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content + (e != null ? "\n" + e.getMessage() : ""));
        alert.showAndWait();
    }

    public void handlegallery(ActionEvent actionEvent) {
        loadForm("/FrontArtwork.fxml");
    }

    @FXML
    private void handleAccueil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadArtworks();
        });
    }

    private void loadArtworks() {
        artworkGrid.getChildren().clear();
        List<Artwork> artworks = artworkService.getAll();

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
        titleLabel.setWrapText(true); // Fixed missing parenthesis

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
        imageContainer.getChildren().addAll(imageView, bottomButtons);

        // Add only necessary components to card
        card.getChildren().clear();
        card.getChildren().addAll(imageContainer, infoBox);
        return card;
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
        TextField shareSearchField = new TextField();
        shareSearchField.setPromptText("Search by name or email");
        shareSearchField.getStyleClass().add("share-search");

        shareBox.getChildren().addAll(header, shareOptions, new Separator(), shareSearchField);
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
        artworkGrid.getChildren().clear();
        loadForums();
    }

    private void loadForums() {
        List<Forum> forums = serviceForum.getAll();
        artworkGrid.getChildren().clear();
        artworkGrid.setPrefColumns(3);
        artworkGrid.setHgap(16);
        artworkGrid.setVgap(20);
        artworkGrid.setPadding(new Insets(16));

        for (Forum forum : forums) {
            HBox card = createForumCard(forum);
            artworkGrid.getChildren().add(card);
        }
    }

    private HBox createForumCard(Forum forum) {
        HBox card = new HBox();
        card.getStyleClass().add("forum-card");
        card.setPadding(new Insets(15));
        card.setSpacing(15);
        card.setMaxWidth(300);
        card.setStyle("-fx-background-color: white; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");

        ImageView imageView = new ImageView();
        try {
            String imagePath = forum.getImage_f();
            if (imagePath != null && !imagePath.isEmpty()) {
                Image image = new Image("file:" + imagePath);
                imageView.setImage(image);
            } else {
                Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/default-forum.png")));
                imageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Failed to load forum image: " + e.getMessage());
            Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/default-forum.png")));
            imageView.setImage(defaultImage);
        }
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("forum-image");

        VBox details = new VBox();
        details.setSpacing(8);
        details.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label(forum.getTitre_f());
        titleLabel.getStyleClass().add("forum-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(180);

        LocalDate localDate = LocalDate.parse(String.valueOf(forum.getDate_f()), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Label dateLabel = new Label("Date: " + localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateLabel.getStyleClass().add("forum-detail");

        Label categoryLabel = new Label("Category: " + forum.getCategorie_f());
        categoryLabel.getStyleClass().add("forum-detail");

        Label descriptionLabel = new Label("Description: " + forum.getDescription_f());
        descriptionLabel.getStyleClass().add("forum-detail");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(180);

        Button commentButton = new Button("Comment");
        commentButton.getStyleClass().add("comment-button");
        commentButton.setOnAction(event -> showCommentPopup(forum));

        details.getChildren().addAll(titleLabel, dateLabel, categoryLabel, descriptionLabel, commentButton);
        card.getChildren().addAll(imageView, details);

        return card;
    }

    private void showCommentPopup(Forum forum) {
        try {
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UTILITY);
            popupStage.setTitle("Add Comment");

            VBox popupLayout = new VBox(10);
            popupLayout.setPadding(new Insets(20));
            popupLayout.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 5px; -fx-background-radius: 5px;");

            Label titleLabel = new Label("Add a Comment for: " + forum.getTitre_f());
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            TextArea commentTextArea = new TextArea();
            commentTextArea.setPromptText("Enter your comment here...");
            commentTextArea.setPrefHeight(100);
            commentTextArea.setWrapText(true);

            Label errorLabel = new Label();
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setVisible(false);

            Button submitButton = new Button("Submit");
            submitButton.getStyleClass().add("primary-button");
            submitButton.setOnAction(event -> {
                String commentText = commentTextArea.getText().trim();
                if (commentText.length() < 3) {
                    errorLabel.setText("Comment must be at least 3 characters long.");
                    errorLabel.setVisible(true);
                } else {
                    CommantairesF comment = new CommantairesF();
                    comment.setTexte_c(commentText);
                    comment.setDate_c(LocalDate.now());
                    comment.setForum(forum);
                    // Assuming a logged-in user; replace 1 with actual user ID
                    comment.setUser(new User(1)); // TODO: Replace with actual user authentication
                    serviceComment.add(comment);
                    popupStage.close();
                }
            });

            Button cancelButton = new Button("Cancel");
            cancelButton.getStyleClass().add("secondary-button");
            cancelButton.setOnAction(event -> popupStage.close());

            HBox buttonBox = new HBox(10, submitButton, cancelButton);
            buttonBox.setAlignment(Pos.CENTER);

            popupLayout.getChildren().addAll(titleLabel, commentTextArea, errorLabel, buttonBox);

            Scene popupScene = new Scene(popupLayout, 400, 250);
            popupScene.getStylesheets().add(getClass().getResource("/css/pinterest-style.css").toExternalForm());
            popupStage.setScene(popupScene);
            popupStage.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing comment popup: " + e.getMessage());
            e.printStackTrace();
        }
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
            URI uri = new URI(url); // Replaced deprecated URL constructor
            URL obj = uri.toURL();
            java.awt.Desktop.getDesktop().browse(obj.toURI());
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
            URI uri = new URI(url); // Replaced deprecated URL constructor
            URL obj = uri.toURL();
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

    private User getSelectedUserData() {
        // Assuming there is a table or list to select users from
        // Replace 'userTable' with the actual variable name of the user table
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a user to view their profile.");
            alert.showAndWait();
        }
        return selectedUser;
    }

    @FXML
    private void handleViewProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile.fxml"));
            Parent profileView = loader.load();

            // Assuming ProfileController is the controller for profile.fxml
            ProfileController controller = loader.getController();
            controller.setUser(getSelectedUserData()); // Pass user data to the profile controller

            Stage stage = new Stage();
            stage.setScene(new Scene(profileView));
            stage.setTitle("User Profile");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}