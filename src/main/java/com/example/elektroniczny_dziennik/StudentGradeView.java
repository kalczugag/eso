package com.example.elektroniczny_dziennik;

import java.sql.Date;

/**
 * Klasa modelu widoku reprezentująca pojedynczy wiersz oceny w tabeli ucznia.
 * Łączy dane z tabel 'grades' i 'subjects', aby wyświetlić pełną informację (nazwa przedmiotu + ocena).
 * Obiekty tej klasy są tworzone zazwyczaj po pobraniu danych z bazy SQL.
 */
public class StudentGradeView {

    /** Nazwa przedmiotu (np. Matematyka). */
    private String subjectName;

    /** Wartość liczbowa oceny. */
    private double value;

    /** Opis oceny (np. Sprawdzian, Kartkówka). */
    private String description;

    /** Data wystawienia oceny. */
    private Date date;

    /**
     * Konstruktor tworzący obiekt widoku oceny.
     *
     * @param subjectName Nazwa przedmiotu.
     * @param value Wartość oceny.
     * @param description Opis oceny.
     * @param date Data wystawienia.
     */
    public StudentGradeView(String subjectName, double value, String description, Date date) {
        this.subjectName = subjectName;
        this.value = value;
        this.description = description;
        this.date = date;
    }

    /**
     * Pobiera nazwę przedmiotu.
     * @return Nazwa przedmiotu.
     */
    public String getSubjectName() { return subjectName; }

    /**
     * Pobiera wartość oceny.
     * @return Wartość oceny.
     */
    public double getValue() { return value; }

    /**
     * Pobiera opis oceny.
     * @return Opis oceny.
     */
    public String getDescription() { return description; }

    /**
     * Pobiera datę wystawienia oceny.
     * @return Obiekt daty SQL.
     */
    public Date getDate() { return date; }
}