package com.gatcha.auth.model;

/**
 * DTO pour la requete de connexion
 * Contient l'identifiant et le mot de passe
 */
public class LoginRequest {

    private String username;
    private String password;

    // Constructeur vide
    public LoginRequest() {
    }

    // Constructeur avec parametres
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters et Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
