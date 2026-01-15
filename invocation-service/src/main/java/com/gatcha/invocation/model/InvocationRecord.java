package com.gatcha.invocation.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Enregistrement d'une invocation dans la base tampon
 * Permet de recrer les invocations en cas de probleme
 */
@Document(collection = "invocation_records")
public class InvocationRecord {

    @Id
    private String id;
    private String username;
    private int baseMonsterIdNumeric;  // ID numerique du monstre de base
    private String createdMonsterId;    // ID du monstre cree (retourne par l'API Monster)
    private LocalDateTime timestamp;
    private InvocationStatus status;

    public enum InvocationStatus {
        PENDING,            // Invocation en cours
        MONSTER_CREATED,    // Monstre cree dans l'API Monster
        PLAYER_UPDATED,     // Monstre ajoute au joueur
        COMPLETED,          // Invocation terminee avec succes
        FAILED              // Invocation echouee
    }

    // Constructeur vide
    public InvocationRecord() {
        this.timestamp = LocalDateTime.now();
        this.status = InvocationStatus.PENDING;
    }

    // Constructeur avec parametres
    public InvocationRecord(String username, int baseMonsterIdNumeric) {
        this.username = username;
        this.baseMonsterIdNumeric = baseMonsterIdNumeric;
        this.timestamp = LocalDateTime.now();
        this.status = InvocationStatus.PENDING;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getBaseMonsterIdNumeric() {
        return baseMonsterIdNumeric;
    }

    public void setBaseMonsterIdNumeric(int baseMonsterIdNumeric) {
        this.baseMonsterIdNumeric = baseMonsterIdNumeric;
    }

    public String getCreatedMonsterId() {
        return createdMonsterId;
    }

    public void setCreatedMonsterId(String createdMonsterId) {
        this.createdMonsterId = createdMonsterId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public InvocationStatus getStatus() {
        return status;
    }

    public void setStatus(InvocationStatus status) {
        this.status = status;
    }
}
