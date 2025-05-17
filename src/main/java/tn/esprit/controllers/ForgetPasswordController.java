package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.services.UserService;
import tn.esprit.services.EmailService;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Random;

public class ForgetPasswordController {
    @FXML
    private TextField emailTextField;
    @FXML
    private Button sendButton;
    @FXML
    private Hyperlink backToLoginLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private StackPane successPopup;

    private UserService userService;
    private String currentVerificationCode; // Store the current verification code

    @FXML
    public void initialize() {
        userService = new UserService();
        errorLabel.setVisible(false);
        successPopup.setVisible(false);
    }

    @FXML
    private void handleSend() {
        String email = emailTextField.getText();

        // Clear previous error
        errorLabel.setVisible(false);

        if (email == null || email.isEmpty()) {
            showError("Please enter your email address");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Please enter a valid email address");
            return;
        }

        try {
            if (!userService.emailExists(email)) {
                showError("Email not found in our system");
                return;
            }

            // Generate a 4-digit verification code
            currentVerificationCode = generateVerificationCode();
            userService.saveResetToken(email, currentVerificationCode);

            // Send email with the code
            String emailContent = "<html><body>"
                    + "<p>You requested a password reset. Here is your verification code:</p>"
                    + "<h2 style='color: #424242; font-size: 24px;'>" + currentVerificationCode + "</h2>"
                    + "<p>Please enter this code in the verification form.</p>"
                    + "<p>If you did not request this, please ignore this email.</p>"
                    + "</body></html>";
            EmailService.sendHtmlEmail(email, "Reset Your Password", emailContent);

            // Show success popup
            showSuccessPopup();
        } catch (Exception e) {
            showError("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000); // 4-digit code
        return String.valueOf(code);
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
            showVerificationCodeForm(emailTextField.getText());
        });
        fadeOut.play();
    }

    private void showVerificationCodeForm(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/verification_code.fxml"));
            Parent root = loader.load();

            // Get the controller and set the data using the stored verification code
            VerificationCodeController controller = loader.getController();
            controller.setData(email, currentVerificationCode);

            // Show the verification code scene
            Stage stage = (Stage) sendButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Failed to load verification code form.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) backToLoginLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Failed to load login screen");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}