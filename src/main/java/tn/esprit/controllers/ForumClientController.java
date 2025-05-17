package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.Forum;
import tn.esprit.models.User;
import tn.esprit.services.ServiceForum;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;

public class ForumClientController {

    @FXML
    private VBox forumContainer;

    private final ServiceForum serviceForum = new ServiceForum();
    private User currentUser;
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user == null) {
            // Create a default user if none is provided
            this.currentUser = new User();
            this.currentUser.setId(1); // Set a default user ID
            this.currentUser.setNom("Default User");
            this.currentUser.setEmail("default@example.com");
            System.out.println("Warning: No user provided, using default user");
        } else {
            System.out.println("User set successfully: " + user.getNom());
        }
    }

    @FXML
    public void initialize() {
        // Set default user if none is set
        if (currentUser == null) {
            setCurrentUser(null);
        }
        loadForums();
    }

    private void loadForums() {
        Set<Forum> forums = (Set<Forum>) serviceForum.getAll();
        forumContainer.getChildren().clear();

        for (Forum forum : forums) {
            HBox card = createForumCard(forum);
            forumContainer.getChildren().add(card);
            applyEntranceAnimation(card, 0); // Staggered entrance animation
        }
    }

    private HBox createForumCard(Forum forum) {
        HBox card = new HBox();
        card.getStyleClass().add("forum-card"); // Apply CSS class
        card.setPadding(new Insets(15));
        card.setSpacing(15);

        // Image View with hover zoom effect
        ImageView imageView = new ImageView();
        try {
            String imagePath = forum.getImage_f();
            System.out.println("Image path for forum " + forum.getTitre_f() + ": " + imagePath);
            if (imagePath != null && !imagePath.isEmpty()) {
                Image image = new Image("file:" + imagePath);
                imageView.setImage(image);
            } else {
                Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/default-forum.png")));
                imageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Failed to load forum image: " + e.getMessage());
            e.printStackTrace();
            Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/default-forum.png")));
            imageView.setImage(defaultImage);
        }
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("forum-image");

        // Image hover zoom effect
        ScaleTransition imageHover = new ScaleTransition(Duration.millis(200), imageView);
        imageHover.setToX(1.1);
        imageHover.setToY(1.1);
        imageView.setOnMouseEntered(e -> imageHover.playFromStart());
        imageView.setOnMouseExited(e -> {
            imageHover.setToX(1.0);
            imageHover.setToY(1.0);
            imageHover.play();
        });

        imageView.setOnMouseClicked(event -> showImagePopup(imageView.getImage()));

        // Details VBox
        VBox details = new VBox();
        details.setSpacing(8);
        details.setAlignment(javafx.geometry.Pos.TOP_LEFT);

        Label titleLabel = new Label(forum.getTitre_f());
        titleLabel.getStyleClass().add("forum-title");

        LocalDate localDate = LocalDate.parse(String.valueOf(forum.getDate_f()), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Label dateLabel = new Label("Date: " + localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateLabel.getStyleClass().add("forum-detail");

        Label categoryLabel = new Label("Category: " + forum.getCategorie_f());
        categoryLabel.getStyleClass().add("forum-detail");

        Label descriptionLabel = new Label("Description: " + forum.getDescription_f());
        descriptionLabel.getStyleClass().add("forum-detail");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(400);

        Label ratingLabel = new Label("Rating: " + String.format("%.1f", forum.getRating()));
        ratingLabel.getStyleClass().add("forum-detail");

        // Rating system
        HBox ratingBox = new HBox();
        ratingBox.setSpacing(5);
        StackPane ratingContainer = new StackPane(ratingBox);
        ToggleButton[] stars = new ToggleButton[5];
        for (int i = 0; i < 5; i++) {
            final int ratingValue = i + 1;
            stars[i] = new ToggleButton("★");
            stars[i].getStyleClass().add("star-button");
            stars[i].setSelected(i < Math.round(forum.getRating()));

            // Hover animation
            ScaleTransition hoverScale = new ScaleTransition(Duration.millis(200), stars[i]);
            hoverScale.setToX(1.3);
            hoverScale.setToY(1.3);
            stars[i].setOnMouseEntered(e -> hoverScale.playFromStart());
            stars[i].setOnMouseExited(e -> {
                hoverScale.setToX(1.0);
                hoverScale.setToY(1.0);
                hoverScale.play();
            });

            // Click animation
            ScaleTransition clickScale = new ScaleTransition(Duration.millis(100), stars[i]);
            clickScale.setToX(1.5);
            clickScale.setToY(1.5);
            clickScale.setAutoReverse(true);
            clickScale.setCycleCount(2);

            FadeTransition clickFade = new FadeTransition(Duration.millis(100), stars[i]);
            clickFade.setFromValue(1.0);
            clickFade.setToValue(0.7);
            clickFade.setAutoReverse(true);
            clickFade.setCycleCount(2);

            ParallelTransition clickAnimation = new ParallelTransition(clickScale, clickFade);

            stars[i].setOnAction(event -> {
                for (int j = 0; j < 5; j++) {
                    stars[j].setSelected(j < ratingValue);
                }
                clickAnimation.play();
                try {
                    serviceForum.updateRating(forum.getId(), ratingValue);
                    forum.setRating(ratingValue);
                    ratingLabel.setText("Rating: " + String.format("%.1f", forum.getRating()));
                    showSparkleEffect(ratingContainer);
                    showAlert("Success", "Rating submitted: " + ratingValue + " stars");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to submit rating.");
                }
            });
            ratingBox.getChildren().add(stars[i]);
        }

        // Add Comment Button
        Button addCommentButton = new Button("Add Comment");
        addCommentButton.getStyleClass().add("add-comment-button");
        addCommentButton.setOnAction(event -> openAddCommentForm(forum));

        // Button click animation
        ScaleTransition buttonClick = new ScaleTransition(Duration.millis(100), addCommentButton);
        buttonClick.setToX(0.9);
        buttonClick.setToY(0.9);
        buttonClick.setAutoReverse(true);
        buttonClick.setCycleCount(2);
        addCommentButton.setOnMousePressed(e -> buttonClick.play());

        details.getChildren().addAll(titleLabel, dateLabel, categoryLabel, descriptionLabel, ratingLabel, ratingContainer, addCommentButton);
        card.getChildren().addAll(imageView, details);

        // Card hover effect
        ScaleTransition cardHover = new ScaleTransition(Duration.millis(200), card);
        cardHover.setToX(1.02);
        cardHover.setToY(1.02);
        card.setOnMouseEntered(e -> {
            cardHover.playFromStart();
            card.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");
        });
        card.setOnMouseExited(e -> {
            cardHover.setToX(1.0);
            cardHover.setToY(1.0);
            cardHover.play();
            card.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");
        });

        return card;
    }

    private void applyEntranceAnimation(HBox card, int delay) {
        card.setOpacity(0);
        card.setTranslateY(20);

        FadeTransition fade = new FadeTransition(Duration.millis(500), card);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition translate = new TranslateTransition(Duration.millis(500), card);
        translate.setFromY(20);
        translate.setToY(0);

        ParallelTransition entrance = new ParallelTransition(fade, translate);
        entrance.setDelay(Duration.millis(delay));
        entrance.play();
    }

    private void openAddCommentForm(Forum forum) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterCommentaires.fxml"));
            Parent root = loader.load();

            AjouterCommentairesController controller = loader.getController();
            controller.setSelectedForum(forum);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Comment");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load comment form.");
        }
    }

    private void showSparkleEffect(StackPane container) {
        Rectangle sparkle = new Rectangle(50, 50);
        RadialGradient gradient = new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true,
                null,
                new Stop(0, Color.YELLOW),
                new Stop(0.5, Color.GOLD),
                new Stop(1, Color.TRANSPARENT)
        );
        sparkle.setFill(gradient);
        sparkle.setOpacity(0.8);

        container.getChildren().add(sparkle);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), sparkle);
        scale.setFromX(0.1);
        scale.setFromY(0.1);
        scale.setToX(1.8);
        scale.setToY(1.8);

        FadeTransition fade = new FadeTransition(Duration.millis(300), sparkle);
        fade.setFromValue(0.8);
        fade.setToValue(0);

        ParallelTransition sparkleAnimation = new ParallelTransition(scale, fade);
        sparkleAnimation.setOnFinished(e -> container.getChildren().remove(sparkle));
        sparkleAnimation.play();
    }

    private void showImagePopup(Image image) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Image Viewer");

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(600);
        imageView.setFitHeight(600);

        StackPane layout = new StackPane(imageView);
        layout.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        Scene popupScene = new Scene(layout, 600, 600);
        popupStage.setScene(popupScene);

        layout.setOnMouseClicked(event -> popupStage.close());
        popupStage.showAndWait();
    }

    @FXML
    private void showAddForumPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterForum.fxml"));
            Parent root = loader.load();

            // Get the controller and set the current user
            AjouterForumController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Créer un Nouveau Forum");
            popupStage.setScene(new Scene(root));
            
            // Center the popup on the main window
            popupStage.initOwner(forumContainer.getScene().getWindow());
            popupStage.showAndWait();

            // Refresh forums after a new one is added
            loadForums();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire de création de forum.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}