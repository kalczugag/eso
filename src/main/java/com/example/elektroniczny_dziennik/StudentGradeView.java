package com.example.elektroniczny_dziennik;

import java.sql.Date;

public class StudentGradeView {
    private String subjectName;
    private double value;
    private String description;
    private Date date;

    public StudentGradeView(String subjectName, double value, String description, Date date) {
        this.subjectName = subjectName;
        this.value = value;
        this.description = description;
        this.date = date;
    }

    public String getSubjectName() { return subjectName; }
    public double getValue() { return value; }
    public String getDescription() { return description; }
    public Date getDate() { return date; }
}