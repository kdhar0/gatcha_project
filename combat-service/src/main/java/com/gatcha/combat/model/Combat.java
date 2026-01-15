package com.gatcha.combat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modele principal representant un combat complet
 */
@Document(collection = "combats")
public class Combat {

    @Id
    private String id;

    private int combatNumber;
    private LocalDateTime timestamp;

    // Informations des combattants
    private String fighter1MonsterId;
    private String fighter1Owner;
    private String fighter1Element;
    private int fighter1InitialHp;
    private int fighter1FinalHp;

    private String fighter2MonsterId;
    private String fighter2Owner;
    private String fighter2Element;
    private int fighter2InitialHp;
    private int fighter2FinalHp;

    // Resultat
    private String winnerMonsterId;
    private String winnerOwner;
    private int totalTurns;

    // Logs detailles
    private List<TurnLog> turnLogs;

    public Combat() {
        this.timestamp = LocalDateTime.now();
        this.turnLogs = new ArrayList<>();
    }

    public void addTurnLog(TurnLog log) {
        this.turnLogs.add(log);
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getCombatNumber() { return combatNumber; }
    public void setCombatNumber(int combatNumber) { this.combatNumber = combatNumber; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getFighter1MonsterId() { return fighter1MonsterId; }
    public void setFighter1MonsterId(String fighter1MonsterId) { this.fighter1MonsterId = fighter1MonsterId; }
    public String getFighter1Owner() { return fighter1Owner; }
    public void setFighter1Owner(String fighter1Owner) { this.fighter1Owner = fighter1Owner; }
    public String getFighter1Element() { return fighter1Element; }
    public void setFighter1Element(String fighter1Element) { this.fighter1Element = fighter1Element; }
    public int getFighter1InitialHp() { return fighter1InitialHp; }
    public void setFighter1InitialHp(int fighter1InitialHp) { this.fighter1InitialHp = fighter1InitialHp; }
    public int getFighter1FinalHp() { return fighter1FinalHp; }
    public void setFighter1FinalHp(int fighter1FinalHp) { this.fighter1FinalHp = fighter1FinalHp; }

    public String getFighter2MonsterId() { return fighter2MonsterId; }
    public void setFighter2MonsterId(String fighter2MonsterId) { this.fighter2MonsterId = fighter2MonsterId; }
    public String getFighter2Owner() { return fighter2Owner; }
    public void setFighter2Owner(String fighter2Owner) { this.fighter2Owner = fighter2Owner; }
    public String getFighter2Element() { return fighter2Element; }
    public void setFighter2Element(String fighter2Element) { this.fighter2Element = fighter2Element; }
    public int getFighter2InitialHp() { return fighter2InitialHp; }
    public void setFighter2InitialHp(int fighter2InitialHp) { this.fighter2InitialHp = fighter2InitialHp; }
    public int getFighter2FinalHp() { return fighter2FinalHp; }
    public void setFighter2FinalHp(int fighter2FinalHp) { this.fighter2FinalHp = fighter2FinalHp; }

    public String getWinnerMonsterId() { return winnerMonsterId; }
    public void setWinnerMonsterId(String winnerMonsterId) { this.winnerMonsterId = winnerMonsterId; }
    public String getWinnerOwner() { return winnerOwner; }
    public void setWinnerOwner(String winnerOwner) { this.winnerOwner = winnerOwner; }
    public int getTotalTurns() { return totalTurns; }
    public void setTotalTurns(int totalTurns) { this.totalTurns = totalTurns; }
    public List<TurnLog> getTurnLogs() { return turnLogs; }
    public void setTurnLogs(List<TurnLog> turnLogs) { this.turnLogs = turnLogs; }
}
