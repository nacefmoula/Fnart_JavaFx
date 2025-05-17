package tn.esprit.utils;

import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ValidationUtils {
    
    public static boolean validateTextField(TextField field, String fieldName, boolean required) {
        if (required && (field.getText() == null || field.getText().trim().isEmpty())) {
            showError(fieldName + " est obligatoire");
            field.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean validateTextArea(TextArea area, String fieldName, boolean required) {
        if (required && (area.getText() == null || area.getText().trim().isEmpty())) {
            showError(fieldName + " est obligatoire");
            area.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean validateDatePicker(DatePicker datePicker, String fieldName, boolean required) {
        if (required && datePicker.getValue() == null) {
            showError(fieldName + " est obligatoire");
            datePicker.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean validatePositiveNumber(TextField field, String fieldName) {
        try {
            int value = Integer.parseInt(field.getText());
            if (value <= 0) {
                showError(fieldName + " doit être un nombre positif");
                field.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError(fieldName + " doit être un nombre valide");
            field.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean validateEmail(TextField field) {
        String email = field.getText();
        if (email != null && !email.isEmpty()) {
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showError("Format d'email invalide");
                field.requestFocus();
                return false;
            }
        }
        return true;
    }

    private static void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 