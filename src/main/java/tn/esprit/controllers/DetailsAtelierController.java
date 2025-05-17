package tn.esprit.controllers;

import tn.esprit.models.Atelier;
import tn.esprit.services.EmailService;
import tn.esprit.services.InscriptionAtelierService;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class DetailsAtelierController {

    @FXML
    private Text titreLabel;

    @FXML
    private Text lieuLabel;

    @FXML
    private Text descriptionLabel;

    @FXML
    private Text dateLabel;

    @FXML
    private Text placesLabel;

    @FXML
    private ImageView imageView;

    @FXML
    private ListView<String> inscriptionsListView;

    private Atelier atelier;

    private final InscriptionAtelierService inscriptionService = new InscriptionAtelierService();

    public void setAtelier(Atelier atelier) {
        this.atelier = atelier;
        updateUI();
    }

    public void updateUI() {
        if (atelier != null) {
            titreLabel.setText(atelier.getTitre());
            lieuLabel.setText(atelier.getLieu());
            descriptionLabel.setText(atelier.getDescription());
            dateLabel.setText(atelier.getDate().toString());
            placesLabel.setText(String.valueOf(atelier.getParticipantMax()));

            if (atelier.getImage() != null && !atelier.getImage().isEmpty()) {
                try {
                    java.io.File file = new java.io.File(atelier.getImage());
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                } catch (Exception e) {
                    // fallback: try loading directly
                    Image image = new Image(atelier.getImage());
                    imageView.setImage(image);
                }
            }

            List<String> inscriptions = inscriptionService.getByAtelier(atelier.getId())
                    .stream()
                    .map(i -> i.getNomTemporaire() + " (" + i.getEmailTemporaire() + ")")
                    .toList();

            inscriptionsListView.getItems().setAll(inscriptions);

            // Enhance the design by setting a custom cell factory
            inscriptionsListView.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setStyle(null);
                    }
                }
            });
        }
    }
    
    @FXML
    private void deleteSelectedInscription() {
        String selected = inscriptionsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select an inscription to delete.");
            return;
        }
        
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete the selected inscription?");
        
        java.util.Optional<javafx.scene.control.ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            try {
                // Assuming the inscriptionService has a method to delete by name and email extracted from the string
                // Parse the selected string: "Name (email)"
                int start = selected.indexOf('(');
                int end = selected.indexOf(')');
                if (start > 0 && end > start) {
                    String email = selected.substring(start + 1, end);
                    inscriptionService.supprimerParEmail(email);
                    updateUI();
                    showAlert("Success", "Inscription deleted successfully.");
                } else {
                    showAlert("Error", "Failed to parse the selected inscription.");
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to delete inscription: " + e.getMessage());
            }
        }
    }

    @FXML
    private void closeDetails() {
        Stage stage = (Stage) titreLabel.getScene().getWindow();
        stage.close();
    }



    @FXML
    private void downloadPdf() {
        if (atelier == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Workshop Details as PDF");
        fileChooser.setInitialFileName(atelier.getTitre().replaceAll("\\s+", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        Stage stage = (Stage) titreLabel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    // Draw header background
                    contentStream.setNonStrokingColor(106, 17, 203); // Purple color
                    contentStream.addRect(0, 750, page.getMediaBox().getWidth(), 50);
                    contentStream.fill();

                    // Add header text
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(255, 255, 255); // White color
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                    contentStream.newLineAtOffset(50, 765);
                    contentStream.showText("Workshop Details");
                    contentStream.endText();

                    // Reset color for body text
                    contentStream.setNonStrokingColor(0, 0, 0); // Black color

                    // Add workshop details with spacing
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 14);
                    contentStream.newLineAtOffset(50, 700);
                    contentStream.showText("Title: " + atelier.getTitre());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Location: " + atelier.getLieu());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Date: " + atelier.getDate().toString());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Max Participants: " + atelier.getParticipantMax());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Description: " + atelier.getDescription());
                    contentStream.endText();
                }

                document.save(file);
                showAlert("Success", "PDF saved successfully.");
            } catch (IOException e) {
                showAlert("Error", "Failed to save PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    private void acceptInscription() {
        String selected = inscriptionsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Veuillez sélectionner une inscription à accepter.");
            return;
        }

        try {
            int start = selected.indexOf('(');
            int end = selected.indexOf(')');
            if (start > 0 && end > start) {
                String email = selected.substring(start + 1, end);
                inscriptionService.updateInscriptionStatusByEmail(email, "accepted");
                EmailService.sendEmail(email, "Inscription Acceptée", "Votre inscription a été acceptée.");
                updateUI();
                showAlert("Succès", "L'inscription a été acceptée et un email a été envoyé.");
            } else {
                showAlert("Error", "Impossible de traiter l'inscription sélectionnée.");
            }
        } catch (Exception e) {
            showAlert("Error", "Une erreur est survenue lors de l'acceptation de l'inscription : " + e.getMessage());
        }
    }

    @FXML
    private void refuseInscription() {
        String selected = inscriptionsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Veuillez sélectionner une inscription à refuser.");
            return;
        }

        try {
            int start = selected.indexOf('(');
            int end = selected.indexOf(')');
            if (start > 0 && end > start) {
                String email = selected.substring(start + 1, end);
                inscriptionService.updateInscriptionStatusByEmail(email, "refused");
                EmailService.sendEmail(email, "Inscription Refusée", "Votre inscription a été refusée.");
                updateUI();
                showAlert("Succès", "L'inscription a été refusée et un email a été envoyé.");
            } else {
                showAlert("Error", "Impossible de traiter l'inscription sélectionnée.");
            }
        } catch (Exception e) {
            showAlert("Error", "Une erreur est survenue lors du refus de l'inscription : " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setAdminView(boolean b) {

    }
}
