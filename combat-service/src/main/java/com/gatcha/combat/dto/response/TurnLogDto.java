package com.gatcha.combat.dto.response;

/**
 * DTO representant le log d'un tour de combat
 */
public class TurnLogDto {

    private int turn;
    private String attacker;
    private String defender;
    private int skillUsed;
    private int damage;
    private int defenderHpBefore;
    private int defenderHpAfter;
    private String description;

    public TurnLogDto() {}

    public TurnLogDto(int turn, String attacker, String defender, int skillUsed,
                      int damage, int defenderHpBefore, int defenderHpAfter, String description) {
        this.turn = turn;
        this.attacker = attacker;
        this.defender = defender;
        this.skillUsed = skillUsed;
        this.damage = damage;
        this.defenderHpBefore = defenderHpBefore;
        this.defenderHpAfter = defenderHpAfter;
        this.description = description;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public String getAttacker() {
        return attacker;
    }

    public void setAttacker(String attacker) {
        this.attacker = attacker;
    }

    public String getDefender() {
        return defender;
    }

    public void setDefender(String defender) {
        this.defender = defender;
    }

    public int getSkillUsed() {
        return skillUsed;
    }

    public void setSkillUsed(int skillUsed) {
        this.skillUsed = skillUsed;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDefenderHpBefore() {
        return defenderHpBefore;
    }

    public void setDefenderHpBefore(int defenderHpBefore) {
        this.defenderHpBefore = defenderHpBefore;
    }

    public int getDefenderHpAfter() {
        return defenderHpAfter;
    }

    public void setDefenderHpAfter(int defenderHpAfter) {
        this.defenderHpAfter = defenderHpAfter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
