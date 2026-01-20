package com.example.elektroniczny_dziennik;

import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Kontroler obsługujący proces rejestracji nowego użytkownika (domyślnie studenta).
 * Waliduje dane wejściowe, generuje unikalny login oraz haszuje hasło przed zapisem do bazy.
 */
public class RegisterController {

    // Pola formularza
    /** Pole tekstowe na imię. */
    @FXML TextField firstNameInput;
    /** Pole tekstowe na nazwisko. */
    @FXML TextField lastNameInput;
    /** Pole tekstowe na hasło. */
    @FXML PasswordField passwordInput;
    /** Pole tekstowe na powtórzenie hasła. */
    @FXML PasswordField confirmPasswordInput;

    /** Etykieta informacyjna wyświetlająca błędy lub sukces rejestracji. */
    @FXML Label infoLabel;

    // Obiekty sceny
    private Parent root;
    private Scene scene;
    private Stage stage;

    // Klasy stylów CSS do kolorowania komunikatów (zielony/czerwony)
    PseudoClass positive = PseudoClass.getPseudoClass("positive");
    PseudoClass negative = PseudoClass.getPseudoClass("negative");

    /**
     * Główna metoda rejestracji.
     * Sprawdza poprawność danych (puste pola, zgodność haseł).
     * Jeśli walidacja przebiegnie pomyślnie, tworzy użytkownika w bazie danych,
     * przypisuje mu domyślną rolę 'student' i klasę '3A'.
     */
    public void register(){
        String firstName = firstNameInput.getText();
        String lastName = lastNameInput.getText();
        String password = passwordInput.getText();
        String confirmPassword = confirmPasswordInput.getText();

        if(firstName.equals("") || lastName.equals("") || password.equals("") || confirmPassword.equals("")){
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
                String login = getLogin(firstName, lastName, conn); // generowanie unikalnego loginu
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                String role = "student";

                // Wstawianie do tabeli 'user'
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
                    // Pobranie wygenerowanego ID użytkownika
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

                    // Wstawianie do tabeli 'student' (domyślnie klasa 3A)
                    var statement2 = conn.prepareStatement(
                            "INSERT INTO student (class, user_id)" +
                                    "VALUES (?, ?)");

                    statement2.setString(1, "3A");
                    statement2.setInt(2, userId);

                    var result2 = statement2.executeUpdate();

                    if(result2 > 0){
                        infoLabel.setText("Poprawnie utworzono konto\nTwój login to: " + login);
                        infoLabel.pseudoClassStateChanged(positive, true);
                        infoLabel.pseudoClassStateChanged(negative, false);
                    }
                }
            }
            catch(Exception e){
                System.out.println(e);
            }
        }

        // Wyczyszczenie pól po próbie rejestracji
        firstNameInput.setText("");
        lastNameInput.setText("");
        passwordInput.setText("");
        confirmPasswordInput.setText("");
    }

    /**
     * Generuje unikalny login na podstawie imienia i nazwiska.
     * Schemat: "s" + 3 litery imienia + 3 litery nazwiska.
     * Jeśli taki login istnieje, dodaje kolejny numer porządkowy (np. skowalski1, skowalski2).
     *
     * @param firstName Imię użytkownika.
     * @param lastName Nazwisko użytkownika.
     * @param conn Aktywne połączenie do bazy danych.
     * @return Unikalny ciąg znaków login.
     * @throws SQLException W przypadku błędu zapytania do bazy.
     */
    public String getLogin(String firstName, String lastName, Connection conn) throws SQLException{
        String login = "s"
                + firstName.substring(0, Math.min(3, firstName.length())).toLowerCase()
                + lastName.substring(0, Math.min(3, lastName.length())).toLowerCase();

        var statement = conn.prepareStatement("SELECT login FROM user WHERE login LIKE ?");
        statement.setString(1, login + "%");

        var result = statement.executeQuery();

        int counter = 0;

        while(result.next()){
            String existingLogin = result.getString("login");

            // Sprawdzenie czy login ma sufiks liczbowy
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

    /**
     * Przełącza widok z powrotem do ekranu logowania.
     *
     * @param e Zdarzenie kliknięcia przycisku.
     * @throws IOException Gdy nie uda się załadować widoku logowania.
     */
    public void login(ActionEvent e) throws IOException {
        root = FXMLLoader.load(getClass().getResource("loginView.fxml"));
        scene = new Scene(root);

        stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}