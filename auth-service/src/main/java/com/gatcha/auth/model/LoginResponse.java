package com.gatcha.auth.model;

/**
 * DTO pour la reponse de connexion
 * Retourne le token genere
 */
public class LoginResponse {

    private String token;

    // Constructeur vide
    public LoginResponse() {
    }

    // Constructeur avec token
    public LoginResponse(String token) {
        this.token = token;
    }

    // Getter et Setter
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
