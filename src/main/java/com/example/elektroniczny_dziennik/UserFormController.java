package com.example.elektroniczny_dziennik;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Kontroler okna dialogowego formularza użytkownika.
 * Obsługuje zarówno tworzenie nowego konta, jak i edycję istniejącego.
 * Zawiera logikę generowania unikalnych loginów oraz przypisywania danych specyficznych dla ról.
 */
public class UserFormController {
    @FXML private TextField firstNameInput;
    @FXML private TextField lastNameInput;
    @FXML private ComboBox<String> roleComboBox;

    /** Kontener na listę przedmiotów (widoczny tylko gdy rola = nauczyciel). */
    @FXML private VBox subjectContainer;

    /** Komponent wielokrotnego wyboru przedmiotów dla nauczyciela. */
    private CheckComboBox<String> subjectCheckComboBox;

    @FXML private PasswordField passwordInput;
    @FXML private PasswordField confirmPasswordInput;
    @FXML private Label titleLabel;
    @FXML private Button actionButton;
    @FXML private Label infoLabel;

    /** ID edytowanego użytkownika (-1 oznacza tryb tworzenia nowego). */
    private int editingUserId = -1;
    private String originalRole = "";
    private int currentLoggedInUserId = -1;

    /**
     * Inicjalizacja formularza.
     * Ustawia listę ról, dodaje listener widoczności dla kontenera przedmiotów
     * oraz ładuje listę dostępnych przedmiotów.
     */
    @FXML
    public void initialize() {
        subjectCheckComboBox = new CheckComboBox<>();
        subjectCheckComboBox.setMaxWidth(Double.MAX_VALUE);

        subjectContainer.getChildren().add(subjectCheckComboBox);

        roleComboBox.getItems().addAll("student", "nauczyciel", "admin");

        // Pokazuj listę przedmiotów tylko jeśli wybrano rolę nauczyciela
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isTeacher = "nauczyciel".equals(newVal);
            subjectContainer.setVisible(isTeacher);
            subjectContainer.setManaged(isTeacher);
        });

        loadSubjects();

        subjectContainer.setVisible(false);
        subjectContainer.setManaged(false);
    }

    /** Ustawia ID zalogowanego admina (zabezpieczenie przed edycją własnej roli). */
    public void setLoggedInUser(int id) {
        this.currentLoggedInUserId = id;
    }

    /** Konfiguruje formularz w trybie tworzenia nowego użytkownika. */
    public void setCreateMode() {
        titleLabel.setText("Nowy Użytkownik");
        actionButton.setText("Utwórz");
        roleComboBox.setValue("student");
    }

    /**
     * Konfiguruje formularz w trybie edycji istniejącego użytkownika.
     * Wypełnia pola danymi.
     * * @param data Lista danych użytkownika (id, imię, nazwisko, login, rola).
     */
    public void setEditData(ObservableList<String> data) {
        editingUserId = Integer.parseInt(data.get(0));
        firstNameInput.setText(data.get(1));
        lastNameInput.setText(data.get(2));
        originalRole = data.get(4);
        roleComboBox.setValue(originalRole);

        titleLabel.setText("Edycja Użytkownika");
        actionButton.setText("Zapisz Zmiany");
        passwordInput.setPromptText("Zostaw puste, aby nie zmieniać");

        if (editingUserId == currentLoggedInUserId) {
            roleComboBox.setDisable(true);
            infoLabel.setText("Nie możesz zmienić roli samemu sobie!");
        }

        if ("nauczyciel".equals(originalRole)) {
            loadTeacherSubjects(editingUserId);
        }
    }

    /** Ładuje wszystkie dostępne przedmioty do CheckComboBoxa. */
    private void loadSubjects() {
        try (var conn = Database.getConnection()) {
            var rs = conn.createStatement().executeQuery("SELECT name FROM subjects");
            while (rs.next()) {
                subjectCheckComboBox.getItems().add(rs.getString("name"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /** Zaznacza przedmioty, których już uczy dany nauczyciel. */
    private void loadTeacherSubjects(int userId) {
        try (var conn = Database.getConnection()) {
            String sql = "SELECT s.name FROM subjects s " +
                    "JOIN teacher_subject ts ON ts.subject_id = s.id " +
                    "JOIN teacher t ON t.id = ts.teacher_id " +
                    "WHERE t.user_id = ?";
            var stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            var rs = stmt.executeQuery();

            while (rs.next()) {
                String subjectName = rs.getString("name");
                subjectCheckComboBox.getCheckModel().check(subjectName);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Główna metoda obsługująca przycisk akcji (Utwórz/Zapisz).
     * Wykonuje walidację pól, a następnie wywołuje odpowiednie metody bazy danych.
     */
    @FXML
    public void handleAction() {
        String fname = firstNameInput.getText();
        String lname = lastNameInput.getText();
        String role = roleComboBox.getValue();
        String pass = passwordInput.getText();
        String conf = confirmPasswordInput.getText();

        ObservableList<String> selectedSubjects = subjectCheckComboBox.getCheckModel().getCheckedItems();

        if (fname.isEmpty() || lname.isEmpty() || role == null) {
            infoLabel.setText("Wypełnij wymagane pola."); return;
        }

        if ("nauczyciel".equals(role) && selectedSubjects.isEmpty()) {
            infoLabel.setText("Nauczyciel musi mieć przypisany przynajmniej jeden przedmiot."); return;
        }

        if (editingUserId == -1 && pass.isEmpty()) {
            infoLabel.setText("Hasło jest wymagane dla nowego konta."); return;
        }

        if (!pass.isEmpty() && !pass.equals(conf)) {
            infoLabel.setText("Hasła się nie zgadzają."); return;
        }

        // Zabezpieczenie przed przypadkowym usunięciem innego admina
        if (editingUserId != -1 && "admin".equals(originalRole) && !"admin".equals(role)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Czy na pewno chcesz odebrać uprawnienia Administratora? Stracisz dostęp.",
                    ButtonType.YES, ButtonType.NO);
            if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.NO) return;
        }

        try (var conn = Database.getConnection()) {
            if (editingUserId == -1) {
                createNewUser(conn, fname, lname, role, pass, selectedSubjects);
            } else {
                updateUser(conn, fname, lname, role, pass, selectedSubjects);
            }
            ((Stage) firstNameInput.getScene().getWindow()).close();
        } catch (SQLException e) {
            e.printStackTrace();
            infoLabel.setText("Błąd bazy danych: " + e.getMessage());
        }
    }

    /**
     * Tworzy nowego użytkownika w bazie.
     * Generuje unikalny login i haszuje hasło.
     */
    private void createNewUser(Connection conn, String f, String l, String role, String pass, ObservableList<String> subjects) throws SQLException {
        String login = getUniqueLogin(conn, f, l, role);
        String hash = BCrypt.hashpw(pass, BCrypt.gensalt());

        var stmt = conn.prepareStatement("INSERT INTO user (first_name, last_name, login, password, role) VALUES (?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
        stmt.setString(1, f); stmt.setString(2, l); stmt.setString(3, login); stmt.setString(4, hash); stmt.setString(5, role);
        stmt.executeUpdate();

        int uid = -1;
        var keys = stmt.getGeneratedKeys();
        if (keys.next()) uid = keys.getInt(1);

        assignRoleData(conn, uid, role, subjects);
    }

    /**
     * Aktualizuje dane istniejącego użytkownika.
     * Jeśli zmieniono rolę, usuwa stare dane specyficzne dla roli i tworzy nowe.
     */
    private void updateUser(Connection conn, String f, String l, String role, String pass, ObservableList<String> subjects) throws SQLException {
        String sql = pass.isEmpty() ?
                "UPDATE user SET first_name=?, last_name=?, role=? WHERE id=?" :
                "UPDATE user SET first_name=?, last_name=?, role=?, password=? WHERE id=?";
        var stmt = conn.prepareStatement(sql);
        stmt.setString(1, f); stmt.setString(2, l); stmt.setString(3, role);
        if (pass.isEmpty()) stmt.setInt(4, editingUserId);
        else { stmt.setString(4, BCrypt.hashpw(pass, BCrypt.gensalt())); stmt.setInt(5, editingUserId); }
        stmt.executeUpdate();

        if (!role.equals(originalRole)) {
            removeRoleData(conn, editingUserId, originalRole);
            assignRoleData(conn, editingUserId, role, subjects);
        } else if ("nauczyciel".equals(role)) {
            updateTeacherSubjects(conn, editingUserId, subjects);
        }
    }

    /**
     * Generuje login wg schematu: prefiks (s/n/a) + 3 litery imienia + 3 nazwiska.
     * Obsługuje konflikty poprzez dodanie numerka.
     */
    private String getUniqueLogin(Connection conn, String firstName, String lastName, String role) throws SQLException {
        String prefix = "s";
        if ("nauczyciel".equals(role)) prefix = "n";
        else if ("admin".equals(role)) prefix = "a";

        String loginBase = prefix + firstName.substring(0, Math.min(3, firstName.length())).toLowerCase() + lastName.substring(0, Math.min(3, lastName.length())).toLowerCase();

        var statement = conn.prepareStatement("SELECT login FROM user WHERE login LIKE ?");
        statement.setString(1, loginBase + "%");
        var result = statement.executeQuery();
        int counter = 0;
        while(result.next()){
            String existingLogin = result.getString("login");
            if(existingLogin.matches(loginBase + "(\\d+)$")){
                int number = Integer.parseInt(existingLogin.replace(loginBase, ""));
                if(number > counter) counter = number;
                else if (number == counter) counter += 1;
            } else if (existingLogin.equals(loginBase)) counter = 1;
        }
        if(counter == 1) loginBase += Integer.toString(counter);
        else if (counter > 1) loginBase += Integer.toString(counter);
        return loginBase;
    }

    /**
     * Przypisuje odpowiednie rekordy do tabel 'student' lub 'teacher' w zależności od roli.
     */
    private void assignRoleData(Connection conn, int uid, String role, ObservableList<String> subjects) throws SQLException {
        if ("student".equals(role)) {
            var s = conn.prepareStatement("INSERT INTO student (class, user_id) VALUES ('3A', ?)");
            s.setInt(1, uid);
            s.executeUpdate();
        } else if ("nauczyciel".equals(role)) {
            var t = conn.prepareStatement("INSERT INTO teacher (user_id) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
            t.setInt(1, uid);
            t.executeUpdate();

            int tid = -1;
            var tk = t.getGeneratedKeys();
            if (tk.next()) tid = tk.getInt(1);

            var ts = conn.prepareStatement("INSERT INTO teacher_subject (teacher_id, subject_id) VALUES (?, ?)");
            for (String subjectName : subjects) {
                int sid = getSubjectId(conn, subjectName);
                ts.setInt(1, tid);
                ts.setInt(2, sid);
                ts.executeUpdate();
            }
        }
    }

    /** Usuwa dane specyficzne dla roli (np. rekord nauczyciela i jego przedmioty) przed zmianą roli. */
    private void removeRoleData(Connection conn, int uid, String role) throws SQLException {
        if ("student".equals(role)) {
            var rs = conn.createStatement().executeQuery("SELECT id FROM student WHERE user_id=" + uid);
            if(rs.next()) {
                int sid = rs.getInt(1);
                conn.createStatement().executeUpdate("DELETE FROM grades WHERE student_id=" + sid);
                conn.createStatement().executeUpdate("DELETE FROM student WHERE id=" + sid);
            }
        } else if ("nauczyciel".equals(role)) {
            var rs = conn.createStatement().executeQuery("SELECT id FROM teacher WHERE user_id=" + uid);
            if(rs.next()) {
                int tid = rs.getInt(1);
                conn.createStatement().executeUpdate("DELETE FROM teacher_subject WHERE teacher_id=" + tid);
                conn.createStatement().executeUpdate("DELETE FROM teacher WHERE id=" + tid);
            }
        }
    }

    /** Aktualizuje przedmioty przypisane nauczycielowi (usuwa stare, dodaje nowe). */
    private void updateTeacherSubjects(Connection conn, int uid, ObservableList<String> newSubjects) throws SQLException {
        var rs = conn.createStatement().executeQuery("SELECT id FROM teacher WHERE user_id=" + uid);
        if (rs.next()) {
            int tid = rs.getInt(1);

            conn.createStatement().executeUpdate("DELETE FROM teacher_subject WHERE teacher_id=" + tid);

            var ts = conn.prepareStatement("INSERT INTO teacher_subject (teacher_id, subject_id) VALUES (?, ?)");
            for (String subjectName : newSubjects) {
                int newSid = getSubjectId(conn, subjectName);
                ts.setInt(1, tid);
                ts.setInt(2, newSid);
                ts.executeUpdate();
            }
        }
    }

    /** Pomocnicza metoda do pobierania ID przedmiotu po nazwie. */
    private int getSubjectId(Connection conn, String name) throws SQLException {
        var s = conn.prepareStatement("SELECT id FROM subjects WHERE name=?");
        s.setString(1, name);
        var rs = s.executeQuery();
        return rs.next() ? rs.getInt(1) : -1;
    }
}