package com.example.elektroniczny_dziennik;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class GradeEntryController {

    @FXML
    private JFXComboBox<String> studentSearchField;
    @FXML
    private JFXComboBox<String> subjectComboBox;
    @FXML
    private TableView<Grade> gradesTable;
    @FXML
    private TableColumn<Grade, Double> gradeCol;
    @FXML
    private TableColumn<Grade, String> descriptionCol;
    @FXML
    private TableColumn<Grade, Date> dateCol;
    @FXML
    private JFXTextField newGradeInput;
    @FXML
    private JFXTextField descriptionInput;

    private Map<String, Integer> studentNameIdMap = new HashMap<>();
    private Map<String, Integer> subjectNameIdMap = new HashMap<>();
    private int selectedStudentId = -1;

    @FXML
    public void initialize() {
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        loadAllStudentsForAutocomplete();
        loadAllSubjects();

        studentSearchField.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handleStudentSelection(newVal);
            }
        });

        subjectComboBox.setOnAction(e -> loadGrades());
    }

    private void loadAllStudentsForAutocomplete() {
        String sql = "SELECT id, first_name, last_name FROM user WHERE role = 'student'";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            studentNameIdMap.clear();
            ObservableList<String> studentNames = FXCollections.observableArrayList();

            while (rs.next()) {
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                studentNameIdMap.put(fullName, rs.getInt("id"));
                studentNames.add(fullName);
            }

            studentSearchField.setItems(studentNames);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Błąd Bazy Danych", "Nie udało się załadować listy studentów.");
        }
    }

    private void loadAllSubjects() {
        String sql = "SELECT id, name FROM subjects";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            subjectNameIdMap.clear();
            ObservableList<String> subjects = FXCollections.observableArrayList();
            while (rs.next()) {
                String subjectName = rs.getString("name");
                subjectNameIdMap.put(subjectName, rs.getInt("id"));
                subjects.add(subjectName);
            }
            subjectComboBox.setItems(subjects);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Błąd Bazy Danych", "Nie udało się załadować listy przedmiotów.");
        }
    }

    private void handleStudentSelection(String selectedName) {
        Integer studentId = studentNameIdMap.get(selectedName);
        if (studentId != null) {
            selectedStudentId = studentId;
            loadGrades();
        } else {
            selectedStudentId = -1;
            gradesTable.getItems().clear();
        }
    }

    private void loadGrades() {
        Integer selectedSubjectId = subjectNameIdMap.get(subjectComboBox.getValue());

        if (selectedStudentId == -1 || selectedSubjectId == null) {
            gradesTable.getItems().clear();
            return;
        }

        String sql = "SELECT id, grade, description, date FROM grades WHERE student_id = ? AND subject_id = ?";
        ObservableList<Grade> grades = FXCollections.observableArrayList();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selectedStudentId);
            stmt.setInt(2, selectedSubjectId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                grades.add(new Grade(
                        rs.getInt("id"),
                        rs.getDouble("grade"),
                        rs.getString("description"),
                        rs.getDate("date")
                ));
            }
            gradesTable.setItems(grades);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Błąd Bazy Danych", "Nie udało się załadować ocen.");
        }
    }

    @FXML
    private void saveGrade() {
        Integer selectedSubjectId = subjectNameIdMap.get(subjectComboBox.getValue());
        String gradeText = newGradeInput.getText();
        String description = descriptionInput.getText();

        if (selectedStudentId == -1 || selectedSubjectId == null || gradeText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "BBrak Danych", "Musisz wybrać studenta, przedmiot oraz wpisać ocenę.");
            return;
        }

        double gradeValue;
        try {
            gradeValue = Double.parseDouble(gradeText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Błędny Format", "Ocena musi być liczbą (np. 4.5).");
            return;
        }

        String sql = "INSERT INTO Grades (student_id, subject_id, grade, description, date) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selectedStudentId);
            stmt.setInt(2, selectedSubjectId);
            stmt.setDouble(3, gradeValue);
            stmt.setString(4, description);
            stmt.setDate(5, Date.valueOf(LocalDate.now()));

            stmt.executeUpdate();

            newGradeInput.clear();
            descriptionInput.clear();
            loadGrades();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Błąd Zapisu", "Nie udało się zapisać oceny.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}