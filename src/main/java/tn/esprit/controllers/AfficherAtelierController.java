package tn.esprit.controllers;

import tn.esprit.models.Atelier;
import tn.esprit.services.AtelierService;
import tn.esprit.services.InscriptionAtelierService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class AfficherAtelierController {

    @FXML
    private ListView<Atelier> workshopsListView;

    @FXML
    private TextField searchField;

    @FXML
    private PieChart detailedStatsChart;

    @FXML
    private ChoiceBox<String> sortOrderChoice;

    @FXML
    private AnchorPane profilePanel;

    @FXML
    private Label totalAteliersLabel;

    @FXML
    private Label totalAteliersFilteredLabel;

    @FXML
    private Label totalParticipantsLabel;

    @FXML
    private Label ateliersMoisLabel;

    @FXML
    private Label tauxParticipationLabel;

    @FXML
    private Label atelierPopulaireLabel;

    @FXML
    private Label moyenneParticipantsLabel;

    @FXML
    private Label jourPopulaireLabel;

    @FXML
    private Label lieuFrequentLabel;

    @FXML
    private Label availableSeatsLabel;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private Button prevPageButton;

    @FXML
    private Button nextPageButton;

    @FXML
    private Label pageInfoLabel;

    @FXML
    private ComboBox<Integer> pageSizeComboBox;

    @FXML
    private HBox pageNumbers;

    private final AtelierService atelierService = new AtelierService();
    private final InscriptionAtelierService inscriptionService = new InscriptionAtelierService();
    private ObservableList<Atelier> ateliers = FXCollections.observableArrayList();
    private ObservableList<Atelier> filteredAteliers = FXCollections.observableArrayList();

    // Pagination fields
    private int currentPage = 0;
    private int pageSize = 10; // Default page size
    private int totalPages = 1;
    private Date fromDate = null;
    private Date toDate = null;
    private String currentSortField = "date";
    private boolean sortAscending = false; // Default to most recent first

    @FXML
    public void initialize() {
        // Setup sorting options
        setupSortChoice();

        // Setup profile panel and interaction
        setupProfilePanel();

        // Setup date pickers
        setupDatePickers();

        // Setup pagination controls
        setupPagination();

        // Setup search functionality
        setupSearch();

        // Load data with pagination
        refreshAteliers();
    }

    private void setupSortChoice() {
        sortOrderChoice.setItems(FXCollections.observableArrayList(
                "Date (récent)",
                "Date (ancien)",
                "Titre (A-Z)",
                "Titre (Z-A)",
                "Participants (décroissant)",
                "Participants (croissant)",
                "Lieu (A-Z)",
                "Lieu (Z-A)"
        ));

        sortOrderChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateSortCriteria(newVal);
                // Reset to first page when sort changes
                currentPage = 0;
                applyPaginationAndFilters();
            }
        });

        sortOrderChoice.setValue("Date (récent)");
    }

    private void updateSortCriteria(String sortOption) {
        switch (sortOption) {
            case "Date (récent)":
                currentSortField = "date";
                sortAscending = false;
                break;
            case "Date (ancien)":
                currentSortField = "date";
                sortAscending = true;
                break;
            case "Titre (A-Z)":
                currentSortField = "titre";
                sortAscending = true;
                break;
            case "Titre (Z-A)":
                currentSortField = "titre";
                sortAscending = false;
                break;
            case "Participants (décroissant)":
                currentSortField = "participants_max";
                sortAscending = false;
                break;
            case "Participants (croissant)":
                currentSortField = "participants_max";
                sortAscending = true;
                break;
            case "Lieu (A-Z)":
                currentSortField = "lieu";
                sortAscending = true;
                break;
            case "Lieu (Z-A)":
                currentSortField = "lieu";
                sortAscending = false;
                break;
        }
    }

    private void setupDatePickers() {
        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            final java.text.SimpleDateFormat dateFormatter = new java.text.SimpleDateFormat("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return dateFormatter.parse(string).toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                    } catch (Exception e) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        };

        fromDatePicker.setConverter(converter);
        toDatePicker.setConverter(converter);

        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
    }

    private void setupPagination() {
        ObservableList<Integer> pageSizes = FXCollections.observableArrayList(5, 10, 20, 50, 100);
        pageSizeComboBox.setItems(pageSizes);
        pageSizeComboBox.setValue(pageSize);

        pageSizeComboBox.setOnAction(event -> {
            Integer selected = pageSizeComboBox.getValue();
            if (selected != null) {
                pageSize = selected;
                currentPage = 0;
                applyPaginationAndFilters();
            }
        });
    }

    private void setupProfilePanel() {
        profilePanel.setVisible(false);
    }



    private void loadAteliers() {
        ateliers.clear();
        ateliers.addAll(atelierService.getall());
        applyPaginationAndFilters();
    }

    private void applyPaginationAndFilters() {
        if (fromDatePicker.getValue() != null) {
            fromDate = Date.from(fromDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else {
            fromDate = null;
        }

        if (toDatePicker.getValue() != null) {
            toDate = Date.from(toDatePicker.getValue().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else {
            toDate = null;
        }

        try {
            java.sql.Date sqlFromDate = (fromDate != null) ? new java.sql.Date(fromDate.getTime()) : null;
            java.sql.Date sqlToDate = (toDate != null) ? new java.sql.Date(toDate.getTime()) : null;

            int totalFilteredAteliers = atelierService.getTotalAteliers(sqlFromDate, sqlToDate);
            totalPages = (int) Math.ceil((double) totalFilteredAteliers / pageSize);
            if (totalPages == 0) totalPages = 1;

            if (totalAteliersFilteredLabel != null) {
                totalAteliersFilteredLabel.setText(String.valueOf(totalFilteredAteliers));
            }

            if (currentPage >= totalPages) {
                currentPage = totalPages - 1;
            }
            if (currentPage < 0) {
                currentPage = 0;
            }

            List<Atelier> paginatedAteliers = atelierService.getAteliersPaginated(
                currentPage, pageSize, sqlFromDate, sqlToDate, currentSortField, sortAscending
            );

            String searchTerm = searchField.getText().trim().toLowerCase();
            if (!searchTerm.isEmpty()) {
                paginatedAteliers = paginatedAteliers.stream()
                    .filter(atelier ->
                        atelier.getTitre().toLowerCase().contains(searchTerm) ||
                        atelier.getLieu().toLowerCase().contains(searchTerm) ||
                        atelier.getDescription().toLowerCase().contains(searchTerm)
                    )
                    .collect(Collectors.toList());
            }

            filteredAteliers.clear();
            filteredAteliers.addAll(paginatedAteliers);

            displayAteliersInListView();

            updatePaginationControls();

            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du filtrage des ateliers: " + e.getMessage());
        }
    }

    private void displayAteliersInListView() {
        workshopsListView.setItems(filteredAteliers);
        workshopsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Atelier atelier, boolean empty) {
                super.updateItem(atelier, empty);

                if (empty || atelier == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create a container for the workshop item
                    HBox container = new HBox(10);
                    container.getStyleClass().add("workshop-list-item");
                    container.setPrefWidth(listView.getWidth() - 20);
                    container.setPadding(new javafx.geometry.Insets(10));

                    // Workshop icon/image
                    Circle circleIcon = new Circle(25);
                    circleIcon.getStyleClass().add("workshop-icon");
                    String initialLetter = atelier.getTitre().substring(0, 1).toUpperCase();
                    Label iconLabel = new Label(initialLetter);
                    iconLabel.setAlignment(javafx.geometry.Pos.CENTER);
                    iconLabel.getStyleClass().add("workshop-icon-text");
                    StackPane iconContainer = new StackPane(circleIcon, iconLabel);

                    // Workshop details container
                    VBox detailsBox = new VBox(5);
                    detailsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    detailsBox.setMinWidth(300);
                    detailsBox.setPrefWidth(500);
                    detailsBox.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(detailsBox, Priority.ALWAYS);

                    // Workshop title
                    Label titleLabel = new Label(atelier.getTitre());
                    titleLabel.getStyleClass().add("workshop-title");

                    // Workshop details (date and location)
                    HBox detailsLine = new HBox(15);
                    detailsLine.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    Label dateLabel = new Label(formatDate(atelier.getDate()));
                    dateLabel.getStyleClass().add("workshop-detail");

                    Label locationLabel = new Label(atelier.getLieu());
                    locationLabel.getStyleClass().add("workshop-detail");

                    detailsLine.getChildren().addAll(
                            createIconLabelPair("📅", dateLabel),
                            createIconLabelPair("📍", locationLabel)
                    );

                    // Add a short description
                    Label descriptionLabel = new Label(truncateDescription(atelier.getDescription(), 80));
                    descriptionLabel.getStyleClass().add("workshop-description");

                    detailsBox.getChildren().addAll(titleLabel, detailsLine, descriptionLabel);

                    // Stats section
                    VBox statsBox = new VBox(5);
                    statsBox.setAlignment(javafx.geometry.Pos.CENTER);
                    statsBox.setMinWidth(150);

                    int inscriptionCount = inscriptionService.getByAtelier(atelier.getId()).size();
                    int availableSeats = Math.max(0, atelier.getParticipantMax() - inscriptionCount);

                    Label participantsLabel = new Label(inscriptionCount + "/" + atelier.getParticipantMax());
                    participantsLabel.getStyleClass().add("participants-count");

                    // Progress bar for capacity
                    ProgressBar capacityBar = new ProgressBar((double) inscriptionCount / atelier.getParticipantMax());
                    capacityBar.getStyleClass().add("capacity-bar");
                    capacityBar.setPrefWidth(100);

                    // Status label
                    Label statusLabel = new Label(availableSeats > 0 ? "Places disponibles" : "Complet");
                    statusLabel.getStyleClass().add(availableSeats > 0 ? "status-available" : "status-full");

                    statsBox.getChildren().addAll(
                            new Label("Participants:"),
                            participantsLabel,
                            capacityBar,
                            statusLabel
                    );

                    // Add all components to the container
                    container.getChildren().addAll(iconContainer, detailsBox, statsBox);

                    // Set the graphic for this cell
                    setGraphic(container);
                    setText(null);

                    // Add hover effect
                    container.setOnMouseEntered(event -> container.getStyleClass().add("workshop-list-item-hover"));
                    container.setOnMouseExited(event -> container.getStyleClass().remove("workshop-list-item-hover"));
                }
            }
        });

        // Add event listener for double click to view details
        workshopsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Atelier selectedAtelier = workshopsListView.getSelectionModel().getSelectedItem();
                if (selectedAtelier != null) {
                    voirDetailAtelier(selectedAtelier);
                }
            }
        });
    }

    private HBox createIconLabelPair(String icon, Label label) {
        HBox pairBox = new HBox(5);
        pairBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label iconLabel = new Label(icon);
        pairBox.getChildren().addAll(iconLabel, label);
        return pairBox;
    }

    private String truncateDescription(String description, int maxLength) {
        if (description == null || description.length() <= maxLength) {
            return description;
        }
        return description.substring(0, maxLength) + "...";
    }

    private String formatDate(Date date) {
        if (date == null) return "Date non spécifiée";
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
    }

    private void updatePaginationControls() {
        pageInfoLabel.setText("Page " + (currentPage + 1) + " sur " + totalPages);

        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);
    }

    @FXML
    private void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            applyPaginationAndFilters();
        }
    }

    @FXML
    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            applyPaginationAndFilters();
        }
    }

    @FXML
    private void applyDateFilter() {
        currentPage = 0;
        applyPaginationAndFilters();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentPage = 0;
            applyPaginationAndFilters();
        });
    }

    private void updateStatistics() {
        List<Atelier> allAteliers = atelierService.getall();

        List<Atelier> filteredAteliersList = new ArrayList<>(filteredAteliers);

        int totalAteliers = allAteliers.size();
        int totalInscriptions = allAteliers.stream()
                .mapToInt(a -> inscriptionService.getByAtelier(a.getId()).size())
                .sum();

        int totalCapacity = allAteliers.stream()
                .mapToInt(Atelier::getParticipantMax)
                .sum();

        double tauxRemplissage = totalCapacity > 0 ?
                (double) totalInscriptions / totalCapacity * 100 : 0;

        int remainingCapacity = totalCapacity - totalInscriptions;
        if (remainingCapacity < 0) remainingCapacity = 0;

        int filteredAvailableSeats = filteredAteliersList.stream()
                .mapToInt(a -> {
                    int inscriptionCount = inscriptionService.getByAtelier(a.getId()).size();
                    return Math.max(0, a.getParticipantMax() - inscriptionCount);
                })
                .sum();

        if (availableSeatsLabel != null) {
            availableSeatsLabel.setText(String.valueOf(filteredAvailableSeats));
        }

        updateDetailedChart(allAteliers);

        totalAteliersLabel.setText(String.valueOf(totalAteliers));
        totalParticipantsLabel.setText(String.valueOf(totalInscriptions));

        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        long ateliersCeMois = allAteliers.stream()
                .filter(atelier -> {
                    Calendar atelierCal = Calendar.getInstance();
                    atelierCal.setTime(atelier.getDate());
                    return atelierCal.get(Calendar.MONTH) == currentMonth &&
                           atelierCal.get(Calendar.YEAR) == currentYear;
                })
                .count();

        ateliersMoisLabel.setText(String.valueOf(ateliersCeMois));
        tauxParticipationLabel.setText(String.format("%.1f%%", tauxRemplissage));

        Optional<Atelier> mostPopular = allAteliers.stream()
                .max(Comparator.comparingInt(a -> inscriptionService.getByAtelier(a.getId()).size()));

        atelierPopulaireLabel.setText(mostPopular.isPresent() ?
                mostPopular.get().getTitre() : "-");

        double avgParticipants = allAteliers.isEmpty() ? 0 :
                allAteliers.stream().mapToInt(Atelier::getParticipantMax).average().orElse(0);
        moyenneParticipantsLabel.setText(String.format("%.1f", avgParticipants));

        Map<String, Long> dayCount = allAteliers.stream()
                .collect(Collectors.groupingBy(atelier -> {
                    Calendar atelierCal = Calendar.getInstance();
                    atelierCal.setTime(atelier.getDate());
                    return getDayOfWeek(atelierCal.get(Calendar.DAY_OF_WEEK));
                }, Collectors.counting()));

        Optional<Map.Entry<String, Long>> mostPopularDay = dayCount.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        jourPopulaireLabel.setText(mostPopularDay.isPresent() ?
                mostPopularDay.get().getKey() : "-");

        Map<String, Long> locationCount = allAteliers.stream()
                .collect(Collectors.groupingBy(Atelier::getLieu, Collectors.counting()));

        Optional<Map.Entry<String, Long>> mostFrequentLocation = locationCount.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        lieuFrequentLabel.setText(mostFrequentLocation.isPresent() ?
                mostFrequentLocation.get().getKey() : "-");
    }

    private String getDayOfWeek(int day) {
        switch (day) {
            case Calendar.MONDAY: return "Lundi";
            case Calendar.TUESDAY: return "Mardi";
            case Calendar.WEDNESDAY: return "Mercredi";
            case Calendar.THURSDAY: return "Jeudi";
            case Calendar.FRIDAY: return "Vendredi";
            case Calendar.SATURDAY: return "Samedi";
            case Calendar.SUNDAY: return "Dimanche";
            default: return "Inconnu";
        }
    }

    private void updateDetailedChart(List<Atelier> ateliers) {
        Map<String, Long> monthlyStats = ateliers.stream()
                .collect(Collectors.groupingBy(atelier -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(atelier.getDate());
                    return getMonth(cal.get(Calendar.MONTH));
                }, Collectors.counting()));

        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();
        monthlyStats.forEach((month, count) -> {
            chartData.add(new PieChart.Data(month + ": " + count, count));
        });

        if (detailedStatsChart != null) {
            detailedStatsChart.setData(chartData);
        }
    }

    private String getMonth(int month) {
        String[] months = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                         "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return months[month];
    }

    @FXML
    private void ajouterAtelier() {
        switchScene("/AjouterAtelier.fxml", "Ajouter Atelier");
    }

    @FXML
    private void openPublicInterface() {
        switchScene("/FrontendAtelier.fxml", "Interface Publique");
    }

    @FXML
    private void modifierAtelier() {
        Atelier selectedAtelier = getSelectedAtelier();
        if (selectedAtelier != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierAtelier.fxml"));
                AnchorPane modifierView = loader.load();

                ModifierAtelierController controller = loader.getController();
                controller.setAtelier(selectedAtelier);
                controller.setParentController(this);

                workshopsListView.getScene().setRoot(modifierView);

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner un atelier à modifier.");
        }
    }

    @FXML
    private void supprimerAtelier() {
        Atelier selectedAtelier = getSelectedAtelier();
        if (selectedAtelier == null) {
            showAlert(Alert.AlertType.WARNING, "Aucun atelier sélectionné", "Veuillez sélectionner un atelier à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cet atelier ?");
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                atelierService.supprimer(selectedAtelier.getId());
                refreshAteliers();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Atelier supprimé avec succès.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de l'atelier.");
            }
        }
    }

    @FXML
    public void voirDetailAtelier(Atelier atelier) {
        if (atelier == null) {
            showAlert(Alert.AlertType.WARNING, "Aucun atelier sélectionné", "Veuillez sélectionner un atelier pour voir ses détails.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsAtelier.fxml"));
            AnchorPane detailsView = loader.load();

            DetailsAtelierController controller = loader.getController();
            controller.setAtelier(atelier);

            Stage stage = new Stage();
            stage.setScene(new Scene(detailsView));
            stage.setTitle("Détails de l'Atelier");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails de l'atelier: " + e.getMessage());
        }
    }

    @FXML
    public void filterByParticipants() {
        sortOrderChoice.setValue("Participants (décroissant)");
    }

    @FXML
    public void resetFilters() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        fromDate = null;
        toDate = null;
        searchField.setText("");

        sortOrderChoice.setValue("Date (récent)");

        currentPage = 0;

        applyPaginationAndFilters();
    }

    @FXML
    public void refreshTable() {
        applyPaginationAndFilters();
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) workshopsListView.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la scène: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void refreshAteliers() {
        loadAteliers();
    }

    public void refreshCards() {
        refreshAteliers();
        // Make sure we're using the ListView display method
        displayAteliersInListView();
    }

    private Atelier getSelectedAtelier() {
        return workshopsListView.getSelectionModel().getSelectedItem();
    }

    public void handleAfficherAtelier() {
        // TODO: Implement logic for displaying atelier details if needed
    }
}
