package com.example.elektroniczny_dziennik;

import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class AddTeacherController {
    // Inputs
    @FXML TextField firstNameInput;
    @FXML TextField lastNameInput;
    @FXML PasswordField passwordInput;
    @FXML PasswordField confirmPasswordInput;
    @FXML ComboBox<String> subjectComboBox;

    // Labels
    @FXML Label infoLabel;

    // Scene objects
    private Parent root;
    private Scene scene;
    private Stage stage;

    // Pseudo classes
    PseudoClass positive = PseudoClass.getPseudoClass("positive");
    PseudoClass negative = PseudoClass.getPseudoClass("negative");

    @FXML void initialize(){
        try{
            loadSubjects();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void register(){
        String firstName = firstNameInput.getText();
        String lastName = lastNameInput.getText();
        String password = passwordInput.getText();
        String confirmPassword = confirmPasswordInput.getText();
        String subjectName = subjectComboBox.getValue();

        if(firstName.isEmpty() || lastName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || subjectName == null){
            infoLabel.setText("Żadne pole nie może być puste");
            infoLabel.pseudoClassStateChanged(positive, false);
            infoLabel.pseudoClassStateChanged(negative, true);
        }
        else if (!password.equals(confirmPassword)){
            infoLabel.setText("Podane hasła nie są takie same");
            infoLabel.pseudoClassStateChanged(positive, false);
            infoLabel.pseudoClassStateChanged(negative, true);
        }
        else{
            try(var conn = Database.getConnection()){
                String login = getLogin(firstName, lastName, conn); // get unique login
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                String role = "nauczyciel";

                var statement = conn.prepareStatement(
                        "INSERT INTO user (first_name, last_name, login, password, role)" +
                                " VALUES (?, ?, ?, ?, ?)"
                );

                statement.setString(1, firstName);
                statement.setString(2, lastName);
                statement.setString(3, login);
                statement.setString(4, hashedPassword);
                statement.setString(5, role);

                var result = statement.executeUpdate();

                if(result == 0){
                    infoLabel.setText("Bład podczas dodawania użytkownika");
                    infoLabel.pseudoClassStateChanged(positive, false);
                    infoLabel.pseudoClassStateChanged(negative, true);
                }
                else{
                    int userId = -1;
                    try(var generatedId = statement.getGeneratedKeys()){
                        if(generatedId.next()){
                            userId = generatedId.getInt(1);
                        } else {
                            infoLabel.setText("Bład podczas dodawania użytkownika");
                            infoLabel.pseudoClassStateChanged(positive, false);
                            infoLabel.pseudoClassStateChanged(negative, true);
                            return;
                        }
                    }

                    var statement2 = conn.prepareStatement(
                            "INSERT INTO teacher (user_id)" +
                                    "VALUES (?)");

                    statement2.setInt(1, userId);

                    var result2 = statement2.executeUpdate();

                    if(result2 > 0){
                        // Zdobycie ID nowo utworzonego nauczyciela
                        int teacherId = -1;
                        try(var generatedId = statement2.getGeneratedKeys()){
                            if(generatedId.next()) teacherId = generatedId.getInt(1);
                        }

                        // Zdobycie ID przedmiotu, którego będzie nauczał
                        int subjectId = -1;
                        var getSubjectIdQuery = conn.prepareStatement("SELECT id FROM subjects WHERE name = ?");
                        getSubjectIdQuery.setString(1, subjectName);
                        var getSubjectIdResult = getSubjectIdQuery.executeQuery();

                        if(getSubjectIdResult.next()) subjectId = getSubjectIdResult.getInt("id");

                        // Dodanie nauczyciela do tabeli teacher_subject
                        var insertTeacherSubjectQuery = conn.prepareStatement("INSERT INTO teacher_subject (teacher_id, subject_id)" +
                                " VALUES (?, ?)");
                        insertTeacherSubjectQuery.setInt(1, teacherId);
                        insertTeacherSubjectQuery.setInt(2, subjectId);

                        var insertResult = insertTeacherSubjectQuery.executeUpdate();

                        if(insertResult > 0){
                            infoLabel.setText("Poprawnie utworzono konto\nTwój login to: " + login);
                            infoLabel.pseudoClassStateChanged(positive, true);
                            infoLabel.pseudoClassStateChanged(negative, false);
                        }
                    }
                }
            }
            catch(Exception e){
                System.out.println(e);
            }
        }

        firstNameInput.setText("");
        lastNameInput.setText("");
        passwordInput.setText("");
        confirmPasswordInput.setText("");
    }

    // Method to create unique login
    public String getLogin(String firstName, String lastName, Connection conn) throws SQLException {
        String login = "n"
                + firstName.substring(0, Math.min(3, firstName.length())).toLowerCase()
                + lastName.substring(0, Math.min(3, lastName.length())).toLowerCase();

        var statement = conn.prepareStatement("SELECT login FROM user WHERE login LIKE ?");
        statement.setString(1, login + "%");

        var result = statement.executeQuery();

        int counter = 0;

        while(result.next()){
            String existingLogin = result.getString("login");

            if(existingLogin.matches(login + "(\\d+)$")){
                int number = Integer.parseInt(existingLogin.replace(login, ""));
                if(number > counter) counter = number;
                else if (number == counter) counter += 1;
            }
            else if (existingLogin.equals(login)) counter = 1;
        }

        if(counter == 1) login += Integer.toString(counter);
        else if (counter > 1) login += Integer.toString(counter);

        return login;
    }

    void loadSubjects(){
        try(var conn = Database.getConnection()){
            var statement = conn.prepareStatement("SELECT name FROM subjects;");
            var result = statement.executeQuery();

            while(result.next()){
                subjectComboBox.getItems().add(result.getString("name"));
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void test(){
        System.out.println(subjectComboBox.getValue());
    }
}
