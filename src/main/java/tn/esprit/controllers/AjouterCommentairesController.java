package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tn.esprit.models.CommantairesF;
import tn.esprit.models.Forum;
import tn.esprit.services.ServiceCommentaire_f;

import java.sql.Date;

public class AjouterCommentairesController {

    @FXML
    private VBox formContainer;

    @FXML
    private TextArea TFtexte;

    @FXML
    private Button ajouterCommentAction;

    @FXML
    private Button voirComments;

    @FXML
    private Label min;

    @FXML
    private Label bien;

    @FXML
    private Label forumTitleLabel;

    private Forum selectedForum;
    private ServiceCommentaire_f serviceCommentaire = new ServiceCommentaire_f();

    @FXML
    public void initialize() {
        min.setVisible(false);
        bien.setVisible(false);
        if (forumTitleLabel != null) {
            forumTitleLabel.setVisible(false);
        }
        applyEntranceAnimation();
    }

    public void setSelectedForum(Forum forum) {
        this.selectedForum = forum;
        if (forumTitleLabel != null && forum != null) {
            forumTitleLabel.setText("Forum: " + forum.getTitre_f());
            forumTitleLabel.setVisible(true);
        }
    }

    @FXML
    public void ajouterCommentAction(ActionEvent actionEvent) {
        if (!isInputValid()) {
            showAnimatedErrorAlert("Erreur", "Veuillez entrer un commentaire d'au moins 3 caractères.");
            return;
        }

        String commentText = TFtexte.getText();

        if (selectedForum != null) {
            CommantairesF commentaire = new CommantairesF(1, selectedForum, new Date(System.currentTimeMillis()).toLocalDate(), commentText);
            try {
                serviceCommentaire.add(commentaire);
                showAnimatedSuccessAlert("Succès", "Commentaire ajouté avec succès!");
                applySuccessAnimation();
                TFtexte.clear();
                Stage stage = (Stage) TFtexte.getScene().getWindow();
                stage.close();
            } catch (Exception e) {
                showAlert("Error", "An unexpected error occurred", "An unexpected error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        } else {
            showAlert("Forum Error", "Forum not found", "No forum is selected.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void voirComments(ActionEvent actionEvent) {
        System.out.println("Voir la liste des commentaires is clicked");
    }

    private boolean isInputValid() {
        String commentText = TFtexte.getText().trim();
        return commentText.length() >= 3;
    }

    @FXML
    private void handleTextInput() {
        String commentText = TFtexte.getText().trim();
        if (commentText.length() < 3) {
            min.setVisible(true);
            bien.setVisible(false);
            animateValidationLabel(min);
        } else {
            min.setVisible(false);
            bien.setVisible(true);
            animateValidationLabel(bien);
        }
    }

    private void applyEntranceAnimation() {
        formContainer.setOpacity(0);
        formContainer.setScaleX(0.8);
        formContainer.setScaleY(0.8);

        FadeTransition fade = new FadeTransition(Duration.millis(400), formContainer);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(400), formContainer);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1);
        scale.setToY(1);

        ParallelTransition entrance = new ParallelTransition(fade, scale);
        entrance.play();
    }

    private void animateValidationLabel(Label label) {
        FadeTransition fade = new FadeTransition(Duration.millis(300), label);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), label);
        scale.setFromX(0.9);
        scale.setFromY(0.9);
        scale.setToX(1);
        scale.setToY(1);

        ParallelTransition animation = new ParallelTransition(fade, scale);
        animation.play();
    }

    private void applySuccessAnimation() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#4CAF50"));
        shadow.setRadius(10);
        shadow.setSpread(0.5);

        formContainer.setEffect(shadow);

        FadeTransition glow = new FadeTransition(Duration.millis(500), formContainer);
        glow.setFromValue(1);
        glow.setToValue(0.7);
        glow.setAutoReverse(true);
        glow.setCycleCount(2);
        glow.setOnFinished(e -> formContainer.setEffect(null));
        glow.play();
    }

    private void showAlert(String title, String header, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAnimatedErrorAlert(String title, String message) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        Button okButton = new Button("OK");
        okButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 15; -fx-background-radius: 5;");
        okButton.setOnAction(event -> dialogStage.close());

        VBox dialogVBox = new VBox(10, messageLabel, okButton);
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setStyle("-fx-background-color: rgba(231, 76, 60, 0.9); -fx-padding: 20px; -fx-background-radius: 10;");

        Scene dialogScene = new Scene(dialogVBox);
        dialogScene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(dialogScene);

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), dialogVBox);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.5), dialogVBox);
        scaleTransition.setFromX(0.5);
        scaleTransition.setFromY(0.5);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);

        ParallelTransition animation = new ParallelTransition(fadeTransition, scaleTransition);
        animation.play();

        dialogStage.showAndWait();
    }

    private void showAnimatedSuccessAlert(String title, String message) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        Button okButton = new Button("OK");
        okButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 15; -fx-background-radius: 5;");
        okButton.setOnAction(event -> dialogStage.close());

        VBox dialogVBox = new VBox(10, messageLabel, okButton);
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setStyle("-fx-background-color: rgba(46, 204, 113, 0.9); -fx-padding: 20px; -fx-background-radius: 10;");

        Scene dialogScene = new Scene(dialogVBox);
        dialogScene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(dialogScene);

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), dialogVBox);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.5), dialogVBox);
        scaleTransition.setFromX(0.5);
        scaleTransition.setFromY(0.5);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);

        ParallelTransition animation = new ParallelTransition(fadeTransition, scaleTransition);
        animation.play();

        dialogStage.showAndWait();
    }
}