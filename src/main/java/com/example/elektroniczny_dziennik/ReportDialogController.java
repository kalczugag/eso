package com.example.elektroniczny_dziennik;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Map;

public class ReportDialogController {

    @FXML private Label dateLabel;
    @FXML private Label avgLabel;
    @FXML private ProgressBar avgProgressBar;

    @FXML private Label failingLabel;

    @FXML private Label bestSubjectNameLabel;
    @FXML private Label bestSubjectScoreLabel;

    @FXML private Label totalGradesLabel;

    @FXML private BarChart<String, Number> gradesChart;
    @FXML private ListView<String> detailsList;

    private ReportData data;

    public void setData(ReportData data) {
        this.data = data;

        dateLabel.setText("Data generowania: " + LocalDate.now());
        avgLabel.setText(String.format("%.2f", data.globalAverage));

        avgProgressBar.setProgress(data.globalAverage / 6.0);

        if (data.globalAverage < 2.0) avgProgressBar.setStyle("-fx-accent: #EF4444;");
        else if (data.globalAverage >= 4.75) avgProgressBar.setStyle("-fx-accent: #10B981;");
        else avgProgressBar.setStyle("-fx-accent: #3B82F6;");

        failingLabel.setText(String.valueOf(data.failingGrades));
        if (data.failingGrades > 0) failingLabel.setStyle("-fx-text-fill: #EF4444;");
        else failingLabel.setStyle("-fx-text-fill: #10B981;");


        bestSubjectNameLabel.setText(data.bestSubject);
        bestSubjectScoreLabel.setText(String.format("%.2f", data.bestAvg));

        totalGradesLabel.setText(String.valueOf(data.totalGrades));

        detailsList.getItems().clear();
        for (Map.Entry<String, Double> entry : data.averagePerSubject.entrySet()) {
            detailsList.getItems().add(entry.getKey() + ": " + String.format("%.2f", entry.getValue()));
        }

        populateChart(data.averagePerSubject);
    }

    private void populateChart(Map<String, Double> averages) {
        gradesChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Średnia");

        for (Map.Entry<String, Double> entry : averages.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        gradesChart.getData().add(series);
    }

    @FXML
    private void exportToFile() {
        if (data != null) {
            Stage stage = (Stage) dateLabel.getScene().getWindow();
            ReportService.saveReportToFile(data, stage);
        }
    }
}