package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import tn.esprit.models.ElasticScroll;
import tn.esprit.models.Forum;
import tn.esprit.models.User;
import tn.esprit.services.ServiceForum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AfficherForumController {

    @FXML
    private void deleteForum(javafx.event.ActionEvent event) {
        Forum forumToDelete = getSelectedForum();
        if (forumToDelete == null) {
            showAlert("Aucun forum sélectionné", "Veuillez sélectionner un forum à supprimer.");
            return;
        }
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer la suppression");
        confirmAlert.setHeaderText("Supprimer le forum");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer le forum : " + forumToDelete.getTitre_f() + " ?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    ServiceForum serviceForum = new ServiceForum();
                    serviceForum.supprimer(forumToDelete.getId());
                    loadForumData();
                    showAlert("Succès", "Forum supprimé avec succès.");
                } catch (Exception e) {
                    showAlert("Erreur", "Échec de la suppression du forum.");
                }
            }
        });
    }


    private Forum selectedForum = null;


    @FXML
    private void updateForum(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierForum.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et passer le forum à modifier
            tn.esprit.controllers.ModifierForumController controller = loader.getController();
            // TODO : Remplacer par le forum sélectionné dans la grille (ici exemple Forum fictif)
            Forum selectedForum = getSelectedForum();
            if (selectedForum != null) {
                controller.setForum(selectedForum);
            }

            Stage popupStage = new Stage();
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.setTitle("Modifier Forum");
            popupStage.setScene(new Scene(root));
            popupStage.initOwner(((Button) event.getSource()).getScene().getWindow());
            popupStage.showAndWait();

            // Rafraîchir la liste après modification
            loadForumData();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la fenêtre de modification de forum.");
        }
    }

    // Retourne le forum sélectionné dans la grille
    private Forum getSelectedForum() {
        return selectedForum;
    }


    @FXML
    private void addForum(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterForum.fxml"));
            Parent root = loader.load();

            // Passer ce contrôleur à AjouterForumController pour rafraîchir après ajout
            tn.esprit.controllers.AjouterForumController controller = loader.getController();
            controller.setAfficherForumController(this);

            Stage popupStage = new Stage();
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.setTitle("Créer un Nouveau Forum");
            popupStage.setScene(new Scene(root));
            popupStage.initOwner(((Button) event.getSource()).getScene().getWindow());
            popupStage.showAndWait();

            // Rafraîchir la liste des forums après ajout
            loadForumData();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire d'ajout de forum.");
        }
    }


    private static final Logger logger = LoggerFactory.getLogger(AfficherForumController.class);

    @FXML
    private AnchorPane MainAnchorPaneBaladity;

    @FXML
    private TextField RechercherActualiteAdmin;

    @FXML
    private Button sortActualiteAdmin;

    @FXML
    private ComboBox<String> categoryFilterComboBox;

    @FXML
    private ScrollPane scrollAdmin;

    @FXML
    private FlowPane forumFlowPane;

    @FXML
    private Button exportExcelButton;

    @FXML
    private Button exportPdfButton;

    @FXML
    private Label welcomeLabel;

    private ServiceForum serviceForum = new ServiceForum();
    private ObservableList<Forum> forumList = FXCollections.observableArrayList();
    private ObservableList<Forum> masterForumList = FXCollections.observableArrayList();
    private User currentUser;
    private AfficherForumController afficherForumController;

    @FXML
    void initialize() {
        ElasticScroll.applyElasticScrolling(scrollAdmin);
        setupCategoryFilter();
        loadForumData();
    }

    private void setupCategoryFilter() {
        categoryFilterComboBox.getItems().add("Toutes les catégories");
        categoryFilterComboBox.setValue("Toutes les catégories");
    }

    private void loadForumData() {
        try {
            List<Forum> forumListData = serviceForum.getAll();
            logger.info("Loaded {} forums from ServiceForum", forumListData.size());
            forumList.clear();
            forumList.addAll(forumListData);
            masterForumList.clear();
            masterForumList.addAll(forumListData);

            Set<String> categories = forumListData.stream()
                    .map(Forum::getCategorie_f)
                    .collect(Collectors.toSet());
            List<String> categoryList = new ArrayList<>(categories);
            categoryList.add(0, "Toutes les catégories");
            categoryFilterComboBox.setItems(FXCollections.observableArrayList(categoryList));

            displayForums(forumList);
        } catch (Exception e) {
            logger.error("Error loading forum data", e);
            showAlert("Erreur", "Échec du chargement des données du forum: " + e.getMessage());
        }
    }

    private void displayForums(ObservableList<Forum> forums) {
        forumFlowPane.getChildren().clear();
        for (Forum forum : forums) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ForumItem.fxml"));
                AnchorPane forumItem = loader.load();
                ForumItemController controller = loader.getController();
                controller.setData(forum);

                // Add action handlers for edit and delete buttons
                controller.getEditButton().setOnAction(event -> modifyForum(forum));
                controller.getDeleteButton().setOnAction(event -> deleteForum(forum));

                // Apply fade-in animation
                forumItem.setOpacity(0);
                forumFlowPane.getChildren().add(forumItem);
                FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), forumItem);
                fadeTransition.setFromValue(0);
                fadeTransition.setToValue(1);
                fadeTransition.play();
            } catch (IOException e) {
                logger.error("Error loading forum item", e);
                showAlert("Erreur", "Échec du chargement de l'élément du forum.");
            }
        }
    }

    private void deleteForum(Forum forum) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer la suppression");
        confirmAlert.setHeaderText("Supprimer le forum");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer le forum : " + forum.getTitre_f() + " ?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceForum.supprimer(forum.getId());
                    loadForumData();
                    showAlert("Succès", "Forum supprimé avec succès.");
                } catch (SQLException e) {
                    logger.error("Error deleting forum", e);
                    showAlert("Erreur", "Échec de la suppression du forum.");
                }
            }
        });
    }

    private void modifyForum(Forum forum) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierForum.fxml"));
            Parent root = loader.load();
            ModifierForumController controller = loader.getController();
            controller.setServiceForum(serviceForum);
            controller.setData(forum);
            controller.setAfficherForumController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Forum");
            stage.show();
        } catch (IOException e) {
            logger.error("Error opening modify forum window", e);
            showAlert("Erreur", "Échec de l'ouverture de la fenêtre de modification du forum.");
        }
    }

    @FXML
    private void RechercherActualiteAdmin(ActionEvent event) {
        String searchText = RechercherActualiteAdmin.getText().toLowerCase();
        List<Forum> filteredList = masterForumList.stream()
                .filter(forum -> forum.getTitre_f().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        forumList.clear();
        forumList.addAll(filteredList);
        displayForums(forumList);
    }

    @FXML
    private void sortActualiteAdmin(ActionEvent event) {
        List<Forum> sortedList = new ArrayList<>(forumList);
        sortedList.sort(Comparator.comparing(Forum::getDate_f));
        forumList.clear();
        forumList.addAll(sortedList);
        displayForums(forumList);
    }

    @FXML
    void filterByCategory(ActionEvent event) {
        String selectedCategory = categoryFilterComboBox.getValue();
        if (selectedCategory == null || selectedCategory.equals("Toutes les catégories")) {
            forumList.clear();
            forumList.addAll(masterForumList);
        } else {
            List<Forum> filteredList = masterForumList.stream()
                    .filter(forum -> forum.getCategorie_f().equals(selectedCategory))
                    .collect(Collectors.toList());
            forumList.clear();
            forumList.addAll(filteredList);
        }
        displayForums(forumList);
    }

    @FXML
    void exportToExcel(ActionEvent event) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Forums");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Titre", "Date", "Catégorie", "Description"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Forum forum : forumList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(forum.getTitre_f());
                row.createCell(1).setCellValue(forum.getFormattedDate());
                row.createCell(2).setCellValue(forum.getCategorie_f());
                row.createCell(3).setCellValue(forum.getDescription_f());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Excel File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            fileChooser.setInitialFileName("Forums_List.xlsx");
            File file = fileChooser.showSaveDialog(MainAnchorPaneBaladity.getScene().getWindow());

            if (file != null) {
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                    showAlert("Succès", "Fichier Excel exporté avec succès à " + file.getAbsolutePath());
                }
            }

            workbook.close();
        } catch (IOException e) {
            logger.error("Error exporting to Excel", e);
            showAlert("Erreur", "Échec de l'exportation du fichier Excel : " + e.getMessage());
        }
    }

    @FXML
    void exportToPdf(ActionEvent event) {
        showAlert("Information", "Exportation PDF non implémentée.");
    }

    public void stat(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/stat.fxml"));
            Stage stage = (Stage) MainAnchorPaneBaladity.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.error("Error loading stats view", e);
            showAlert("Erreur", "Erreur lors du chargement de la vue.");
        }
    }

    public void loadForumList() {
        loadForumData();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            welcomeLabel.setText("Bienvenue, " + user.getNom());
        }
    }

    public void setAfficherForumController(AfficherForumController afficherForumController) {
        this.afficherForumController = afficherForumController;
    }
}