package com.example.elektroniczny_dziennik;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static String URL = "jdbc:postgresql://localhost:5432/edziennik";
    private static String USER = "postgres";
    private static String PASSWORD = "admin";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
