package com.example.elektroniczny_dziennik;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Kontroler panelu głównego Studenta.
 * Wyświetla podstawowe informacje: powitanie, średnią ocen oraz listę ostatnich ocen.
 */
public class StudentDashboardController {

    /** Etykieta powitalna. */
    @FXML private Label welcomeLabel;

    /** Etykieta wyświetlająca średnią ocen. */
    @FXML private Label avgLabel;

    /** Lista wyświetlająca ostatnie oceny ucznia. */
    @FXML private ListView<String> recentGradesList;

    /** Etykieta liczby uwag (nieużywana w obecnej wersji). */
    @FXML private Label remarksCountLabel;
    /** Etykieta podsumowania uwag (nieużywana w obecnej wersji). */
    @FXML private Label remarksSummaryLabel;

    /** Obiekt zalogowanego użytkownika. */
    private User user;

    /** ID ucznia w tabeli 'student' (różne od ID w tabeli 'user'). */
    private int studentId = -1;

    /**
     * Ustawia kontekst użytkownika dla dashboardu.
     * Wyświetla powitanie i inicjuje pobieranie szczegółowych danych studenta.
     *
     * @param user Zalogowany użytkownik.
     */
    public void setUser(User user) {
        this.user = user;
        welcomeLabel.setText("Witaj, " + user.getFirstName() + "!");

        loadStudentIdAndData();
    }

    /**
     * Pobiera ID studenta na podstawie ID użytkownika, a następnie ładuje jego dane.
     * Jest to konieczne, ponieważ oceny są przypisane do ID studenta, nie użytkownika.
     */
    private void loadStudentIdAndData() {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id FROM student WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(User.id));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.studentId = rs.getInt("id");

                loadAverage(conn);
                loadRecentGrades(conn);

                loadRemarksMock();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Oblicza i wyświetla średnią wszystkich ocen studenta.
     *
     * @param conn Aktywne połączenie z bazą.
     * @throws SQLException W przypadku błędu zapytania.
     */
    private void loadAverage(Connection conn) throws SQLException {
        String sql = "SELECT AVG(grade) as srednia FROM grades WHERE student_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, studentId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            double avg = rs.getDouble("srednia");
            avgLabel.setText(String.format("%.2f", avg));
        } else {
            avgLabel.setText("-");
        }
    }

    /**
     * Pobiera 5 ostatnich ocen studenta i wyświetla je na liście.
     * Dołącza nazwę przedmiotu dzięki złączeniu tabel.
     *
     * @param conn Aktywne połączenie z bazą.
     * @throws SQLException W przypadku błędu zapytania.
     */
    private void loadRecentGrades(Connection conn) throws SQLException {
        String sql = "SELECT s.name, g.grade, g.date FROM grades g " +
                "JOIN subjects s ON s.id = g.subject_id " +
                "WHERE g.student_id = ? " +
                "ORDER BY g.date DESC LIMIT 5";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, studentId);
        ResultSet rs = stmt.executeQuery();

        recentGradesList.getItems().clear();
        while (rs.next()) {
            String line = String.format("%s: %.1f (%s)",
                    rs.getString("name"),
                    rs.getDouble("grade"),
                    rs.getString("date"));
            recentGradesList.getItems().add(line);
        }
    }

    /**
     * Metoda atrapa (mock) dla systemu uwag.
     * W przyszłości powinna pobierać dane z bazy.
     */
    private void loadRemarksMock() {
        int positiveCount = 2;
        int negativeCount = 0;

        if (negativeCount == 0) {
            // Logika wyświetlania pozytywnego komunikatu
        } else {
            // Logika wyświetlania ostrzeżeń
        }
    }
}