package com.example.elektroniczny_dziennik;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
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

    @FXML private VBox bestStudentsContainer;

    @FXML void initialize() throws SQLException {
        initializeTeacherData();
        initializeStudentData();
        initializeBestStudents();
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

    void initializeBestStudents() throws SQLException{
        try(var conn = Database.getConnection()){
            var statement = conn.prepareStatement("SELECT u.first_name, u.last_name, ROUND(AVG(g.grade), 2) AS srednia FROM user AS u" +
                    " JOIN student AS s ON s.user_id = u.id" +
                    " JOIN grades AS g ON g.student_id = s.id" +
                    " GROUP BY s.id" +
                    " ORDER BY srednia DESC LIMIT 5;");

            var result = statement.executeQuery();
            int counter = 0;

            while(result.next()){
                HBox hbox = new HBox(10);
                counter++;

                Label rankNumber = new Label(String.valueOf(counter));
                Label label1 = new Label(result.getString(1) + " " + result.getString(2));
                Label label2 = new Label(result.getString(3));

                hbox.getChildren().addAll(rankNumber, label1, label2);
                hbox.setAlignment(Pos.CENTER);
                hbox.setPrefHeight(40);

                rankNumber.getStyleClass().add("rankNumber");
                label1.getStyleClass().add("rankName");
                label2.getStyleClass().add("rankScore");
                hbox.getStyleClass().add("rankItem");
                bestStudentsContainer.getChildren().add(hbox);
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
}
