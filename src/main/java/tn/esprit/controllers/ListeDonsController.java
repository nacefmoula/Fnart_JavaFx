package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import tn.esprit.models.Dons;
import tn.esprit.services.ServicesDons;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ListeDonsController implements Initializable {

    @FXML
    private ListView<Dons> donsListView;

    @FXML
    private TextField searchTextField;

    @FXML
    private ChoiceBox<String> typeFilterChoice;

    @FXML
    private Button backButton;

    @FXML
    private AnchorPane rootPane;

    private final ServicesDons servicesDons = new ServicesDons();
    private ObservableList<Dons> donsList;
    private FilteredList<Dons> filteredDons;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String cssPath = getClass().getResource("/css/style.css").toExternalForm();
        System.out.println("CSS file found at: " + cssPath);

        loadDons();
        backButton.setOnAction(event -> handleBack());
    }

    private void loadDons() {
        try {
            donsList = FXCollections.observableArrayList(servicesDons.getAll());
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

                                Label typeLabel = new Label("Type: " + item.getType());

                                HBox valueBox = new HBox(10);
                                Label valeurLabel = new Label("Valeur: " + item.getValeur());
                                valueBox.getChildren().add(valeurLabel);

                                Label beneficiaireLabel = new Label("Bénéficiaire: " + item.getBeneficiaire().getNom());

                                Label descriptionLabel = new Label(item.getDescription());
                                descriptionLabel.setWrapText(true);

                                vbox.getChildren().addAll(typeLabel, valueBox, beneficiaireLabel, descriptionLabel);
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




    @FXML
    private void handleBack() {
        try {
            Parent fxml = FXMLLoader.load(getClass().getResource("/AddDons.fxml"));
            rootPane.getChildren().removeAll();
            rootPane.getChildren().setAll(fxml);
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
            FXMLLoader loader = new FXMLLoader(ListeDonsController.class.getResource("/ListeDons.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            primaryStage.setTitle("Liste des Dons");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger la liste des dons.");
            alert.showAndWait();
        }
    }
} 