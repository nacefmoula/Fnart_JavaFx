package tn.esprit.controllers;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.File;
import tn.esprit.models.Artwork;

public class ArtworkListCell extends ListCell<Artwork> {
    private final HBox content;
    private final ImageView imageView;
    private final VBox detailsBox;
    private final Label titleLabel;
    private final Label artistLabel;
    private final Label priceLabel;
    private final Label descriptionLabel;
    private final Button detailsButton;

    public ArtworkListCell() {
        super();
        imageView = new ImageView();
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 6, 0, 0, 0);");

        titleLabel = new Label();
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        artistLabel = new Label();
        priceLabel = new Label();
        descriptionLabel = new Label();
        descriptionLabel.setWrapText(true);
        detailsButton = new Button("Détails");
        detailsButton.setStyle("-fx-background-color: #26608d; -fx-text-fill: white;");

        detailsBox = new VBox(4, titleLabel, artistLabel, priceLabel, descriptionLabel, detailsButton);
        detailsBox.setPadding(new Insets(0, 0, 0, 10));
        content = new HBox(10, imageView, detailsBox);
        content.setPadding(new Insets(10));
    }

    @Override
    protected void updateItem(Artwork artwork, boolean empty) {
        super.updateItem(artwork, empty);
        if (empty || artwork == null) {
            setText(null);
            setGraphic(null);
        } else {
            // Always show image if possible (on the card, not just on click)
            if (artwork.getImage() != null && !artwork.getImage().isEmpty()) {
                try {
                    String imgPath = artwork.getImage();
                    if (imgPath.startsWith("http://") || imgPath.startsWith("https://")) {
                        imageView.setImage(new Image(imgPath, true));
                    } else {
                        // Try to load from local file (htdocs)
                        File imgFile = new File(imgPath);
                        if (!imgFile.isAbsolute()) {
                            // Assume relative to htdocs
                            String base = "http://localhost/";
                            imageView.setImage(new Image(base + imgPath, true));
                        } else {
                            imageView.setImage(new Image(imgFile.toURI().toString(), true));
                        }
                    }
                } catch (Exception e) {
                    imageView.setImage(null);
                }
            } else {
                imageView.setImage(null);
            }
            titleLabel.setText(artwork.getTitre());
            artistLabel.setText("Artiste: " + artwork.getArtistenom());
            priceLabel.setText("Prix: " + artwork.getPrix() + " DT");
            descriptionLabel.setText(artwork.getDescription());
            // Remove details button and its action for backoffice
            detailsButton.setVisible(false);
            detailsButton.setManaged(false);
            setGraphic(content);
        }
    }

    private void openDetails(Artwork artwork) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Detailartwork.fxml"));
            javafx.scene.Parent root = loader.load();
            DetailartworkController controller = loader.getController();
            controller.setArtwork(artwork);
            Stage stage = new Stage();
            stage.setTitle("Détails de l'œuvre");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
