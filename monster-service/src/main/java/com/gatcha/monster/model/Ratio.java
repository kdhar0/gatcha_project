package com.gatcha.monster.model;

/**
 * Modele pour le ratio de degats supplementaires d'une competence
 * - stat: la statistique utilisee (hp, atk, def, vit)
 * - percent: le pourcentage de cette stat ajoute aux degats
 */
public class Ratio {

    private String stat;    // "hp", "atk", "def", ou "vit"
    private double percent; // Pourcentage de la stat

    // Constructeur vide
    public Ratio() {
    }

    // Constructeur avec parametres
    public Ratio(String stat, double percent) {
        this.stat = stat;
        this.percent = percent;
    }

    // Getters et Setters
    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }
}
