package com.gatcha.combat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Compteur pour generer des numeros de combat uniques
 */
@Document(collection = "counters")
public class CombatCounter {

    @Id
    private String id;
    private int sequence;

    public CombatCounter() {
        this.id = "combat_counter";
        this.sequence = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getSequence() { return sequence; }
    public void setSequence(int sequence) { this.sequence = sequence; }
}
