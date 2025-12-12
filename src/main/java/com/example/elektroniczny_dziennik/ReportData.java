package com.example.elektroniczny_dziennik;

import java.util.Map;

public class ReportData {
    public double globalAverage;
    public String bestSubject;
    public double bestAvg;
    public String worstSubject;
    public double worstAvg;
    public int failingGrades;
    public int totalGrades;
    public Map<String, Double> averagePerSubject;

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