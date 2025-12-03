package com.example.elektroniczny_dziennik;

import javafx.fxml.FXML;

import java.sql.SQLException;

public class gradesController {
    private int studentId;

    @FXML
    void initialize() throws SQLException{
        try(var conn = Database.getConnection()){
            var statement = conn.prepareStatement("SELECT s.id FROM student AS s" +
                    " JOIN user AS u ON u.id = s.user_id" +
                    " WHERE u.id = ?");

            statement.setString(1, User.id);
            var result = statement.executeQuery();

            if(!result.next()) System.out.println("Error");
            else studentId = result.getInt("id");
        }
        catch(SQLException e){
            System.out.println(e);
        }

        showGrades();
    }


    void showGrades() throws SQLException {
        try(var conn = Database.getConnection()){
            var statement = conn.prepareStatement("SELECT g.grade, u.first_name, u.last_name, sb.name FROM grades AS g" +
                    " JOIN student AS s ON s.id = g.student_id" +
                    " JOIN subjects AS sb ON sb.id = g.subject_id" +
                    " JOIN user AS u ON u.id = s.user_id" +
                    " WHERE s.id = ?");

            statement.setInt(1, studentId);
            var result = statement.executeQuery();

            while(result.next()){
                System.out.println(result.getInt("grade") + result.getString("first_name") + result.getString("last_name")
                + result.getString("name"));
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
}
