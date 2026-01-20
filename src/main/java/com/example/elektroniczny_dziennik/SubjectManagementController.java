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

/**
 * Kontroler zarządzania przedmiotami (Admin).
 * Pozwala dodawać/usuwać przedmioty oraz przypisywać do nich nauczycieli
 * przy użyciu listy z wielokrotnym wyborem (CheckComboBox).
 */
public class SubjectManagementController {

    /** Tabela wyświetlająca listę przedmiotów. */
    @FXML private TableView<Subject> subjectsTable;
    @FXML private TableColumn<Subject, Integer> idColumn;
    @FXML private TableColumn<Subject, String> nameColumn;

    /** Pole tekstowe do wprowadzania nazwy nowego przedmiotu. */
    @FXML private TextField newSubjectName;

    /** Etykieta pokazująca nazwę aktualnie edytowanego przedmiotu. */
    @FXML private Label selectedSubjectLabel;

    /** Kontener na komponent CheckComboBox (lista nauczycieli). */
    @FXML private VBox teachersContainer;

    /** Przycisk zapisu przydziału nauczycieli. */
    @FXML private Button saveTeachersBtn;

    /** Etykieta informacyjna (sukces/błąd). */
    @FXML private Label infoLabel;

    /** Komponent biblioteki ControlsFX umożliwiający wybór wielu opcji z listy rozwijanej. */
    private CheckComboBox<String> teachersCheckComboBox;

    /** Mapa mapująca imię i nazwisko nauczyciela na jego ID w bazie. */
    private Map<String, Integer> teacherMap = new HashMap<>();

    /**
     * Inicjalizacja kontrolera.
     * Konfiguruje tabelę, tworzy CheckComboBox oraz ładuje dane wstępne.
     */
    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Nasłuchiwanie wyboru wiersza w tabeli
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

    /** Pobiera listę przedmiotów z bazy i wyświetla w tabeli. */
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

    /**
     * Dodaje nowy przedmiot do bazy danych.
     * Pobiera nazwę z pola tekstowego.
     */
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

    /**
     * Usuwa zaznaczony przedmiot.
     * Sprawdza, czy przedmiot ma powiązania (choć w kodzie usuwanie jest proste,
     * baza danych może wyrzucić błąd klucza obcego, jeśli są oceny).
     */
    @FXML
    public void deleteSubject() {
        Subject selected = subjectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Usunąć przedmiot " + selected.getName() + "?\nUWAGA: Jeśli istnieją oceny z tego przedmiotu, operacja się nie uda.", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try (var conn = Database.getConnection()) {
                    // Najpierw usuwamy powiązania z nauczycielami
                    var stmt1 = conn.prepareStatement("DELETE FROM teacher_subject WHERE subject_id = ?");
                    stmt1.setInt(1, selected.getId());
                    stmt1.executeUpdate();

                    // Potem usuwamy sam przedmiot
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

    /** Pobiera listę wszystkich nauczycieli do komponentu wyboru. */
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

    /**
     * Obsługuje wybór przedmiotu z tabeli.
     * Pobiera aktualnie przypisanych nauczycieli i zaznacza ich w CheckComboBox.
     * * @param subject Wybrany przedmiot.
     */
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

    /**
     * Zapisuje zmiany w przypisaniu nauczycieli do wybranego przedmiotu.
     * Usuwa stare przypisania i dodaje nowe na podstawie zaznaczeń w CheckComboBox.
     */
    @FXML
    public void saveTeachersAssignment() {
        Subject subject = subjectsTable.getSelectionModel().getSelectedItem();
        if (subject == null) return;

        ObservableList<String> selectedNames = teachersCheckComboBox.getCheckModel().getCheckedItems();

        try (var conn = Database.getConnection()) {
            // Usunięcie starych przypisań
            var deleteStmt = conn.prepareStatement("DELETE FROM teacher_subject WHERE subject_id = ?");
            deleteStmt.setInt(1, subject.getId());
            deleteStmt.executeUpdate();

            // Dodanie nowych
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

    /** Czyści interfejs po odznaczeniu przedmiotu. */
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