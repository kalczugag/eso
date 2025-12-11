package com.example.elektroniczny_dziennik;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    public static String analyzeGrades(List<StudentGradeView> grades) {
        if (grades == null || grades.isEmpty()) {
            return "Brak ocen do analizy.";
        }

        StringBuilder report = new StringBuilder();
        report.append("=== RAPORT POSTĘPÓW W NAUCE ===\n");
        report.append("Data generowania: ").append(LocalDate.now()).append("\n\n");

        Map<String, Double> sumPerSubject = new HashMap<>();
        Map<String, Integer> countPerSubject = new HashMap<>();
        int failingGrades = 0;
        double totalSum = 0;

        for (StudentGradeView grade : grades) {
            String subject = grade.getSubjectName();
            double value = grade.getValue();

            sumPerSubject.put(subject, sumPerSubject.getOrDefault(subject, 0.0) + value);
            countPerSubject.put(subject, countPerSubject.getOrDefault(subject, 0) + 1);

            totalSum += value;

            if (value < 2.0) {
                failingGrades++;
            }
        }

        double globalAverage = totalSum / grades.size();
        String bestSubject = "-";
        double bestAvg = -1.0;
        String worstSubject = "-";
        double worstAvg = 7.0;

        report.append("--- Średnie z przedmiotów ---\n");

        for (String subject : sumPerSubject.keySet()) {
            double avg = sumPerSubject.get(subject) / countPerSubject.get(subject);
            report.append(String.format("%-20s: %.2f\n", subject, avg));

            if (avg > bestAvg) {
                bestAvg = avg;
                bestSubject = subject;
            }
            if (avg < worstAvg) {
                worstAvg = avg;
                worstSubject = subject;
            }
        }

        report.append("\n--- Podsumowanie Ogólne ---\n");
        report.append(String.format("Ogólna średnia:       %.2f\n", globalAverage));
        report.append("Liczba ocen:          ").append(grades.size()).append("\n");
        report.append("Najlepszy przedmiot:  ").append(bestSubject).append(" (").append(String.format("%.2f", bestAvg)).append(")\n");
        report.append("Najsłabszy przedmiot: ").append(worstSubject).append(" (").append(String.format("%.2f", worstAvg)).append(")\n");

        report.append("\n--- Status ---\n");
        if (failingGrades > 0) {
            report.append("UWAGA: Masz ").append(failingGrades).append(" ocen(y) poniżej normy! Zalecana poprawa.\n");
        } else {
            report.append("Gratulacje! Brak zagrożeń. Utrzymuj ten poziom.\n");
        }

        return report.toString();
    }

    public static void saveReport(String content, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz Raport");
        fileChooser.setInitialFileName("raport_ocen_" + LocalDate.now() + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki tekstowe", "*.txt"));

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}