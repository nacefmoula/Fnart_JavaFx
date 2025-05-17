package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.text.similarity.CosineDistance;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ChatBotTacheController implements Initializable {
    private final Map<String, String> keywordResponses;

    @FXML
    public VBox chatContainer;
    @FXML
    public VBox chatArea; // Changed to VBox
    @FXML
    public TextField userInput;
    @FXML
    private ScrollPane chatScrollPane;

    public ChatBotTacheController() {
        keywordResponses = new HashMap<>();

        // Greetings
        keywordResponses.put("hi", "Hello!");
        keywordResponses.put("hello", "Hi there!");

        keywordResponses.put("time temp wa9t wakt heure hours minutes d9aye9", "The current time is " + LocalTime.now());
        keywordResponses.put("date", "Today's date is " + LocalDate.now());

        keywordResponses.put("job 5idma travail", "Sorry, I'm just a chatbot and don't have job listings.");
        keywordResponses.put("animal 7ayawen", "I love animals too!");
        keywordResponses.put("dog kalb", "Dogs are great companions!");

        keywordResponses.put("how old are you kdeh omrik", "I'm just a computer program, so I don't have an age.");
        keywordResponses.put("where are you from mnin enty", "I exist in the digital world, so I don't have a physical location.");

        keywordResponses.put("what is the capital of France capital mtaa fransa", "The capital of France is Paris.");
        keywordResponses.put("what is the population of China", "As of the latest data, the population of China is over 1.4 billion people.");
        keywordResponses.put("what languages do you speak chnia lo8tik", "I can communicate in multiple languages, including English, French, and Arabic.");
    }

    @FXML
    public void getAnswer(ActionEvent actionEvent) {
        sendMessage();
    }

    public void processInput(ActionEvent actionEvent) {
        sendMessage();
    }

    private void sendMessage() {
        String inputText = userInput.getText().trim();
        if (!inputText.isEmpty()) {
            addUserMessage(inputText);
            userInput.clear();

            Task<String> responseTask = new Task<String>() {
                @Override
                protected String call() throws Exception {
                    Thread.sleep(1000); // Délai artificiel
                    return guessResponse(inputText);
                }
            };

            responseTask.setOnSucceeded(event -> {
                String response = responseTask.getValue();
                addChatbotMessage(response);
            });

            new Thread(responseTask).start();
        }
    }

    private void addUserMessage(String message) {
        Platform.runLater(() -> {
            Label messageLabel = new Label(message);
            messageLabel.setStyle("-fx-background-color: #d4edda; -fx-padding: 8px; -fx-border-radius: 5px;");
            messageLabel.setMaxWidth(400);
            messageLabel.setWrapText(true);

            HBox messageContainer = new HBox(messageLabel);
            messageContainer.setAlignment(Pos.CENTER_LEFT);
            chatArea.getChildren().add(messageContainer);
            scrollToBottom();
        });
    }

    private void addChatbotMessage(String message) {
        Platform.runLater(() -> {
            Label messageLabel = new Label(message);
            messageLabel.setStyle("-fx-background-color: #cce5ff; -fx-padding: 8px; -fx-border-radius: 5px;");
            messageLabel.setMaxWidth(400);
            messageLabel.setWrapText(true);

            HBox messageContainer = new HBox(messageLabel);
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
            chatArea.getChildren().add(messageContainer);
            scrollToBottom();
        });
    }

    private Label createMessageLabel(String message, String styleClass) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setMaxWidth(400); // Adjust as needed

        // Apply animations
        messageLabel.getStyleClass().add("fade-in"); // Fade-in animation
        // messageLabel.getStyleClass().add("slide-in-left");
        return messageLabel;
    }

    private HBox createMessageContainer(Label messageLabel, Pos alignment) {
        HBox messageContainer = new HBox(messageLabel);
        messageContainer.setAlignment(alignment);
        messageContainer.getStyleClass().add("message-container");
        return messageContainer;
    }

    private String guessResponse(String input) {
        System.out.println("User input: " + input); // Debug log
        double bestSimilarity = 0.0;
        String bestResponse = "I didn't understand that. Can you rephrase?"; // Default response

        for (Map.Entry<String, String> entry : keywordResponses.entrySet()) {
            String keyword = entry.getKey();
            double similarity = calculateSimilarity(input, keyword);
            System.out.println("Keyword: " + keyword + " | Similarity: " + similarity); // Debug log

            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestResponse = entry.getValue();
            }
        }

        System.out.println("Best response: " + bestResponse); // Debug log
        return bestResponse;
    }

    private double calculateSimilarity(String input, String keyword) {
        // Tokenize the input and keyword
        String[] inputTokens = input.split("\\s+");
        String[] keywordTokens = keyword.split("\\s+");

        // Calculate similarity using different metrics
        CosineDistance cosineDistance = new CosineDistance();
        JaccardSimilarity jaccardSimilarity = new JaccardSimilarity();
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

        // Calculate similarities using different metrics
        double cosineSim = 1 - cosineDistance.apply(input, keyword);
        double jaccardSim = jaccardSimilarity.apply(input, keyword);
        double levenshteinSim = 1.0 / (1.0 + levenshteinDistance.apply(input, keyword));

        // Return the average similarity
        return (cosineSim + jaccardSim + levenshteinSim) / 3.0;
    }

    @FXML
    public void clearConversation(ActionEvent actionEvent) {
        chatArea.getChildren().clear();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            chatArea.requestLayout();
            if (chatArea.getParent() instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) chatArea.getParent();
                scrollPane.setVvalue(1.0);
            }
        });
    }

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (chatArea == null) {
            System.err.println("ERROR: chatArea is null!");
        } else {
            System.out.println("chatArea initialized successfully");
        }
    }
}