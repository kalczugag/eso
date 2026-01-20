package com.example.elektroniczny_dziennik;

import com.jfoenix.controls.JFXComboBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.DoubleStringConverter;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Kontroler odpowiedzialny za moduł wprowadzania i edycji ocen.
 * Umożliwia nauczycielom (i administratorom) wybór przedmiotu, studenta,
 * a następnie dodawanie lub modyfikowanie ocen w tabeli.
 */
public class GradeEntryController {

    /** Obiekt aktualnie zalogowanego użytkownika. */
    private User user;

    /** Mapa pomocnicza mapująca imię i nazwisko studenta na jego ID w bazie. */
    private Map<String, Integer> studentNameIdMap = new HashMap<>();

    /** Mapa pomocnicza mapująca nazwę przedmiotu na jego ID w bazie. */
    private Map<String, Integer> subjectNameIdMap = new HashMap<>();

    /** ID aktualnie wybranego przedmiotu. Wartość -1 oznacza brak wyboru. */
    private int currentSubjectId = -1;

    /** ID aktualnie wybranego studenta (tabela 'student'). Wartość -1 oznacza brak wyboru konkretnego studenta. */
    private int currentStudentId = -1;

    /** Lista rozwijana do wyszukiwania/wybierania studenta. */
    @FXML private JFXComboBox<String> studentSearchField;

    /** Lista rozwijana do wyboru przedmiotu. */
    @FXML private JFXComboBox<String> subjectComboBox;

    /** Tabela wyświetlająca listę ocen. */
    @FXML private TableView<Grade> gradesTable;

    /** Kolumna tabeli zawierająca imię i nazwisko studenta. */
    @FXML private TableColumn<Grade, String> studentNameCol;

    /** Kolumna tabeli zawierająca wartość oceny. */
    @FXML private TableColumn<Grade, Double> gradeCol;

    /** Kolumna tabeli zawierająca opis oceny (np. "sprawdzian", "kartkówka"). */
    @FXML private TableColumn<Grade, String> descriptionCol;

    /** Kolumna tabeli zawierająca datę wystawienia oceny. */
    @FXML private TableColumn<Grade, Date> dateCol;

    /** Pole tekstowe do wprowadzania wartości nowej oceny. */
    @FXML private TextField newGradeInput;

    /** Pole tekstowe do wprowadzania opisu nowej oceny. */
    @FXML private TextField descriptionInput;

    /**
     * Ustawia kontekst użytkownika dla kontrolera i ładuje odpowiednie dane.
     * Wywołuje ładowanie listy przedmiotów w zależności od roli użytkownika.
     *
     * @param user Obiekt zalogowanego użytkownika.
     */
    public void setUser(User user){
        this.user = user;
        loadSubjectsForTeacher();
    }

    /**
     * Inicjalizuje kontroler, konfiguruje kolumny tabeli oraz handlery edycji.
     * Ustawia fabryki komórek dla edytowalnych kolumn (ocena, opis) oraz
     * blokuje pola wyboru do momentu załadowania danych.
     */
    @FXML
    public void initialize() {
        studentNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStudentName()));

        gradeCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        gradeCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        descriptionCol.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));

        // Obsługa edycji wartości oceny w tabeli
        gradeCol.setOnEditCommit(event -> {
            Grade g = event.getRowValue();
            g.setValue(event.getNewValue());
            updateGradeInDatabase(g);
        });

        // Obsługa edycji opisu oceny w tabeli
        descriptionCol.setOnEditCommit(event -> {
            Grade g = event.getRowValue();
            g.setDescription(event.getNewValue());
            updateGradeInDatabase(g);
        });

        subjectComboBox.setOnAction(e -> handleSubjectSelection());
        studentSearchField.setOnAction(e -> handleStudentSelection());

        studentSearchField.setDisable(true);
        loadAllStudentsList();
    }

    /**
     * Ładuje listę przedmiotów dostępnych dla danego użytkownika.
     * Administrator widzi wszystkie przedmioty, nauczyciel tylko te przypisane do niego.
     */
    private void loadSubjectsForTeacher() {
        String sql;
        try (Connection conn = Database.getConnection()){
            if(this.user.getRole().equals("admin")){
                sql = "SELECT id, name FROM subjects";
                var stmt = conn.prepareStatement(sql);
                fillSubjectCombo(stmt);
            } else {
                sql = "SELECT s.id, s.name FROM subjects s JOIN teacher_subject ts ON s.id = ts.subject_id WHERE ts.teacher_id = ?";
                var stmt = conn.prepareStatement(sql);
                stmt.setInt(1, getTeacherId());
                fillSubjectCombo(stmt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wypełnia ComboBox przedmiotami na podstawie zapytania SQL.
     * Uzupełnia również mapę pomocniczą `subjectNameIdMap`.
     *
     * @param stmt Przygotowane zapytanie SQL zwracające id i nazwę przedmiotu.
     * @throws SQLException W przypadku błędu wykonania zapytania.
     */
    private void fillSubjectCombo(PreparedStatement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery();
        subjectNameIdMap.clear();
        ObservableList<String> subjects = FXCollections.observableArrayList();
        while (rs.next()) {
            String name = rs.getString("name");
            subjectNameIdMap.put(name, rs.getInt("id"));
            subjects.add(name);
        }
        subjectComboBox.setItems(subjects);
    }

    /**
     * Pobiera listę wszystkich studentów z bazy danych i wypełnia pole wyszukiwania.
     * Dodaje opcję "--- Wszyscy ---" umożliwiającą wyświetlenie ocen wszystkich uczniów.
     */
    private void loadAllStudentsList() {
        String sql = "SELECT id, first_name, last_name FROM user WHERE role = 'student'";
        try (Connection conn = Database.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {

            studentNameIdMap.clear();
            ObservableList<String> names = FXCollections.observableArrayList();
            names.add("--- Wszyscy ---");

            while(rs.next()) {
                int userId = rs.getInt("id");
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");

                int studentTableId = getStudentIdByUserId(userId);

                studentNameIdMap.put(fullName, studentTableId);
                names.add(fullName);
            }
            studentSearchField.setItems(names);

        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Pobiera ID studenta (z tabeli 'student') na podstawie ID użytkownika (z tabeli 'user').
     *
     * @param userId ID użytkownika w tabeli user.
     * @return ID studenta lub -1, jeśli nie znaleziono.
     */
    private int getStudentIdByUserId(int userId) {
        try (var conn = Database.getConnection()) {
            var ps = conn.prepareStatement("SELECT id FROM student WHERE user_id = ?");
            ps.setInt(1, userId);
            var rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    /**
     * Obsługuje zdarzenie wyboru przedmiotu z listy rozwijanej.
     * Odblokowuje pole wyboru studenta i ładuje oceny dla wybranego przedmiotu.
     */
    private void handleSubjectSelection() {
        String selectedSubject = subjectComboBox.getValue();
        if (selectedSubject != null) {
            this.currentSubjectId = subjectNameIdMap.get(selectedSubject);
            studentSearchField.setDisable(false);
            loadGrades();
        }
    }

    /**
     * Obsługuje zdarzenie wyboru studenta z listy.
     * Aktualizuje ID bieżącego studenta i przeładowuje tabelę ocen.
     */
    private void handleStudentSelection() {
        String selectedName = studentSearchField.getValue();
        if (selectedName == null || selectedName.equals("--- Wszyscy ---") || selectedName.isEmpty()) {
            this.currentStudentId = -1;
        } else {
            this.currentStudentId = studentNameIdMap.get(selectedName);
        }
        loadGrades();
    }

    /**
     * Pobiera oceny z bazy danych na podstawie wybranych filtrów (przedmiot, student).
     * Wypełnia tabelę wynikami zapytania.
     */
    private void loadGrades() {
        if (currentSubjectId == -1) return;

        ObservableList<Grade> gradesList = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder(
                "SELECT g.id, g.grade, g.description, g.date, u.first_name, u.last_name " +
                        "FROM grades g " +
                        "JOIN student s ON g.student_id = s.id " +
                        "JOIN user u ON s.user_id = u.id " +
                        "WHERE g.subject_id = ? "
        );

        if (currentStudentId != -1) {
            sql.append("AND g.student_id = ? ");
        }

        sql.append("ORDER BY g.date DESC");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setInt(1, currentSubjectId);
            if (currentStudentId != -1) {
                stmt.setInt(2, currentStudentId);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String studentName = rs.getString("first_name") + " " + rs.getString("last_name");
                Grade g = new Grade(
                        rs.getInt("id"),
                        rs.getDouble("grade"),
                        rs.getString("description"),
                        Date.valueOf(rs.getString("date"))
                );
                g.setStudentName(studentName);
                gradesList.add(g);
            }

            gradesTable.setItems(gradesList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Zapisuje nową ocenę do bazy danych.
     * Pobiera dane z pól tekstowych, waliduje je i wykonuje insert SQL.
     * W przypadku błędnych danych wyświetla odpowiedni komunikat.
     */
    @FXML
    private void saveGrade() {
        if (currentSubjectId == -1) {
            showAlert(Alert.AlertType.WARNING, "Błąd", "Wybierz przedmiot!");
            return;
        }
        if (currentStudentId == -1) {
            showAlert(Alert.AlertType.WARNING, "Błąd", "Aby dodać ocenę, musisz wybrać konkretnego studenta z listy!");
            return;
        }

        String gradeText = newGradeInput.getText();
        String desc = descriptionInput.getText();

        if (gradeText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Błąd", "Wpisz ocenę!");
            return;
        }

        try {
            double val = Double.parseDouble(gradeText);

            String sql = "INSERT INTO grades (student_id, subject_id, grade, description, date) VALUES (?, ?, ?, ?, ?)";
            try (var conn = Database.getConnection(); var stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, currentStudentId);
                stmt.setInt(2, currentSubjectId);
                stmt.setDouble(3, val);
                stmt.setString(4, desc);
                stmt.setString(5, LocalDate.now().toString());
                stmt.executeUpdate();

                newGradeInput.clear();
                descriptionInput.clear();
                loadGrades();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Niepoprawny format oceny");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Aktualizuje istniejącą ocenę w bazie danych.
     * Wywoływana po edycji komórki w tabeli ocen.
     *
     * @param grade Obiekt oceny ze zaktualizowanymi wartościami.
     */
    private void updateGradeInDatabase(Grade grade) {
        String sql = "UPDATE grades SET grade = ?, description = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, grade.getValue());
            stmt.setString(2, grade.getDescription());
            stmt.setInt(3, grade.getId());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Pobiera ID nauczyciela z tabeli 'teacher' na podstawie statycznego ID zalogowanego użytkownika.
     *
     * @return ID nauczyciela lub -1 w przypadku błędu.
     */
    public int getTeacherId(){
        int teacherId = -1;
        try(Connection conn = Database.getConnection()){
            var stmt = conn.prepareStatement("SELECT id FROM teacher WHERE user_id = ?");
            stmt.setInt(1, Integer.parseInt(User.id));
            var result = stmt.executeQuery();
            if(result.next()) teacherId = result.getInt("id");
        } catch (SQLException e){ e.printStackTrace(); }
        return teacherId;
    }

    /**
     * Wyświetla okno dialogowe z komunikatem (Alert).
     *
     * @param type Typ alertu (np. WARNING, ERROR).
     * @param title Tytuł okna alertu.
     * @param content Treść komunikatu.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}