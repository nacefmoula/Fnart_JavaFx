package tn.esprit.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.models.User;

public class UserCard extends HBox {
    private final User user;
    private Button actionButton;

    public UserCard(User user) {
        this.user = user;
        getStyleClass().add("user-card");
        setSpacing(15);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER_LEFT);

        try {
            // Profile image
            ImageView imageView = new ImageView();
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            imageView.setPreserveRatio(true);

            try {
                if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                    Image image = new Image(user.getProfilePicture(), true);
                    imageView.setImage(image);
                } else {
                    // Fallback to a default image if resource is available
                    try {
                        String defaultImagePath = "/assets/default-profile.png";
                        if (getClass().getResource(defaultImagePath) != null) {
                            imageView.setImage(new Image(getClass().getResource(defaultImagePath).toExternalForm()));
                        } else {
                            // No resource found, create a placeholder
                            Region placeholder = new Region();
                            placeholder.setMinSize(50, 50);
                            placeholder.setPrefSize(50, 50);
                            placeholder.setStyle("-fx-background-color: #cccccc; -fx-background-radius: 25;");
                            getChildren().add(placeholder);
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading default image: " + e.getMessage());
                        // Create a colored placeholder instead
                        Region placeholder = new Region();
                        placeholder.setMinSize(50, 50);
                        placeholder.setPrefSize(50, 50);
                        placeholder.setStyle("-fx-background-color: #cccccc; -fx-background-radius: 25;");
                        getChildren().add(placeholder);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading user image: " + e.getMessage());
                // Create a colored placeholder
                Region placeholder = new Region();
                placeholder.setMinSize(50, 50);
                placeholder.setPrefSize(50, 50);
                placeholder.setStyle("-fx-background-color: #cccccc; -fx-background-radius: 25;");
                getChildren().add(placeholder);
            }

            imageView.setStyle("-fx-background-radius: 50%;");
            getChildren().add(imageView);

            // User info
            VBox infoBox = new VBox(5);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            Label nameLabel = new Label(user.getNom() != null ? user.getNom() : "Unknown User");
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15;");

            Label emailLabel = new Label(user.getEmail() != null ? user.getEmail() : "No email");
            emailLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

            // Add role and status if available
            if (user.getRole() != null) {
                Label roleLabel = new Label("Role: " + user.getRole().toString());
                roleLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
                infoBox.getChildren().addAll(nameLabel, emailLabel, roleLabel);
            } else {
                infoBox.getChildren().addAll(nameLabel, emailLabel);
            }

            if (user.getStatus() != null && !user.getStatus().isEmpty()) {
                Label statusLabel = new Label("Status: " + user.getStatus());
                statusLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
                infoBox.getChildren().add(statusLabel);
            }

            getChildren().add(infoBox);

            // Action button
            actionButton = new Button("View Profile");
            actionButton.setStyle("-fx-background-color: #e7f3ff; -fx-text-fill: #1877f2; -fx-background-radius: 8;");
            getChildren().add(actionButton);

        } catch (Exception e) {
            System.err.println("Error creating UserCard: " + e.getMessage());
            e.printStackTrace();

            // Add minimal content in case of an error
            Label errorLabel = new Label("Error loading user");
            errorLabel.setStyle("-fx-text-fill: red;");
            getChildren().add(errorLabel);
        }
    }

    public User getUser() {
        return user;
    }

    public Button getActionButton() {
        return actionButton;
    }

    public void setActionButtonText(String text) {
        if (actionButton != null) {
            actionButton.setText(text);
        }
    }

    public void setActionButtonStyle(String style) {
        if (actionButton != null) {
            actionButton.setStyle(style);
        }
    }

    public void setOnActionButtonClick(javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        if (actionButton != null) {
            actionButton.setOnAction(handler);
        }
    }
}