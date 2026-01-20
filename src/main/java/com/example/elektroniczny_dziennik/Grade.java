package com.example.elektroniczny_dziennik;

import java.sql.Date;

/**
 * Klasa modelu reprezentująca pojedynczą ocenę ucznia.
 * Przechowuje informacje o wartości oceny, opisie, dacie jej wystawienia
 * oraz powiązaniu z konkretnym uczniem (pobierane zazwyczaj złączeniem tabel).
 */
public class Grade {

    /** Unikalny identyfikator oceny w bazie danych. */
    private int id;

    /**
     * Wartość liczbowa oceny (np. 2.0, 3.5, 5.0).
     * W bazie danych przechowywana zazwyczaj jako typ zmiennoprzecinkowy.
     */
    private double value;

    /**
     * Opis oceny precyzujący, za co została wystawiona.
     * Przykłady: "Sprawdzian z działu 1", "Aktywność", "Kartkówka".
     */
    private String description;

    /** Data wystawienia oceny. */
    private Date date;

    /**
     * Imię i nazwisko ucznia, do którego przypisana jest ocena.
     * Pole to nie mapuje bezpośrednio kolumny w tabeli 'grades', lecz jest
     * uzupełniane po pobraniu danych (JOIN z tabelą users/students) w celu wyświetlenia w widoku.
     */
    private String studentName;

    /**
     * Konstruktor tworzący obiekt oceny z danymi.
     * Wykorzystywany głównie przy mapowaniu wyników zapytania SQL na obiekty Java.
     *
     * @param id Unikalne ID oceny.
     * @param value Wartość oceny.
     * @param description Opis słowny oceny.
     * @param date Data wystawienia.
     */
    public Grade(int id, double value, String description, Date date) {
        this.id = id;
        this.value = value;
        this.description = description;
        this.date = date;
    }

    // --- Gettery i Settery ---

    /**
     * Pobiera identyfikator oceny.
     * @return ID oceny.
     */
    public int getId() {
        return id;
    }

    /**
     * Ustawia identyfikator oceny.
     * @param id Nowe ID oceny.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Pobiera wartość liczbową oceny.
     * @return Wartość oceny.
     */
    public double getValue() {
        return value;
    }

    /**
     * Ustawia nową wartość oceny.
     * Wykorzystywane np. podczas edycji oceny w tabeli.
     * @param value Nowa wartość oceny.
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Pobiera opis oceny.
     * @return Opis oceny.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Ustawia opis oceny.
     * @param description Nowy opis.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Pobiera datę wystawienia oceny.
     * @return Data oceny.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Ustawia datę wystawienia oceny.
     * @param date Nowa data.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Pobiera imię i nazwisko ucznia przypisanego do tej oceny.
     * @return Imię i nazwisko ucznia.
     */
    public String getStudentName() {
        return studentName;
    }

    /**
     * Przypisuje imię i nazwisko ucznia do obiektu oceny.
     * Używane przez kontroler po pobraniu danych z bazy, aby wyświetlić czytelne dane w tabeli.
     * @param studentName Imię i nazwisko ucznia.
     */
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
}