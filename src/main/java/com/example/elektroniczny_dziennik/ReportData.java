package com.example.elektroniczny_dziennik;

import java.util.Map;

/**
 * Klasa przechowywująca przeliczone dane statystyczne do raportu ucznia.
 * Służy jako kontener danych (DTO) przekazywany z serwisu raportującego do kontrolera widoku raportu.
 * Zawiera informacje o średnich, najlepszych/najgorszych przedmiotach oraz zagrożeniach.
 */
public class ReportData {

    /** Obliczona średnia ogólna ze wszystkich przedmiotów. */
    public double globalAverage;

    /** Nazwa przedmiotu z najwyższą średnią. */
    public String bestSubject;

    /** Wartość najwyższej średniej. */
    public double bestAvg;

    /** Nazwa przedmiotu z najniższą średnią. */
    public String worstSubject;

    /** Wartość najniższej średniej. */
    public double worstAvg;

    /** Liczba ocen niedostatecznych (poniżej 2.0). */
    public int failingGrades;

    /** Całkowita liczba ocen wziętych pod uwagę w raporcie. */
    public int totalGrades;

    /** Mapa zawierająca średnie ocen dla poszczególnych przedmiotów (Klucz: nazwa przedmiotu, Wartość: średnia). */
    public Map<String, Double> averagePerSubject;

    /**
     * Konstruktor inicjalizujący obiekt raportu wszystkimi niezbędnymi danymi.
     *
     * @param globalAverage Średnia ogólna.
     * @param bestSubject Nazwa najlepszego przedmiotu.
     * @param bestAvg Średnia najlepszego przedmiotu.
     * @param worstSubject Nazwa najsłabszego przedmiotu.
     * @param worstAvg Średnia najsłabszego przedmiotu.
     * @param failingGrades Liczba jedynek.
     * @param totalGrades Łączna liczba ocen.
     * @param averagePerSubject Mapa średnich cząstkowych.
     */
    public ReportData(double globalAverage, String bestSubject, double bestAvg, String worstSubject, double worstAvg, int failingGrades, int totalGrades, Map<String, Double> averagePerSubject) {
        this.globalAverage = globalAverage;
        this.bestSubject = bestSubject;
        this.bestAvg = bestAvg;
        this.worstSubject = worstSubject;
        this.worstAvg = worstAvg;
        this.failingGrades = failingGrades;
        this.totalGrades = totalGrades;
        this.averagePerSubject = averagePerSubject;
    }
}