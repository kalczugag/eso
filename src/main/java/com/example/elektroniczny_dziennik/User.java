package com.example.elektroniczny_dziennik;

public class User {
    private String username;
    private String password;
    private String role;

    public User(String username, String password, String role){
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters
    public String getUsername(){
        return this.username;
    }

    public String getRole(){
        return this.role;
    }
}
