package tn.esprit.controllers;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.exception.ApiException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import tn.esprit.models.Beneficiaires;
import tn.esprit.services.ServicesBeneficiaires;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;
import java.io.IOException;
import java.util.regex.Pattern;

public class AddBeneficiaireController implements Initializable {
    @FXML
    private Button retourHome;
    @FXML
    private ChoiceBox<String> AssociationChoice;
    @FXML
    private ChoiceBox<String> StatusChoice;
    @FXML
    private Button AjoutBoutton;
    @FXML
    private TextField CauseTextField;
    @FXML
    private TextArea DescriptionTextArea;
    @FXML
    private TextField EmailTextField;
    @FXML
    private Button ListeBenebutton;
    @FXML
    private TextField NomTextField;
    @FXML
    private TextField TelephoneTextField;
    @FXML
    private TextField ValeurTextField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ImageView imagePreview;
    @FXML
    private Button uploadButton;
    @FXML
    private Button clearButton;
    @FXML
    private AnchorPane rootPane;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    // Telephone validation pattern (8 digits)
    private static final Pattern TELEPHONE_PATTERN = Pattern.compile("^\\d{8}$");
    // Number validation pattern
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");

    private String selectedImagePath;
    private String relativeImagePath;
    private static final String UPLOAD_DIRECTORY = "src/main/resources/uploads/beneficiaires/";
    private static final String RELATIVE_PATH_PREFIX = "uploads/beneficiaires/";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("[DEBUG] AddBeneficiaireController INITIALIZED");
        StatusChoice.getItems().addAll("en attente", "Accepté", "Rejeté");
        StatusChoice.setValue("en attente");
        String cssPath = getClass().getResource("/css/style.css").toExternalForm();
        System.out.println("CSS file found at: " + cssPath);

        AssociationChoice.getItems().addAll("Oui", "Non");
        AssociationChoice.setValue("Non");

        // Add input validation listeners
        EmailTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidEmail(newValue)) {
                EmailTextField.setStyle("-fx-border-color: red;");
            } else {
                EmailTextField.setStyle("");
            }
        });

        TelephoneTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidTelephone(newValue)) {
                TelephoneTextField.setStyle("-fx-border-color: red;");
            } else {
                TelephoneTextField.setStyle("");
            }
        });

        ValeurTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidNumber(newValue)) {
                ValeurTextField.setStyle("-fx-border-color: red;");
            } else {
                ValeurTextField.setStyle("");
            }
        });

        AjoutBoutton.setOnAction(event -> handleSubmit());
        ListeBenebutton.setOnAction(event -> handleListeBene());

        // Initialize the clear button as invisible
        clearButton.setVisible(false);

        // Create upload directory if it doesn't exist
        createUploadDirectory();
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidTelephone(String telephone) {
        return TELEPHONE_PATTERN.matcher(telephone).matches();
    }

    private boolean isValidNumber(String number) {
        return NUMBER_PATTERN.matcher(number).matches();
    }

    private void createUploadDirectory() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Upload directory created: " + uploadPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de créer le répertoire d'upload: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(imagePreview.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Generate a unique filename with timestamp
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String originalFilename = selectedFile.getName();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFilename = timestamp + "_" + originalFilename;

                // Create the target file path
                Path targetPath = Paths.get(UPLOAD_DIRECTORY, newFilename);

                // Copy the file to the upload directory
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Store the absolute path for preview
                selectedImagePath = targetPath.toAbsolutePath().toString();

                // Store the relative path for database
                relativeImagePath = RELATIVE_PATH_PREFIX + newFilename;

                // Display the image in the preview
                imagePreview.setImage(new Image("file:" + selectedImagePath));
                clearButton.setVisible(true);

                System.out.println("Image uploaded successfully: " + relativeImagePath);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de copier l'image: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleClearImage() {
        selectedImagePath = null;
        relativeImagePath = null;
        imagePreview.setImage(null);
        clearButton.setVisible(false);
    }

    @FXML
    private void handleSubmit() {
        try {
            // Validate input fields
            if (NomTextField.getText().isEmpty() || EmailTextField.getText().isEmpty() ||
                    TelephoneTextField.getText().isEmpty() || CauseTextField.getText().isEmpty() ||
                    ValeurTextField.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.", Alert.AlertType.ERROR);
                return;
            }

            // Create new beneficiaire
            Beneficiaires beneficiaire = new Beneficiaires();
            beneficiaire.setNom(NomTextField.getText());
            beneficiaire.setEmail(EmailTextField.getText());
            beneficiaire.setTelephone(TelephoneTextField.getText());
            beneficiaire.setEstElleAssociation(AssociationChoice.getValue());
            beneficiaire.setCause(CauseTextField.getText());
            beneficiaire.setStatus(StatusChoice.getValue());
            beneficiaire.setValeurDemande(Double.parseDouble(ValeurTextField.getText()));
            beneficiaire.setDescription(DescriptionTextArea.getText());

            // Set the relative image path
            beneficiaire.setImage(relativeImagePath != null ? relativeImagePath : "default_profile.png");

            // Save to database
            ServicesBeneficiaires services = new ServicesBeneficiaires();
            try {
                services.add(beneficiaire);
                showAlert("Succès", "Bénéficiaire ajouté avec succès.", Alert.AlertType.INFORMATION);
                send_sms();
                // Navigate back to list
                handleBack();
            } catch (RuntimeException ex) {
                showAlert("Erreur SQL", "L'ajout en base a échoué : " + ex.getMessage(), Alert.AlertType.ERROR);
                ex.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de l'ajout du bénéficiaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void clearFields() {
        NomTextField.clear();
        EmailTextField.clear();
        TelephoneTextField.clear();
        AssociationChoice.setValue("Non");
        CauseTextField.clear();
        ValeurTextField.clear();
        DescriptionTextArea.clear();
    }

    @FXML
    private void handleListeBene() {
        try {
            Parent fxml = FXMLLoader.load(getClass().getResource("/ListeBeneficiaires.fxml"));
            rootPane.getChildren().removeAll();
            rootPane.getChildren().setAll(fxml);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la vue ListeBeneficiaires", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(AddBeneficiaireController.class.getResource("/AddBeneficiaire.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle("Ajouter un Bénéficiaire");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger la fenêtre d'ajout de bénéficiaire: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) retourHome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void send_sms() {
        try {
            // Use environment variables for security
            String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
            String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");

            if (ACCOUNT_SID == null || AUTH_TOKEN == null) {
                throw new IllegalStateException("Twilio credentials are not set in environment variables.");
            }

            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            String recepientNumber = "+21628221962"; // Validate this number
            String senderNumber = "+19472247143"; // Validate this number
            String message = "Bonjour Admin, \n"
                    + "Nous sommes ravis de vous informer qu'une nouvelle demande de beneficiaire a été ajoutée.\n"
                    + "Cordialement, \n";

            Message twilioMessage = Message.creator(
                    new PhoneNumber(recepientNumber),
                    new PhoneNumber(senderNumber),
                    message).create();

            System.out.println("SMS envoyé : " + twilioMessage.getSid());
        } catch (ApiException e) {
            System.err.println("Erreur lors de l'envoi du SMS : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Une erreur inattendue est survenue : " + e.getMessage());
        }
    }
}