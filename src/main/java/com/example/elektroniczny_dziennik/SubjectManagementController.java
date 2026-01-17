package com.example.elektroniczny_dziennik;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckComboBox;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SubjectManagementController {

    @FXML private TableView<Subject> subjectsTable;
    @FXML private TableColumn<Subject, Integer> idColumn;
    @FXML private TableColumn<Subject, String> nameColumn;

    @FXML private TextField newSubjectName;
    @FXML private Label selectedSubjectLabel;
    @FXML private VBox teachersContainer;
    @FXML private Button saveTeachersBtn;
    @FXML private Label infoLabel;

    private CheckComboBox<String> teachersCheckComboBox;

    private Map<String, Integer> teacherMap = new HashMap<>();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        subjectsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handleSubjectSelect(newVal);
            } else {
                clearSelection();
            }
        });

        teachersCheckComboBox = new CheckComboBox<>();
        teachersCheckComboBox.setMaxWidth(Double.MAX_VALUE);
        teachersContainer.getChildren().add(teachersCheckComboBox);

        loadSubjects();
        loadTeachersList();
    }

    private void loadSubjects() {
        ObservableList<Subject> list = FXCollections.observableArrayList();
        try (var conn = Database.getConnection()) {
            var rs = conn.createStatement().executeQuery("SELECT id, name FROM subjects");
            while (rs.next()) {
                list.add(new Subject(rs.getInt("id"), rs.getString("name")));
            }
            subjectsTable.setItems(list);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void addSubject() {
        String name = newSubjectName.getText().trim();
        if (name.isEmpty()) {
            showAlert("Wpisz nazwę przedmiotu."); return;
        }

        try (var conn = Database.getConnection()) {
            var stmt = conn.prepareStatement("INSERT INTO subjects (name) VALUES (?)");
            stmt.setString(1, name);
            stmt.executeUpdate();

            newSubjectName.clear();
            loadSubjects();
            infoLabel.setText("Dodano przedmiot: " + name);
            infoLabel.setStyle("-fx-text-fill: green;");
        } catch (SQLException e) {
            showAlert("Błąd: " + e.getMessage());
        }
    }

    @FXML
    public void deleteSubject() {
        Subject selected = subjectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Usunąć przedmiot " + selected.getName() + "?\nUWAGA: Jeśli istnieją oceny z tego przedmiotu, operacja się nie uda.", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try (var conn = Database.getConnection()) {
                    var stmt1 = conn.prepareStatement("DELETE FROM teacher_subject WHERE subject_id = ?");
                    stmt1.setInt(1, selected.getId());
                    stmt1.executeUpdate();

                    var stmt2 = conn.prepareStatement("DELETE FROM subjects WHERE id = ?");
                    stmt2.setInt(1, selected.getId());
                    stmt2.executeUpdate();

                    loadSubjects();
                    clearSelection();
                } catch (SQLException e) {
                    showAlert("Nie można usunąć przedmiotu. Prawdopodobnie istnieją przypisane oceny w dzienniku.");
                }
            }
        });
    }

    private void loadTeachersList() {
        teachersCheckComboBox.getItems().clear();
        teacherMap.clear();

        try (var conn = Database.getConnection()) {
            String sql = "SELECT t.id, u.first_name, u.last_name FROM teacher t JOIN user u ON t.user_id = u.id";
            var rs = conn.createStatement().executeQuery(sql);

            while (rs.next()) {
                int tid = rs.getInt("id");
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");

                teacherMap.put(fullName, tid);
                teachersCheckComboBox.getItems().add(fullName);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void handleSubjectSelect(Subject subject) {
        selectedSubjectLabel.setText(subject.getName());
        saveTeachersBtn.setDisable(false);
        infoLabel.setText("");

        teachersCheckComboBox.getCheckModel().clearChecks();

        try (var conn = Database.getConnection()) {
            String sql = "SELECT t.id, u.first_name, u.last_name FROM teacher_subject ts " +
                    "JOIN teacher t ON ts.teacher_id = t.id " +
                    "JOIN user u ON t.user_id = u.id " +
                    "WHERE ts.subject_id = ?";

            var stmt = conn.prepareStatement(sql);
            stmt.setInt(1, subject.getId());
            var rs = stmt.executeQuery();

            while(rs.next()) {
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                teachersCheckComboBox.getCheckModel().check(fullName);
            }

        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void saveTeachersAssignment() {
        Subject subject = subjectsTable.getSelectionModel().getSelectedItem();
        if (subject == null) return;

        ObservableList<String> selectedNames = teachersCheckComboBox.getCheckModel().getCheckedItems();

        try (var conn = Database.getConnection()) {
            var deleteStmt = conn.prepareStatement("DELETE FROM teacher_subject WHERE subject_id = ?");
            deleteStmt.setInt(1, subject.getId());
            deleteStmt.executeUpdate();

            var insertStmt = conn.prepareStatement("INSERT INTO teacher_subject (teacher_id, subject_id) VALUES (?, ?)");

            for (String teacherName : selectedNames) {
                int teacherId = teacherMap.get(teacherName);
                insertStmt.setInt(1, teacherId);
                insertStmt.setInt(2, subject.getId());
                insertStmt.executeUpdate();
            }

            infoLabel.setText("Zapisano przydział dla: " + subject.getName());
            infoLabel.setStyle("-fx-text-fill: green;");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Błąd zapisu: " + e.getMessage());
        }
    }

    private void clearSelection() {
        selectedSubjectLabel.setText("(Wybierz z listy obok)");
        saveTeachersBtn.setDisable(true);
        teachersCheckComboBox.getCheckModel().clearChecks();
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}