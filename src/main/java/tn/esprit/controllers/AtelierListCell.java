package tn.esprit.controllers;

import tn.esprit.models.Atelier;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class AtelierListCell extends ListCell<Atelier> {

    private final HBox content;
    private final ImageView imageView;
    private final Text title;
    private final Text location;
    private final Text date;
    private final Button detailsButton;

    private final AfficherAtelierController parentController;

    public AtelierListCell(AfficherAtelierController parentController) {
        super();
        this.parentController = parentController;

        imageView = new ImageView();
        imageView.setFitHeight(60);
        imageView.setFitWidth(60);
        imageView.setPreserveRatio(true);

        title = new Text();

        location = new Text();

        date = new Text();

        VBox vBox = new VBox(title, location, date);
        vBox.setSpacing(5);

        detailsButton = new Button("Détails");
        detailsButton.setOnAction(e -> {
            if (getItem() != null) {
                parentController.voirDetailAtelier(getItem());
            }
        });

        content = new HBox(15, vBox, detailsButton);
        content.setPadding(new Insets(10));
    }

    @Override
    protected void updateItem(Atelier atelier, boolean empty) {
        super.updateItem(atelier, empty);
        if (empty || atelier == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (atelier.getImage() != null && !atelier.getImage().isEmpty()) {
                try {
                    Image img = new Image(atelier.getImage(), true);
                    imageView.setImage(img);
                } catch (Exception e) {
                    imageView.setImage(null);
                }
            } else {
                imageView.setImage(null);
            }

            title.setText(atelier.getTitre());
            location.setText("Lieu: " + atelier.getLieu());
            date.setText("Date: " + atelier.getDate().toString());

            setGraphic(content);
        }
    }
}
