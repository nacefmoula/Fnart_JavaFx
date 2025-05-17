package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import tn.esprit.models.Forum;
import tn.esprit.services.ServiceForum;

import java.util.List;

public class ForumController {

    @FXML
    private Text DateForumAff;  // Display forum date

    @FXML
    private GridPane gridForums;  // GridPane to display list of forums

    private ServiceForum serviceForum = new ServiceForum();  // Service to interact with forum data

    // This method is called to initialize the controller and load the forum list
    @FXML
    void initialize() {
        loadForumList();  // Load and display all forums in the grid
    }

    // Method to load the forum list and display in the grid
    private void loadForumList() {
        try {
            List<Forum> forums = (List<Forum>) serviceForum.getAll();  // Get the list of forums from the service
            int row = 0;
            for (Forum forum : forums) {
                // Create text components for each forum's title, description, and date
                Text forumTitle = new Text(forum.getTitre_f());
                Text forumDescription = new Text(forum.getDescription_f());
                Text forumDate = new Text(forum.getFormattedDate());

                // Create an ImageView for the forum's image
                ImageView imageView = new ImageView();
                if (forum.getImage_f() != null) {
                    Image image = new Image("file:" + forum.getImage_f());  // Assuming image path is absolute, adjust if necessary
                    imageView.setImage(image);
                    imageView.setFitHeight(100);  // Resize the image if necessary
                    imageView.setFitWidth(100);
                }

                // Add the text components and the image to the GridPane
                gridForums.add(forumTitle, 0, row);
                gridForums.add(forumDescription, 1, row);
                gridForums.add(forumDate, 2, row);
                gridForums.add(imageView, 3, row);  // Add the image to the grid

                // Optionally add buttons for actions like viewing or deleting
                Button viewButton = new Button("View Details");
                viewButton.setOnAction(event -> viewForumDetails(forum));
                gridForums.add(viewButton, 4, row);

                // Increment the row index for the next forum
                row++;
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load forums from the database.");
            e.printStackTrace();
        }
    }

    // Action method for viewing forum details
    private void viewForumDetails(Forum forum) {
        // Logic to show forum details (could be a new scene or a pop-up)
        System.out.println("Viewing forum details: " + forum.getTitre_f());
    }

    // Utility method to show alerts to the user
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Action method to delete a forum (you can implement it)
    @FXML
    void deleteForumAction(ActionEvent event) {
        // Logic for deleting a forum
    }
}
