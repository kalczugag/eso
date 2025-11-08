package com.example.elektroniczny_dziennik;

public class User {
    private String firstName;
    private String lastName;
    private String role; // admin, student

    public User(String firstName, String lastName, String role){
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // Getters
    public String getFirstName() {return this.firstName;};
    public String getLastName() {return this.lastName;};
    public String getRole() {return this.role;};
}
