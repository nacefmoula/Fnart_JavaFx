package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import tn.esprit.enumerations.Role;
import tn.esprit.models.User;
import tn.esprit.services.UserService;

import java.io.File;

public class SignUpController {

    @FXML
    private TextField nameTF;

    @FXML
    private TextField emailTF;

    @FXML
    private PasswordField PasswordField;

    @FXML
    private PasswordField confimPasswordField;

    @FXML
    private TextField PhoneField;

    @FXML
    private RadioButton maleRadio;

    @FXML
    private RadioButton femaleRadio;

    @FXML
    private ChoiceBox<Role> RoleComboBox;

    @FXML
    private Button SignupButton;

    @FXML
    private Label statusLabel;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Hyperlink loginLink;

    private UserService userService;

    public SignUpController() {
        userService = new UserService();
    }

    @FXML
    public void initialize() {
        // Initialize the role choice box with available roles
        RoleComboBox.getItems().addAll(Role.REGULARUSER, Role.ADMIN, Role.THERAPIST, Role.ARTIST);

        // Redirection du lien "Connectez-vous ici" vers la page de login
        loginLink.setOnAction(event -> redirectToLogin());
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la redirection : " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    public void handleSignUp() {
        String name = nameTF.getText();
        String email = emailTF.getText();
        String password = PasswordField.getText();
        String confirmPassword = confimPasswordField.getText();
        String phone = PhoneField.getText();
        Role role = RoleComboBox.getValue();
        String gender = maleRadio.isSelected() ? "Male" : femaleRadio.isSelected() ? "Female" : null;

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty() || role == null || gender == null) {
            statusLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        User newUser = new User(name, email, password, phone, gender, role);
        boolean success = userService.signUp(newUser);

        if (success) {
            statusLabel.setText("Inscription réussie ! Vous pouvez maintenant vous connecter.");
        } else {
            statusLabel.setText("Échec de l'inscription. L'email est peut-être déjà utilisé.");
        }
    }

    @FXML
    private void handleImageSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(SignupButton.getScene().getWindow());
        if (selectedFile != null) {
            String imagePath = selectedFile.toURI().toString();
            profileImageView.setImage(new Image(imagePath));
            statusLabel.setText("Image selected successfully.");
        } else {
            statusLabel.setText("No image selected.");
        }
    }
}