package com.gatcha.combat.model;

/**
 * Representation d'une competence pour le combat
 */
public class CombatSkill {

    private int num;
    private int dmg;
    private String ratioStat;
    private double ratioPercent;
    private int baseCooldown;
    private int currentCooldown;

    public CombatSkill() {
        this.currentCooldown = 0;
    }

    public CombatSkill(int num, int dmg, String ratioStat, double ratioPercent, int baseCooldown) {
        this.num = num;
        this.dmg = dmg;
        this.ratioStat = ratioStat;
        this.ratioPercent = ratioPercent;
        this.baseCooldown = baseCooldown;
        this.currentCooldown = 0;
    }

    /**
     * Calcule les degats de la competence
     */
    public int calculateDamage(int statValue) {
        return (int) (dmg + (statValue * ratioPercent / 100.0));
    }

    /**
     * Verifie si la competence est utilisable
     */
    public boolean isReady() {
        return currentCooldown == 0;
    }

    /**
     * Active le cooldown apres utilisation
     */
    public void use() {
        this.currentCooldown = this.baseCooldown;
    }

    /**
     * Reduit le cooldown d'un tour
     */
    public void reduceCooldown() {
        if (currentCooldown > 0) {
            currentCooldown--;
        }
    }

    // Getters et Setters
    public int getNum() { return num; }
    public void setNum(int num) { this.num = num; }
    public int getDmg() { return dmg; }
    public void setDmg(int dmg) { this.dmg = dmg; }
    public String getRatioStat() { return ratioStat; }
    public void setRatioStat(String ratioStat) { this.ratioStat = ratioStat; }
    public double getRatioPercent() { return ratioPercent; }
    public void setRatioPercent(double ratioPercent) { this.ratioPercent = ratioPercent; }
    public int getBaseCooldown() { return baseCooldown; }
    public void setBaseCooldown(int baseCooldown) { this.baseCooldown = baseCooldown; }
    public int getCurrentCooldown() { return currentCooldown; }
    public void setCurrentCooldown(int currentCooldown) { this.currentCooldown = currentCooldown; }
}
