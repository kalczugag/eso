package com.example.elektroniczny_dziennik;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;

public class LoginController {
    @FXML private TextField loginInput;
    @FXML private PasswordField passwordInput;


    private Parent root;
    private Scene scene;
    private Stage stage;


    public void login(ActionEvent e) throws IOException {
        String login = loginInput.getText();
        String password = passwordInput.getText();

        try(var conn = Database.getConnection()){
            var statement = conn.prepareStatement("SELECT * FROM Users WHERE username = ? AND password = ?");
            statement.setString(1, login);
            statement.setString(2, password);

            var result = statement.executeQuery();

            if(result.next()){
                User user = new User(
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("role")
                );

                FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
                root = loader.load();

                MainController controller = loader.getController();
                controller.displayUser(user);

                scene = new Scene(root);

                stage = (Stage)((Node)e.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Niepoprawne dane logowanie");
                alert.setContentText("Niepoprawne hasło lub login, spróbuj ponownie");
                alert.show();
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        /*
        if(login.equals("") || password.equals("")){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Niepoprawne dane logowanie");
            alert.setContentText("Niepoprawne hasło lub login, spróbuj ponownie");
            alert.show();
        } else {
            User user = new User(login, password, "admin");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
            root = loader.load();
            scene = new Scene(root);

            MainController controller = loader.getController();
            controller.displayUser(user);

            stage = (Stage)((Node)e.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }
        */
    }
}
