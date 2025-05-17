package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.Beneficiaires;
import tn.esprit.services.ServicesBeneficiaires;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class AddBeneficiaireBackController implements Initializable {

    @FXML private TextField NomTextField;
    @FXML private TextField EmailTextField;
    @FXML private TextField TelephoneTextField;
    @FXML private ChoiceBox<String> AssociationChoice;
    @FXML private TextField CauseTextField;
    @FXML private TextField ValeurTextField;
    @FXML private TextArea DescriptionTextArea;
    @FXML private ChoiceBox<String> StatusChoice;
    @FXML private Button AjoutButton;
    @FXML private Button backButton;

    // New for image
    @FXML private Button uploadImageButton;
    @FXML private Label imagePathLabel;

    @FXML private File selectedImageFile = null;
    private ServicesBeneficiaires servicesBeneficiaires;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("[DEBUG] AddBeneficiaireBackController INITIALIZED");

        servicesBeneficiaires = new ServicesBeneficiaires();

        StatusChoice.getItems().addAll("en attente", "Accepté", "Rejeté");
        StatusChoice.setValue("en attente");

        AssociationChoice.getItems().addAll("Oui", "Non");
        AssociationChoice.setValue("Non");

        AjoutButton.setOnAction(event -> handleSubmit());
        backButton.setOnAction(event -> handleBack());
        uploadImageButton.setOnAction(event -> handleImageUpload());
    }
    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            imagePathLabel.setText(selectedImageFile.getName());
        }
    }

    @FXML
    private void handleSubmit() {
        try {
            if (NomTextField.getText().isEmpty() || EmailTextField.getText().isEmpty() ||
                    TelephoneTextField.getText().isEmpty() || AssociationChoice.getValue() == null ||
                    CauseTextField.getText().isEmpty() || ValeurTextField.getText().isEmpty() ||
                    DescriptionTextArea.getText().isEmpty()) {
                showError("Erreur de validation", "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            Beneficiaires newBeneficiaire = new Beneficiaires();
            newBeneficiaire.setNom(NomTextField.getText());
            newBeneficiaire.setEmail(EmailTextField.getText());
            newBeneficiaire.setTelephone(TelephoneTextField.getText());
            newBeneficiaire.setEstElleAssociation(AssociationChoice.getValue());
            newBeneficiaire.setCause(CauseTextField.getText());
            newBeneficiaire.setValeurDemande(Double.parseDouble(ValeurTextField.getText()));
            newBeneficiaire.setDescription(DescriptionTextArea.getText());
            newBeneficiaire.setStatus(StatusChoice.getValue());

            // Handle image saving
            String savedImagePath = "default_profile.png";
            if (selectedImageFile != null) {
                File destDir = new File("src/main/resources/uploads/beneficiaires/");
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }

                String uniqueFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + selectedImageFile.getName();
                File destFile = new File(destDir, uniqueFileName);

                try (FileInputStream fis = new FileInputStream(selectedImageFile);
                     FileOutputStream fos = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }

                savedImagePath = "uploads/beneficiaires/" + uniqueFileName;
            }

            newBeneficiaire.setImage(savedImagePath);

            try {
                servicesBeneficiaires.add(newBeneficiaire);
                showSuccess("Succès", "Le bénéficiaire a été ajouté avec succès.");
                handleBack();
            } catch (RuntimeException ex) {
                showError("Erreur SQL", "L'ajout en base a échoué : " + ex.getMessage());
                ex.printStackTrace();
            }

        } catch (NumberFormatException e) {
            showError("Erreur de format", "La valeur demandée doit être un nombre valide.");
        } catch (Exception e) {
            showError("Erreur", "Une erreur est survenue lors de l'ajout du bénéficiaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            System.out.println("Retour à la liste des bénéficiaires...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeBeneficiairesBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Bénéficiaires");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du retour à la liste des bénéficiaires: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de retourner à la liste des bénéficiaires: " + e.getMessage());
        }
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
