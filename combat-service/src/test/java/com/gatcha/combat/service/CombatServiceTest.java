package com.gatcha.combat.service;

import com.gatcha.combat.model.*;
import com.gatcha.combat.repository.CombatCounterRepository;
import com.gatcha.combat.repository.CombatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CombatServiceTest {

    @Mock
    private CombatRepository combatRepository;

    @Mock
    private CombatCounterRepository counterRepository;

    @InjectMocks
    private CombatService combatService;

    private Fighter fighter1;
    private Fighter fighter2;
    private Combat combat;
    private CombatCounter counter;

    @BeforeEach
    void setUp() {
        // Configuration du premier combattant
        fighter1 = new Fighter();
        fighter1.setMonsterId("monster1");
        fighter1.setOwnerUsername("player1");
        fighter1.setElement("fire");
        fighter1.setMaxHp(1000);
        fighter1.setCurrentHp(1000);
        fighter1.setAtk(100);
        fighter1.setDef(50);
        fighter1.setVit(80);

        List<CombatSkill> skills1 = new ArrayList<>();
        skills1.add(new CombatSkill(1, 50, "atk", 50.0, 0));
        skills1.add(new CombatSkill(2, 100, "atk", 75.0, 2));
        skills1.add(new CombatSkill(3, 200, "atk", 100.0, 4));
        fighter1.setSkills(skills1);

        // Configuration du second combattant
        fighter2 = new Fighter();
        fighter2.setMonsterId("monster2");
        fighter2.setOwnerUsername("player2");
        fighter2.setElement("water");
        fighter2.setMaxHp(800);
        fighter2.setCurrentHp(800);
        fighter2.setAtk(120);
        fighter2.setDef(30);
        fighter2.setVit(90); // Plus rapide que fighter1

        List<CombatSkill> skills2 = new ArrayList<>();
        skills2.add(new CombatSkill(1, 60, "atk", 40.0, 0));
        skills2.add(new CombatSkill(2, 120, "atk", 60.0, 3));
        fighter2.setSkills(skills2);

        // Configuration du combat
        combat = new Combat();
        combat.setId("combat123");
        combat.setCombatNumber(1);
        combat.setFighter1MonsterId("monster1");
        combat.setFighter1Owner("player1");
        combat.setFighter2MonsterId("monster2");
        combat.setFighter2Owner("player2");

        // Configuration du compteur
        counter = new CombatCounter();
        counter.setSequence(0);
    }

    // ========== Tests pour executeCombat ==========

    @Test
    void executeCombat_ok_returnsCompletedCombat() {
        when(counterRepository.findById("combat_counter")).thenReturn(Optional.of(counter));
        when(counterRepository.save(any(CombatCounter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(combatRepository.save(any(Combat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Combat result = combatService.executeCombat(fighter1, fighter2);

        assertNotNull(result);
        assertNotNull(result.getWinnerMonsterId());
        assertTrue(result.getTotalTurns() > 0);
        assertFalse(result.getTurnLogs().isEmpty());
        verify(combatRepository).save(any(Combat.class));
    }

    @Test
    void executeCombat_fasterFighterGoesFirst() {
        // fighter2 a plus de vitesse (90 > 80), donc il attaque en premier
        when(counterRepository.findById("combat_counter")).thenReturn(Optional.of(counter));
        when(counterRepository.save(any(CombatCounter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(combatRepository.save(any(Combat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Combat result = combatService.executeCombat(fighter1, fighter2);

        // Le premier tour devrait etre fait par fighter2 (le plus rapide)
        TurnLog firstTurn = result.getTurnLogs().get(0);
        assertEquals("monster2", firstTurn.getAttackerId());
        assertEquals("player2", firstTurn.getAttackerOwner());
    }

    @Test
    void executeCombat_fighterWithHigherVitGoesFirst_whenEqual() {
        fighter1.setVit(90); // Maintenant egal a fighter2
        when(counterRepository.findById("combat_counter")).thenReturn(Optional.of(counter));
        when(counterRepository.save(any(CombatCounter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(combatRepository.save(any(Combat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Combat result = combatService.executeCombat(fighter1, fighter2);

        // Quand vitesse egale, fighter1 (le premier passe en parametre) attaque en premier
        TurnLog firstTurn = result.getTurnLogs().get(0);
        assertEquals("monster1", firstTurn.getAttackerId());
    }

    @Test
    void executeCombat_hasWinner() {
        when(counterRepository.findById("combat_counter")).thenReturn(Optional.of(counter));
        when(counterRepository.save(any(CombatCounter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(combatRepository.save(any(Combat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Combat result = combatService.executeCombat(fighter1, fighter2);

        assertNotNull(result.getWinnerMonsterId());
        assertNotNull(result.getWinnerOwner());
        assertTrue(result.getWinnerMonsterId().equals("monster1") ||
                   result.getWinnerMonsterId().equals("monster2"));
    }

    @Test
    void executeCombat_recordsTurnLogs() {
        when(counterRepository.findById("combat_counter")).thenReturn(Optional.of(counter));
        when(counterRepository.save(any(CombatCounter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(combatRepository.save(any(Combat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Combat result = combatService.executeCombat(fighter1, fighter2);

        assertFalse(result.getTurnLogs().isEmpty());
        for (TurnLog log : result.getTurnLogs()) {
            assertTrue(log.getTurnNumber() > 0);
            assertNotNull(log.getAttackerId());
            assertNotNull(log.getDefenderId());
            assertTrue(log.getActualDamage() > 0);
        }
    }

    @Test
    void executeCombat_incrementsCombatNumber() {
        counter.setSequence(5);
        when(counterRepository.findById("combat_counter")).thenReturn(Optional.of(counter));
        when(counterRepository.save(any(CombatCounter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(combatRepository.save(any(Combat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Combat result = combatService.executeCombat(fighter1, fighter2);

        assertEquals(6, result.getCombatNumber());
    }

    @Test
    void executeCombat_createsCounterIfNotExists() {
        when(counterRepository.findById("combat_counter")).thenReturn(Optional.empty());
        when(counterRepository.save(any(CombatCounter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(combatRepository.save(any(Combat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Combat result = combatService.executeCombat(fighter1, fighter2);

        assertEquals(1, result.getCombatNumber());
    }

    @Test
    void executeCombat_stopsAtMaxTurns() {
        // Configurer des combattants avec beaucoup de HP pour forcer le max de tours
        fighter1.setMaxHp(100000);
        fighter1.setCurrentHp(100000);
        fighter1.setDef(1000);
        fighter2.setMaxHp(100000);
        fighter2.setCurrentHp(100000);
        fighter2.setDef(1000);

        when(counterRepository.findById("combat_counter")).thenReturn(Optional.of(counter));
        when(counterRepository.save(any(CombatCounter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(combatRepository.save(any(Combat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Combat result = combatService.executeCombat(fighter1, fighter2);

        assertTrue(result.getTotalTurns() <= 100);
    }

    // ========== Tests pour getCombatByNumber ==========

    @Test
    void getCombatByNumber_ok_returnsCombat() {
        when(combatRepository.findByCombatNumber(1)).thenReturn(Optional.of(combat));

        Combat result = combatService.getCombatByNumber(1);

        assertEquals(1, result.getCombatNumber());
        assertEquals("combat123", result.getId());
    }

    @Test
    void getCombatByNumber_notFound_throws() {
        when(combatRepository.findByCombatNumber(999)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> combatService.getCombatByNumber(999));

        assertEquals("Combat #999 non trouve", ex.getMessage());
    }

    // ========== Tests pour getCombatHistory ==========

    @Test
    void getCombatHistory_ok_returnsAllCombats() {
        Combat combat2 = new Combat();
        combat2.setCombatNumber(2);
        List<Combat> combats = Arrays.asList(combat, combat2);

        when(combatRepository.findAllByOrderByTimestampDesc()).thenReturn(combats);

        List<Combat> result = combatService.getCombatHistory();

        assertEquals(2, result.size());
        verify(combatRepository).findAllByOrderByTimestampDesc();
    }

    @Test
    void getCombatHistory_empty_returnsEmptyList() {
        when(combatRepository.findAllByOrderByTimestampDesc()).thenReturn(new ArrayList<>());

        List<Combat> result = combatService.getCombatHistory();

        assertTrue(result.isEmpty());
    }

    // ========== Tests pour getPlayerCombats ==========

    @Test
    void getPlayerCombats_ok_returnsCombatsForPlayer() {
        List<Combat> combats = Arrays.asList(combat);
        when(combatRepository.findByFighter1OwnerOrFighter2Owner("player1", "player1"))
                .thenReturn(combats);

        List<Combat> result = combatService.getPlayerCombats("player1");

        assertEquals(1, result.size());
        verify(combatRepository).findByFighter1OwnerOrFighter2Owner("player1", "player1");
    }

    @Test
    void getPlayerCombats_noCombats_returnsEmptyList() {
        when(combatRepository.findByFighter1OwnerOrFighter2Owner("newPlayer", "newPlayer"))
                .thenReturn(new ArrayList<>());

        List<Combat> result = combatService.getPlayerCombats("newPlayer");

        assertTrue(result.isEmpty());
    }

    // ========== Tests pour Fighter ==========

    @Test
    void fighter_isAlive_returnsTrueWhenHpPositive() {
        assertTrue(fighter1.isAlive());
    }

    @Test
    void fighter_isAlive_returnsFalseWhenHpZero() {
        fighter1.setCurrentHp(0);
        assertFalse(fighter1.isAlive());
    }

    @Test
    void fighter_takeDamage_reducesDamageByDefense() {
        // Formule: degats = degats_bruts * (100 / (100 + def))
        // Avec def = 50 et degats bruts = 100: 100 * (100/150) = 66.67
        int actualDamage = fighter1.takeDamage(100);

        assertTrue(actualDamage < 100); // Reduit par la defense
        assertTrue(actualDamage >= 1);  // Minimum 1 degat
        assertEquals(1000 - actualDamage, fighter1.getCurrentHp());
    }

    @Test
    void fighter_takeDamage_minimumOneDamage() {
        fighter1.setDef(10000); // Defense tres elevee
        int actualDamage = fighter1.takeDamage(1);

        assertEquals(1, actualDamage); // Minimum 1 degat
    }

    @Test
    void fighter_takeDamage_hpCannotGoBelowZero() {
        fighter1.setCurrentHp(10);
        fighter1.setDef(0);
        fighter1.takeDamage(100);

        assertEquals(0, fighter1.getCurrentHp());
    }

    @Test
    void fighter_selectBestSkill_selectsHighestReadySkill() {
        // Toutes les competences sont pretes (cooldown = 0)
        CombatSkill bestSkill = fighter1.selectBestSkill();

        assertEquals(3, bestSkill.getNum()); // La competence 3 a le plus grand numero
    }

    @Test
    void fighter_selectBestSkill_skipsSkillsOnCooldown() {
        fighter1.getSkills().get(2).use(); // Mettre skill 3 en cooldown
        CombatSkill bestSkill = fighter1.selectBestSkill();

        assertEquals(2, bestSkill.getNum()); // Skill 3 en cooldown, donc skill 2
    }

    @Test
    void fighter_selectBestSkill_returnsNullWhenAllOnCooldown() {
        for (CombatSkill skill : fighter1.getSkills()) {
            skill.use();
            skill.setCurrentCooldown(10); // Forcer un long cooldown
        }

        CombatSkill bestSkill = fighter1.selectBestSkill();

        assertNull(bestSkill);
    }

    @Test
    void fighter_reduceAllCooldowns_reducesAllCooldownsByOne() {
        fighter1.getSkills().get(1).use(); // Cooldown = 2
        fighter1.getSkills().get(2).use(); // Cooldown = 4

        fighter1.reduceAllCooldowns();

        assertEquals(1, fighter1.getSkills().get(1).getCurrentCooldown());
        assertEquals(3, fighter1.getSkills().get(2).getCurrentCooldown());
    }

    @Test
    void fighter_getStatValue_returnsCorrectValues() {
        assertEquals(1000, fighter1.getStatValue("hp"));
        assertEquals(100, fighter1.getStatValue("atk"));
        assertEquals(50, fighter1.getStatValue("def"));
        assertEquals(80, fighter1.getStatValue("vit"));
        assertEquals(0, fighter1.getStatValue("unknown"));
    }

    // ========== Tests pour CombatSkill ==========

    @Test
    void combatSkill_calculateDamage_addsStatBonus() {
        CombatSkill skill = new CombatSkill(1, 50, "atk", 50.0, 0);
        // dmg + (statValue * ratioPercent / 100)
        // 50 + (100 * 50 / 100) = 50 + 50 = 100
        int damage = skill.calculateDamage(100);

        assertEquals(100, damage);
    }

    @Test
    void combatSkill_isReady_trueWhenCooldownZero() {
        CombatSkill skill = new CombatSkill(1, 50, "atk", 50.0, 2);
        assertTrue(skill.isReady());
    }

    @Test
    void combatSkill_isReady_falseWhenOnCooldown() {
        CombatSkill skill = new CombatSkill(1, 50, "atk", 50.0, 2);
        skill.use();
        assertFalse(skill.isReady());
    }

    @Test
    void combatSkill_use_setsCooldownToBase() {
        CombatSkill skill = new CombatSkill(1, 50, "atk", 50.0, 3);
        skill.use();

        assertEquals(3, skill.getCurrentCooldown());
    }

    @Test
    void combatSkill_reduceCooldown_decreasesByOne() {
        CombatSkill skill = new CombatSkill(1, 50, "atk", 50.0, 3);
        skill.use();
        skill.reduceCooldown();

        assertEquals(2, skill.getCurrentCooldown());
    }

    @Test
    void combatSkill_reduceCooldown_doesNotGoBelowZero() {
        CombatSkill skill = new CombatSkill(1, 50, "atk", 50.0, 0);
        skill.reduceCooldown();

        assertEquals(0, skill.getCurrentCooldown());
    }

    // ========== Tests pour TurnLog ==========

    @Test
    void turnLog_builderPattern_setsAllFields() {
        TurnLog log = new TurnLog(1)
                .attacker("monster1", "player1")
                .defender("monster2", "player2")
                .skill(3)
                .damage(100, 75)
                .defenderHp(1000, 925)
                .describe("Test description");

        assertEquals(1, log.getTurnNumber());
        assertEquals("monster1", log.getAttackerId());
        assertEquals("player1", log.getAttackerOwner());
        assertEquals("monster2", log.getDefenderId());
        assertEquals("player2", log.getDefenderOwner());
        assertEquals(3, log.getSkillUsed());
        assertEquals(100, log.getRawDamage());
        assertEquals(75, log.getActualDamage());
        assertEquals(1000, log.getDefenderHpBefore());
        assertEquals(925, log.getDefenderHpAfter());
        assertEquals("Test description", log.getDescription());
    }

    // ========== Tests pour Combat ==========

    @Test
    void combat_addTurnLog_addsToList() {
        Combat newCombat = new Combat();
        TurnLog log = new TurnLog(1);

        newCombat.addTurnLog(log);

        assertEquals(1, newCombat.getTurnLogs().size());
        assertEquals(log, newCombat.getTurnLogs().get(0));
    }

    @Test
    void combat_constructor_initializesTurnLogs() {
        Combat newCombat = new Combat();

        assertNotNull(newCombat.getTurnLogs());
        assertTrue(newCombat.getTurnLogs().isEmpty());
        assertNotNull(newCombat.getTimestamp());
    }

    // ========== Tests pour CombatCounter ==========

    @Test
    void combatCounter_constructor_setsDefaultValues() {
        CombatCounter newCounter = new CombatCounter();

        assertEquals("combat_counter", newCounter.getId());
        assertEquals(0, newCounter.getSequence());
    }

    @Test
    void combatCounter_setSequence_updatesValue() {
        CombatCounter newCounter = new CombatCounter();
        newCounter.setSequence(42);

        assertEquals(42, newCounter.getSequence());
    }

    // ========== Tests supplementaires pour couvrir les setters ==========

    @Test
    void combat_setters_workCorrectly() {
        Combat c = new Combat();

        c.setFighter1MonsterId("m1");
        assertEquals("m1", c.getFighter1MonsterId());

        c.setFighter1Owner("p1");
        assertEquals("p1", c.getFighter1Owner());

        c.setFighter1Element("fire");
        assertEquals("fire", c.getFighter1Element());

        c.setFighter1InitialHp(1000);
        assertEquals(1000, c.getFighter1InitialHp());

        c.setFighter1FinalHp(500);
        assertEquals(500, c.getFighter1FinalHp());

        c.setFighter2MonsterId("m2");
        assertEquals("m2", c.getFighter2MonsterId());

        c.setFighter2Owner("p2");
        assertEquals("p2", c.getFighter2Owner());

        c.setFighter2Element("water");
        assertEquals("water", c.getFighter2Element());

        c.setFighter2InitialHp(800);
        assertEquals(800, c.getFighter2InitialHp());

        c.setFighter2FinalHp(0);
        assertEquals(0, c.getFighter2FinalHp());

        c.setWinnerMonsterId("m1");
        assertEquals("m1", c.getWinnerMonsterId());

        c.setWinnerOwner("p1");
        assertEquals("p1", c.getWinnerOwner());

        c.setTotalTurns(15);
        assertEquals(15, c.getTotalTurns());
    }

    @Test
    void turnLog_setters_workCorrectly() {
        TurnLog log = new TurnLog();

        log.setTurnNumber(5);
        assertEquals(5, log.getTurnNumber());

        log.setAttackerId("a1");
        assertEquals("a1", log.getAttackerId());

        log.setAttackerOwner("p1");
        assertEquals("p1", log.getAttackerOwner());

        log.setDefenderId("d1");
        assertEquals("d1", log.getDefenderId());

        log.setDefenderOwner("p2");
        assertEquals("p2", log.getDefenderOwner());

        log.setSkillUsed(2);
        assertEquals(2, log.getSkillUsed());

        log.setRawDamage(100);
        assertEquals(100, log.getRawDamage());

        log.setActualDamage(75);
        assertEquals(75, log.getActualDamage());

        log.setDefenderHpBefore(1000);
        assertEquals(1000, log.getDefenderHpBefore());

        log.setDefenderHpAfter(925);
        assertEquals(925, log.getDefenderHpAfter());

        log.setDescription("desc");
        assertEquals("desc", log.getDescription());
    }

    @Test
    void combatSkill_setters_workCorrectly() {
        CombatSkill skill = new CombatSkill();

        skill.setNum(2);
        assertEquals(2, skill.getNum());

        skill.setDmg(100);
        assertEquals(100, skill.getDmg());

        skill.setRatioStat("def");
        assertEquals("def", skill.getRatioStat());

        skill.setRatioPercent(75.0);
        assertEquals(75.0, skill.getRatioPercent());

        skill.setBaseCooldown(3);
        assertEquals(3, skill.getBaseCooldown());

        skill.setCurrentCooldown(2);
        assertEquals(2, skill.getCurrentCooldown());
    }
}
