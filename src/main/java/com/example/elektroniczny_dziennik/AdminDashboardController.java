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

public class AdminDashboardController {

    @FXML private Label totalUsersLabel;
    @FXML private Label teachersLabel;
    @FXML private PieChart userChart;

    @FXML
    public void initialize() {
        loadStatistics();
    }

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