package com.example.elektroniczny_dziennik;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

public class AdminDashboardController {
    private String studentCount;
    private String teacherCount;

    @FXML private Label studentCounter;
    @FXML private Label teacherCounter;

    @FXML private AnchorPane dashboardContainer;

    @FXML void initialize() throws SQLException {
        initializeTeacherData();
        initializeStudentData();
    }

    void initializeStudentData() throws SQLException{
        try(var conn = Database.getConnection()){
            var statement = conn.prepareStatement("SELECT COUNT(*) FROM student;");
            var result = statement.executeQuery();

            if(result.next()) {
                studentCount = result.getString(1);
                studentCounter.setText(studentCount);
            }
        }
    }

    void initializeTeacherData() throws SQLException{
        try(var conn = Database.getConnection()){
            var statement = conn.prepareStatement("SELECT COUNT(*) FROM teacher");
            var result = statement.executeQuery();

            if(result.next()){
                teacherCount = result.getString(1);
                teacherCounter.setText(teacherCount);
            }
        }
    }
}
