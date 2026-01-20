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

/**
 * Kontroler obsługujący widok logowania do systemu.
 * Odpowiada za autoryzację użytkowników, weryfikację haseł oraz przekierowanie do głównego panelu aplikacji.
 */
public class LoginController {

    /** Pole tekstowe do wprowadzenia loginu. */
    @FXML private TextField loginInput;

    /** Pole tekstowe (ukryte) do wprowadzenia hasła. */
    @FXML private PasswordField passwordInput;

    /** Etykieta służąca do wyświetlania komunikatów o błędach logowania. */
    @FXML private Label errorLabel;

    /** Główny węzeł (root) sceny JavaFX. */
    private Parent root;
    /** Obiekt sceny JavaFX. */
    private Scene scene;
    /** Obiekt okna (Stage) aplikacji. */
    private Stage stage;

    /**
     * Metoda inicjalizująca kontroler.
     * Ustawia nasłuchiwacze zdarzeń (Listenery) na polach tekstowych, aby umożliwić logowanie
     * po wciśnięciu klawisza ENTER.
     */
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

    /**
     * Obsługuje proces logowania użytkownika.
     * Weryfikuje dane w bazie, sprawdza poprawność hasła (BCrypt) i w razie sukcesu
     * ładuje główny widok aplikacji (MainController).
     *
     * @param e Zdarzenie wywołujące (np. kliknięcie przycisku).
     * @throws IOException Wyrzucany w przypadku problemów z ładowaniem pliku widoku (FXML).
     */
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

    /**
     * Przełącza widok na formularz rejestracji nowego użytkownika.
     *
     * @param e Zdarzenie wywołujące (np. kliknięcie przycisku "Zarejestruj").
     * @throws IOException Wyrzucany, gdy nie uda się załadować pliku 'registerView.fxml'.
     */
    public void register(ActionEvent e) throws IOException{
        root = FXMLLoader.load(getClass().getResource("registerView.fxml"));
        scene = new Scene(root);

        stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}