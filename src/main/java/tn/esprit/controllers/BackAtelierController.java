package tn.esprit.controllers;

import tn.esprit.models.Atelier;
import tn.esprit.services.AtelierService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class BackAtelierController implements Initializable {
    @FXML
    private TableView<Atelier> ateliersTable;
    @FXML
    private TableColumn<Atelier, String> titreColumn;
    @FXML
    private TableColumn<Atelier, String> lieuColumn;
    @FXML
    private TableColumn<Atelier, String> dateColumn;
    @FXML
    private TableColumn<Atelier, Integer> participantMaxColumn;
    @FXML
    private TableColumn<Atelier, String> descriptionColumn;
    @FXML
    private TableColumn<Atelier, Void> actionsColumn;

    private AtelierService atelierService;
    private ObservableList<Atelier> ateliersList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        atelierService = new AtelierService();
        ateliersList = FXCollections.observableArrayList();
        ateliersTable.setItems(ateliersList);

        // Configuration des colonnes
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        participantMaxColumn.setCellValueFactory(new PropertyValueFactory<>("participantMax"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Configuration de la colonne des actions
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button detailsButton = new Button("Voir détails");
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                detailsButton.setOnAction(event -> {
                    Atelier atelier = getTableView().getItems().get(getIndex());
                    voirDetails(atelier);
                });

                editButton.setOnAction(event -> {
                    Atelier atelier = getTableView().getItems().get(getIndex());
                    modifierAtelier(atelier);
                });

                deleteButton.setOnAction(event -> {
                    Atelier atelier = getTableView().getItems().get(getIndex());
                    supprimerAtelier(atelier);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, detailsButton, editButton, deleteButton);
                    buttons.setStyle("-fx-alignment: center;");
                    setGraphic(buttons);
                }
            }
        });

        updateUI();
    }

    private void updateUI() {
        chargerAteliers();
        ateliersTable.refresh();
    }

    private void chargerAteliers() {
        ateliersList.clear();
        ateliersList.addAll(atelierService.getall());
    }

    private void voirDetails(Atelier atelier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/ressources/DetailsAtelier.fxml")); // Correct path
            Parent root = loader.load();
            
            DetailsAtelierController controller = loader.getController();
            controller.setAtelier(atelier);
            controller.setAdminView(true);
            
            Stage stage = new Stage();
            stage.setTitle("Détails de l'atelier: " + atelier.getTitre());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des détails: " + e.getMessage());
        }
    }

    private void supprimerAtelier(Atelier atelier) {
        if (atelier == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun atelier sélectionné");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cet atelier ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                atelierService.supprimer(atelier);
                updateUI();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Atelier supprimé avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void modifierAtelier(Atelier atelier) {
        try {
            // Make sure the atelier is not null
            if (atelier == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun atelier sélectionné");
                return;
            }
            
            // Load the modification form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/ressources/ModifierAtelier.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the atelier to modify
            ModifierAtelierController controller = loader.getController();
            controller.setAtelier(atelier);
            controller.setBackController(this); // Set reference to this controller for refresh
            
            // Create a new stage for the modification form
            Stage stage = new Stage();
            stage.setTitle("Modifier l'atelier: " + atelier.getTitre());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement du formulaire de modification: " + e.getMessage());
        }
    }
    
    /**
     * Public method to refresh the table data from outside the controller
     */
    public void refreshTable() {
        updateUI();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
