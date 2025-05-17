package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import tn.esprit.models.CommantairesF;
import tn.esprit.models.User;
import tn.esprit.services.ServiceCommentaire_f;
import tn.esprit.services.ServiceForum;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherCommentController {

    @FXML
    private VBox commentContainerVBox;

    private final ServiceCommentaire_f serviceCommentaire = new ServiceCommentaire_f();
    private final ServiceForum serviceForum = new ServiceForum();
    private User currentUser;

    public void setCurrentUser(User user) {
        if (user == null) {
            // Create a default user if none is provided
            this.currentUser = new User();
            this.currentUser.setId(1); // Set a default user ID
            this.currentUser.setNom("Default User");
            this.currentUser.setEmail("default@example.com");
            System.out.println("Warning: No user provided, using default user");
        } else {
            this.currentUser = user;
            System.out.println("User set successfully: " + user.getNom());
        }
    }

    @FXML
    public void initialize() {
        // Set default user if none is set
        if (currentUser == null) {
            setCurrentUser(null);
        }
        
        // Load all comments
        loadCommentaires();

        // Add animation to the container
        FadeTransition ft = new FadeTransition(Duration.millis(1000), commentContainerVBox);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void loadCommentaires() {
        System.out.println("Loading comments...");
        System.out.println("Current user: " + (currentUser != null ? currentUser.getNom() : "null"));

        List<CommantairesF> commentaires = serviceCommentaire.getAll();

        if (commentaires == null || commentaires.isEmpty()) {
            System.out.println("No comments fetched from service");
            Label noCommentsLabel = new Label("Aucun commentaire trouvé.");
            noCommentsLabel.setFont(Font.font("Arial", 14));
            noCommentsLabel.getStyleClass().add("comment-text");
            commentContainerVBox.getChildren().add(noCommentsLabel);
            return;
        }

        Platform.runLater(() -> {
            commentContainerVBox.getChildren().clear();
            commentaires.forEach(commentaire -> {
                VBox commentContainer = createCommentContainer(commentaire);
                commentContainerVBox.getChildren().add(commentContainer);
            });
        });
    }

    private VBox createCommentContainer(CommantairesF commentaire) {
        VBox container = new VBox(10);
        container.getStyleClass().add("comment-container");
        container.setPrefWidth(700);
        container.setMaxWidth(700);

        // Date and Forum info
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label dateLabel = new Label("Date: " + commentaire.getDate_c().toString());
        Label forumLabel = new Label("Forum: " + (commentaire.getForum() != null ? commentaire.getForum().getTitre_f() : "N/A"));
        
        dateLabel.getStyleClass().add("comment-date");
        forumLabel.getStyleClass().add("comment-forum");
        
        headerBox.getChildren().addAll(dateLabel, forumLabel);

        // Comment text
        TextFlow textFlow = new TextFlow();
        Text commentText = new Text(commentaire.getTexte_c());
        commentText.getStyleClass().add("comment-text");
        textFlow.getChildren().add(commentText);

        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button modifyButton = new Button("Modifier");
        modifyButton.getStyleClass().add("modify-button");
        modifyButton.setOnAction(e -> modifyCommentaire(commentaire));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> deleteCommentaire(commentaire));

        Button addForumButton = new Button("Add Forum");
        addForumButton.getStyleClass().add("add-forum-button");
        addForumButton.setOnAction(e -> showAddForumPopup());

        buttonBox.getChildren().addAll(modifyButton, deleteButton, addForumButton);

        container.getChildren().addAll(headerBox, textFlow, buttonBox);
        container.setPadding(new javafx.geometry.Insets(10));

        return container;
    }

    private void deleteCommentaire(CommantairesF commentaire) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Suppression du commentaire");
        alert.setContentText("Voulez-vous vraiment supprimer ce commentaire ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceCommentaire.supprimer(commentaire.getId());
                    loadCommentaires(); // Reload all comments after deletion
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Commentaire supprimé avec succès.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le commentaire.");
                }
            }
        });
    }

    private void modifyCommentaire(CommantairesF commentaire) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCommentaire.fxml"));
            Parent root = loader.load();

            ModifierCommentaireController modifierController = loader.getController();
            modifierController.setData(commentaire);

            commentContainerVBox.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page de modification.");
        }
    }

    private void showAddForumPopup() {
        if (currentUser == null) {
            setCurrentUser(null); // This will create a default user
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterForum.fxml"));
            Parent root = loader.load();

            // Get the controller and set the current user
            AjouterForumController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Add New Forum");
            popupStage.setScene(new Scene(root));
            
            // Center the popup on the main window
            popupStage.initOwner(commentContainerVBox.getScene().getWindow());
            popupStage.showAndWait();

            // Refresh comments after forum is added
            loadCommentaires();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load the Add Forum form.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }

    public void pdf(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                PDDocument document = new PDDocument();
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                PDPageContentStream contentStream = new PDPageContentStream(document, page);

                float yPosition = 750;
                float margin = 50;
                float rowHeight = 25;
                float tableWidth = 500;
                float[] columnWidths = {100f, 250f, 150f};

                // Titre du PDF
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Liste des Commentaires");
                contentStream.endText();
                yPosition -= 50; // Plus d'espace après le titre

                // Fond gris pour l'en-tête
                contentStream.setNonStrokingColor(220, 220, 220);
                contentStream.addRect(margin, yPosition, tableWidth, rowHeight);
                contentStream.fill();
                contentStream.setNonStrokingColor(0, 0, 0); // Noir pour le texte

                // En-tête du tableau
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin + 5, yPosition + 7);
                contentStream.showText("Date");
                contentStream.newLineAtOffset(columnWidths[0] + 20, 0); // Décalage du texte à droite
                contentStream.showText("Texte");
                contentStream.newLineAtOffset(columnWidths[1] + 20, 0); // Décalage du texte à droite
                contentStream.showText("Forum");
                contentStream.endText();
                yPosition -= rowHeight;

                // Récupérer les commentaires
                List<CommantairesF> commentaires = serviceCommentaire.getAll();

                if (commentaires.isEmpty()) {
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Aucun commentaire disponible.");
                    contentStream.endText();
                } else {
                    contentStream.setFont(PDType1Font.HELVETICA, 11);
                    boolean alternateColor = false;

                    for (CommantairesF commentaire : commentaires) {
                        if (commentaire != null) {
                            // Fond alterné pour la lisibilité
                            if (alternateColor) {
                                contentStream.setNonStrokingColor(245, 245, 245);
                                contentStream.addRect(margin, yPosition, tableWidth, rowHeight);
                                contentStream.fill();
                                contentStream.setNonStrokingColor(0, 0, 0);
                            }
                            alternateColor = !alternateColor;

                            // Texte dans le tableau
                            contentStream.beginText();
                            contentStream.newLineAtOffset(margin + 5, yPosition + 7);
                            contentStream.showText(commentaire.getDate_c().toString());
                            contentStream.newLineAtOffset(columnWidths[0] + 20, 0); // Texte un peu plus à droite
                            contentStream.showText(commentaire.getTexte_c());
                            contentStream.newLineAtOffset(columnWidths[1] + 20, 0); // Texte un peu plus à droite
                            contentStream.showText(commentaire.getForum() != null ? commentaire.getForum().getTitre_f() : "N/A");
                            contentStream.endText();

                            // Lignes séparatrices
                            contentStream.moveTo(margin, yPosition);
                            contentStream.lineTo(margin + tableWidth, yPosition);
                            contentStream.stroke();

                            // Descendre d'une ligne
                            yPosition -= rowHeight;

                            // Saut de page si nécessaire
                            if (yPosition < 100) {
                                contentStream.close();
                                page = new PDPage(PDRectangle.A4);
                                document.addPage(page);
                                contentStream = new PDPageContentStream(document, page);
                                yPosition = 750;
                            }
                        }
                    }
                }

                // Fermer et sauvegarder
                contentStream.close();
                document.save(file);
                document.close();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "PDF généré avec succès !");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la génération du PDF.");
            }
        }
    }
}