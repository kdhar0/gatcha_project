package com.gatcha.combat.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de reponse pour un combat
 */
public class CombatResponseDto {

    private int combatNumber;
    private LocalDateTime timestamp;
    private String winnerName;
    private int totalTurns;
    private FighterResultDto fighter1;
    private FighterResultDto fighter2;
    private List<TurnLogDto> turnLogs;

    public CombatResponseDto() {}

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

    public FighterResultDto getFighter1() {
        return fighter1;
    }

    public void setFighter1(FighterResultDto fighter1) {
        this.fighter1 = fighter1;
    }

    public FighterResultDto getFighter2() {
        return fighter2;
    }

    public void setFighter2(FighterResultDto fighter2) {
        this.fighter2 = fighter2;
    }

    public List<TurnLogDto> getTurnLogs() {
        return turnLogs;
    }

    public void setTurnLogs(List<TurnLogDto> turnLogs) {
        this.turnLogs = turnLogs;
    }
}
