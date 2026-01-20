package com.example.elektroniczny_dziennik;

/**
 * Klasa modelu reprezentująca przedmiot szkolny (np. Matematyka, Historia).
 * Służy do mapowania rekordów z tabeli 'subjects'.
 */
public class Subject {

    /** Unikalny identyfikator przedmiotu w bazie danych. */
    private int id;

    /** Nazwa przedmiotu. */
    private String name;

    /**
     * Konstruktor inicjalizujący obiekt przedmiotu.
     *
     * @param id ID przedmiotu.
     * @param name Nazwa przedmiotu.
     */
    public Subject(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Pobiera ID przedmiotu.
     * @return ID przedmiotu.
     */
    public int getId() {
        return id;
    }

    /**
     * Ustawia ID przedmiotu.
     * @param id Nowe ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Pobiera nazwę przedmiotu.
     * @return Nazwa przedmiotu.
     */
    public String getName() {
        return name;
    }

    /**
     * Ustawia nazwę przedmiotu.
     * @param name Nowa nazwa.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Zwraca reprezentację tekstową obiektu.
     * Jest to kluczowe dla kontrolek typu ComboBox w JavaFX,
     * aby wyświetlały samą nazwę przedmiotu zamiast referencji do obiektu.
     *
     * @return Nazwa przedmiotu.
     */
    @Override
    public String toString() {
        return name;
    }
}