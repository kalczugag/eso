package com.example.elektroniczny_dziennik;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserFormController {
    @FXML private TextField firstNameInput;
    @FXML private TextField lastNameInput;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private VBox subjectContainer;
    @FXML private ComboBox<String> subjectComboBox;
    @FXML private PasswordField passwordInput;
    @FXML private PasswordField confirmPasswordInput;
    @FXML private Label titleLabel;
    @FXML private Button actionButton;
    @FXML private Label infoLabel;

    private int editingUserId = -1;
    private String originalRole = "";

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("student", "nauczyciel", "admin");

        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isTeacher = "nauczyciel".equals(newVal);
            subjectContainer.setVisible(isTeacher);
            subjectContainer.setManaged(isTeacher);
        });

        loadSubjects();

        subjectContainer.setVisible(false);
        subjectContainer.setManaged(false);
    }

    private int currentLoggedInUserId = -1;

    public void setLoggedInUser(int id) {
        this.currentLoggedInUserId = id;
    }

    public void setCreateMode() {
        titleLabel.setText("Nowy Użytkownik");
        actionButton.setText("Utwórz");
        roleComboBox.setValue("student");
    }

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
            loadTeacherSubject(editingUserId);
        }
    }

    private void loadSubjects() {
        try (var conn = Database.getConnection()) {
            var rs = conn.createStatement().executeQuery("SELECT name FROM subjects");
            while (rs.next()) subjectComboBox.getItems().add(rs.getString("name"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadTeacherSubject(int userId) {
        try (var conn = Database.getConnection()) {
            String sql = "SELECT s.name FROM subjects s " +
                    "JOIN teacher_subject ts ON ts.subject_id = s.id " +
                    "JOIN teacher t ON t.id = ts.teacher_id " +
                    "WHERE t.user_id = ?";
            var stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                subjectComboBox.setValue(rs.getString("name"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void handleAction() {
        String fname = firstNameInput.getText();
        String lname = lastNameInput.getText();
        String role = roleComboBox.getValue();
        String pass = passwordInput.getText();
        String conf = confirmPasswordInput.getText();
        String subject = subjectComboBox.getValue();

        if (fname.isEmpty() || lname.isEmpty() || role == null) {
            infoLabel.setText("Wypełnij wymagane pola."); return;
        }

        if ("nauczyciel".equals(role) && subject == null) {
            infoLabel.setText("Nauczyciel musi mieć przypisany przedmiot."); return;
        }

        if (editingUserId == -1 && pass.isEmpty()) {
            infoLabel.setText("Hasło jest wymagane dla nowego konta."); return;
        }

        if (!pass.isEmpty() && !pass.equals(conf)) {
            infoLabel.setText("Hasła się nie zgadzają."); return;
        }

        if (editingUserId != -1 && "admin".equals(originalRole) && !"admin".equals(role)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Czy na pewno chcesz odebrać uprawnienia Administratora temu użytkownikowi? Jeśli to Twoje konto, stracisz dostęp.",
                    ButtonType.YES, ButtonType.NO);

            if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.NO) {
                return;
            }
        }

        try (var conn = Database.getConnection()) {
            if (editingUserId == -1) {
                createNewUser(conn, fname, lname, role, pass, subject);
            } else {
                updateUser(conn, fname, lname, role, pass, subject);
            }

            ((Stage) firstNameInput.getScene().getWindow()).close();

        } catch (SQLException e) {
            e.printStackTrace();
            infoLabel.setText("Błąd bazy danych: " + e.getMessage());
        }
    }

    private void createNewUser(Connection conn, String f, String l, String role, String pass, String subj) throws SQLException {
        String login = getUniqueLogin(conn, f, l, role);
        String hash = BCrypt.hashpw(pass, BCrypt.gensalt());

        var stmt = conn.prepareStatement("INSERT INTO user (first_name, last_name, login, password, role) VALUES (?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
        stmt.setString(1, f); stmt.setString(2, l); stmt.setString(3, login); stmt.setString(4, hash); stmt.setString(5, role);
        stmt.executeUpdate();

        int uid = -1;
        var keys = stmt.getGeneratedKeys();
        if (keys.next()) uid = keys.getInt(1);

        assignRoleData(conn, uid, role, subj);
    }

    private void updateUser(Connection conn, String f, String l, String role, String pass, String subj) throws SQLException {
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
            assignRoleData(conn, editingUserId, role, subj);
        } else if ("nauczyciel".equals(role)) {
            updateTeacherSubject(conn, editingUserId, subj);
        }
    }

    private String getUniqueLogin(Connection conn, String firstName, String lastName, String role) throws SQLException {
        String prefix = "s"; // domyślnie student
        if ("nauczyciel".equals(role)) prefix = "n";
        else if ("admin".equals(role)) prefix = "a";

        String loginBase = prefix
                + firstName.substring(0, Math.min(3, firstName.length())).toLowerCase()
                + lastName.substring(0, Math.min(3, lastName.length())).toLowerCase();

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
            }
            else if (existingLogin.equals(loginBase)) counter = 1;
        }

        if(counter == 1) loginBase += Integer.toString(counter);
        else if (counter > 1) loginBase += Integer.toString(counter);

        return loginBase;
    }

    private void assignRoleData(Connection conn, int uid, String role, String subj) throws SQLException {
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

            int sid = getSubjectId(conn, subj);
            var ts = conn.prepareStatement("INSERT INTO teacher_subject (teacher_id, subject_id) VALUES (?, ?)");
            ts.setInt(1, tid); ts.setInt(2, sid);
            ts.executeUpdate();
        }
    }

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

    private void updateTeacherSubject(Connection conn, int uid, String newSubjName) throws SQLException {
        var rs = conn.createStatement().executeQuery("SELECT id FROM teacher WHERE user_id=" + uid);
        if (rs.next()) {
            int tid = rs.getInt(1);
            int newSid = getSubjectId(conn, newSubjName);
            conn.createStatement().executeUpdate("DELETE FROM teacher_subject WHERE teacher_id=" + tid);
            var ts = conn.prepareStatement("INSERT INTO teacher_subject (teacher_id, subject_id) VALUES (?, ?)");
            ts.setInt(1, tid); ts.setInt(2, newSid);
            ts.executeUpdate();
        }
    }

    private int getSubjectId(Connection conn, String name) throws SQLException {
        var s = conn.prepareStatement("SELECT id FROM subjects WHERE name=?");
        s.setString(1, name);
        var rs = s.executeQuery();
        return rs.next() ? rs.getInt(1) : -1;
    }
}