package com.example.elektroniczny_dziennik;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Klasa odpowiedzialna za obsługę połączenia z bazą danych SQLite.
 * Dostarcza statycznych metod umożliwiających uzyskanie połączenia z plikiem bazy danych.
 */
public class Database {

    /**
     * Adres URL połączenia do bazy danych SQLite.
     * Wskazuje na plik "database.db" w katalogu "database".
     */
    private static String URL = "jdbc:sqlite:database/database.db";

    /**
     * Nawiązuje i zwraca połączenie z bazą danych.
     * Wykorzystuje sterownik DriverManager do utworzenia obiektu Connection.
     *
     * @return Obiekt Connection reprezentujący aktywne połączenie z bazą danych.
     * @throws SQLException Wyrzucany w przypadku błędu dostępu do bazy danych lub nieprawidłowego adresu URL.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}