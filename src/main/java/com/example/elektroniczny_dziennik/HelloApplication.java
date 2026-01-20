package com.example.elektroniczny_dziennik;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Główna klasa aplikacji rozszerzająca {@link javafx.application.Application}.
 * Odpowiada za inicjalizację środowiska JavaFX, załadowanie pierwszego widoku (ekranu logowania)
 * oraz wyświetlenie głównego okna (Stage).
 */
public class HelloApplication extends Application {

    /**
     * Metoda startowa aplikacji JavaFX.
     * Ładuje plik FXML widoku logowania i ustawia go na scenie.
     *
     * @param stage Główne okno aplikacji (dostarczane przez platformę JavaFX).
     * @throws IOException W przypadku problemów z załadowaniem pliku 'loginView.fxml'.
     */
    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("loginView.fxml"));
        System.out.println(root); // Logowanie obiektu root (opcjonalne, do debugowania)
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.centerOnScreen();

        stage.show();
    }
}