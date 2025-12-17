package com.example.elektroniczny_dziennik;

import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import java.io.IOException;

import java.sql.Date;
import java.sql.SQLException;

public class gradesController {
    @FXML private TableView<StudentGradeView> gradesTable;
    @FXML private TableColumn<StudentGradeView, String> subjectCol;
    @FXML private TableColumn<StudentGradeView, Double> gradeCol;
    @FXML private TableColumn<StudentGradeView, String> descCol;
    @FXML private TableColumn<StudentGradeView, Date> dateCol;
    @FXML private Label averageLabel;

    private int studentId;

    @FXML
    void initialize() {
        subjectCol.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        try {
            getStudentId();
            loadGradesAndStats();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Błąd pobierania danych: " + e.getMessage());
        }
    }
    @FXML
    void generateReport() {
        var items = gradesTable.getItems();
        if (items.isEmpty()) return;

        ReportData data = ReportService.generateReportData(items);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("reportView.fxml"));
            Parent root = loader.load();

            ReportDialogController controller = loader.getController();
            controller.setData(data);

            Stage stage = new Stage();
            stage.setTitle("Raport Ucznia");
            stage.setScene(new Scene(root));

            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(gradesTable.getScene().getWindow());

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showReportDialog(String reportText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Raport Statystyczny");
        alert.setHeaderText("Podsumowanie Twoich wyników");

        TextArea textArea = new TextArea(reportText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane content = new GridPane();
        content.setMaxWidth(Double.MAX_VALUE);
        content.add(textArea, 0, 0);

        alert.getDialogPane().setContent(content);

        ButtonType saveButtonType = new ButtonType("Zapisz do pliku .txt");
        ButtonType closeButtonType = new ButtonType("Zamknij", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveButtonType, closeButtonType);

        alert.showAndWait().ifPresent(type -> {
            if (type == saveButtonType) {
                Stage stage = (Stage) gradesTable.getScene().getWindow();
                ReportService.saveReport(reportText, stage);
            }
        });
    }

    private void getStudentId() throws SQLException {
        try (var conn = Database.getConnection()) {
            var statement = conn.prepareStatement(
                    "SELECT s.id FROM student AS s JOIN user AS u ON u.id = s.user_id WHERE u.id = ?"
            );
            statement.setString(1, User.id);
            var result = statement.executeQuery();

            if (result.next()) {
                studentId = result.getInt("id");
            }
        }
    }

    private void loadGradesAndStats() throws SQLException {
        ObservableList<StudentGradeView> gradesList = FXCollections.observableArrayList();
        double sum = 0;
        int count = 0;

        try (var conn = Database.getConnection()) {
            var statement = conn.prepareStatement(
                    "SELECT g.grade, g.description, g.date, sb.name " +
                            "FROM grades AS g " +
                            "JOIN student AS s ON s.id = g.student_id " +
                            "JOIN subjects AS sb ON sb.id = g.subject_id " +
                            "WHERE s.id = ? " +
                            "ORDER BY g.date DESC"
            );
            statement.setInt(1, studentId);
            var result = statement.executeQuery();

            while (result.next()) {
                double gradeValue = result.getDouble("grade");
                String subject = result.getString("name");
                String desc = result.getString("description");
                String dateStr = result.getString("date");
                Date date = null;
                try {
                    date = Date.valueOf(dateStr);
                } catch (Exception e) {
                    date = new Date(System.currentTimeMillis());
                }

                gradesList.add(new StudentGradeView(subject, gradeValue, desc, date));

                sum += gradeValue;
                count++;
            }
        }

        gradesTable.setItems(gradesList);

        if (count > 0) {
            double average = sum / count;
            averageLabel.setText(String.format("Średnia ocen: %.2f", average));
        } else {
            averageLabel.setText("Brak ocen");
        }
    }
}