package com.gatcha.combat.dto.response;

import java.time.LocalDateTime;

/**
 * DTO resume d'un combat pour l'historique
 */
public class CombatSummaryDto {

    private int combatNumber;
    private LocalDateTime timestamp;
    private String player1;
    private String player1Element;
    private String player2;
    private String player2Element;
    private String winnerName;
    private int totalTurns;

    public CombatSummaryDto() {}

    public int getCombatNumber() {
        return combatNumber;
    }

    public void setCombatNumber(int combatNumber) {
        this.combatNumber = combatNumber;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer1Element() {
        return player1Element;
    }

    public void setPlayer1Element(String player1Element) {
        this.player1Element = player1Element;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getPlayer2Element() {
        return player2Element;
    }

    public void setPlayer2Element(String player2Element) {
        this.player2Element = player2Element;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public int getTotalTurns() {
        return totalTurns;
    }

    public void setTotalTurns(int totalTurns) {
        this.totalTurns = totalTurns;
    }
}
