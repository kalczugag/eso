package com.example.elektroniczny_dziennik;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Kontroler panelu głównego Administratora.
 * Odpowiada za pobranie z bazy danych statystyk dotyczących użytkowników
 * i wyświetlenie ich w formie liczbowej oraz wykresu kołowego.
 */
public class AdminDashboardController {

    /** Etykieta z całkowitą liczbą użytkowników. */
    @FXML private Label totalUsersLabel;

    /** Etykieta z liczbą nauczycieli. */
    @FXML private Label teachersLabel;

    /** Wykres kołowy prezentujący podział użytkowników na role. */
    @FXML private PieChart userChart;

    /**
     * Inicjalizuje kontroler.
     * Automatycznie wywołuje ładowanie statystyk przy starcie widoku.
     */
    @FXML
    public void initialize() {
        loadStatistics();
    }

    /**
     * Pobiera statystyki ról użytkowników z bazy danych.
     * Grupuje użytkowników po roli, zlicza ich, a następnie aktualizuje
     * etykiety oraz dane wykresu kołowego.
     */
    private void loadStatistics() {
        int studentCount = 0;
        int teacherCount = 0;
        int adminCount = 0;

        String sql = "SELECT role, COUNT(*) as count FROM user GROUP BY role";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String role = rs.getString("role");
                int count = rs.getInt("count");

                switch (role) {
                    case "student" -> studentCount = count;
                    case "nauczyciel" -> teacherCount = count;
                    case "admin" -> adminCount = count;
                }
            }

            int total = studentCount + teacherCount + adminCount;
            totalUsersLabel.setText(String.valueOf(total));
            teachersLabel.setText(String.valueOf(teacherCount));

            // Przygotowanie danych dla wykresu
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Uczniowie (" + studentCount + ")", studentCount),
                    new PieChart.Data("Nauczyciele (" + teacherCount + ")", teacherCount),
                    new PieChart.Data("Administratorzy (" + adminCount + ")", adminCount)
            );

            userChart.setData(pieChartData);

        } catch (SQLException e) {
            e.printStackTrace();
            totalUsersLabel.setText("Błąd");
        }
    }
}