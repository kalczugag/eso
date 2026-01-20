package com.example.elektroniczny_dziennik;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;

/**
 * Kontroler panelu głównego Nauczyciela.
 * Wyświetla podstawowe statystyki (liczba klas, uczniów) oraz umożliwia
 * szybkie przejście do modułu oceniania.
 */
public class TeacherDashboardController {

    /** Etykieta powitalna z imieniem i nazwiskiem nauczyciela. */
    @FXML private Label welcomeLabel;

    /** Etykieta liczby klas (obecnie wyświetla dane przykładowe). */
    @FXML private Label classesCountLabel;

    /** Etykieta liczby uczniów (obecnie wyświetla dane przykładowe). */
    @FXML private Label studentsCountLabel;

    /** Etykieta liczby sprawdzianów do sprawdzenia (dane przykładowe). */
    @FXML private Label testsToCheckLabel;

    /** Obiekt zalogowanego użytkownika. */
    private User user;

    /** ID nauczyciela w bazie danych (tabela 'teacher'). */
    private int teacherId = -1;

    /** Referencja do głównego kontrolera w celu obsługi nawigacji. */
    private MainController mainController;

    /**
     * Ustawia referencję do głównego kontrolera aplikacji.
     * Umożliwia temu kontrolerowi wywoływanie zmian widoku w głównym oknie.
     *
     * @param mainController Główny kontroler.
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Ustawia dane użytkownika (nauczyciela) i inicjuje widok.
     *
     * @param user Zalogowany nauczyciel.
     */
    public void setUser(User user) {
        this.user = user;
        welcomeLabel.setText("Dzień dobry, " + user.getFirstName() + " " + user.getLastName());
        loadTeacherData();
    }

    /**
     * Pobiera ID nauczyciela z bazy danych.
     * Ustawia również przykładowe (hardcoded) wartości statystyk na dashboardzie.
     * W pełnej wersji te wartości powinny być pobierane zapytaniami COUNT SQL.
     */
    private void loadTeacherData() {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id FROM teacher WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(User.id));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.teacherId = rs.getInt("id");

                // Wartości przykładowe (mock)
                classesCountLabel.setText("4");
                studentsCountLabel.setText("112");
                testsToCheckLabel.setText("2");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obsługuje kliknięcie przycisku przejścia do oceniania.
     * Wykorzystuje MainController do załadowania widoku 'gradeEntry.fxml'.
     */
    @FXML
    private void goToGrading() {
        if (mainController != null) {
            try {
                mainController.loadView("gradeEntry.fxml");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}