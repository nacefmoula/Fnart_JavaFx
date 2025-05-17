package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tn.esprit.models.Forum;
import tn.esprit.models.User;
import tn.esprit.services.ServiceForum;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;

public class AjouterForumController implements Initializable {

    @FXML
    private Label warningDescription;

    @FXML
    private Label warningComm;

    @FXML
    private Label warningtitle;

    @FXML
    private TextField TFtitre;

    @FXML
    private TextArea TFDescription;

    @FXML
    private Button ajouterForumAction;

    @FXML
    private ComboBox<String> TFCategorie;

    @FXML
    private ImageView imgView_reclamation;

    @FXML
    private Button uploadbutton;

    private final ServiceForum serviceForum = new ServiceForum();
    private String imagePath = "";
    private AfficherForumController afficherForumController;
    private User currentUser;

    java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TFCategorie.getItems().addAll(
                "Art-Thérapie et Bien-Être",
                "Techniques Artistiques",
                "Témoignages et Inspirations",
                "Ressources et Outils",
                "Questions et Conseils"
        );

        // Add text length limit for title
        TFtitre.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 40) {
                TFtitre.setText(oldValue);
                showWarning(warningtitle, true);
                warningtitle.setText("Le titre ne peut pas dépasser 40 caractères");
            } else if (newValue.length() < 4) {
                showWarning(warningtitle, true);
                warningtitle.setText("Le titre doit contenir au moins 4 caractères");
            } else {
                showWarning(warningtitle, false);
            }
        });

        // Add word count limit for description
        TFDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            int wordCount = newValue.trim().split("\\s+").length;
            if (wordCount > 40) {
                TFDescription.setText(oldValue);
                showWarning(warningDescription, true);
                warningDescription.setText("La description ne peut pas dépasser 40 mots");
            } else if (newValue.length() < 12) {
                showWarning(warningDescription, true);
                warningDescription.setText("La description doit contenir au moins 12 caractères");
            } else {
                showWarning(warningDescription, false);
            }
        });

        TFCategorie.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                showWarning(warningComm, true);
            } else {
                showWarning(warningComm, false);
            }
        });

        animateButton(ajouterForumAction);
        animateButton(uploadbutton);
    }

    private void showWarning(Label label, boolean show) {
        FadeTransition ft = new FadeTransition(Duration.millis(400), label);
        ft.setFromValue(show ? 0.0 : 1.0);
        ft.setToValue(show ? 1.0 : 0.0);
        ft.play();
        label.setVisible(show);
    }

    private void animateButton(Button button) {
        button.setOnMouseEntered(e -> {
            button.setScaleX(1.1);
            button.setScaleY(1.1);
        });

        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
    }

    public void ajouterForumAction(ActionEvent actionEvent) {
        if (currentUser == null) {
            System.err.println("ERREUR CRITIQUE: currentUser est null");
            showAlert("Erreur", "Aucun utilisateur connecté",
                    "Veuillez vous reconnecter.", Alert.AlertType.ERROR);
            return;
        }

        if (TFDescription.getText().length() < 12 || TFtitre.getText().length() < 4 ||
                TFCategorie.getValue() == null || TFCategorie.getValue().isEmpty()) {
            showAnimatedErrorAlert("Erreur", "Veuillez vous assurer que tous les champs ont la longueur requise ou sont remplis !");
            return;
        }

        try {
            Forum newForum = new Forum();
            newForum.setDate_f(sqlDate);
            newForum.setTitre_f(TFtitre.getText());
            newForum.setCategorie_f(TFCategorie.getValue());
            newForum.setDescription_f(TFDescription.getText());
            newForum.setImage_f(imagePath);
            newForum.setRating(0.0f);
            newForum.setUser(currentUser);

            serviceForum.ajouter(newForum);

            if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                try {
                    sendEmailNotification(currentUser.getEmail(), newForum);
                } catch (MessagingException e) {
                    System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
                    showAlert("Avertissement", "Email non envoyé",
                            "Le forum a été ajouté, mais l'email de confirmation n'a pas pu être envoyé: " + e.getMessage(),
                            Alert.AlertType.WARNING);
                }
            }

            if (afficherForumController != null) {
                afficherForumController.loadForumList();
            }

            TFtitre.clear();
            TFDescription.clear();
            TFCategorie.setValue(null);
            imgView_reclamation.setImage(null);
            imagePath = "";

            showSuccessMessage(actionEvent);

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout du forum",
                    "Une erreur s'est produite: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }



    private void sendEmailNotification(String recipientEmail, Forum forum) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        properties.put("mail.debug", "true");

        final String username = "dhias341@gmail.com";
        final String password = "vvli ekcf gbyb czzh";

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        session.setDebug(true);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Confirmation d'ajout de forum");

        String emailContent = "<html><body>"
                + "<h2>Bonjour " + currentUser.getNom() + ",</h2>"
                + "<p>Votre forum a été ajouté avec succès ! Voici les détails :</p>"
                + "<ul>"
                + "<li><strong>Titre:</strong> " + forum.getTitre_f() + "</li>"
                + "<li><strong>Catégorie:</strong> " + forum.getCategorie_f() + "</li>"
                + "<li><strong>Description:</strong> " + forum.getDescription_f() + "</li>"
                + "<li><strong>Date:</strong> " + forum.getFormattedDate() + "</li>"
                + (forum.getImage_f() != null && !forum.getImage_f().isEmpty()
                ? "<li><strong>Image:</strong> incluse</li>" : "")
                + "</ul>"
                + "<p>Merci d'avoir contribué à notre communauté !</p>"
                + "<p>Cordialement,<br>L'équipe Fnart</p>"
                + "</body></html>";

        message.setContent(emailContent, "text/html; charset=utf-8");
        Transport.send(message);
        System.out.println("Email envoyé avec succès à " + recipientEmail);
    }

    private void showSuccessMessage(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText("Forum Créé avec Succès !");
        alert.setContentText("Votre forum a été ajouté avec succès et une notification a été envoyée par email.");
        alert.show();
    }

    public void uploadimg(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(uploadbutton.getScene().getWindow());

        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();
            Image image = new Image("file:///" + imagePath);
            imgView_reclamation.setImage(image);
        }
    }

    public void setAfficherForumController(AfficherForumController afficherForumController) {
        this.afficherForumController = afficherForumController;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user == null) {
            System.err.println("Attention: Tentative de définir un utilisateur null");
            this.currentUser = new User();
            this.currentUser.setId(1);
            this.currentUser.setNom("Utilisateur Anonyme");
            this.currentUser.setEmail("default@example.com");
        } else {
            System.out.println("Utilisateur défini: " + user.getNom());
        }
    }

    @FXML
    void addForum(ActionEvent event) {
        if (afficherForumController != null) {
            afficherForumController.loadForumList();
        } else {
            System.err.println("afficherForumController is null. Forum list may not refresh.");
        }
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
        dialogScene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialogStage.setScene(dialogScene);

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), dialogVBox);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.5), dialogVBox);
        scaleTransition.setFromX(0.5);
        scaleTransition.setFromY(0.5);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);

        fadeTransition.play();
        scaleTransition.play();

        dialogStage.showAndWait();
    }
}