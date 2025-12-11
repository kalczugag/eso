package com.example.elektroniczny_dziennik;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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