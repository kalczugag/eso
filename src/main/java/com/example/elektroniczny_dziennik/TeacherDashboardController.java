package com.example.elektroniczny_dziennik;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;

public class TeacherDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label classesCountLabel;
    @FXML private Label studentsCountLabel;
    @FXML private Label testsToCheckLabel;

    private User user;
    private int teacherId = -1;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setUser(User user) {
        this.user = user;
        welcomeLabel.setText("Dzień dobry, " + user.getFirstName() + " " + user.getLastName());
        loadTeacherData();
    }

    private void loadTeacherData() {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id FROM teacher WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(User.id));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.teacherId = rs.getInt("id");

                classesCountLabel.setText("4");
                studentsCountLabel.setText("112");
                testsToCheckLabel.setText("2");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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