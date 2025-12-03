package com.example.elektroniczny_dziennik;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.DefaultStringConverter;

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

    private User user;

    public void setUser(User user){
        this.user = user;
        loadAllSubjects();
    }

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
    private TextField newGradeInput;
    @FXML
    private TextField descriptionInput;

    private Map<String, Integer> studentNameIdMap = new HashMap<>();
    private Map<String, Integer> subjectNameIdMap = new HashMap<>();
    private int selectedStudentId = -1;

    @FXML
    public void initialize() {
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        gradeCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        descriptionCol.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));

        gradeCol.setOnEditCommit(event -> {
            Grade gradeToUpdate = event.getRowValue();
            gradeToUpdate.setValue(event.getNewValue());
            updateGradeInDatabase(gradeToUpdate);
        });

        descriptionCol.setOnEditCommit(event -> {
            Grade gradeToUpdate = event.getRowValue();
            gradeToUpdate.setDescription(event.getNewValue());
            updateGradeInDatabase(gradeToUpdate);
        });

        loadAllStudentsForAutocomplete();

        studentSearchField.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try{
                    handleStudentSelection(newVal);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        subjectComboBox.setOnAction(e -> loadGrades());
    }

    private void updateGradeInDatabase(Grade grade) {
        String sql = "UPDATE grades SET grade = ?, description = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, grade.getValue());
            stmt.setString(2, grade.getDescription());
            stmt.setInt(3, grade.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Błąd Aktualizacji", "Nie udało się zaktualizować oceny w bazie danych.");
            loadGrades();
        }
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
        String sql;

        try (Connection conn = Database.getConnection()){
            sql = "SELECT s.id, s.name FROM subjects AS s, teacher_subject AS ts WHERE s.id = ts.subject_id AND ts.teacher_id = ?";
            var stmt = conn.prepareStatement(sql);
            stmt.setInt(1, getTeacherId());

            if(this.user.getRole().equals("admin")){
                sql = "SELECT id, name FROM subjects";
                stmt = conn.prepareStatement(sql);
            }

            ResultSet rs = stmt.executeQuery();

            subjectNameIdMap.clear();
            ObservableList<String> subjects = FXCollections.observableArrayList();
            while (rs.next()) {
                System.out.println("test");
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

    private void handleStudentSelection(String selectedName) throws SQLException {
        Integer userId = studentNameIdMap.get(selectedName);
        if (userId != null) {
            try(var conn = Database.getConnection()){
                var statement = conn.prepareStatement("SELECT s.id FROM student AS s" +
                                " JOIN user AS u ON u.id = s.user_id" +
                                " WHERE u.id = ?"
                );

                statement.setInt(1, userId);

                var result = statement.executeQuery();

                if(!result.next()){
                    System.out.println("To nie jest student");
                } else {
                    selectedStudentId = result.getInt("id");
                }
            }
            catch (SQLException e){
                System.out.println(e);
            }


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
                        Date.valueOf(rs.getString("date"))
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
            showAlert(Alert.AlertType.WARNING, "Brak Danych", "Musisz wybrać studenta, przedmiot oraz wpisać ocenę.");
            return;
        }

        double gradeValue;
        // DODAC TUTAJ ZEBY OCENA BYŁA Z ZAKRESU 2-6 I ZWIEKSZALA SIE TYLKO CO 0.5
        try {
            gradeValue = Double.parseDouble(gradeText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Błędny Format", "Ocena musi być liczbą (np. 4.5).");
            return;
        }

        String sql = "INSERT INTO grades (student_id, subject_id, grade, description, date) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selectedStudentId);
            stmt.setInt(2, selectedSubjectId);
            stmt.setDouble(3, gradeValue);
            stmt.setString(4, description);
            stmt.setString(5, LocalDate.now().toString());

            stmt.executeUpdate();

            newGradeInput.clear();
            descriptionInput.clear();
            loadGrades();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Błąd Zapisu", "Nie udało się zapisać oceny.");
        }
    }

    public int getTeacherId(){
        int teacherId = -1;

        try(Connection conn = Database.getConnection()){
            var stmt = conn.prepareStatement("SELECT id FROM teacher WHERE user_id = ?");
            stmt.setInt(1, Integer.parseInt(User.id));

            var result = stmt.executeQuery();

            if(result.next()) teacherId = result.getInt("id");
        }
        catch (SQLException e){
            e.printStackTrace();
        }

        return teacherId;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}