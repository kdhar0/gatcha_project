package com.gatcha.monster.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

/**
 * Modele representant un monstre d'un joueur
 * Chaque monstre a des stats, un element et 3 competences
 */
@Document(collection = "monsters")
public class Monster {

    @Id
    private String id;
    private int baseId;         // ID du monstre de base (template)
    private String ownerUsername; // Username du proprietaire
    private String element;     // fire, water, wind
    private int hp;
    private int atk;
    private int def;
    private int vit;
    private int level;
    private int experience;
    private int experienceToNextLevel;
    private int skillPoints;    // Points de competence a distribuer
    private List<Skill> skills;

    // Constructeur vide
    public Monster() {
        this.level = 1;
        this.experience = 0;
        this.experienceToNextLevel = 100;
        this.skillPoints = 0;
    }

    /**
     * Ajoute de l'experience au monstre
     * Retourne true si le monstre a gagne un niveau
     */
    public boolean addExperience(int amount) {
        this.experience += amount;
        boolean leveledUp = false;

        while (this.experience >= this.experienceToNextLevel) {
            this.experience -= this.experienceToNextLevel;
            levelUp();
            leveledUp = true;
        }

        return leveledUp;
    }

    /**
     * Fait monter le monstre de niveau
     */
    private void levelUp() {
        this.level++;

        // Augmenter les stats de 5%
        this.hp = (int) (this.hp * 1.05);
        this.atk = (int) (this.atk * 1.05);
        this.def = (int) (this.def * 1.05);
        this.vit = (int) (this.vit * 1.05);

        // Augmenter le seuil d'XP
        this.experienceToNextLevel = (int) (this.experienceToNextLevel * 1.1);

        // Gagner un point de competence
        this.skillPoints++;
    }

    /**
     * Utilise un point de competence pour ameliorer une competence
     */
    public boolean upgradeSkill(int skillNum) {
        if (this.skillPoints <= 0) {
            return false;
        }

        for (Skill skill : skills) {
            if (skill.getNum() == skillNum) {
                if (skill.upgrade()) {
                    this.skillPoints--;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Recupere la valeur d'une stat par son nom
     */
    public int getStatValue(String statName) {
        switch (statName.toLowerCase()) {
            case "hp": return hp;
            case "atk": return atk;
            case "def": return def;
            case "vit": return vit;
            default: return 0;
        }
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getBaseId() {
        return baseId;
    }

    public void setBaseId(int baseId) {
        this.baseId = baseId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAtk() {
        return atk;
    }

    public void setAtk(int atk) {
        this.atk = atk;
    }

    public int getDef() {
        return def;
    }

    public void setDef(int def) {
        this.def = def;
    }

    public int getVit() {
        return vit;
    }

    public void setVit(int vit) {
        this.vit = vit;
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

    public int getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }
}
