package com.gatcha.combat.service;

import com.gatcha.combat.model.*;
import com.gatcha.combat.repository.CombatCounterRepository;
import com.gatcha.combat.repository.CombatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service principal gerant la logique de combat
 */
@Service
public class CombatService {

    private static final int MAX_TURNS = 100;

    @Autowired
    private CombatRepository combatRepository;

    @Autowired
    private CombatCounterRepository counterRepository;

    /**
     * Execute un combat entre deux Fighters
     */
    public Combat executeCombat(Fighter fighter1, Fighter fighter2) {
        Combat combat = new Combat();
        combat.setCombatNumber(getNextCombatNumber());

        combat.setFighter1MonsterId(fighter1.getMonsterId());
        combat.setFighter1Owner(fighter1.getOwnerUsername());
        combat.setFighter1Element(fighter1.getElement());
        combat.setFighter1InitialHp(fighter1.getMaxHp());

        combat.setFighter2MonsterId(fighter2.getMonsterId());
        combat.setFighter2Owner(fighter2.getOwnerUsername());
        combat.setFighter2Element(fighter2.getElement());
        combat.setFighter2InitialHp(fighter2.getMaxHp());

        Fighter first, second;
        if (fighter1.getVit() >= fighter2.getVit()) {
            first = fighter1;
            second = fighter2;
        } else {
            first = fighter2;
            second = fighter1;
        }

        int turn = 0;
        while (first.isAlive() && second.isAlive() && turn < MAX_TURNS) {
            turn++;

            if (first.isAlive() && second.isAlive()) {
                TurnLog log = executeTurn(turn, first, second);
                combat.addTurnLog(log);
            }

            if (first.isAlive() && second.isAlive()) {
                turn++;
                TurnLog log = executeTurn(turn, second, first);
                combat.addTurnLog(log);
            }

            first.reduceAllCooldowns();
            second.reduceAllCooldowns();
        }

        Fighter winner = first.isAlive() ? first : second;

        combat.setWinnerMonsterId(winner.getMonsterId());
        combat.setWinnerOwner(winner.getOwnerUsername());
        combat.setTotalTurns(turn);

        combat.setFighter1FinalHp(fighter1.getCurrentHp());
        combat.setFighter2FinalHp(fighter2.getCurrentHp());

        return combatRepository.save(combat);
    }

    /**
     * Execute un tour de combat
     */
    private TurnLog executeTurn(int turnNumber, Fighter attacker, Fighter defender) {
        TurnLog log = new TurnLog(turnNumber);
        log.attacker(attacker.getMonsterId(), attacker.getOwnerUsername());
        log.defender(defender.getMonsterId(), defender.getOwnerUsername());

        CombatSkill skill = attacker.selectBestSkill();

        if (skill == null) {
            int rawDamage = attacker.getAtk();
            int actualDamage = defender.takeDamage(rawDamage);

            log.skill(0);
            log.damage(rawDamage, actualDamage);
            log.defenderHp(defender.getCurrentHp() + actualDamage, defender.getCurrentHp());
            log.describe(String.format("%s utilise une attaque basique et inflige %d degats",
                    attacker.getOwnerUsername(), actualDamage));
        } else {
            int statValue = attacker.getStatValue(skill.getRatioStat());
            int rawDamage = skill.calculateDamage(statValue);
            int hpBefore = defender.getCurrentHp();
            int actualDamage = defender.takeDamage(rawDamage);

            skill.use();

            log.skill(skill.getNum());
            log.damage(rawDamage, actualDamage);
            log.defenderHp(hpBefore, defender.getCurrentHp());
            log.describe(String.format("%s utilise Competence %d et inflige %d degats (HP: %d -> %d)",
                    attacker.getOwnerUsername(), skill.getNum(), actualDamage, hpBefore, defender.getCurrentHp()));
        }

        return log;
    }

    /**
     * Genere le prochain numero de combat
     */
    private synchronized int getNextCombatNumber() {
        CombatCounter counter = counterRepository.findById("combat_counter")
                .orElse(new CombatCounter());
        counter.setSequence(counter.getSequence() + 1);
        counterRepository.save(counter);
        return counter.getSequence();
    }

    /**
     * Recupere un combat par son numero (pour la rediffusion)
     */
    public Combat getCombatByNumber(int combatNumber) {
        return combatRepository.findByCombatNumber(combatNumber)
                .orElseThrow(() -> new RuntimeException("Combat #" + combatNumber + " non trouve"));
    }

    /**
     * Recupere l'historique des combats
     */
    public List<Combat> getCombatHistory() {
        return combatRepository.findAllByOrderByTimestampDesc();
    }

    /**
     * Recupere les combats d'un joueur
     */
    public List<Combat> getPlayerCombats(String playerName) {
        return combatRepository.findByFighter1OwnerOrFighter2Owner(playerName, playerName);
    }
}
