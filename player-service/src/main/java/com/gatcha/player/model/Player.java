package com.gatcha.player.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;

/**
 * Modele representant un joueur
 * - level: va de 0 a 50
 * - experience: commence a 50 pour level up, puis *1.1 a chaque niveau
 * - monstres: liste limitee par le niveau (10 + level)
 */
@Document(collection = "players")
public class Player {

    @Id
    private String id;
    private String username;
    private int level;
    private int experience;
    private int experienceToNextLevel;
    private List<String> monsterIds; // IDs des monstres possedes

    // Constructeur vide
    public Player() {
        this.level = 0;
        this.experience = 0;
        this.experienceToNextLevel = 50;
        this.monsterIds = new ArrayList<>();
    }

    // Constructeur avec username
    public Player(String username) {
        this.username = username;
        this.level = 0;
        this.experience = 0;
        this.experienceToNextLevel = 50;
        this.monsterIds = new ArrayList<>();
    }

    /**
     * Calcule la taille maximale de la liste de monstres
     * Commence a 10 puis +1 par niveau
     */
    public int getMaxMonsters() {
        return 10 + level;
    }

    /**
     * Verifie si le joueur peut ajouter un monstre
     */
    public boolean canAddMonster() {
        return monsterIds.size() < getMaxMonsters();
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getExperienceToNextLevel() {
        return experienceToNextLevel;
    }

    public void setExperienceToNextLevel(int experienceToNextLevel) {
        this.experienceToNextLevel = experienceToNextLevel;
    }

    public List<String> getMonsterIds() {
        return monsterIds;
    }

    public void setMonsterIds(List<String> monsterIds) {
        this.monsterIds = monsterIds;
    }
}
