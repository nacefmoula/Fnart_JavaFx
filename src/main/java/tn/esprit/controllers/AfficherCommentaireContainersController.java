package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import tn.esprit.models.CommantairesF;
import tn.esprit.models.Forum;
import tn.esprit.services.ServiceCommentaire_f;
import tn.esprit.services.ServiceForum;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class AfficherCommentaireContainersController {

    @FXML
    private VBox commentContainerVBox;

    @FXML
    private ComboBox<Forum> forumComboBox;

    private final ServiceCommentaire_f serviceCommentaire = new ServiceCommentaire_f();
    private final ServiceForum serviceForum = new ServiceForum();

    @FXML
    public void initialize() throws SQLException {
        // Populate ComboBox with forums
        populateForumComboBox();
        // Load all comments initially
        loadCommentaires(null);
        // Set up ComboBox listener for filtering
        forumComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            loadCommentaires(newValue);
        });
    }

    private void populateForumComboBox() throws SQLException {
        Set<Forum> forums = (Set<Forum>) serviceForum.getAll();
        if (forums != null && !forums.isEmpty()) {
            forumComboBox.getItems().addAll(forums);
            // Set custom cell factory to display forum titles
            forumComboBox.setCellFactory(listView -> new javafx.scene.control.ListCell<Forum>() {
                @Override
                protected void updateItem(Forum forum, boolean empty) {
                    super.updateItem(forum, empty);
                    if (empty || forum == null) {
                        setText(null);
                    } else {
                        setText(forum.getTitre_f());
                    }
                }
            });
            forumComboBox.setButtonCell(new javafx.scene.control.ListCell<Forum>() {
                @Override
                protected void updateItem(Forum forum, boolean empty) {
                    super.updateItem(forum, empty);
                    if (empty || forum == null) {
                        setText("Tous les forums");
                    } else {
                        setText(forum.getTitre_f());
                    }
                }
            });
            // Add an "All Forums" option
            forumComboBox.getItems().add(0, null);
            forumComboBox.getSelectionModel().select(0); // Select "All Forums" by default
        } else {
            System.out.println("No forums found.");
        }
    }

    private void loadCommentaires(Forum selectedForum) {
        List<CommantairesF> commentaires;
        if (selectedForum == null) {
            commentaires = serviceCommentaire.getAll();
        } else {
            commentaires = serviceCommentaire.getCommentairesByForumId(selectedForum.getId());
        }

        Platform.runLater(() -> {
            commentContainerVBox.getChildren().clear(); // Clear existing comments
            if (commentaires != null && !commentaires.isEmpty()) {
                commentaires.forEach(commentaire -> {
                    VBox commentContainer = createCommentContainer(commentaire);
                    commentContainerVBox.getChildren().add(commentContainer);
                });
            } else {
                System.out.println("No comments found for selected forum.");
                // Optionally display a message in the UI
                Label noCommentsLabel = new Label("Aucun commentaire trouvé.");
                noCommentsLabel.setFont(Font.font("Arial", 14));
                noCommentsLabel.getStyleClass().add("comment-text");
                commentContainerVBox.getChildren().add(noCommentsLabel);
            }
        });
    }

    private VBox createCommentContainer(CommantairesF commentaire) {
        VBox container = new VBox();
        container.getStyleClass().add("comment-card"); // Apply CSS class for styling
        container.setSpacing(8);
        container.setPadding(new Insets(15));
        HBox.setHgrow(container, Priority.ALWAYS);

        // Forum Title
        Label forumLabel = new Label("Forum: " + commentaire.getForum().getTitre_f());
        forumLabel.setFont(Font.font("Arial", 16));
        forumLabel.getStyleClass().add("comment-title");

        // Date
        Label dateLabel = new Label("Date: " + commentaire.getDate_c().toString());
        dateLabel.setFont(Font.font("Arial", 12));
        dateLabel.getStyleClass().add("comment-date");

        // Comment Text
        Label textLabel = new Label("Texte: " + commentaire.getTexte_c());
        textLabel.setFont(Font.font("Arial", 14));
        textLabel.setWrapText(true);
        textLabel.getStyleClass().add("comment-text");
        textLabel.prefWidthProperty().bind(commentContainerVBox.widthProperty().subtract(60));

        // Button Container
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        // Modify Button
        Button modifierButton = new Button("Modifier");
        modifierButton.setOnAction(event -> modifierCommentaire(commentaire));
        modifierButton.getStyleClass().addAll("action-button", "modify-button");

        // Delete Button
        Button deleteButton = new Button("Supprimer");
        deleteButton.setOnAction(event -> supprimerCommentaire(commentaire));
        deleteButton.getStyleClass().addAll("action-button", "delete-button");

        buttonBox.getChildren().addAll(modifierButton, deleteButton);
        container.getChildren().addAll(forumLabel, dateLabel, textLabel, buttonBox);

        // Animation setup (fade-in effect)
        container.setOpacity(0);
        Platform.runLater(() -> {
            container.setStyle("-fx-opacity: 0; -fx-translate-y: 20px;");
            // Trigger fade-in animation via CSS
            container.setStyle("-fx-opacity: 1; -fx-translate-y: 0;");
        });

        return container;
    }

    private void modifierCommentaire(CommantairesF commentaire) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCommentaire.fxml"));
            Parent root = loader.load();

            ModifierCommentaireController modifierController = loader.getController();
            modifierController.setData(commentaire);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Commentaire");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void supprimerCommentaire(CommantairesF commentaire) {
        try {
            serviceCommentaire.supprimer(commentaire.getId());
            // Reload comments based on current ComboBox selection
            loadCommentaires(forumComboBox.getSelectionModel().getSelectedItem());
            System.out.println("Comment deleted successfully!");
        } catch (SQLException e) {
            System.err.println("Error deleting comment: " + e.getMessage());
        }
    }
}