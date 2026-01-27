package com.gatcha.monster.service;

import com.gatcha.monster.model.Monster;
import com.gatcha.monster.model.Ratio;
import com.gatcha.monster.model.Skill;
import com.gatcha.monster.repository.MonsterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonsterServiceTest {

    @Mock
    private MonsterRepository monsterRepository;

    @InjectMocks
    private MonsterService monsterService;

    private Monster monster;
    private List<Skill> skills;

    @BeforeEach
    void setUp() {
        monster = new Monster();
        monster.setId("monster123");
        monster.setOwnerUsername("testUser");
        monster.setBaseId(1);
        monster.setElement("fire");
        monster.setHp(1000);
        monster.setAtk(100);
        monster.setDef(50);
        monster.setVit(80);

        skills = new ArrayList<>();
        Ratio ratio = new Ratio();
        ratio.setStat("atk");
        ratio.setPercent(50.0);

        Skill skill1 = new Skill(1, 50, ratio, 0, 5);
        Skill skill2 = new Skill(2, 100, ratio, 2, 5);
        Skill skill3 = new Skill(3, 200, ratio, 4, 5);

        skills.add(skill1);
        skills.add(skill2);
        skills.add(skill3);
        monster.setSkills(skills);
    }

    // ========== Tests pour getMonstersByOwner ==========

    @Test
    void getMonstersByOwner_ok_returnsList() {
        Monster monster2 = new Monster();
        monster2.setId("monster456");
        monster2.setOwnerUsername("testUser");

        List<Monster> monsters = Arrays.asList(monster, monster2);
        when(monsterRepository.findByOwnerUsername("testUser")).thenReturn(monsters);

        List<Monster> result = monsterService.getMonstersByOwner("testUser");

        assertEquals(2, result.size());
        verify(monsterRepository).findByOwnerUsername("testUser");
    }

    @Test
    void getMonstersByOwner_noMonsters_returnsEmptyList() {
        when(monsterRepository.findByOwnerUsername("testUser")).thenReturn(new ArrayList<>());

        List<Monster> result = monsterService.getMonstersByOwner("testUser");

        assertTrue(result.isEmpty());
    }

    // ========== Tests pour getMonster ==========

    @Test
    void getMonster_ok_returnsMonster() {
        when(monsterRepository.findById("monster123")).thenReturn(Optional.of(monster));

        Monster result = monsterService.getMonster("monster123", "testUser");

        assertEquals("monster123", result.getId());
        assertEquals("testUser", result.getOwnerUsername());
    }

    @Test
    void getMonster_notFound_throws() {
        when(monsterRepository.findById("unknown")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> monsterService.getMonster("unknown", "testUser"));

        assertEquals("Monstre non trouve: unknown", ex.getMessage());
    }

    @Test
    void getMonster_wrongOwner_throws() {
        when(monsterRepository.findById("monster123")).thenReturn(Optional.of(monster));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> monsterService.getMonster("monster123", "otherUser"));

        assertEquals("Ce monstre ne vous appartient pas", ex.getMessage());
    }

    // ========== Tests pour createMonster ==========

    @Test
    void createMonster_ok_createsAndSavesMonster() {
        Map<String, Object> baseMonsterData = new HashMap<>();
        baseMonsterData.put("_id", 1);
        baseMonsterData.put("element", "water");
        baseMonsterData.put("hp", 800);
        baseMonsterData.put("atk", 120);
        baseMonsterData.put("def", 40);
        baseMonsterData.put("vit", 90);

        List<Map<String, Object>> skillsData = new ArrayList<>();
        Map<String, Object> skillData = new HashMap<>();
        skillData.put("num", 1);
        skillData.put("dmg", 60);
        skillData.put("cooldown", 0);
        skillData.put("lvlMax", 5);

        Map<String, Object> ratioData = new HashMap<>();
        ratioData.put("stat", "atk");
        ratioData.put("percent", 30.0);
        skillData.put("ratio", ratioData);

        skillsData.add(skillData);
        baseMonsterData.put("skills", skillsData);

        when(monsterRepository.save(any(Monster.class))).thenAnswer(invocation -> {
            Monster m = invocation.getArgument(0);
            m.setId("newMonsterId");
            return m;
        });

        Monster result = monsterService.createMonster("testUser", baseMonsterData);

        assertEquals("testUser", result.getOwnerUsername());
        assertEquals("water", result.getElement());
        assertEquals(800, result.getHp());
        assertEquals(120, result.getAtk());
        assertEquals(40, result.getDef());
        assertEquals(90, result.getVit());
        assertEquals(1, result.getSkills().size());
        verify(monsterRepository).save(any(Monster.class));
    }

    @Test
    void createMonster_withMultipleSkills_createsAllSkills() {
        Map<String, Object> baseMonsterData = new HashMap<>();
        baseMonsterData.put("_id", 2);
        baseMonsterData.put("element", "wind");
        baseMonsterData.put("hp", 900);
        baseMonsterData.put("atk", 110);
        baseMonsterData.put("def", 45);
        baseMonsterData.put("vit", 100);

        List<Map<String, Object>> skillsData = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> skillData = new HashMap<>();
            skillData.put("num", i);
            skillData.put("dmg", 50 * i);
            skillData.put("cooldown", i - 1);
            skillData.put("lvlMax", 5);

            Map<String, Object> ratioData = new HashMap<>();
            ratioData.put("stat", "atk");
            ratioData.put("percent", 20.0 * i);
            skillData.put("ratio", ratioData);

            skillsData.add(skillData);
        }
        baseMonsterData.put("skills", skillsData);

        when(monsterRepository.save(any(Monster.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Monster result = monsterService.createMonster("testUser", baseMonsterData);

        assertEquals(3, result.getSkills().size());
        assertEquals(1, result.getSkills().get(0).getLevel()); // Toutes les competences niveau 1
    }

    // ========== Tests pour addExperience ==========

    @Test
    void addExperience_ok_addsXp() {
        monster.setExperience(0);
        when(monsterRepository.findById("monster123")).thenReturn(Optional.of(monster));
        when(monsterRepository.save(any(Monster.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Monster result = monsterService.addExperience("monster123", "testUser", 50);

        assertEquals(50, result.getExperience());
        verify(monsterRepository).save(monster);
    }

    @Test
    void addExperience_levelUp_increasesStats() {
        monster.setExperience(0);
        monster.setExperienceToNextLevel(100);
        monster.setLevel(1);
        int initialHp = monster.getHp();
        int initialAtk = monster.getAtk();

        when(monsterRepository.findById("monster123")).thenReturn(Optional.of(monster));
        when(monsterRepository.save(any(Monster.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Monster result = monsterService.addExperience("monster123", "testUser", 150);

        assertEquals(2, result.getLevel());
        assertTrue(result.getHp() > initialHp); // HP augmente de 5%
        assertTrue(result.getAtk() > initialAtk); // ATK augmente de 5%
        assertEquals(1, result.getSkillPoints()); // Gagne un point de competence
    }

    @Test
    void addExperience_wrongOwner_throws() {
        when(monsterRepository.findById("monster123")).thenReturn(Optional.of(monster));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> monsterService.addExperience("monster123", "otherUser", 50));

        assertEquals("Ce monstre ne vous appartient pas", ex.getMessage());
    }

    // ========== Tests pour upgradeSkill ==========

    @Test
    void upgradeSkill_ok_upgradesSkill() {
        monster.setSkillPoints(1);
        when(monsterRepository.findById("monster123")).thenReturn(Optional.of(monster));
        when(monsterRepository.save(any(Monster.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Monster result = monsterService.upgradeSkill("monster123", "testUser", 1);

        assertEquals(0, result.getSkillPoints());
        assertEquals(2, result.getSkills().get(0).getLevel());
        verify(monsterRepository).save(monster);
    }

    @Test
    void upgradeSkill_noPoints_throws() {
        monster.setSkillPoints(0);
        when(monsterRepository.findById("monster123")).thenReturn(Optional.of(monster));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> monsterService.upgradeSkill("monster123", "testUser", 1));

        assertTrue(ex.getMessage().contains("Impossible d'ameliorer"));
        verify(monsterRepository, never()).save(any());
    }

    @Test
    void upgradeSkill_maxLevel_throws() {
        monster.setSkillPoints(1);
        monster.getSkills().get(0).setLevel(5); // Niveau max
        monster.getSkills().get(0).setLvlMax(5);
        when(monsterRepository.findById("monster123")).thenReturn(Optional.of(monster));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> monsterService.upgradeSkill("monster123", "testUser", 1));

        assertTrue(ex.getMessage().contains("Impossible d'ameliorer"));
    }

    @Test
    void upgradeSkill_skillNotFound_throws() {
        monster.setSkillPoints(1);
        when(monsterRepository.findById("monster123")).thenReturn(Optional.of(monster));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> monsterService.upgradeSkill("monster123", "testUser", 99)); // Competence inexistante

        assertTrue(ex.getMessage().contains("Impossible d'ameliorer"));
    }

    // ========== Tests pour deleteMonster ==========

    @Test
    void deleteMonster_ok_deletesMonster() {
        when(monsterRepository.findById("monster123")).thenReturn(Optional.of(monster));
        doNothing().when(monsterRepository).delete(monster);

        monsterService.deleteMonster("monster123", "testUser");

        verify(monsterRepository).delete(monster);
    }

    @Test
    void deleteMonster_notFound_throws() {
        when(monsterRepository.findById("unknown")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> monsterService.deleteMonster("unknown", "testUser"));

        assertEquals("Monstre non trouve: unknown", ex.getMessage());
        verify(monsterRepository, never()).delete(any());
    }

    @Test
    void deleteMonster_wrongOwner_throws() {
        when(monsterRepository.findById("monster123")).thenReturn(Optional.of(monster));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> monsterService.deleteMonster("monster123", "otherUser"));

        assertEquals("Ce monstre ne vous appartient pas", ex.getMessage());
        verify(monsterRepository, never()).delete(any());
    }

    // ========== Tests pour createMonsterInternal ==========

    @Test
    void createMonsterInternal_ok_callsCreateMonster() {
        Map<String, Object> baseMonsterData = new HashMap<>();
        baseMonsterData.put("_id", 1);
        baseMonsterData.put("element", "fire");
        baseMonsterData.put("hp", 1000);
        baseMonsterData.put("atk", 100);
        baseMonsterData.put("def", 50);
        baseMonsterData.put("vit", 80);
        baseMonsterData.put("skills", new ArrayList<>());

        when(monsterRepository.save(any(Monster.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Monster result = monsterService.createMonsterInternal("testUser", baseMonsterData);

        assertNotNull(result);
        assertEquals("testUser", result.getOwnerUsername());
    }

    // ========== Tests pour getMonsterById ==========

    @Test
    void getMonsterById_ok_returnsMonsterWithoutOwnerCheck() {
        when(monsterRepository.findById("monster123")).thenReturn(Optional.of(monster));

        Monster result = monsterService.getMonsterById("monster123");

        assertEquals("monster123", result.getId());
        // Pas de verification du proprietaire
    }

    @Test
    void getMonsterById_notFound_throws() {
        when(monsterRepository.findById("unknown")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> monsterService.getMonsterById("unknown"));

        assertEquals("Monstre non trouve: unknown", ex.getMessage());
    }

    // ========== Tests supplementaires pour Monster model ==========

    @Test
    void monster_getStatValue_returnsCorrectValues() {
        assertEquals(1000, monster.getStatValue("hp"));
        assertEquals(100, monster.getStatValue("atk"));
        assertEquals(50, monster.getStatValue("def"));
        assertEquals(80, monster.getStatValue("vit"));
        assertEquals(0, monster.getStatValue("unknown"));
    }

    @Test
    void monster_addExperience_multipleLevelUps() {
        monster.setExperience(0);
        monster.setExperienceToNextLevel(100);
        monster.setLevel(1);

        // Ajouter assez d'XP pour 2 niveaux
        boolean leveledUp = monster.addExperience(250);

        assertTrue(leveledUp);
        assertEquals(3, monster.getLevel());
        assertEquals(2, monster.getSkillPoints());
    }
}
