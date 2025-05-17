package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import tn.esprit.models.Forum;
import tn.esprit.services.ServiceForum;

import javax.swing.*;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ShowStatsController implements Initializable {

    @FXML
    private AnchorPane showStatsPane; // Changed from Showstats

    @FXML
    private StackPane chartContainer; // Added to hold the chart with potential effects

    private final ServiceForum serviceForum = new ServiceForum();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showStatsPane.getStylesheets().add(getClass().getResource("/styles/stat.css").toExternalForm()); // Adjust path
        try {
            showStatistics();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showStatistics() throws SQLException {
        Map<String, Integer> stats = getForumStats();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            dataset.addValue(entry.getValue(), "Forums créés", entry.getKey());
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Nombre de Forums par Date",
                "Date",
                "Nombre de Forums",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        SwingNode swingNode = new SwingNode();
        createSwingContent(swingNode, barChart);

        // Add fade-in transition
        FadeTransition ft = new FadeTransition(Duration.millis(1000), chartContainer);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();

        chartContainer.getChildren().add(swingNode);
        //showStatsPane.getChildren().add(chartContainer); //Use chartContainer instead
    }

    private void createSwingContent(final SwingNode swingNode, JFreeChart chart) {
        SwingUtilities.invokeLater(() -> {
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new java.awt.Dimension(400, 300));
            swingNode.setContent(chartPanel);
        });
    }

    private Map<String, Integer> getForumStats() throws SQLException {
        List<Forum> forums = new ArrayList<>(serviceForum.getAll());
        Map<String, Integer> stats = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (Forum forum : forums) {
            String date = sdf.format(forum.getDate_f());
            stats.put(date, stats.getOrDefault(date, 0) + 1);
        }
        return stats;
    }
}