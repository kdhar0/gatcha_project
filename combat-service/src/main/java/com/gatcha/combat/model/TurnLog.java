package com.gatcha.combat.model;

/**
 * Log d'un tour de combat
 */
public class TurnLog {

    private int turnNumber;
    private String attackerId;
    private String attackerOwner;
    private String defenderId;
    private String defenderOwner;
    private int skillUsed;
    private int rawDamage;
    private int actualDamage;
    private int defenderHpBefore;
    private int defenderHpAfter;
    private String description;

    public TurnLog() {}

    public TurnLog(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    // Builder pattern pour construction fluide
    public TurnLog attacker(String id, String owner) {
        this.attackerId = id;
        this.attackerOwner = owner;
        return this;
    }

    public TurnLog defender(String id, String owner) {
        this.defenderId = id;
        this.defenderOwner = owner;
        return this;
    }

    public TurnLog skill(int skillNum) {
        this.skillUsed = skillNum;
        return this;
    }

    public TurnLog damage(int raw, int actual) {
        this.rawDamage = raw;
        this.actualDamage = actual;
        return this;
    }

    public TurnLog defenderHp(int before, int after) {
        this.defenderHpBefore = before;
        this.defenderHpAfter = after;
        return this;
    }

    public TurnLog describe(String desc) {
        this.description = desc;
        return this;
    }

    // Getters et Setters
    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }
    public String getAttackerId() { return attackerId; }
    public void setAttackerId(String attackerId) { this.attackerId = attackerId; }
    public String getAttackerOwner() { return attackerOwner; }
    public void setAttackerOwner(String attackerOwner) { this.attackerOwner = attackerOwner; }
    public String getDefenderId() { return defenderId; }
    public void setDefenderId(String defenderId) { this.defenderId = defenderId; }
    public String getDefenderOwner() { return defenderOwner; }
    public void setDefenderOwner(String defenderOwner) { this.defenderOwner = defenderOwner; }
    public int getSkillUsed() { return skillUsed; }
    public void setSkillUsed(int skillUsed) { this.skillUsed = skillUsed; }
    public int getRawDamage() { return rawDamage; }
    public void setRawDamage(int rawDamage) { this.rawDamage = rawDamage; }
    public int getActualDamage() { return actualDamage; }
    public void setActualDamage(int actualDamage) { this.actualDamage = actualDamage; }
    public int getDefenderHpBefore() { return defenderHpBefore; }
    public void setDefenderHpBefore(int defenderHpBefore) { this.defenderHpBefore = defenderHpBefore; }
    public int getDefenderHpAfter() { return defenderHpAfter; }
    public void setDefenderHpAfter(int defenderHpAfter) { this.defenderHpAfter = defenderHpAfter; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
