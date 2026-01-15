package com.gatcha.combat.dto.response;

/**
 * DTO representant le resultat d'un combattant apres le combat
 */
public class FighterResultDto {

    private String playerName;
    private String element;
    private int hpStart;
    private int hpEnd;
    private boolean isWinner;

    public FighterResultDto() {}

    public FighterResultDto(String playerName, String element, int hpStart, int hpEnd, boolean isWinner) {
        this.playerName = playerName;
        this.element = element;
        this.hpStart = hpStart;
        this.hpEnd = hpEnd;
        this.isWinner = isWinner;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public int getHpStart() {
        return hpStart;
    }

    public void setHpStart(int hpStart) {
        this.hpStart = hpStart;
    }

    public int getHpEnd() {
        return hpEnd;
    }

    public void setHpEnd(int hpEnd) {
        this.hpEnd = hpEnd;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public void setWinner(boolean winner) {
        isWinner = winner;
    }
}
