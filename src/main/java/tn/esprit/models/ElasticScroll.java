package tn.esprit.models;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

public class ElasticScroll {
    public static void applyElasticScrolling(ScrollPane scrollPane) {
        scrollPane.setOnScrollFinished(event -> {
            double vvalue = scrollPane.getVvalue();
            if (vvalue < 0) {
                animateScroll(scrollPane, 0);
            } else if (vvalue > 1) {
                animateScroll(scrollPane, 1);
            }
        });
    }

    private static void animateScroll(ScrollPane scrollPane, double target) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(scrollPane.vvalueProperty(), target)
                )
        );
        timeline.play();
    }
}