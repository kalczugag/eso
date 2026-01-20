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

/**
 * Kontroler okna dialogowego wyświetlającego raport ucznia.
 * Prezentuje dane statystyczne w formie tekstowej oraz wykresu słupkowego.
 * Odpowiada również za wizualizację postępu (pasek postępu) i kolorowanie ostrzeżeń.
 */
public class ReportDialogController {

    /** Etykieta z datą wygenerowania raportu. */
    @FXML private Label dateLabel;

    /** Etykieta wyświetlająca średnią ogólną. */
    @FXML private Label avgLabel;

    /** Pasek postępu wizualizujący średnią (skala 0-6). */
    @FXML private ProgressBar avgProgressBar;

    /** Etykieta liczby ocen niedostatecznych. */
    @FXML private Label failingLabel;

    /** Etykieta nazwy najlepszego przedmiotu. */
    @FXML private Label bestSubjectNameLabel;

    /** Etykieta średniej najlepszego przedmiotu. */
    @FXML private Label bestSubjectScoreLabel;

    /** Etykieta całkowitej liczby ocen. */
    @FXML private Label totalGradesLabel;

    /** Wykres słupkowy prezentujący średnie z poszczególnych przedmiotów. */
    @FXML private BarChart<String, Number> gradesChart;

    /** Lista szczegółowa ze średnimi (tekstowa). */
    @FXML private ListView<String> detailsList;

    /** Przechowywany obiekt z danymi raportu. */
    private ReportData data;

    /**
     * Ustawia dane raportu w kontrolerze i aktualizuje widok.
     * Dynamicznie zmienia kolory paska postępu i etykiet w zależności od wyników
     * (np. czerwony dla zagrożeń, zielony dla wysokiej średniej).
     *
     * @param data Obiekt {@link ReportData} wygenerowany przez ReportService.
     */
    public void setData(ReportData data) {
        this.data = data;

        dateLabel.setText("Data generowania: " + LocalDate.now());
        avgLabel.setText(String.format("%.2f", data.globalAverage));

        // Ustawienie paska postępu (wartość 0.0 - 1.0, gdzie 1.0 to średnia 6.0)
        avgProgressBar.setProgress(data.globalAverage / 6.0);

        // Zmiana koloru paska w zależności od średniej
        if (data.globalAverage < 2.0) avgProgressBar.setStyle("-fx-accent: #EF4444;"); // Czerwony
        else if (data.globalAverage >= 4.75) avgProgressBar.setStyle("-fx-accent: #10B981;"); // Zielony (pasek)
        else avgProgressBar.setStyle("-fx-accent: #3B82F6;"); // Niebieski

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

    /**
     * Pomocnicza metoda wypełniająca wykres słupkowy danymi.
     *
     * @param averages Mapa średnich z przedmiotów.
     */
    private void populateChart(Map<String, Double> averages) {
        gradesChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Średnia");

        for (Map.Entry<String, Double> entry : averages.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        gradesChart.getData().add(series);
    }

    /**
     * Obsługa przycisku eksportu do pliku.
     * Wywołuje odpowiednią metodę z klasy ReportService.
     */
    @FXML
    private void exportToFile() {
        if (data != null) {
            Stage stage = (Stage) dateLabel.getScene().getWindow();
            ReportService.saveReportToFile(data, stage);
        }
    }
}