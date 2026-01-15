package com.gatcha.combat.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Representation d'un combattant (monstre) pendant le combat
 */
public class Fighter {

    private String monsterId;
    private String ownerUsername;
    private String element;
    private int maxHp;
    private int currentHp;
    private int atk;
    private int def;
    private int vit;
    private List<CombatSkill> skills;

    public Fighter() {
        this.skills = new ArrayList<>();
    }

    /**
     * Verifie si le combattant est encore en vie
     */
    public boolean isAlive() {
        return currentHp > 0;
    }

    /**
     * Recoit des degats (reduits par la defense)
     */
    public int takeDamage(int rawDamage) {
        // Formule: degats = degats_bruts * (100 / (100 + def))
        double reduction = 100.0 / (100.0 + def);
        int actualDamage = (int) (rawDamage * reduction);
        actualDamage = Math.max(1, actualDamage); // Minimum 1 degat

        currentHp -= actualDamage;
        if (currentHp < 0) currentHp = 0;

        return actualDamage;
    }

    /**
     * Selectionne la meilleure competence disponible
     * Priorite: competence avec le plus grand numero dont le cooldown est a 0
     */
    public CombatSkill selectBestSkill() {
        return skills.stream()
                .filter(CombatSkill::isReady)
                .max(Comparator.comparingInt(CombatSkill::getNum))
                .orElse(null);
    }

    /**
     * Reduit les cooldowns de toutes les competences
     */
    public void reduceAllCooldowns() {
        skills.forEach(CombatSkill::reduceCooldown);
    }

    /**
     * Recupere la valeur d'une stat par son nom
     */
    public int getStatValue(String statName) {
        return switch (statName.toLowerCase()) {
            case "hp" -> maxHp;
            case "atk" -> atk;
            case "def" -> def;
            case "vit" -> vit;
            default -> 0;
        };
    }

    // Getters et Setters
    public String getMonsterId() { return monsterId; }
    public void setMonsterId(String monsterId) { this.monsterId = monsterId; }
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public String getElement() { return element; }
    public void setElement(String element) { this.element = element; }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
    public int getAtk() { return atk; }
    public void setAtk(int atk) { this.atk = atk; }
    public int getDef() { return def; }
    public void setDef(int def) { this.def = def; }
    public int getVit() { return vit; }
    public void setVit(int vit) { this.vit = vit; }
    public List<CombatSkill> getSkills() { return skills; }
    public void setSkills(List<CombatSkill> skills) { this.skills = skills; }
}
