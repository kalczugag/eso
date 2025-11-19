package com.example.elektroniczny_dziennik;

import java.sql.Date;

public class Grade {
    private int id;
    private double value;
    private String description;
    private Date date;

    public Grade(int id, double value, String description, Date date) {
        this.id = id;
        this.value = value;
        this.description = description;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public double getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}