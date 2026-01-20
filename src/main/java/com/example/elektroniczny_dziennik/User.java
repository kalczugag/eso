package com.example.elektroniczny_dziennik;

/**
 * Klasa modelu reprezentująca użytkownika systemu.
 * Przechowuje informacje o imieniu, nazwisku, roli oraz identyfikatorze użytkownika.
 */
public class User {

    /** Imię użytkownika. */
    private String firstName;

    /** Nazwisko użytkownika. */
    private String lastName;

    /**
     * Rola użytkownika w systemie.
     * Przykładowe wartości: "admin", "student", "nauczyciel".
     */
    private String role;

    /**
     * Statyczne pole przechowujące ID aktualnie zalogowanego użytkownika.
     * Pole to jest współdzielone w całej aplikacji, co pozwala na łatwy dostęp do ID w różnych kontrolerach.
     */
    static String id;

    /**
     * Konstruktor tworzący nowy obiekt użytkownika.
     *
     * @param firstName Imię użytkownika.
     * @param lastName Nazwisko użytkownika.
     * @param role Rola przypisana użytkownikowi (np. uprawnienia).
     */
    public User(String firstName, String lastName, String role){
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // Getters

    /**
     * Pobiera imię użytkownika.
     * @return Imię użytkownika.
     */
    public String getFirstName() {return this.firstName;};

    /**
     * Pobiera nazwisko użytkownika.
     * @return Nazwisko użytkownika.
     */
    public String getLastName() {return this.lastName;};

    /**
     * Pobiera rolę użytkownika w systemie.
     * @return Rola użytkownika.
     */
    public String getRole() {return this.role;};
}