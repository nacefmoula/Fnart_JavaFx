package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import tn.esprit.models.User;
import tn.esprit.services.UserService;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ProfileController {
    @FXML private ImageView profilePicture;
    @FXML private ImageView editProfilePicture;
    @FXML private ImageView topProfilePicture;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label dateLabel;
    @FXML private Label welcomeLabel;
    @FXML private Button profileMenuButton;
    @FXML private Button changeProfilePictureButton;
    @FXML private Button saveButton;
    @FXML private Button resetPasswordButton;
    @FXML private Button viewProfileButton;
    @FXML private Button editProfileButton;

    // View Profile Labels
    @FXML private Label displayNameLabel;
    @FXML private Label displayEmailLabel;
    @FXML private Label displayPhoneLabel;
    @FXML private Label displayRoleLabel;
    @FXML private Label displayGenderLabel;

    // Edit Profile Fields
    @FXML private TextField fullNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private TextArea bioTextArea;

    // Sections
    @FXML private VBox viewProfileSection;
    @FXML private VBox editProfileSection;

    private User currentUser;
    private UserService userService;
    private ContextMenu profileMenu;
    private static final String DEFAULT_PROFILE_IMAGE = "/assets/default-user.png";

    @FXML
    public void initialize() {
        userService = new UserService();
        setupProfileMenu();
        setupEventHandlers();
        updateDateLabel();

        // Show view profile section by default
        viewProfileSection.setVisible(true);
        editProfileSection.setVisible(false);

        // Set default profile picture
        setDefaultProfilePicture();

    }

    private void setDefaultProfilePicture() {
        Image defaultImage = new Image(getClass().getResourceAsStream(DEFAULT_PROFILE_IMAGE));
        profilePicture.setImage(defaultImage);
        editProfilePicture.setImage(defaultImage);
        topProfilePicture.setImage(defaultImage);
    }

    private LocalDate convertToLocalDate(Date dateToConvert) {
        if (dateToConvert == null) {
            return LocalDate.now();
        }
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private Date convertToDate(LocalDate dateToConvert) {
        if (dateToConvert == null) {
            return new Date();
        }
        return Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    public void setUser(User user) {
        this.currentUser = user;
        updateProfileDisplay();
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome back, " + user.getNom() + "!");
            welcomeLabel.setTextFill(Color.web("#331B19"));
        }
    }

    private void setupProfileMenu() {
        profileMenu = new ContextMenu();
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> handleLogout());
        profileMenu.getItems().add(logoutItem);
    }

    private void updateDateLabel() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM, yyyy");
        dateLabel.setText(now.format(formatter));
    }

    private void setupEventHandlers() {
        changeProfilePictureButton.setOnAction(e -> handleChangeProfilePicture());
        saveButton.setOnAction(e -> handleSaveChanges());
        resetPasswordButton.setOnAction(e -> navigateToForgetPassword());
        viewProfileButton.setOnAction(e -> showViewProfile());
        editProfileButton.setOnAction(e -> showEditProfile());

        // Profile menu
        profileMenuButton.setOnAction(e -> {
            double x = profileMenuButton.localToScreen(profileMenuButton.getBoundsInLocal()).getMaxX();
            double y = profileMenuButton.localToScreen(profileMenuButton.getBoundsInLocal()).getMaxY();
            profileMenu.show(profileMenuButton, x, y);
        });
    }
    @FXML
    private void showViewProfile() {
        viewProfileSection.setVisible(true);
        editProfileSection.setVisible(false);
        viewProfileButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white;");
        editProfileButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
    }
    @FXML
    private void showEditProfile() {
        viewProfileSection.setVisible(false);
        editProfileSection.setVisible(true);
        viewProfileButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        editProfileButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white;");
        populateEditFields();
    }

    private void populateEditFields() {
        if (currentUser != null) {
            String[] names = currentUser.getNom().split(" ", 2);
            fullNameField.setText(names[0]);
            lastNameField.setText(names.length > 1 ? names[1] : "");
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhone());

            // Convert Date to LocalDate for DatePicker
            LocalDate birthDate = convertToLocalDate(currentUser.getDateOfBirth());
            dateOfBirthPicker.setValue(birthDate);

            //bioTextArea.setText(currentUser.getBio());
        }
    }

    private void updateProfileDisplay() {
        if (currentUser != null) {
            // Update display labels
            if (displayNameLabel != null) displayNameLabel.setText(currentUser.getNom());
            if (displayEmailLabel != null) displayEmailLabel.setText(currentUser.getEmail());
            if (displayPhoneLabel != null) displayPhoneLabel.setText(currentUser.getPhone());
            if (displayRoleLabel != null && currentUser.getRole() != null) displayRoleLabel.setText(currentUser.getRole().toString());
            if (displayGenderLabel != null) displayGenderLabel.setText(currentUser.getGender());

            // Update header user info
            if (userNameLabel != null) userNameLabel.setText(currentUser.getNom());
            if (userEmailLabel != null) userEmailLabel.setText(currentUser.getEmail());

            // Update profile pictures
            String profilePic = currentUser.getProfilePicture();
            if (profilePic != null && !profilePic.isEmpty()) {
                try {
                    Image img = new Image(profilePic, true);
                    if (!img.isError()) {
                        if (profilePicture != null) profilePicture.setImage(img);
                        if (editProfilePicture != null) editProfilePicture.setImage(img);
                        if (topProfilePicture != null) topProfilePicture.setImage(img);
                    } else {
                        setDefaultProfilePicture();
                    }
                } catch (Exception e) {
                    setDefaultProfilePicture();
                }
            } else {
                setDefaultProfilePicture();
            }
        }
    }
    @FXML
    private void handleChangeProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(profilePicture.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Image newProfilePicture = new Image(selectedFile.toURI().toString());
                if (newProfilePicture.isError()) {
                    showError("Invalid image file");
                    return;
                }
                // Set the new image directly
                if (profilePicture != null) profilePicture.setImage(newProfilePicture);
                if (editProfilePicture != null) editProfilePicture.setImage(newProfilePicture);
                if (topProfilePicture != null) topProfilePicture.setImage(newProfilePicture);
                currentUser.setProfilePicture(selectedFile.toURI().toString());
                showSuccess("Profile picture updated successfully");
            } catch (Exception e) {
                showError("Error loading image: " + e.getMessage());
            }
        }
    }

    private void handleSaveChanges() {
        try {
            // Update user information
            String fullName = fullNameField.getText().trim() + " " + lastNameField.getText().trim();
            currentUser.setNom(fullName.trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setPhone(phoneField.getText().trim());

            // Convert LocalDate to Date for the User model
            Date birthDate = convertToDate(dateOfBirthPicker.getValue());
            currentUser.setDateOfBirth(birthDate);

            //currentUser.setBio(bioTextArea.getText().trim());

            // Save changes to database
            userService.update(currentUser);

            // Update session user
            SessionManager.setCurrentUser(currentUser);

            // Update display
            updateProfileDisplay();
            showViewProfile();
            showSuccess("Profile updated successfully");
        } catch (Exception e) {
            showError("Error updating profile: " + e.getMessage());
        }
    }

    private void navigateToForgetPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forgetPassword.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) resetPasswordButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Error loading password reset page: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            SessionManager.clearSession();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) profileMenuButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Error during logout: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to make the scene responsive
    private void makeSceneResponsive(Scene scene, Stage stage) {
        // Get screen dimensions
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

        // Set initial size
        stage.setWidth(Math.min(900, screenWidth * 0.9));
        stage.setHeight(Math.min(700, screenHeight * 0.9));

        // Center the stage
        stage.centerOnScreen();
    }


}