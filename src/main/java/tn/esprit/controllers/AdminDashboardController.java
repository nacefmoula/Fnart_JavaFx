package tn.esprit.controllers;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;
import tn.esprit.components.UserCard;
import tn.esprit.enumerations.Role;
import tn.esprit.models.Artwork;
import tn.esprit.models.Beneficiaires;
import tn.esprit.models.Dons;
import tn.esprit.models.User;
import tn.esprit.services.ServiceCommande;
import tn.esprit.services.ServicesBeneficiaires;
import tn.esprit.services.ServicesDons;
import tn.esprit.services.UserService;
import tn.esprit.services.serviceartwork;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AdminDashboardController implements Initializable, UserAwareController {
    @FXML
    private void handleAjoutBeneficiaire(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddBeneficiaire.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un bénéficiaire");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Affiche une alerte ou log si besoin
        }
    }

    // ====================== USER MANAGEMENT SECTION ======================
    @FXML private VBox profilePanel;
    @FXML private Label adminNameLabel;
    @FXML private Label adminEmailLabel;
    @FXML private VBox allUsersCardContainer;
    @FXML private VBox pendingUsersCardContainer;
    @FXML private TextField searchTextField; // Correction du nom pour correspondre au fichier FXML
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button deleteUserButton;
    @FXML private ListView<String> activityList;
    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label pendingUsersLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private PieChart userRoleChart;

    // ====================== BENEFICIARIES & DONATIONS SECTION ======================
    /**
     * Initialise uniquement la liste des dons pour l'onglet admin (lecture seule)
     */
    private void initializeDonsListOnly() {
        try {
            donsList.setAll(servicesDons.getAll());
            filteredDons = new FilteredList<>(donsList, p -> true);
            donsListView.setCellFactory(lv -> new ListCell<Dons>() {
                @Override
                protected void updateItem(Dons item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        VBox vbox = new VBox(3);
                        vbox.getChildren().addAll(
                            new Label("Description : " + item.getDescription()),
                            new Label("Valeur : " + item.getValeur() + " DT"),
                            new Label("Type : " + item.getType()),
                            new Label("Bénéficiaire : " + (item.getBeneficiaire() != null ? item.getBeneficiaire().getNom() : "-"))
                        );
                        setGraphic(vbox);
                    }
                }
            });
            donsListView.setItems(filteredDons);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les dons");
            e.printStackTrace();
        }
    }
    /**
     * Initialise uniquement la liste des bénéficiaires pour l'onglet admin (lecture seule)
     */
    private void initializeBeneficiairesListOnly() {
    try {
        beneficiairesList.setAll(servicesBeneficiaires.getAll());
        filteredBeneficiaires = new FilteredList<>(beneficiairesList, p -> true);
        beneficiairesListView.setCellFactory(lv -> new ListCell<Beneficiaires>() {
            @Override
            protected void updateItem(Beneficiaires item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(3);
                    vbox.getChildren().addAll(
                        new Label("Nom : " + item.getNom()),
                        new Label("Email : " + item.getEmail()),
                        new Label("Téléphone : " + item.getTelephone()),
                        new Label("Cause : " + item.getCause()),
                        new Label("Valeur demandée : " + (item.getValeurDemande() != null ? item.getValeurDemande() + " DT" : "-")),
                        new Label("Statut : " + (item.getStatus() == null ? "En attente" : item.getStatus()))
                    );

                    HBox actions = new HBox(10);
                    Button approveBtn = new Button("Approuver");
                    Button rejectBtn = new Button("Rejeter");
                    approveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    rejectBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white;");

                    approveBtn.setDisable("Accepté".equalsIgnoreCase(item.getStatus()));
                    rejectBtn.setDisable("Refusé".equalsIgnoreCase(item.getStatus()));

                    approveBtn.setOnAction(e -> handleApproveBeneficiaire(item));
                    rejectBtn.setOnAction(e -> handleRejectBeneficiaire(item));

                    actions.getChildren().addAll(approveBtn, rejectBtn);
                    VBox wrapper = new VBox(5, vbox, actions);
                    setGraphic(wrapper);
                }
            }
        });
        beneficiairesListView.setItems(filteredBeneficiaires);
    } catch (Exception e) {
        showAlert("Erreur", "Impossible de charger les bénéficiaires");
        e.printStackTrace();
    }
}

private void handleApproveBeneficiaire(Beneficiaires beneficiaire) {
    if (!"Accepté".equalsIgnoreCase(beneficiaire.getStatus())) {
        beneficiaire.setStatus("Accepté");
        servicesBeneficiaires.update(beneficiaire);
        showSuccessAlert("Succès", "Le bénéficiaire a été approuvé.");
        refreshBeneficiairesList();
    }
}

private void handleRejectBeneficiaire(Beneficiaires beneficiaire) {
    if (!"Refusé".equalsIgnoreCase(beneficiaire.getStatus())) {
        beneficiaire.setStatus("Refusé");
        servicesBeneficiaires.update(beneficiaire);
        showSuccessAlert("Succès", "Le bénéficiaire a été rejeté.");
        refreshBeneficiairesList();
    }
}

private void refreshBeneficiairesList() {
    beneficiairesList.setAll(servicesBeneficiaires.getAll());
    filteredBeneficiaires = new FilteredList<>(beneficiairesList, p -> true);
    beneficiairesListView.setItems(filteredBeneficiaires);
}
    @FXML private AnchorPane beneficiairesPane;
    @FXML private AnchorPane donsPane;
    @FXML private ListView<Beneficiaires> beneficiairesListView;
    @FXML private ListView<Dons> donsListView;
    private FilteredList<Beneficiaires> filteredBeneficiaires;
    private FilteredList<Dons> filteredDons;
    private ObservableList<Dons> donsList;

    // Beneficiary form fields
    @FXML private TextField nomTextField;
    @FXML private TextField emailTextField;
    @FXML private TextField telephoneTextField;
    @FXML private ChoiceBox<String> AssociationChoice;
    @FXML private TextField causeTextField;
    @FXML private TextField valeurTextField;
    @FXML private TextArea descriptionTextArea;
    @FXML private ChoiceBox<String> statusChoice;
    @FXML private Button uploadImageButton;
    @FXML private Label imagePathLabel;

    // Donation form fields
    @FXML private TextArea descriptionDonTextArea;
    @FXML private TextField valeurDonTextField;
    @FXML private ChoiceBox<String> typeDonChoice;
    @FXML private ChoiceBox<String> beneficiaireChoice;
    @FXML private Button ajoutDonButton;

    // Statistics labels
    @FXML private Label totalBeneficiairesLabel;
    @FXML private Label enAttenteLabel;
    @FXML private Label acceptesLabel;
    @FXML private Label refusesLabel;
    @FXML private Label totalDonsLabel;
    @FXML private Label valeurTotaleLabel;
    @FXML private Label donsNatureLabel;
    @FXML private Label donsFinanciersLabel;

    // Charts
    @FXML private PieChart beneficiairesChart;
    @FXML private PieChart donsChart;
    @FXML private PieChart beneficiairesStatsChart;
    @FXML private PieChart donsStatsChart;

    // ====================== ARTWORK MANAGEMENT SECTION ======================
    @FXML private ListView<Artwork> artworksListView;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private Button artworkButton;
    @FXML private Button ajoutArtworkButton;
    @FXML private Button exportPdfArtworkButton;

    // ====================== FORUM SECTION ======================
    @FXML private AnchorPane ajouterForumPane;
    @FXML private AnchorPane afficherForumPane;
    private AjouterForumController ajouterForumController;
    private AfficherForumController afficherForumController;

    // Services
    private UserService userService;
    private ServiceCommande commandeService;
    private ServicesBeneficiaires servicesBeneficiaires;
    private ServicesDons servicesDons;
    private serviceartwork artworkService;

    // Data collections
    private List<User> allUsers = new ArrayList<>();
    private List<User> pendingUsers = new ArrayList<>();
    private ObservableList<Beneficiaires> beneficiairesList;
    private ObservableList<Artwork> artworkObservableList = FXCollections.observableArrayList();
    private FilteredList<Artwork> filteredArtworks;

    // Selected items
    private User selectedUser;
    private User selectedPendingUser;
    private User currentUser;

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]{1,2})?$");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize all services
        userService = new UserService();
        artworkService = new serviceartwork();
        servicesBeneficiaires = new ServicesBeneficiaires();
        servicesDons = new ServicesDons();
        beneficiairesList = FXCollections.observableArrayList();
        donsList = FXCollections.observableArrayList();
        // Initialiser le champ Statut
        if (statusChoice != null) {
            statusChoice.setItems(FXCollections.observableArrayList("En attente", "Accepté", "Refusé"));
        }

        // User Management (All Users & Pending)
        if (allUsersCardContainer != null && pendingUsersCardContainer != null) {
            initializeUserManagement();
        }
        // Beneficiaires
        if (beneficiairesPane != null) {
            initializeBeneficiairesListOnly();
        }
        // Dons
        if (donsPane != null) {
            initializeDonsListOnly();
        }

        // Initialize Artwork Management section if UI components exist
        if (artworksListView != null) {
            initializeArtworkManagement();
        }

        // Initialize Forum section if UI components exist
        if (ajouterForumPane != null && afficherForumPane != null) {
            initializeForumControllers();
        }

        // Add responsiveness to the dashboard
        makeDashboardResponsive();
    }

    private void makeDashboardResponsive() {
        // Responsive binding: attendre que la scène soit disponible pour chaque composant
        if (beneficiairesPane != null) {
            beneficiairesPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    beneficiairesPane.prefWidthProperty().bind(newScene.widthProperty());
                    beneficiairesPane.prefHeightProperty().bind(newScene.heightProperty());
                }
            });
        }

        if (donsPane != null) {
            donsPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    donsPane.prefWidthProperty().bind(newScene.widthProperty());
                    donsPane.prefHeightProperty().bind(newScene.heightProperty());
                }
            });
        }

        if (artworksListView != null) {
            artworksListView.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    artworksListView.prefWidthProperty().bind(newScene.widthProperty().multiply(0.8));
                    artworksListView.prefHeightProperty().bind(newScene.heightProperty().multiply(0.8));
                }
            });
        }

        if (userRoleChart != null) {
            userRoleChart.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    userRoleChart.prefWidthProperty().bind(newScene.widthProperty().multiply(0.5));
                    userRoleChart.prefHeightProperty().bind(newScene.heightProperty().multiply(0.5));
                }
            });
        }

        // Add listeners to adjust layout dynamically
        if (profilePanel != null) {
            profilePanel.widthProperty().addListener((obs, oldVal, newVal) -> {
                profilePanel.setSpacing(newVal.doubleValue() / 50);
            });
        }
    }

    private void initializeUserManagement() {
        // Set up admin profile
        User currentAdmin = userService.getCurrentAdmin();
        if (currentAdmin != null) {
            adminNameLabel.setText(currentAdmin.getNom());
            adminEmailLabel.setText(currentAdmin.getEmail());
        }

        // Load data
        refreshData();

        // Initialize event listeners for user selection
        initializeUserSelectionListeners();
    }

    private void initializeUserSelectionListeners() {
        // The user cards will be created with an OnMouseClicked handler
        // that sets the selectedUser and enables the delete button
    }
    // ====================== USER MANAGEMENT METHODS ======================
    private void refreshData() {
        // Load all users and pending users
        allUsers = userService.getAll();
        pendingUsers = userService.getPendingUsers();

        // Update UI components
        displayUsers();
        updateStatistics();
        updateCharts();

        // Update last updated time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (lastUpdatedLabel != null) {
            lastUpdatedLabel.setText("Last updated: " + now.format(formatter));
        }

        // Add activity log entry
        if (activityList != null) {
            activityList.getItems().add(0, "Dashboard refreshed at " + now.format(formatter));
        }
    }

    private void displayUsers() {
        // Clear containers
        allUsersCardContainer.getChildren().clear();
        pendingUsersCardContainer.getChildren().clear();

        // Display all users using UserCard component
        for (User user : allUsers) {
            UserCard userCard = createUserCard(user, true);
            allUsersCardContainer.getChildren().add(userCard);
        }

        // Display pending users using UserCard component
        for (User user : pendingUsers) {
            UserCard userCard = createUserCard(user, false);
            pendingUsersCardContainer.getChildren().add(userCard);
        }
    }

    private UserCard createUserCard(User user, boolean isRegularUser) {
        UserCard card = new UserCard(user);

        // Add status label
        Label statusLabel = new Label("Status: " + user.getStatus());
        statusLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 12;");
        card.getChildren().add(statusLabel);

        // Add role label if needed
        if (user.getRole() != null) {
            Label roleLabel = new Label("Role: " + user.getRole().toString());
            roleLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 12;");
            card.getChildren().add(roleLabel);
        }

        // Set selection handling
        card.setOnMouseClicked(event -> {
            // Deselect all other cards
            if (isRegularUser) {
                allUsersCardContainer.getChildren().forEach(node ->
                        node.getStyleClass().remove("selected-card"));
                selectedUser = user;
                System.out.println("Selected user: " + selectedUser.getNom()); // Log selected user
                deleteUserButton.setDisable(false);
            } else {
                pendingUsersCardContainer.getChildren().forEach(node ->
                        node.getStyleClass().remove("selected-card"));
                selectedPendingUser = user;
                System.out.println("Selected pending user: " + selectedPendingUser.getNom()); // Log selected pending user
                approveButton.setDisable(false);
                rejectButton.setDisable(false);
            }

            // Select this card
            card.getStyleClass().add("selected-card");
        });

        return card;
    }

    private void updateStatistics() {
        if (totalUsersLabel != null) {
            totalUsersLabel.setText(String.valueOf(userService.getTotalUsersCount()));
        }
        if (activeUsersLabel != null) {
            activeUsersLabel.setText(String.valueOf(userService.getActiveUsersCount()));
        }
        if (pendingUsersLabel != null) {
            pendingUsersLabel.setText(String.valueOf(userService.getPendingUsersCount()));
        }
    }

    private void updateCharts() {
        if (userRoleChart != null) {
            // Clear existing data
            userRoleChart.getData().clear();

            // Add data for each role
            userRoleChart.getData().add(new PieChart.Data("Regular Users",
                    userService.getUserCountByRole(Role.REGULARUSER)));
            userRoleChart.getData().add(new PieChart.Data("Admins",
                    userService.getUserCountByRole(Role.ADMIN)));
            userRoleChart.getData().add(new PieChart.Data("Artists",
                    userService.getUserCountByRole(Role.ARTIST)));
            userRoleChart.getData().add(new PieChart.Data("Therapists",
                    userService.getUserCountByRole(Role.THERAPIST)));
        }
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchTextField.getText().trim(); // Utilisation de searchTextField
        if (searchTerm.isEmpty()) {
            refreshData();
            return;
        }

        // Search for users
        allUsersCardContainer.getChildren().clear();
        pendingUsersCardContainer.getChildren().clear();

        List<User> filteredUsers = userService.searchUsers(searchTerm);
        for (User user : filteredUsers) {
            UserCard userCard = createUserCard(user, true);
            allUsersCardContainer.getChildren().add(userCard);
        }

        List<User> filteredPendingUsers = userService.searchPendingUsers(searchTerm);
        for (User user : filteredPendingUsers) {
            UserCard userCard = createUserCard(user, false);
            pendingUsersCardContainer.getChildren().add(userCard);
        }
    }

    @FXML
    private void handleRefresh() {
        //searchField.clear();
        refreshData();
    }

    @FXML
    private void handleApprove() {
        if (selectedPendingUser != null) {
            boolean success = userService.approveUser(selectedPendingUser.getId());
            if (success) {
                activityList.getItems().add(0, "User " + selectedPendingUser.getEmail() + " approved");
                refreshData();
                approveButton.setDisable(true);
                rejectButton.setDisable(true);
                selectedPendingUser = null;
            }
        }
    }

    @FXML
    private void handleReject() {
        if (selectedPendingUser != null) {
            boolean success = userService.rejectUser(selectedPendingUser.getId());
            if (success) {
                activityList.getItems().add(0, "User " + selectedPendingUser.getEmail() + " rejected");
                refreshData();
                approveButton.setDisable(true);
                rejectButton.setDisable(true);
                selectedPendingUser = null;
            }
        }
    }

    @FXML
    private void handleDeleteUser() {
        if (selectedUser != null) {
            // Show confirmation dialog
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirm Deletion");
            confirmDialog.setHeaderText("Delete User");
            confirmDialog.setContentText("Are you sure you want to delete user: " + selectedUser.getEmail() + "?");

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = userService.deleteUser(selectedUser.getId());
                if (success) {
                    activityList.getItems().add(0, "User " + selectedUser.getEmail() + " deleted");
                    refreshData();
                    deleteUserButton.setDisable(true);
                    selectedUser = null;
                } else {
                    // Show error dialog
                    Alert errorDialog = new Alert(Alert.AlertType.ERROR);
                    errorDialog.setTitle("Error");
                    errorDialog.setHeaderText("Delete Failed");
                    errorDialog.setContentText("Failed to delete the user. Please try again.");
                    errorDialog.showAndWait();
                }
            }
        }
    }

    @FXML
    private void toggleProfilePanel() {
        profilePanel.setVisible(!profilePanel.isVisible());
        profilePanel.setManaged(profilePanel.isVisible());
    }

    @FXML
    private void handleEditProfile() {
        // Implementation for editing profile
        System.out.println("Edit profile");
    }

    @FXML
    private Button logoutButton;

    @FXML
    private void handleLogout() {
        try {
            // 1. Nettoyer la session utilisateur
            tn.esprit.utils.SessionManager.clearSession();

            // 2. Charger la page de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            // 3. Récupérer la fenêtre actuelle et afficher la page login
            Stage stage = (Stage) logoutButton.getScene().getWindow();
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
    private void showAccountsMenu(MouseEvent event) {
        ContextMenu accountsMenu = new ContextMenu();

        MenuItem profileItem = new MenuItem("Mon profil");
        profileItem.setOnAction(e -> handleEditProfile());

        MenuItem logoutItem = new MenuItem("Déconnexion");
        logoutItem.setOnAction(e -> handleLogout());

        accountsMenu.getItems().addAll(profileItem, logoutItem);
        
        accountsMenu.show(((Node) event.getSource()), event.getScreenX(), event.getScreenY());
    }

    // ====================== BENEFICIARIES & DONATIONS METHODS ======================
    private void initializeBeneficiariesAndDonations() {
        loadBeneficiaires();
        loadDons();
        setupBeneficiairesSearchAndFilter();
        setupDonsSearchAndFilter();
        setupValidation();
        updateBeneficiariesStatistics();
    }

    private void loadBeneficiaires() {
        try {
            beneficiairesList.setAll(servicesBeneficiaires.getAll());
            filteredBeneficiaires = new FilteredList<>(beneficiairesList, p -> true);
            beneficiairesListView.setCellFactory(lv -> new ListCell<Beneficiaires>() {
                private final Button modifierBtn = new Button("Modifier");
                private final Button detailsBtn = new Button("Détails");
                private final Button accepterBtn = new Button("Accepter");
                private final Button refuserBtn = new Button("Refuser");
                private final Button supprimerBtn = new Button("Supprimer");
                private final HBox buttonsBox = new HBox(5, modifierBtn, detailsBtn, accepterBtn, refuserBtn, supprimerBtn);

                {
                    modifierBtn.setOnAction(e -> handleUpdate(getItem()));
                    detailsBtn.setOnAction(e -> handleDetail(getItem()));
                    accepterBtn.setOnAction(e -> handleAccept(getItem()));
                    refuserBtn.setOnAction(e -> handleReject(getItem()));
                    supprimerBtn.setOnAction(e -> handleDelete(getItem()));
                }

                @Override
                protected void updateItem(Beneficiaires item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        VBox infoBox = new VBox(
                                new Label("Nom: " + item.getNom()),
                                new Label("Email: " + item.getEmail()),
                                new Label("Cause: " + item.getCause()),
                                new Label("Statut: " + (item.getStatus() == null ? "En attente" : item.getStatus()))
                        );
                        HBox card = new HBox(10, infoBox, buttonsBox);
                        setGraphic(card);
                    }
                }
            });
            beneficiairesListView.setItems(filteredBeneficiaires);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les bénéficiaires");
            e.printStackTrace();
        }
    }

    private void loadDons() {
        try {
            donsList.setAll(servicesDons.getAll());
            filteredDons = new FilteredList<>(donsList, p -> true);
            donsListView.setCellFactory(param -> new ListCell<Dons>() {
                @Override
                protected void updateItem(Dons item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        VBox vbox = new VBox(5);
                        HBox headerBox = new HBox(10);
                        Label typeLabel = new Label("Type: " + item.getType());
                        Label valeurLabel = new Label("Valeur: " + item.getValeur());
                        headerBox.getChildren().addAll(typeLabel, valeurLabel);
                        Label descriptionLabel = new Label("Description: " + item.getDescription());
                        descriptionLabel.setWrapText(true);
                        HBox beneficiaireBox = new HBox(10);
                        Label beneficiaireLabel = new Label("Bénéficiaire: " +
                                (item.getBeneficiaire() != null ? item.getBeneficiaire().getNom() : "Non spécifié"));
                        beneficiaireBox.getChildren().add(beneficiaireLabel);
                        Button detailButton = new Button("Détails");
                        detailButton.setOnAction(event -> handleDetailDon(item));
                        HBox buttonBox = new HBox(10);
                        buttonBox.getChildren().add(detailButton);
                        vbox.getChildren().addAll(headerBox, descriptionLabel, beneficiaireBox, buttonBox);
                        setGraphic(vbox);
                    }
                }
            });
            donsListView.setItems(filteredDons);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les dons");
            e.printStackTrace();
        }
    }

    private void setupBeneficiairesSearchAndFilter() {
        if (searchTextField == null) return;
        filteredBeneficiaires = new FilteredList<>(beneficiairesList, p -> true);
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            String lowerCaseFilter = newValue == null ? "" : newValue.toLowerCase();
            filteredBeneficiaires.setPredicate(beneficiaire ->
                    beneficiaire.getNom().toLowerCase().contains(lowerCaseFilter) ||
                            beneficiaire.getEmail().toLowerCase().contains(lowerCaseFilter) ||
                            beneficiaire.getCause().toLowerCase().contains(lowerCaseFilter)
            );
        });
        beneficiairesListView.setItems(filteredBeneficiaires);
    }

    private void setupDonsSearchAndFilter() {
        if (searchTextField == null) return;
        filteredDons = new FilteredList<>(donsList, p -> true);
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            String lowerCaseFilter = newValue == null ? "" : newValue.toLowerCase();
            filteredDons.setPredicate(don ->
                    don.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                            (don.getBeneficiaire() != null && don.getBeneficiaire().getNom().toLowerCase().contains(lowerCaseFilter))
            );
        });
    }

    private void setupValidation() {
        // Email validation
        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!EMAIL_PATTERN.matcher(newValue).matches()) {
                emailTextField.setStyle("-fx-border-color: red;");
            } else {
                emailTextField.setStyle("");
            }
        });
        // Phone validation
        telephoneTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!PHONE_PATTERN.matcher(newValue).matches()) {
                telephoneTextField.setStyle("-fx-border-color: red;");
            } else {
                telephoneTextField.setStyle("");
            }
        });
        // Numeric validation for valeurTextField
        valeurTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                valeurTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        // Numeric validation for valeurDonTextField
        valeurDonTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                valeurDonTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void updateBeneficiariesStatistics() {
        // Update beneficiaires statistics
        int total = beneficiairesList.size();
        int enAttente = (int) beneficiairesList.stream().filter(b -> "en attente".equals(b.getStatus())).count();
        int acceptes = (int) beneficiairesList.stream().filter(b -> "accepté".equals(b.getStatus())).count();
        int refuses = (int) beneficiairesList.stream().filter(b -> "refusé".equals(b.getStatus())).count();
        totalBeneficiairesLabel.setText(String.valueOf(total));
        enAttenteLabel.setText(String.valueOf(enAttente));
        acceptesLabel.setText(String.valueOf(acceptes));
        refusesLabel.setText(String.valueOf(refuses));
        // Update dons statistics
        int totalDons = donsList.size();
        double valeurTotale = donsList.stream().mapToDouble(Dons::getValeur).sum();
        int donsNature = (int) donsList.stream().filter(d -> "Materiels".equals(d.getType())).count();
        int donsFinanciers = (int) donsList.stream().filter(d -> "Argent".equals(d.getType())).count();
        totalDonsLabel.setText(String.valueOf(totalDons));
        valeurTotaleLabel.setText(String.format("%.2f DT", valeurTotale));
        donsNatureLabel.setText(String.valueOf(donsNature));
        donsFinanciersLabel.setText(String.valueOf(donsFinanciers));
        // Update charts
        updateBeneficiariesCharts();
    }

    private void updateBeneficiariesCharts() {
        beneficiairesChart.getData().clear();
        beneficiairesStatsChart.getData().clear();
        donsChart.getData().clear();
        donsStatsChart.getData().clear();
        ObservableList<PieChart.Data> beneficiairesData = FXCollections.observableArrayList(
                new PieChart.Data("En Attente",
                        beneficiairesList.stream().filter(b -> "en attente".equals(b.getStatus())).count()),
                new PieChart.Data("Acceptés",
                        beneficiairesList.stream().filter(b -> "accepté".equals(b.getStatus())).count()),
                new PieChart.Data("Refusés",
                        beneficiairesList.stream().filter(b -> "refusé".equals(b.getStatus())).count())
        );
        beneficiairesChart.setData(beneficiairesData);
        beneficiairesStatsChart.setData(FXCollections.observableArrayList(beneficiairesData));
        ObservableList<PieChart.Data> donsData = FXCollections.observableArrayList(
                new PieChart.Data("Dons en Nature",
                        donsList.stream().filter(d -> "Materiels".equals(d.getType())).count()),
                new PieChart.Data("Dons Financiers",
                        donsList.stream().filter(d -> "Argent".equals(d.getType())).count())
        );
        donsChart.setData(donsData);
        donsStatsChart.setData(FXCollections.observableArrayList(donsData));
    }

    // ====================== ARTWORK MANAGEMENT METHODS ======================
    private void initializeArtworkManagement() {
        loadArtworks();
        setupArtworkSearch();
        ajoutArtworkButton.setOnAction(event -> openAddArtworkDialog());
        exportPdfArtworkButton.setOnAction(event -> handleExportArtworkPdf());
        setupArtworksListView();
    }

    public void loadArtworks() {
        artworkObservableList.setAll(artworkService.getAll());
        filteredArtworks = new FilteredList<>(artworkObservableList, p -> true);
        artworksListView.setItems(filteredArtworks);
    }

    private void setupArtworksListView() {
        artworksListView.setCellFactory(param -> new ListCell<Artwork>() {
            private final ImageView artworkImageView;
            private final VBox detailsBox;
            private final Label titleLabel;
            private final Label artistLabel;
            private final Label priceLabel;
            private final Label descriptionLabel;
            private final Button approveButton;
            private final Button denyButton;
            private final Button updateButton;
            private final Button deleteButton;
            private final HBox buttonBox;
            private final HBox content;

            {
                artworkImageView = new ImageView();
                artworkImageView.setFitWidth(80);
                artworkImageView.setFitHeight(80);
                artworkImageView.setPreserveRatio(true);
                artworkImageView.setSmooth(true);
                titleLabel = new Label();
                artistLabel = new Label();
                priceLabel = new Label();
                descriptionLabel = new Label();
                approveButton = new Button("Approve");
                denyButton = new Button("Deny");
                updateButton = new Button("Update");
                deleteButton = new Button("Delete");
                detailsBox = new VBox(titleLabel, artistLabel, priceLabel, descriptionLabel);
                buttonBox = new HBox(10, approveButton, denyButton, updateButton, deleteButton);
                content = new HBox(10, artworkImageView, detailsBox, buttonBox);
            }

            @Override
            protected void updateItem(Artwork artwork, boolean empty) {
                super.updateItem(artwork, empty);
                if (empty || artwork == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Always re-bind button actions to the current artwork!
                    approveButton.setOnAction(e -> handleApproveArtwork(artwork));
                    denyButton.setOnAction(e -> handleDenyArtwork(artwork));
                    updateButton.setOnAction(e -> handleUpdateArtwork(artwork));
                    deleteButton.setOnAction(e -> handleDeleteArtwork(artwork));

                    if (artwork.getImage() != null && !artwork.getImage().isEmpty()) {
                        try {
                            String imgPath = artwork.getImage();
                            if (!imgPath.startsWith("http://") && !imgPath.startsWith("https://")) {
                                while (imgPath.startsWith("/") || imgPath.startsWith("\\")) {
                                    imgPath = imgPath.substring(1);
                                }
                                imgPath = "http://localhost/artwork_images/" + imgPath;
                            }
                            artworkImageView.setImage(new Image(imgPath, true));
                        } catch (Exception e) {
                            artworkImageView.setImage(null);
                        }
                    } else {
                        artworkImageView.setImage(null);
                    }
                    titleLabel.setText(artwork.getTitre());
                    artistLabel.setText("Artiste: " + artwork.getArtistenom());
                    priceLabel.setText("Prix: " + artwork.getPrix() + " DT");
                    descriptionLabel.setText(artwork.getDescription());
                    setGraphic(content);
                }
            }
        });
    }

    private void handleApproveArtwork(Artwork artwork) {
        if (artwork != null) {
            artwork.setStatus("approved");
            artworkService.update(artwork);
            loadArtworks();
            showSuccessAlert("Artwork Approved", "Artwork has been approved.");
        }
    }

    private void handleDenyArtwork(Artwork artwork) {
        if (artwork != null) {
            artwork.setStatus("denied");
            artworkService.update(artwork);
            loadArtworks();
            showSuccessAlert("Artwork Denied", "Artwork has been denied.");
        }
    }

    private void handleUpdateArtwork(Artwork artwork) {
        if (artwork != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Modifierartwork.fxml"));
                Parent root = loader.load();
                ModifierartworkController controller = loader.getController();
                controller.setArtwork(artwork);
                Stage stage = new Stage();
                stage.setTitle("Modifier l'oeuvre");
                stage.setScene(new Scene(root));
                stage.setMaximized(true);
                stage.showAndWait();
                loadArtworks();
            } catch (IOException e) {
                showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification d'oeuvre: " + e.getMessage());
            }
        }
    }

    private void handleDeleteArtwork(Artwork artwork) {
        if (artwork != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirmation de suppression");
            confirmDialog.setHeaderText("Supprimer l'oeuvre");
            confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette oeuvre ?");
            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    artworkService.delete(artwork);
                    loadArtworks();
                    showSuccessAlert("Artwork Deleted", "Artwork has been deleted.");
                } catch (Exception e) {
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        }
    }

    private void openAddArtworkDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddArtwork.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Oeuvre");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadArtworks();
        } catch (IOException e) {
            System.err.println("[ERROR] Impossible d'ouvrir le formulaire d'ajout d'oeuvre: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout d'oeuvre: " + e.getMessage());
        }
    }

    private void handleExportArtworkPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la liste des oeuvres en PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(exportPdfArtworkButton.getScene().getWindow());
        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                document.add(new Paragraph("Liste des Oeuvres"));
                for (Artwork artwork : artworkObservableList) {
                    document.add(new Paragraph("Titre: " + artwork.getTitre()));
                    document.add(new Paragraph("Artiste: " + artwork.getArtistenom()));
                    document.add(new Paragraph("Prix: " + artwork.getPrix() + " DT"));
                    document.add(new Paragraph("Description: " + artwork.getDescription()));
                    document.add(new Paragraph("-----------------------------"));
                }
                document.close();
                showSuccessAlert("Export PDF", "La liste des oeuvres a été exportée avec succès.");
            } catch (Exception e) {
                showAlert("Erreur", "Impossible d'exporter le PDF: " + e.getMessage());
            }
        }
    }

    private void setupArtworkSearch() {
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            String lowerCaseFilter = newValue == null ? "" : newValue.toLowerCase();
            filteredArtworks.setPredicate(artwork ->
                    artwork.getTitre().toLowerCase().contains(lowerCaseFilter) ||
                            artwork.getArtistenom().toLowerCase().contains(lowerCaseFilter) ||
                            artwork.getDescription().toLowerCase().contains(lowerCaseFilter)
            );
        });
    }

    // ====================== FORUM MANAGEMENT METHODS ======================
    private void initializeForumControllers() {
        try {
            // Charger AjouterForumController
            FXMLLoader ajouterLoader = (FXMLLoader) ajouterForumPane.getProperties().get("fx:controller");
            if (ajouterLoader != null) {
                ajouterForumController = ajouterLoader.getController();
            }

            // Charger AfficherForumController
            FXMLLoader afficherLoader = (FXMLLoader) afficherForumPane.getProperties().get("fx:controller");
            if (afficherLoader != null) {
                afficherForumController = afficherLoader.getController();
                afficherForumController.setAfficherForumController(afficherForumController);

                if (ajouterForumController != null) {
                    ajouterForumController.setAfficherForumController(afficherForumController);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'initialiser les contrôleurs Forum");
        }
    }

    // ====================== SHARED UTILITY METHODS ======================
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ====================== BENEFICIARIES & DONATIONS HANDLERS ======================
    private void handleUpdate(Beneficiaires beneficiaire) {
        showAlert("Update", "Update logic for: " + (beneficiaire != null ? beneficiaire.getNom() : "null"));
    }

    private void handleDetail(Beneficiaires beneficiaire) {
        showAlert("Detail", "Detail logic for: " + (beneficiaire != null ? beneficiaire.getNom() : "null"));
    }

    private void handleAccept(Beneficiaires beneficiaire) {
        showSuccessAlert("Accept", "Accepted: " + (beneficiaire != null ? beneficiaire.getNom() : "null"));
    }

    private void handleReject(Beneficiaires beneficiaire) {
        showAlert("Reject", "Rejected: " + (beneficiaire != null ? beneficiaire.getNom() : "null"));
    }

    private void handleDelete(Beneficiaires beneficiaire) {
        showSuccessAlert("Delete", "Deleted: " + (beneficiaire != null ? beneficiaire.getNom() : "null"));
    }

    private void handleDetailDon(Dons don) {
        showAlert("Don Detail", "Detail for Don: " + (don != null ? don.getDescription() : "null"));
    }

    @FXML
    public void handleAfficherAtelier() {
        showSuccessAlert("Afficher Atelier", "Logic to show atelier list goes here.");
    }

    @FXML
    public void handleFilterEnAttente() {
        showSuccessAlert("Filtre En Attente", "Filtre 'En attente' appliqué.");
    }

    @FXML
    public void handleFilterAccepte() {
        showSuccessAlert("Filtre Accepté", "Filtre 'Accepté' appliqué.");
    }

    @FXML
    public void handleFilterRefuse() {
        showSuccessAlert("Filtre Refusé", "Filtre 'Refusé' appliqué.");
    }

    @FXML
    public void handleImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Image");
        File file = fileChooser.showOpenDialog(beneficiairesPane.getScene().getWindow());
        if (file != null) {
            showSuccessAlert("Image Upload", "Selected file: " + file.getName());
        } else {
            showAlert("Image Upload", "No file selected.");
        }
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        showSuccessAlert("Submit", "Beneficiary form submitted!");
    }

    @FXML
    public void handleDonSubmit(ActionEvent event) {
        showSuccessAlert("Submit Don", "Donation form submitted!");
    }

    @FXML
    public void switchToAtelierManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherAtelier.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Attempt to retrieve the current stage from a reliable source
            Stage stage = null;
            if (profilePanel != null && profilePanel.getScene() != null) {
                stage = (Stage) profilePanel.getScene().getWindow();
            }

            // Log a warning if no stage is found
            if (stage == null) {
                System.err.println("Warning: No existing stage found for Atelier Management redirection.");
                return; // Exit without creating a new stage
            }

            stage.setScene(scene);
            stage.setTitle("Atelier Management");
            stage.show();
        } catch (IOException e) {
            System.err.println("IOException occurred during redirection to Atelier Management: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Navigation Error");
            alert.setContentText("Unable to load the Atelier Management view.");
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Unexpected error during redirection to Atelier Management: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Unexpected Error");
            alert.setContentText("An unexpected error occurred during navigation.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleViewProfile(ActionEvent event) {
        System.out.println("handleViewProfile method triggered.");
        try {
            // Load the profile.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile.fxml"));
            Parent profileView = loader.load();
            System.out.println("profile.fxml loaded successfully.");

            // Get the ProfileController instance
            ProfileController profileController = loader.getController();

            // Pass the selected user to the ProfileController
            User selectedUser = getSelectedUser(); // Implement this method to fetch the selected user
            if (selectedUser == null) {
                System.err.println("Error: No user selected. Cannot display profile.");
                return;
            }
            System.out.println("Selected user: " + selectedUser.getNom());
            profileController.setUser(selectedUser);

            // Display the profile view in a new stage
            Stage stage = new Stage();
            stage.setScene(new Scene(profileView));
            stage.setTitle("User Profile");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading profile view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private User getSelectedUser() {
        if (selectedUser != null) {
            return selectedUser;
        } else {
            // Show an alert if no user is selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No User Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a user before viewing their profile.");
            alert.showAndWait();
            return null;
        }
    }

    @Override
    public void setUser(User user) {

    }
}