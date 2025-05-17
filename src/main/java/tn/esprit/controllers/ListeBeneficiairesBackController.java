package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Beneficiaires;
import tn.esprit.services.ServicesBeneficiaires;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ListeBeneficiairesBackController implements Initializable {

    @FXML
    private ListView<Beneficiaires> beneficiairesListView;


    @FXML
    private ChoiceBox<String> sortChoice;

    @FXML
    private TextField searchTextField;

    @FXML
    private Button addButton;

    @FXML
    private Button backButton;

    private final ServicesBeneficiaires servicesBeneficiaires = new ServicesBeneficiaires();
    private ObservableList<Beneficiaires> beneficiairesList;
    private FilteredList<Beneficiaires> filteredBeneficiaires;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load beneficiaires
        loadBeneficiaires();

        // Set up search functionality
        setupSearch();

        // Set up back button
        backButton.setOnAction(event -> handleBack());

        // Set up add button
        addButton.setOnAction(event -> handleAdd());
    }

    private void loadBeneficiaires() {
        try {
            beneficiairesList = FXCollections.observableArrayList(servicesBeneficiaires.getAll());
            filteredBeneficiaires = new FilteredList<>(beneficiairesList, p -> true);

            beneficiairesListView.setCellFactory(lv -> new ListCell<Beneficiaires>() {
                private final Button modifierBtn = new Button("Modifier");
                private final Button detailsBtn = new Button("Détails");
                private final Button accepterBtn = new Button("Accepter");
                private final Button refuserBtn = new Button("Refuser");
                
                {
                    modifierBtn.getStyleClass().add("forum-btn");
                    detailsBtn.getStyleClass().add("forum-btn-alt");
                    accepterBtn.getStyleClass().add("forum-btn");
                    refuserBtn.getStyleClass().add("forum-btn-alt");
                }

                @Override
                protected void updateItem(Beneficiaires item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox mainBox = new HBox(10);
                        mainBox.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5;");

                        // Image container
                        ImageView imageView = new ImageView();
                        imageView.setFitWidth(80);
                        imageView.setFitHeight(80);
                        imageView.setPreserveRatio(true);

                        // Load image
                        if (item.getImage() != null && !item.getImage().isEmpty()) {
                            try {
                                File imageFile = new File(item.getImage());
                                if (imageFile.exists()) {
                                    Image image = new Image(imageFile.toURI().toString());
                                    imageView.setImage(image);
                                } else {
                                    // Load default image if file doesn't exist
                                    File defaultImage = new File("src/main/resources/images/default_profile.png");
                                    if (defaultImage.exists()) {
                                        imageView.setImage(new Image(defaultImage.toURI().toString()));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Load default image if no image path
                            try {
                                File defaultImage = new File("src/main/resources/images/default_profile.png");
                                if (defaultImage.exists()) {
                                    imageView.setImage(new Image(defaultImage.toURI().toString()));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        VBox infoBox = new VBox(5);
                        Label nomLabel = new Label("Nom: " + item.getNom());
                        Label emailLabel = new Label("Email: " + item.getEmail());
                        Label telLabel = new Label("Tél: " + item.getTelephone());
                        Label causeLabel = new Label("Cause: " + item.getCause());
                        Label statusLabel = new Label("Statut: " + (item.getStatus() != null ? item.getStatus() : "En attente"));
                        
                        infoBox.getChildren().addAll(nomLabel, emailLabel, telLabel, causeLabel, statusLabel);

                        HBox buttonsBox = new HBox(5);
                        modifierBtn.setOnAction(e -> handleUpdate(item));
                        detailsBtn.setOnAction(e -> handleDetail(item));
                        accepterBtn.setOnAction(e -> handleAccept(item));
                        refuserBtn.setOnAction(e -> handleReject(item));
                        
                        buttonsBox.getChildren().addAll(modifierBtn, detailsBtn, accepterBtn, refuserBtn);
                        
                        mainBox.getChildren().addAll(imageView, infoBox, buttonsBox);
                        setGraphic(mainBox);
                    }
                }
            });

            beneficiairesListView.setItems(filteredBeneficiaires);

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les bénéficiaires");
            e.printStackTrace();
        }
    }


    private void setupSearch() {
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredBeneficiaires.setPredicate(beneficiaire -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return beneficiaire.getNom().toLowerCase().contains(lowerCaseFilter) ||
                       beneficiaire.getEmail().toLowerCase().contains(lowerCaseFilter) ||
                       beneficiaire.getTelephone().toLowerCase().contains(lowerCaseFilter) ||
                       beneficiaire.getCause().toLowerCase().contains(lowerCaseFilter) ||
                       (beneficiaire.getStatus() != null && beneficiaire.getStatus().toLowerCase().contains(lowerCaseFilter));
            });
            beneficiairesListView.setItems(filteredBeneficiaires);
        });
    }
    private void setupSorting() {
        sortChoice.valueProperty().addListener((observable, oldValue, newValue) -> {
            SortedList<Beneficiaires> sortedList = new SortedList<>(filteredBeneficiaires);
            if ("Nom (A-Z)".equals(newValue)) {
                sortedList.setComparator((b1, b2) -> b1.getNom().compareToIgnoreCase(b2.getNom()));
            } else if ("Nom (Z-A)".equals(newValue)) {
                sortedList.setComparator((b1, b2) -> b2.getNom().compareToIgnoreCase(b1.getNom()));
            }
            beneficiairesListView.setItems(sortedList);
        });
    }
    private void handleAccept(Beneficiaires beneficiaire) {
        try {
            beneficiaire.setStatus("Accepté");
            servicesBeneficiaires.update(beneficiaire);
            loadBeneficiaires(); // Refresh the list
            showSuccessAlert("Succès", "Le bénéficiaire a été accepté avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'accepter le bénéficiaire: " + e.getMessage());
        }
    }

    private void handleReject(Beneficiaires beneficiaire) {
        try {
            beneficiaire.setStatus("Refusé");
            servicesBeneficiaires.update(beneficiaire);
            loadBeneficiaires(); // Refresh the list
            showSuccessAlert("Succès", "Le bénéficiaire a été refusé avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de refuser le bénéficiaire: " + e.getMessage());
        }
    }
    
    private void handleDetail(Beneficiaires beneficiaire) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailBeneficiaireBack.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the beneficiaire object
            DetailBeneficiaireBackController controller = loader.getController();
            controller.setBeneficiaire(beneficiaire);
            
            Stage stage = new Stage();
            stage.setTitle("Détails du bénéficiaire");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails du bénéficiaire: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleUpdate(Beneficiaires beneficiaire) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateBeneficiaireBack.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the beneficiaire object
            UpdateBeneficiaireBackController controller = loader.getController();
            controller.setBeneficiaire(beneficiaire);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier le bénéficiaire");
            stage.setScene(new Scene(root));
            stage.show();
            
            // Refresh the list when the update form is closed
            stage.setOnHidden(event -> loadBeneficiaires());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            System.out.println("Retour à l'accueil...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du retour à l'accueil: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à l'accueil: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        try {
            System.out.println("Ouverture du formulaire d'ajout...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddBeneficiaireBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) addButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Ajouter un bénéficiaire");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du formulaire d'ajout: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void handleFilterEnAttente() {
        filteredBeneficiaires.setPredicate(beneficiaire -> 
            beneficiaire.getStatus() != null && beneficiaire.getStatus().equals("en attente"));
        beneficiairesListView.setItems(filteredBeneficiaires);
    }

    @FXML
    private void handleFilterAccepte() {
        filteredBeneficiaires.setPredicate(beneficiaire -> 
            beneficiaire.getStatus() != null && beneficiaire.getStatus().equals("Accepté"));
        beneficiairesListView.setItems(filteredBeneficiaires);
    }

    @FXML
    private void handleFilterRefuse() {
        filteredBeneficiaires.setPredicate(beneficiaire -> 
            beneficiaire.getStatus() != null && beneficiaire.getStatus().equals("Refusé"));
        beneficiairesListView.setItems(filteredBeneficiaires);
    }

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

    public static void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(ListeBeneficiairesBackController.class.getResource("/ListeBeneficiairesBack.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            primaryStage.setTitle("Gestion des Bénéficiaires");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger la liste des bénéficiaires: " + e.getMessage());
            alert.showAndWait();
        }
    }
} 