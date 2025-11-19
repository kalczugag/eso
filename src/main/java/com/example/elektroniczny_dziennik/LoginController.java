package com.example.elektroniczny_dziennik;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.io.IOException;

public class LoginController {
    @FXML private TextField loginInput;
    @FXML private PasswordField passwordInput;

    // Labels
    @FXML private Label errorLabel;


    private Parent root;
    private Scene scene;
    private Stage stage;

    // Obsługuje autoryzację użytkownika po wciśnięciu klawisza Enter w polach wprowadzania.
    @FXML
    public void initialize() {
        loginInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    login(new ActionEvent(loginInput, null));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                event.consume();
            }
        });

        passwordInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    login(new ActionEvent(passwordInput, null));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                event.consume();
            }
        });
    }

    public void login(ActionEvent e) throws IOException {
        String login = loginInput.getText();
        String password = passwordInput.getText();

        try(var conn = Database.getConnection()){
            var statement = conn.prepareStatement("SELECT * FROM user WHERE login = ?");
            statement.setString(1, login);

            var result = statement.executeQuery();

            if(result.next()){
                String storedHash = result.getString("password");

                if(BCrypt.checkpw(password, storedHash)) {
                    User user = new User(
                            result.getString("first_name"),
                            result.getString("last_name"),
                            result.getString("role")
                    );

                    User.id = result.getString("id");

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
                    root = loader.load();

                    MainController controller = loader.getController();
                    controller.displayUser(user);

                    scene = new Scene(root);

                    stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                    stage.setScene(scene);
                    stage.centerOnScreen();
                    stage.show();
                }
                else{
                    errorLabel.setText("Niepoprawne hasło, spróbuj ponownie");
                }
            }
            else {
                errorLabel.setText("Niepoprawny login, spróbuj ponownie");
            }
        }
        catch (Exception ex){
            System.out.println(ex);
        }
    }

    public void register(ActionEvent e) throws IOException{
        root = FXMLLoader.load(getClass().getResource("registerView.fxml"));
        scene = new Scene(root);

        stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}
