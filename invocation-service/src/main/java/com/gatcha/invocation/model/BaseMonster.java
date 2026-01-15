package com.gatcha.invocation.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;
import java.util.Map;

/**
 * Modele pour les monstres de base (templates)
 * Contient les stats de base et le taux de loot
 */
@Document(collection = "base_monsters")
public class BaseMonster {

    @Id
    private Integer baseId;      // Utilise _id de MongoDB directement
    private String element;
    private int hp;
    private int atk;
    private int def;
    private int vit;
    private List<Map<String, Object>> skills;
    private double lootRate;    // Taux de drop (entre 0 et 1)

    // Constructeur vide
    public BaseMonster() {
    }

    // Getters et Setters
    public Integer getBaseId() {
        return baseId;
    }

    public void setBaseId(Integer baseId) {
        this.baseId = baseId;
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

    public List<Map<String, Object>> getSkills() {
        return skills;
    }

    public void setSkills(List<Map<String, Object>> skills) {
        this.skills = skills;
    }

    public double getLootRate() {
        return lootRate;
    }

    public void setLootRate(double lootRate) {
        this.lootRate = lootRate;
    }

    /**
     * Convertit en Map pour l'envoi a l'API Monster
     */
    public Map<String, Object> toMap() {
        return Map.of(
            "_id", baseId,
            "element", element,
            "hp", hp,
            "atk", atk,
            "def", def,
            "vit", vit,
            "skills", skills
        );
    }
}
