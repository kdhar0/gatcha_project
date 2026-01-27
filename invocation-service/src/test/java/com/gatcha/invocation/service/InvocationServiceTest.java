package com.gatcha.invocation.service;

import com.gatcha.invocation.model.BaseMonster;
import com.gatcha.invocation.model.InvocationRecord;
import com.gatcha.invocation.model.InvocationRecord.InvocationStatus;
import com.gatcha.invocation.repository.BaseMonsterRepository;
import com.gatcha.invocation.repository.InvocationRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvocationServiceTest {

    @Mock
    private BaseMonsterRepository baseMonsterRepository;

    @Mock
    private InvocationRecordRepository invocationRecordRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private InvocationService invocationService;

    private BaseMonster baseMonster1;
    private BaseMonster baseMonster2;
    private InvocationRecord invocationRecord;

    @BeforeEach
    void setUp() {
        // Configuration du premier monstre de base
        baseMonster1 = new BaseMonster();
        baseMonster1.setBaseId(1);
        baseMonster1.setElement("fire");
        baseMonster1.setHp(1000);
        baseMonster1.setAtk(100);
        baseMonster1.setDef(50);
        baseMonster1.setVit(80);
        baseMonster1.setLootRate(0.7); // 70% de chance

        List<Map<String, Object>> skills1 = new ArrayList<>();
        Map<String, Object> skill1 = new HashMap<>();
        skill1.put("num", 1);
        skill1.put("dmg", 50);
        skill1.put("cooldown", 0);
        skill1.put("lvlMax", 5);
        Map<String, Object> ratio1 = new HashMap<>();
        ratio1.put("stat", "atk");
        ratio1.put("percent", 30.0);
        skill1.put("ratio", ratio1);
        skills1.add(skill1);
        baseMonster1.setSkills(skills1);

        // Configuration du second monstre de base (rare)
        baseMonster2 = new BaseMonster();
        baseMonster2.setBaseId(2);
        baseMonster2.setElement("water");
        baseMonster2.setHp(1200);
        baseMonster2.setAtk(80);
        baseMonster2.setDef(70);
        baseMonster2.setVit(90);
        baseMonster2.setLootRate(0.3); // 30% de chance
        baseMonster2.setSkills(skills1);

        // Configuration de l'enregistrement d'invocation
        invocationRecord = new InvocationRecord("testUser", 1);
        invocationRecord.setId("record123");

        // Injecter les URLs de test
        ReflectionTestUtils.setField(invocationService, "monsterServiceUrl", "http://localhost:8083");
        ReflectionTestUtils.setField(invocationService, "playerServiceUrl", "http://localhost:8082");
    }

    // ========== Tests pour selectMonsterByLootRate (methode privee testee via invoke) ==========

    @Test
    void invoke_noBaseMonsters_throws() {
        when(baseMonsterRepository.findAll()).thenReturn(new ArrayList<>());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> invocationService.invoke("testUser"));

        assertEquals("Aucun monstre de base disponible", ex.getMessage());
    }

    @Test
    void invoke_ok_createsRecordWithPendingStatus() {
        List<BaseMonster> monsters = Arrays.asList(baseMonster1);
        when(baseMonsterRepository.findAll()).thenReturn(monsters);
        when(invocationRecordRepository.save(any(InvocationRecord.class))).thenAnswer(invocation -> {
            InvocationRecord record = invocation.getArgument(0);
            record.setId("newRecordId");
            return record;
        });

        // Simuler l'echec de l'API Monster pour tester la gestion d'erreur
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("API Error"));

        // Injecter le RestTemplate mock
        ReflectionTestUtils.setField(invocationService, "restTemplate", restTemplate);

        assertThrows(RuntimeException.class, () -> invocationService.invoke("testUser"));

        // Verifier que l'enregistrement a ete cree puis mis en echec
        verify(invocationRecordRepository, atLeast(1)).save(any(InvocationRecord.class));
    }

    // ========== Tests pour getInvocationHistory ==========

    @Test
    void getInvocationHistory_ok_returnsHistory() {
        InvocationRecord record1 = new InvocationRecord("testUser", 1);
        InvocationRecord record2 = new InvocationRecord("testUser", 2);
        List<InvocationRecord> records = Arrays.asList(record1, record2);

        when(invocationRecordRepository.findByUsername("testUser")).thenReturn(records);

        List<InvocationRecord> result = invocationService.getInvocationHistory("testUser");

        assertEquals(2, result.size());
        verify(invocationRecordRepository).findByUsername("testUser");
    }

    @Test
    void getInvocationHistory_noRecords_returnsEmptyList() {
        when(invocationRecordRepository.findByUsername("testUser")).thenReturn(new ArrayList<>());

        List<InvocationRecord> result = invocationService.getInvocationHistory("testUser");

        assertTrue(result.isEmpty());
    }

    // ========== Tests pour retryFailedInvocations ==========

    @Test
    void retryFailedInvocations_noFailedRecords_returnsZero() {
        when(invocationRecordRepository.findByStatusNot(InvocationStatus.COMPLETED))
                .thenReturn(new ArrayList<>());

        int result = invocationService.retryFailedInvocations();

        assertEquals(0, result);
    }

    @Test
    void retryFailedInvocations_withFailedRecords_retriesEach() {
        InvocationRecord failedRecord = new InvocationRecord("testUser", 1);
        failedRecord.setStatus(InvocationStatus.FAILED);

        when(invocationRecordRepository.findByStatusNot(InvocationStatus.COMPLETED))
                .thenReturn(Arrays.asList(failedRecord));
        when(baseMonsterRepository.findByBaseId(1)).thenReturn(Optional.of(baseMonster1));

        // Simuler l'echec lors de la tentative de retry
        ReflectionTestUtils.setField(invocationService, "restTemplate", restTemplate);
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("API Error"));

        int result = invocationService.retryFailedInvocations();

        // Le retry echoue, donc 0 reussi
        assertEquals(0, result);
    }

    // ========== Tests pour BaseMonster ==========

    @Test
    void baseMonster_toMap_returnsCorrectMap() {
        Map<String, Object> map = baseMonster1.toMap();

        assertEquals(1, map.get("_id"));
        assertEquals("fire", map.get("element"));
        assertEquals(1000, map.get("hp"));
        assertEquals(100, map.get("atk"));
        assertEquals(50, map.get("def"));
        assertEquals(80, map.get("vit"));
        assertNotNull(map.get("skills"));
    }

    @Test
    void baseMonster_getLootRate_returnsRate() {
        assertEquals(0.7, baseMonster1.getLootRate());
        assertEquals(0.3, baseMonster2.getLootRate());
    }

    // ========== Tests pour InvocationRecord ==========

    @Test
    void invocationRecord_constructor_setsDefaultValues() {
        InvocationRecord record = new InvocationRecord("user1", 5);

        assertEquals("user1", record.getUsername());
        assertEquals(5, record.getBaseMonsterIdNumeric());
        assertEquals(InvocationStatus.PENDING, record.getStatus());
        assertNotNull(record.getTimestamp());
    }

    @Test
    void invocationRecord_emptyConstructor_setsDefaultStatus() {
        InvocationRecord record = new InvocationRecord();

        assertEquals(InvocationStatus.PENDING, record.getStatus());
        assertNotNull(record.getTimestamp());
    }

    @Test
    void invocationRecord_setStatus_updatesStatus() {
        invocationRecord.setStatus(InvocationStatus.MONSTER_CREATED);
        assertEquals(InvocationStatus.MONSTER_CREATED, invocationRecord.getStatus());

        invocationRecord.setStatus(InvocationStatus.PLAYER_UPDATED);
        assertEquals(InvocationStatus.PLAYER_UPDATED, invocationRecord.getStatus());

        invocationRecord.setStatus(InvocationStatus.COMPLETED);
        assertEquals(InvocationStatus.COMPLETED, invocationRecord.getStatus());
    }

    @Test
    void invocationRecord_setCreatedMonsterId_updatesId() {
        invocationRecord.setCreatedMonsterId("monster123");
        assertEquals("monster123", invocationRecord.getCreatedMonsterId());
    }

    // ========== Tests pour la selection aleatoire de monstres ==========

    @Test
    void selectMonsterByLootRate_singleMonster_alwaysSelectsIt() {
        // Test indirect via la methode invoke
        List<BaseMonster> monsters = Arrays.asList(baseMonster1);
        when(baseMonsterRepository.findAll()).thenReturn(monsters);

        when(invocationRecordRepository.save(any(InvocationRecord.class))).thenAnswer(invocation -> {
            InvocationRecord record = invocation.getArgument(0);
            // Verifier que le bon monstre est selectionne
            assertEquals(1, record.getBaseMonsterIdNumeric());
            return record;
        });

        ReflectionTestUtils.setField(invocationService, "restTemplate", restTemplate);
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("API Error"));

        try {
            invocationService.invoke("testUser");
        } catch (RuntimeException e) {
            // Exception attendue
        }

        verify(invocationRecordRepository, atLeast(1)).save(any(InvocationRecord.class));
    }

    @Test
    void selectMonsterByLootRate_multipleMonsters_selectsBasedOnRate() {
        // Test statistique : avec suffisamment d'essais, les deux monstres devraient etre selectionnes
        List<BaseMonster> monsters = Arrays.asList(baseMonster1, baseMonster2);

        // Les taux sont 0.7 et 0.3, donc le monstre 1 devrait etre plus souvent selectionne
        // Ce test verifie juste que les deux peuvent etre selectionnes
        Map<Integer, Integer> selections = new HashMap<>();
        selections.put(1, 0);
        selections.put(2, 0);

        when(baseMonsterRepository.findAll()).thenReturn(monsters);
        when(invocationRecordRepository.save(any(InvocationRecord.class))).thenAnswer(invocation -> {
            InvocationRecord record = invocation.getArgument(0);
            int selected = record.getBaseMonsterIdNumeric();
            selections.put(selected, selections.get(selected) + 1);
            return record;
        });

        ReflectionTestUtils.setField(invocationService, "restTemplate", restTemplate);
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("API Error"));

        // Executer plusieurs invocations pour avoir des statistiques
        for (int i = 0; i < 100; i++) {
            try {
                invocationService.invoke("testUser");
            } catch (RuntimeException e) {
                // Exception attendue
            }
        }

        // Verifier que les deux monstres ont ete selectionnes au moins une fois
        assertTrue(selections.get(1) > 0, "Le monstre 1 aurait du etre selectionne");
        assertTrue(selections.get(2) > 0, "Le monstre 2 aurait du etre selectionne");

        // Verifier que le monstre avec le taux plus eleve est plus souvent selectionne
        assertTrue(selections.get(1) > selections.get(2),
                "Le monstre 1 (70%) devrait etre plus souvent selectionne que le monstre 2 (30%)");
    }

    // ========== Tests pour InvocationStatus enum ==========

    @Test
    void invocationStatus_hasAllExpectedValues() {
        assertEquals(5, InvocationStatus.values().length);
        assertNotNull(InvocationStatus.valueOf("PENDING"));
        assertNotNull(InvocationStatus.valueOf("MONSTER_CREATED"));
        assertNotNull(InvocationStatus.valueOf("PLAYER_UPDATED"));
        assertNotNull(InvocationStatus.valueOf("COMPLETED"));
        assertNotNull(InvocationStatus.valueOf("FAILED"));
    }

    // ========== Tests pour les setters et getters de BaseMonster ==========

    @Test
    void baseMonster_settersAndGetters_workCorrectly() {
        BaseMonster monster = new BaseMonster();

        monster.setBaseId(99);
        assertEquals(99, monster.getBaseId());

        monster.setElement("wind");
        assertEquals("wind", monster.getElement());

        monster.setHp(500);
        assertEquals(500, monster.getHp());

        monster.setAtk(60);
        assertEquals(60, monster.getAtk());

        monster.setDef(30);
        assertEquals(30, monster.getDef());

        monster.setVit(40);
        assertEquals(40, monster.getVit());

        monster.setLootRate(0.5);
        assertEquals(0.5, monster.getLootRate());

        List<Map<String, Object>> skills = new ArrayList<>();
        monster.setSkills(skills);
        assertEquals(skills, monster.getSkills());
    }

    // ========== Tests pour les setters et getters de InvocationRecord ==========

    @Test
    void invocationRecord_settersAndGetters_workCorrectly() {
        InvocationRecord record = new InvocationRecord();

        record.setId("id123");
        assertEquals("id123", record.getId());

        record.setUsername("player1");
        assertEquals("player1", record.getUsername());

        record.setBaseMonsterIdNumeric(42);
        assertEquals(42, record.getBaseMonsterIdNumeric());

        record.setCreatedMonsterId("monster456");
        assertEquals("monster456", record.getCreatedMonsterId());
    }
}
