package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import tn.esprit.models.Dons;
import tn.esprit.services.ServicesDons;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ListeDonsBackController implements Initializable {

    @FXML
    private ListView<Dons> donsListView;

    @FXML
    private TextField searchTextField;

    @FXML
    private ChoiceBox<String> typeFilterChoice;

    @FXML
    private Button addButton;

    @FXML
    private Button backButton;

    private final ServicesDons servicesDons = new ServicesDons();
    private ObservableList<Dons> donsList;
    private FilteredList<Dons> filteredDons;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize type filter choices
        typeFilterChoice.getItems().addAll("Tous", "argent", "materiel", "locale", "oeuvre");
        typeFilterChoice.setValue("Tous");

        // Load dons
        loadDons();

        // Set up search functionality
        setupSearch();

        // Set up type filter
        setupTypeFilter();

        // Set up back button
        backButton.setOnAction(event -> handleBack());

        // Set up add button
        addButton.setOnAction(event -> handleAdd());
    }

    private void loadDons() {
        try {
            donsList = FXCollections.observableArrayList(servicesDons.getAll());
            filteredDons = new FilteredList<>(donsList, p -> true);

            // Set up the list view cell factory
            donsListView.setCellFactory(new Callback<ListView<Dons>, ListCell<Dons>>() {
                @Override
                public ListCell<Dons> call(ListView<Dons> param) {
                    return new ListCell<Dons>() {
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

                                // Add detail button
                                Button detailButton = new Button("Détails");
                                detailButton.setOnAction(event -> handleDetail(item));
                                
                                HBox buttonBox = new HBox(10);
                                buttonBox.getChildren().add(detailButton);

                                vbox.getChildren().addAll(headerBox, descriptionLabel, beneficiaireBox, buttonBox);
                                setGraphic(vbox);
                            }
                        }
                    };
                }
            });

            // Set the items to the filtered list
            donsListView.setItems(filteredDons);

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les dons");
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilter();
        });
    }

    private void setupTypeFilter() {
        typeFilterChoice.valueProperty().addListener((observable, oldValue, newValue) -> {
            filteredDons.setPredicate(don -> {
                if ("Tous".equals(newValue)) {
                    return true;
                }
                return don.getType().equals(newValue);
            });
        });
    }
    
    private void updateFilter() {
        String searchText = searchTextField.getText().toLowerCase();
        String selectedType = typeFilterChoice.getValue();
        
        filteredDons.setPredicate(don -> {
            if (don == null) return false;
            
            boolean matchesSearch = searchText.isEmpty() || 
                (don.getType() != null && don.getType().toLowerCase().contains(searchText)) ||
                (don.getDescription() != null && don.getDescription().toLowerCase().contains(searchText)) ||
                (don.getBeneficiaire() != null && don.getBeneficiaire().getNom().toLowerCase().contains(searchText)) ||
                String.valueOf(don.getValeur()).contains(searchText);
                
            boolean matchesType = selectedType == null || "Tous".equals(selectedType) || 
                (don.getType() != null && don.getType().equals(selectedType));
                
            return matchesSearch && matchesType;
        });
    }

    @FXML
    private void handleDetail(Dons don) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailDonsBack.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the don object
            DetailDonsBackController controller = (DetailDonsBackController) loader.getController();
            controller.setdons(don);
            
            Stage stage = new Stage();
            stage.setTitle("Détails du don");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails du don: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Back Office - Administration");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à l'accueil: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddDonsBack.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un don");
            stage.setScene(new Scene(root));
            stage.show();
            
            // Refresh the list when the add form is closed
            stage.setOnHidden(event -> loadDons());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(ListeDonsBackController.class.getResource("/ListeDonsBack.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            primaryStage.setTitle("Gestion des Dons");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger la liste des dons: " + e.getMessage());
            alert.showAndWait();
        }
    }
} 