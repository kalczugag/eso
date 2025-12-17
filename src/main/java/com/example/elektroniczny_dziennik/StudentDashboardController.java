package com.example.elektroniczny_dziennik;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label avgLabel;
    @FXML private ListView<String> recentGradesList;

    @FXML private Label remarksCountLabel;
    @FXML private Label remarksSummaryLabel;

    private User user;
    private int studentId = -1;

    public void setUser(User user) {
        this.user = user;
        welcomeLabel.setText("Witaj, " + user.getFirstName() + "!");

        loadStudentIdAndData();
    }

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


    private void loadRemarksMock() {
        int positiveCount = 2;
        int negativeCount = 0;

        if (negativeCount == 0) {
        } else {
        }

    }
}