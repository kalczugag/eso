package com.example.elektroniczny_dziennik;

import javafx.application.Application;

/**
 * Klasa startowa (Launcher) aplikacji.
 * Służy jako punkt wejścia dla maszyny wirtualnej Java (JVM).
 * Pośrednio uruchamia właściwą aplikację JavaFX {@link HelloApplication}.
 * Jest to częsta praktyka mająca na celu uniknięcie błędów ładowania modułów JavaFX.
 */
public class Launcher {

    /**
     * Metoda main - punkt wejścia do programu.
     *
     * @param args Argumenty wiersza poleceń.
     */
    public static void main(String[] args) {
        Application.launch(HelloApplication.class, args);
    }
}