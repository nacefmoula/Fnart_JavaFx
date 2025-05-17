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
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import tn.esprit.models.Beneficiaires;
import tn.esprit.services.ServicesBeneficiaires;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ListeBeneficiairesController implements Initializable {

    @FXML
    private ListView<Beneficiaires> beneficiairesListView;

    @FXML
    private TextField searchTextField;

    @FXML
    private ChoiceBox<String> sortChoice;

    @FXML
    private Button backButton;

    @FXML
    private AnchorPane rootPane;

    private final ServicesBeneficiaires servicesBeneficiaires = new ServicesBeneficiaires();
    private ObservableList<Beneficiaires> beneficiairesList;
    private FilteredList<Beneficiaires> filteredBeneficiaires;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sortChoice.getItems().addAll("Nom (A-Z)", "Nom (Z-A)");
        sortChoice.setValue("Nom (A-Z)");

        loadBeneficiaires();
        setupSearch();
        setupSorting();

        backButton.setOnAction(event -> handleBack());

        beneficiairesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Beneficiaires selected = beneficiairesListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openUpdateForm(selected);
                }
            }
        });
    }

    private void loadBeneficiaires() {
        try {
            List<Beneficiaires> beneficiaires = servicesBeneficiaires.getAll();
            beneficiairesList = FXCollections.observableArrayList(beneficiaires);
            filteredBeneficiaires = new FilteredList<>(beneficiairesList, p -> true);

            beneficiairesListView.setCellFactory(new Callback<ListView<Beneficiaires>, ListCell<Beneficiaires>>() {
                @Override
                public ListCell<Beneficiaires> call(ListView<Beneficiaires> param) {
                    return new ListCell<>() {
                        @Override
                        protected void updateItem(Beneficiaires item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setGraphic(null);
                            } else {
                                HBox mainBox = new HBox(10);

                                VBox imageContainer = new VBox();
                                imageContainer.setPrefWidth(100);
                                ImageView imageView = new ImageView();
                                imageView.setFitWidth(100);
                                imageView.setFitHeight(100);
                                imageView.setPreserveRatio(true);

                                // Load image from file system
                                String imagePath = item.getImage();
                                if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("default_image.png")) {
                                    File imageFile = new File("src/main/resources/" + imagePath);
                                    if (imageFile.exists()) {
                                        imageView.setImage(new Image(imageFile.toURI().toString()));
                                    } else {
                                        System.err.println("Image file not found: " + imagePath);
                                        imageView.setImage(getDefaultImage());
                                    }
                                } else {
                                    imageView.setImage(getDefaultImage());
                                }

                                imageContainer.getChildren().add(imageView);

                                VBox contentBox = new VBox(5);

                                Label nameLabel = new Label(item.getNom());

                                HBox contactBox = new HBox(10);
                                Label emailLabel = new Label("Email: " + item.getEmail());
                                Label phoneLabel = new Label("Téléphone: " + item.getTelephone());

                                contactBox.getChildren().addAll(emailLabel, phoneLabel);

                                HBox detailsBox = new HBox(10);
                                Label causeLabel = new Label("Cause: " + item.getCause());
                                Label associationLabel = new Label("Association: " + item.getEstElleAssociation());
                                Label valeurLabel = new Label("Valeur Demandée: " + item.getValeurDemande() + " DT");

                                detailsBox.getChildren().addAll(causeLabel, associationLabel, valeurLabel);

                                VBox descriptionBox = new VBox(5);
                                Label descriptionTitle = new Label("Description:");
                                Label descriptionLabel = new Label(item.getDescription());
                                descriptionLabel.setWrapText(true);
                                descriptionBox.getChildren().addAll(descriptionTitle, descriptionLabel);

                                HBox actionBox = new HBox(10);
                                Button updateButton = new Button("Modifier");
                                updateButton.setOnAction(event -> openUpdateForm(item));
                                actionBox.getChildren().add(updateButton);

                                contentBox.getChildren().addAll(nameLabel, contactBox, detailsBox, descriptionBox, actionBox);

                                mainBox.getChildren().addAll(imageContainer, contentBox);
                                setGraphic(mainBox);
                            }
                        }
                    };
                }
            });

            beneficiairesListView.setItems(filteredBeneficiaires);

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les bénéficiaires");
            e.printStackTrace();
        }
    }

    private Image getDefaultImage() {
        try {
            return new Image(getClass().getResourceAsStream("/images/default_profile.png"));
        } catch (Exception e) {
            System.err.println("Default image not found.");
            return null;
        }
    }

    private void setupSearch() {
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredBeneficiaires.setPredicate(beneficiaire -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return beneficiaire.getNom().toLowerCase().contains(lowerCaseFilter) ||
                        beneficiaire.getEmail().toLowerCase().contains(lowerCaseFilter) ||
                        beneficiaire.getTelephone().toLowerCase().contains(lowerCaseFilter) ||
                        beneficiaire.getCause().toLowerCase().contains(lowerCaseFilter);
            });
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

    @FXML
    private void handleBack() {
        try {
            Parent fxml = FXMLLoader.load(getClass().getResource("/AddBeneficiaire.fxml"));
            rootPane.getChildren().removeAll();
            rootPane.getChildren().setAll(fxml);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de retourner à l'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openUpdateForm(Beneficiaires beneficiaire) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateBeneficiaire.fxml"));
            Parent root = loader.load();
            UpdateBeneficiaireController controller = loader.getController();
            controller.setBeneficiaire(beneficiaire);
            Stage stage = new Stage();
            stage.setTitle("Modifier un bénéficiaire");
            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnHidden(event -> loadBeneficiaires());
        } catch (IOException e) {
            e.printStackTrace();
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
            FXMLLoader loader = new FXMLLoader(ListeBeneficiairesController.class.getResource("/ListeBeneficiaires.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setTitle("Liste des Bénéficiaires");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger la liste des bénéficiaires.");
            alert.showAndWait();
        }
    }
}