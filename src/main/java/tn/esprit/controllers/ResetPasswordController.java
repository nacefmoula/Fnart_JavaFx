package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import tn.esprit.services.UserService;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class ResetPasswordController {
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button resetButton;
    @FXML
    private StackPane successPopup;

    private UserService userService;
    private String email;

    @FXML
    public void initialize() {
        userService = new UserService();
        errorLabel.setVisible(false);
        successPopup.setVisible(false);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    private void handleResetPassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Clear previous error
        errorLabel.setVisible(false);

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (newPassword.length() < 8) {
            showError("Password must be at least 8 characters long");
            return;
        }

        // Password validation regex (at least one uppercase, one lowercase, one number)
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        if (!newPassword.matches(passwordPattern)) {
            showError("Password must contain at least one uppercase letter, one lowercase letter, and one number");
            return;
        }

        if (email == null || email.isEmpty()) {
            showError("Email not set. Please try the reset process again.");
            return;
        }

        boolean success = userService.resetPasswordByEmail(email, newPassword);
        if (success) {
            showSuccessPopup();
        } else {
            showError("Failed to reset password. Please try again.");
        }
    }

    private void showSuccessPopup() {
        // Show the success popup
        successPopup.setVisible(true);

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), successPopup);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    @FXML
    private void handleClosePopup() {
        // Fade out animation
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), successPopup);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            successPopup.setVisible(false);
            showLoginScreen();
        });
        fadeOut.play();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    private void handleTryAnotherMethod() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/smsVerification.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) resetButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading SMS verification screen");
        }
    }

    private void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) resetButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error returning to login screen");
        }
    }

    // Method to create and show the reset password window
    public static void showResetPasswordWindow(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(ResetPasswordController.class.getResource("/fxml/ResetPassword.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Reset Password");
            stage.setScene(scene);

            ResetPasswordController controller = loader.getController();
            controller.setEmail(email);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}