package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import tn.esprit.services.EmailService;
import tn.esprit.services.UserService;

public class VerificationCodeController {
    @FXML
    private TextField code1;
    @FXML
    private TextField code2;
    @FXML
    private TextField code3;
    @FXML
    private TextField code4;
    @FXML
    private Text emailText;
    @FXML
    private Button submitButton;

    private String expectedCode;
    private String userEmail;
    private String phoneNumber;
    private UserService userService;

    @FXML
    public void initialize() {
        setupCodeFields();
        userService = new UserService();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setData(String email, String verificationCode) {
        this.userEmail = email;
        this.expectedCode = verificationCode;
        emailText.setText("We've sent a code to " + email);
    }

    private void setupCodeFields() {
        // Auto-focus next field
        code1.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() == 1) code2.requestFocus();
        });
        code2.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() == 1) code3.requestFocus();
        });
        code3.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() == 1) code4.requestFocus();
        });

        // Limit to one character
        TextField[] codeFields = {code1, code2, code3, code4};
        for (TextField field : codeFields) {
            field.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 1) {
                    field.setText(newValue.substring(0, 1));
                }
            });
        }
    }

    @FXML
    private void handleSubmit() {
        String enteredCode = code1.getText() + code2.getText() + code3.getText() + code4.getText();

        // Verify against the stored token in the database
        if (userService.verifyResetToken(userEmail, enteredCode)) {
            try {
                // Load the reset password scene
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resetPassword.fxml"));
                Parent root = loader.load();

                // Get the controller and pass the email
                ResetPasswordController resetController = loader.getController();
                resetController.setEmail(userEmail);

                // Show the reset password scene
                Stage stage = (Stage) submitButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Error", "Could not load reset password page: " + e.getMessage());
            }
        } else {
            showAlert(AlertType.ERROR, "Invalid Code", "The verification code you entered is incorrect. Please try again.");
            clearFields();
        }
    }

    @FXML
    private void handleResendCode() {
        // Generate a new verification code
        String newCode = String.format("%04d", 1000 + (int)(Math.random() * 9000));
        this.expectedCode = newCode;

        // Save the new code in the database
        userService.saveResetToken(userEmail, newCode);

        // Send the new code via email
        String emailContent = "<html><body>"
                + "<p>You requested a new verification code. Here is your code:</p>"
                + "<h2 style='color: #7C4DFF; font-size: 24px;'>" + newCode + "</h2>"
                + "<p>Please enter this code in the verification form.</p>"
                + "<p>If you did not request this, please ignore this email.</p>"
                + "</body></html>";

        EmailService.sendHtmlEmail(userEmail, "Your New Verification Code", emailContent);

        showAlert(AlertType.INFORMATION, "Code Resent", "A new verification code has been sent to your email.");
        clearFields();
    }

    private void clearFields() {
        code1.clear();
        code2.clear();
        code3.clear();
        code4.clear();
        code1.requestFocus();
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}