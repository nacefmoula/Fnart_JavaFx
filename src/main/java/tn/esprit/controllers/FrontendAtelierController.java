package tn.esprit.controllers;

import tn.esprit.models.Atelier;
import tn.esprit.services.AtelierService;
import tn.esprit.services.InscriptionAtelierService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class FrontendAtelierController {

    @FXML
    private TilePane atelierGrid;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private Button prevPageButton;

    @FXML
    private Button nextPageButton;

    @FXML
    private Label pageInfoLabel;

    @FXML
    private ComboBox<Integer> pageSizeComboBox;

    @FXML
    private TextField searchField;

    private final AtelierService atelierService = new AtelierService();
    private final InscriptionAtelierService inscriptionService = new InscriptionAtelierService();

    // Pagination and filtering fields
    private int currentPage = 0;
    private int pageSize = 4; // Default page size
    private int totalPages = 1;
    private java.sql.Date fromDate = null;
    private java.sql.Date toDate = null;
    private String currentSortField = "date";
    private boolean sortAscending = true;

    private Stage detailsStage;
    private DetailsAtelierController detailsController;

    @FXML
    public void initialize() {
        // Initialize date pickers
        setupDatePickers();

        // Initialize sort options
        initSortComboBox();

        // Initialize page size options
        initPageSizeComboBox();

        // Initialize search field with listener
        setupSearchField();

        // Load initial data
        refreshGrid();

        // Update UI controls state
        updatePaginationControls();
    }

    private void setupDatePickers() {
        // Set a formatter for the date pickers
        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return dateFormatter.parse(string).toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                    } catch (Exception e) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        };

        fromDatePicker.setConverter(converter);
        toDatePicker.setConverter(converter);

        // Set default values (optional)
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
    }

    private void initSortComboBox() {
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
                "Date (croissant)",
                "Date (décroissant)",
                "Titre (A-Z)",
                "Titre (Z-A)",
                "Lieu (A-Z)",
                "Lieu (Z-A)"
        );
        sortComboBox.setItems(sortOptions);
        sortComboBox.getSelectionModel().select(0); // Default sort is by date ascending

        sortComboBox.setOnAction(e -> {
            String selected = sortComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                switch (selected) {
                    case "Date (croissant)":
                        currentSortField = "date";
                        sortAscending = true;
                        break;
                    case "Date (décroissant)":
                        currentSortField = "date";
                        sortAscending = false;
                        break;
                    case "Titre (A-Z)":
                        currentSortField = "titre";
                        sortAscending = true;
                        break;
                    case "Titre (Z-A)":
                        currentSortField = "titre";
                        sortAscending = false;
                        break;
                    case "Lieu (A-Z)":
                        currentSortField = "lieu";
                        sortAscending = true;
                        break;
                    case "Lieu (Z-A)":
                        currentSortField = "lieu";
                        sortAscending = false;
                        break;
                }

                // Reset to first page when sorting changes
                currentPage = 0;
                refreshGrid();
                updatePaginationControls();
            }
        });
    }

    private void initPageSizeComboBox() {
        ObservableList<Integer> pageSizes = FXCollections.observableArrayList(4, 8, 12, 16, 24);
        pageSizeComboBox.setItems(pageSizes);
        pageSizeComboBox.getSelectionModel().select(Integer.valueOf(pageSize));

        pageSizeComboBox.setOnAction(e -> {
            Integer selected = pageSizeComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                pageSize = selected;
                // Reset to first page when page size changes
                currentPage = 0;
                refreshGrid();
                updatePaginationControls();
            }
        });
    }

    private void setupSearchField() {
        // Add listener to search field for real-time search
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Reset to first page when search text changes
            currentPage = 0;
            refreshGrid();
            updatePaginationControls();
        });
    }

    private void refreshGrid() {
        atelierGrid.getChildren().clear(); // Clear existing cards

        try {
            // Get filtered & paginated data
            List<Atelier> ateliers;

            // Convert LocalDate to Date for filtering
            if (fromDatePicker.getValue() != null) {
                fromDate = (java.sql.Date) Date.from(fromDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            if (toDatePicker.getValue() != null) {
                toDate = (java.sql.Date) java.sql.Date.from(toDatePicker.getValue().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            }

            // Get paginated ateliers with filters
            ateliers = atelierService.getAteliersPaginated(
                    currentPage,
                    pageSize,
                    fromDate,
                    toDate,
                    currentSortField,
                    sortAscending
            );

            // Get total count for pagination
            int totalAteliers = atelierService.getTotalAteliers(fromDate, toDate);
            totalPages = (int) Math.ceil((double) totalAteliers / pageSize);
            if (totalPages == 0) totalPages = 1; // At least 1 page even if empty

            // Filter by search text if provided
            String searchText = searchField.getText().toLowerCase().trim();
            if (!searchText.isEmpty()) {
                ateliers.removeIf(a ->
                        !a.getTitre().toLowerCase().contains(searchText) &&
                                !a.getLieu().toLowerCase().contains(searchText) &&
                                !a.getDescription().toLowerCase().contains(searchText)
                );
            }

            // Display the ateliers
            for (Atelier atelier : ateliers) {
                VBox card = createAtelierCard(atelier);
                atelierGrid.getChildren().add(card);
            }

            // Update pagination controls
            updatePaginationControls();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des ateliers: " + e.getMessage());
        }
    }

    private void updatePaginationControls() {
        pageInfoLabel.setText("Page " + (currentPage + 1) + " sur " + totalPages);

        // Enable/disable navigation buttons
        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(currentPage + 1 >= totalPages);
    }

    @FXML
    private void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            refreshGrid();
        }
    }

    @FXML
    private void nextPage() {
        if (currentPage + 1 < totalPages) {
            currentPage++;
            refreshGrid();
        }
    }

    @FXML
    private void applyDateFilter() {
        // Reset to first page when applying filter
        currentPage = 0;
        refreshGrid();
    }

    @FXML
    private void resetFilters() {
        // Clear date filters
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        fromDate = null;
        toDate = null;

        // Reset search
        searchField.setText("");

        // Reset sort to default
        sortComboBox.getSelectionModel().select(0);
        currentSortField = "date";
        sortAscending = true;

        // Reset page
        currentPage = 0;

        // Refresh display
        refreshGrid();
    }

    private VBox createAtelierCard(Atelier atelier) {
        // Create a card with beautiful styling
        VBox card = new VBox();
        card.getStyleClass().add("atelier-card");
        card.setCursor(javafx.scene.Cursor.HAND);

        // Add hover effect through CSS class

        // Create image container with rounded top corners
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("atelier-card-image-container");

        // Create image view
        ImageView imageView = new ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);

        // Apply rounded corners to the image using clip
        Rectangle clip = new Rectangle(imageView.getFitWidth(), imageView.getFitHeight());
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        // Try to load image
        try {
            if (atelier.getImage() != null && !atelier.getImage().isEmpty()) {
                File imageFile = new File(atelier.getImage());
                if (imageFile.exists()) {
                    imageView.setImage(new Image(imageFile.toURI().toString()));
                } else {
                    // Default image if file not found
                    imageView.setImage(new Image("/images/workshop-icon.png"));
                }
            } else {
                // Default image if no path specified
                imageView.setImage(new Image("/images/workshop-icon.png"));
            }
        } catch (Exception e) {
            // Fallback for any loading issues
            System.err.println("Error loading image: " + e.getMessage());
            try {
                imageView.setImage(new Image("/images/workshop-icon.png"));
            } catch (Exception ex) {
                // If even default image fails, leave it blank
            }
        }

        imageContainer.getChildren().add(imageView);

        // Badge for status or category (can be adjusted based on atelier properties)
        HBox badgeContainer = new HBox();
        badgeContainer.setAlignment(Pos.TOP_RIGHT);
        badgeContainer.setPadding(new Insets(10, 10, 0, 0));

        Label statusBadge = new Label("Nouveau");
        statusBadge.getStyleClass().add("status-badge");
        statusBadge.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-padding: 4px 8px; " +
                "-fx-background-radius: 4px; -fx-font-size: 12px; -fx-font-weight: bold;");

        badgeContainer.getChildren().add(statusBadge);

        // Add badge overlay to the image
        StackPane imageWithBadge = new StackPane();
        imageWithBadge.getChildren().addAll(imageContainer, badgeContainer);
        StackPane.setAlignment(badgeContainer, Pos.TOP_RIGHT);

        // Content container
        VBox content = new VBox();
        content.getStyleClass().add("atelier-card-content");
        content.setSpacing(8);
        content.setPadding(new Insets(15));

        // Title with emoji
        Label title = new Label("🎨 " + atelier.getTitre());
        title.getStyleClass().add("atelier-card-title");
        title.setWrapText(true);

        // Location with icon
        HBox locationBox = new HBox(5);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        Label locationIcon = new Label("📍");
        Label locationText = new Label(atelier.getLieu());
        locationText.getStyleClass().add("atelier-card-location");
        locationBox.getChildren().addAll(locationIcon, locationText);

        // Date with icon
        HBox dateBox = new HBox(5);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        Label dateIcon = new Label("📅");

        // Fix: Use SimpleDateFormat instead of DateTimeFormatter for java.util.Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        Label dateText = new Label(sdf.format(atelier.getDate()));
        dateText.getStyleClass().add("atelier-card-date");
        dateBox.getChildren().addAll(dateIcon, dateText);

        // Description preview (if available)
        Text description = new Text();
        if (atelier.getDescription() != null && !atelier.getDescription().isEmpty()) {
            String shortDesc = atelier.getDescription().length() > 60
                    ? atelier.getDescription().substring(0, 60) + "..."
                    : atelier.getDescription();
            description.setText(shortDesc);
        } else {
            description.setText("Découvrez cet atelier unique d'art-thérapie...");
        }
        description.getStyleClass().add("atelier-card-description");
        description.setWrappingWidth(250);

        // Price section with styled box
        HBox priceBox = new HBox();
        priceBox.setAlignment(Pos.CENTER_LEFT);
        priceBox.getStyleClass().add("atelier-card-price-box");
        priceBox.setPadding(new Insets(8, 12, 8, 12));
        priceBox.setStyle("-fx-background-color: #f8f4f0; -fx-background-radius: 6px;");

        // Price with icon (we're using a placeholder price since it's not in your model)
        Label priceIcon = new Label("💰");
        Label priceValue = new Label("39,99 €");
        priceValue.getStyleClass().add("atelier-card-price");
        priceValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #a47148;");

        priceBox.getChildren().addAll(priceIcon, priceValue);

        // Rating stars (placeholder - could be dynamic based on reviews)
        HBox ratingBox = new HBox(2);
        ratingBox.setAlignment(Pos.CENTER_RIGHT);

        for (int i = 0; i < 5; i++) {
            Label star = new Label("⭐");
            star.setStyle("-fx-font-size: 10px;");
            ratingBox.getChildren().add(star);
        }

        // Combine price and rating in a single row
        HBox priceRatingRow = new HBox();
        priceRatingRow.setAlignment(Pos.CENTER);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        priceRatingRow.getChildren().addAll(priceBox, spacer, ratingBox);

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 5, 0));

        // Details button with icon
        Button detailsButton = new Button("Détails");
        detailsButton.getStyleClass().add("atelier-details-button");
        detailsButton.setStyle("-fx-background-color: #5e60ce; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 8px 16px; -fx-background-radius: 20px;");
        detailsButton.setGraphic(new Label("ℹ️"));
        detailsButton.setOnAction(event -> afficherDetailsAtelier(atelier));

        // Inscription button with icon
        Button inscriptionButton = new Button("S'inscrire");
        inscriptionButton.getStyleClass().add("atelier-inscription-button");
        inscriptionButton.setStyle("-fx-background-color: #a47148; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 8px 16px; -fx-background-radius: 20px;");
        inscriptionButton.setGraphic(new Label("✍️"));
        inscriptionButton.setOnAction(event -> inscrireAtelier(atelier));

        buttonBox.getChildren().addAll(detailsButton, inscriptionButton);

        // Add everything to content
        content.getChildren().addAll(title, locationBox, dateBox, description, priceRatingRow, buttonBox);

        // Add content to card
        card.getChildren().addAll(imageWithBadge, content);

        // Add card click handler
        card.setOnMouseClicked(event -> afficherDetailsAtelier(atelier));

        return card;
    }

    private void afficherDetailsAtelier(Atelier atelier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontendDetailsAtelier.fxml"));
            AnchorPane detailsView = loader.load();

            FrontendDetailsAtelierController frontendDetailsController = loader.getController();
            frontendDetailsController.setAtelier(atelier);

            // Créer une nouvelle fenêtre pour les détails au lieu de remplacer la scène principale
            Stage newStage = new Stage();
            newStage.setTitle("Détails de l'atelier: " + atelier.getTitre());
            newStage.setScene(new Scene(detailsView));

            // Configurer la fenêtre pour s'ouvrir en plein écran
            newStage.setMaximized(true);

            // Conserver une référence à cette nouvelle fenêtre
            this.detailsStage = newStage;

            // Afficher la fenêtre de détails
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Afficher la trace complète pour le débogage
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des détails: " + e.getMessage());
        }
    }

   
    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainMenu.fxml"));
            Parent mainMenuView = loader.load();

            Stage stage = (Stage) atelierGrid.getScene().getWindow();
            stage.setScene(new Scene(mainMenuView));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner au menu principal: " + e.getMessage());
        }
    }

    public void loadData() {
        refreshGrid();
    }

    public Stage getDetailsStage() {
        return detailsStage;
    }

    public DetailsAtelierController getDetailsController() {
        return detailsController;
    }

    @FXML
    public void inscrireAtelier(Atelier atelier) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Inscription Atelier");
        dialog.setHeaderText("Inscrivez-vous à l'atelier: " + atelier.getTitre());
        dialog.setContentText("Veuillez entrer votre nom et email (format: Nom,Email):");

        dialog.showAndWait().ifPresent(input -> {
            String[] parts = input.split(",");
            if (parts.length == 2) {
                String nom = parts[0].trim();
                String email = parts[1].trim();

                if (!nom.isEmpty() && !email.isEmpty()) {
                    inscriptionService.inscrire(atelier, nom, email);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Inscription réussie pour " + nom);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Nom ou email invalide.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Format d'entrée invalide. Utilisez: Nom,Email");
            }
        });
    }

    @FXML
    private void handleHome() {
        // Logic to navigate to the home page
        System.out.println("Navigating to Home");
    }

    @FXML
    private void handleAtelier() {
        // Logic to navigate to the Atelier page
        System.out.println("Navigating to Atelier");
    }

    @FXML
    private void handleDons() {
        // Logic to navigate to the Dons page
        System.out.println("Navigating to Dons");
    }

    @FXML
    private void handleForum() {
        // Logic to navigate to the Forum page
        System.out.println("Navigating to Forum");
    }

    @FXML
    private void handleCart() {
        // Logic to navigate to the Cart page
        System.out.println("Navigating to Cart");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void deleteDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                deleteDirectory(file);
            }
        }
        if (!directory.delete()) {
            throw new IOException("Failed to delete " + directory.getAbsolutePath());
        }
    }
}
