package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;

public class ImageGenerationController implements Initializable {
    @FXML private TextField promptField;
    @FXML private Button generateBtn;
    @FXML private Label errorLabel;
    @FXML private RadioButton standardModel;
    @FXML private RadioButton hdModel;
    @FXML private RadioButton geniusModel;
    @FXML private RadioButton speedPref;
    @FXML private RadioButton qualityPref;
    @FXML private CheckBox oldeModelCheck;
    @FXML private ImageView style1;
    @FXML private ImageView style2;
    @FXML private ImageView style3;
    @FXML private ImageView resultImageView;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label promptDisplayLabel;
    @FXML private Button downloadBtn;

    private String apiKey;
    private String apiUrl;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadApiConfiguration();
        generateBtn.setOnAction(e -> onGenerateClicked());
    }

    private void loadApiConfiguration() {
        try (InputStream input = getClass().getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find application.properties");
            }
            Properties prop = new Properties();
            prop.load(input);
            apiKey = prop.getProperty("stability.api.key");
            apiUrl = prop.getProperty("stable.diffusion.api.url");
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load API configuration: " + ex.getMessage(), ex);
        }
    }

    private void onGenerateClicked() {
        String prompt = promptField.getText();
        if (prompt == null || prompt.isEmpty()) {
            errorLabel.setText("Prompt cannot be empty!");
            return;
        }
        errorLabel.setText("");
        loadingIndicator.setVisible(true);
        resultImageView.setImage(null);

        new Thread(() -> {
            try {
                String body = "{\"text_prompts\":[{\"text\":\"" + prompt.replace("\"", "\\\"") + "\"}],\"cfg_scale\":7,\"height\":1024,\"width\":1024,\"samples\":1,\"steps\":30}";

                URI uri = new URI(apiUrl);
                URL url = uri.toURL();
                java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("Authorization", "Bearer " + apiKey);
                con.setDoOutput(true);
                try (java.io.OutputStream os = con.getOutputStream()) {
                    byte[] input = body.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                int code = con.getResponseCode();
                if (code != 200) {
                    java.io.InputStream err = con.getErrorStream();
                    String errMsg = new String(err.readAllBytes());
                    throw new RuntimeException("API error: " + errMsg);
                }
                java.io.InputStream is = con.getInputStream();
                String json = new String(is.readAllBytes());

                String base64 = null;
                int ix = json.indexOf("\"base64\":");
                if (ix != -1) {
                    int start = json.indexOf('"', ix + 9) + 1;
                    int end = json.indexOf('"', start);
                    base64 = json.substring(start, end);
                }
                if (base64 == null) throw new RuntimeException("No image base64 in response: " + json);

                byte[] imageBytes = java.util.Base64.getDecoder().decode(base64);
                javafx.scene.image.Image fxImg = new javafx.scene.image.Image(new java.io.ByteArrayInputStream(imageBytes));

                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    resultImageView.setImage(fxImg);
                    promptDisplayLabel.setText(prompt);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    errorLabel.setText("Generation failed: " + ex.getMessage());
                });
            }
        }).start();
    }
}
