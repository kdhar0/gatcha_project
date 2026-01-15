package com.gatcha.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Modele representant un token d'authentification
 * Le token est valide pendant 1 heure
 */
@Document(collection = "tokens")
public class Token {

    @Id
    private String id;
    private String token;
    private String username;
    private LocalDateTime expirationDate;

    // Constructeur vide
    public Token() {
    }

    // Constructeur avec parametres
    public Token(String token, String username, LocalDateTime expirationDate) {
        this.token = token;
        this.username = username;
        this.expirationDate = expirationDate;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Verifie si le token est encore valide
     */
    public boolean isValid() {
        return LocalDateTime.now().isBefore(expirationDate);
    }
}
