package com.gatcha.monster.model;

/**
 * Modele representant une competence de monstre
 * - dmg: degats de base
 * - ratio: bonus de degats base sur une stat
 * - cooldown: nombre de tours avant reutilisation
 * - level: niveau actuel de la competence
 * - lvlMax: niveau maximum de la competence
 */
public class Skill {

    private int num;           // Numero de la competence (1, 2, ou 3)
    private int dmg;           // Degats de base
    private Ratio ratio;       // Ratio de degats supplementaires
    private int cooldown;      // Cooldown en tours
    private int level;         // Niveau actuel
    private int lvlMax;        // Niveau maximum

    // Constructeur vide
    public Skill() {
        this.level = 1;
    }

    // Constructeur complet
    public Skill(int num, int dmg, Ratio ratio, int cooldown, int lvlMax) {
        this.num = num;
        this.dmg = dmg;
        this.ratio = ratio;
        this.cooldown = cooldown;
        this.level = 1;
        this.lvlMax = lvlMax;
    }

    /**
     * Calcule les degats totaux de la competence en fonction d'une stat
     */
    public int calculateDamage(int statValue) {
        double bonusDamage = statValue * (ratio.getPercent() / 100.0);
        return (int) (dmg + bonusDamage);
    }

    /**
     * Ameliore la competence d'un niveau
     */
    public boolean upgrade() {
        if (level >= lvlMax) {
            return false;
        }
        level++;
        // Augmenter les degats de 10% par niveau
        dmg = (int) (dmg * 1.1);
        return true;
    }

    // Getters et Setters
    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getDmg() {
        return dmg;
    }

    public void setDmg(int dmg) {
        this.dmg = dmg;
    }

    public Ratio getRatio() {
        return ratio;
    }

    public void setRatio(Ratio ratio) {
        this.ratio = ratio;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLvlMax() {
        return lvlMax;
    }

    public void setLvlMax(int lvlMax) {
        this.lvlMax = lvlMax;
    }
}
