package com.gatcha.invocation.service;

import com.gatcha.invocation.model.BaseMonster;
import com.gatcha.invocation.model.InvocationRecord;
import com.gatcha.invocation.model.InvocationRecord.InvocationStatus;
import com.gatcha.invocation.repository.BaseMonsterRepository;
import com.gatcha.invocation.repository.InvocationRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Service gerant les invocations de monstres
 */
@Service
public class InvocationService {

    @Value("${MONSTER_SERVICE_URL:http://localhost:8083}")
    private String monsterServiceUrl;

    @Value("${PLAYER_SERVICE_URL:http://localhost:8082}")
    private String playerServiceUrl;

    @Autowired
    private BaseMonsterRepository baseMonsterRepository;

    @Autowired
    private InvocationRecordRepository invocationRecordRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    /**
     * Effectue une invocation pour un joueur
     * 1. Selectionne un monstre aleatoirement selon les taux de loot
     * 2. Cree le monstre dans l'API Monster
     * 3. Ajoute le monstre au joueur dans l'API Player
     */
    public Map<String, Object> invoke(String username) {
        // Recuperer tous les monstres de base
        List<BaseMonster> baseMonsters = baseMonsterRepository.findAll();

        if (baseMonsters.isEmpty()) {
            throw new RuntimeException("Aucun monstre de base disponible");
        }

        // Selectionner un monstre selon les taux de loot
        BaseMonster selectedMonster = selectMonsterByLootRate(baseMonsters);

        // Creer l'enregistrement d'invocation
        InvocationRecord record = new InvocationRecord(username, selectedMonster.getBaseId());
        record = invocationRecordRepository.save(record);

        try {
            // Etape 1: Creer le monstre dans l'API Monster
            String monsterId = createMonsterInApi(username, selectedMonster);
            record.setCreatedMonsterId(monsterId);
            record.setStatus(InvocationStatus.MONSTER_CREATED);
            invocationRecordRepository.save(record);

            // Etape 2: Ajouter le monstre au joueur
            addMonsterToPlayer(username, monsterId);
            record.setStatus(InvocationStatus.PLAYER_UPDATED);
            invocationRecordRepository.save(record);

            // Marquer comme complete
            record.setStatus(InvocationStatus.COMPLETED);
            invocationRecordRepository.save(record);

            return Map.of(
                "success", true,
                "monsterId", monsterId,
                "monster", Map.of(
                    "baseId", selectedMonster.getBaseId(),
                    "element", selectedMonster.getElement(),
                    "hp", selectedMonster.getHp(),
                    "atk", selectedMonster.getAtk(),
                    "def", selectedMonster.getDef(),
                    "vit", selectedMonster.getVit()
                ),
                "invocationId", record.getId()
            );

        } catch (Exception e) {
            record.setStatus(InvocationStatus.FAILED);
            invocationRecordRepository.save(record);
            throw new RuntimeException("Erreur lors de l'invocation: " + e.getMessage());
        }
    }

    /**
     * Selectionne un monstre aleatoirement selon les taux de loot
     * Algorithme: normalise les taux puis tire au hasard
     */
    private BaseMonster selectMonsterByLootRate(List<BaseMonster> monsters) {
        // Calculer la somme totale des taux
        double totalRate = monsters.stream()
                .mapToDouble(BaseMonster::getLootRate)
                .sum();

        // Tirer un nombre aleatoire entre 0 et totalRate
        double randomValue = random.nextDouble() * totalRate;

        // Parcourir les monstres et selectionner celui correspondant
        double cumulative = 0;
        for (BaseMonster monster : monsters) {
            cumulative += monster.getLootRate();
            if (randomValue <= cumulative) {
                return monster;
            }
        }

        // Par securite, retourner le dernier monstre
        return monsters.get(monsters.size() - 1);
    }

    /**
     * Cree un monstre dans l'API Monster
     */
    private String createMonsterInApi(String username, BaseMonster baseMonster) {
        String url = monsterServiceUrl + "/api/monsters/internal/create?username=" + username;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(baseMonster.toMap(), headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

        if (response == null || !response.containsKey("id")) {
            throw new RuntimeException("Reponse invalide de l'API Monster");
        }

        return (String) response.get("id");
    }

    /**
     * Ajoute un monstre au joueur dans l'API Player
     */
    private void addMonsterToPlayer(String username, String monsterId) {
        String url = playerServiceUrl + "/api/player/internal/monsters?username=" + username + "&monsterId=" + monsterId;
        restTemplate.postForObject(url, null, Object.class);
    }

    /**
     * Re-execute les invocations incompletes
     */
    public int retryFailedInvocations() {
        List<InvocationRecord> failedRecords = invocationRecordRepository
                .findByStatusNot(InvocationStatus.COMPLETED);

        int retried = 0;
        for (InvocationRecord record : failedRecords) {
            try {
                retryInvocation(record);
                retried++;
            } catch (Exception e) {
                // Continuer avec les autres
            }
        }

        return retried;
    }

    /**
     * Re-execute une invocation specifique
     */
    private void retryInvocation(InvocationRecord record) {
        BaseMonster baseMonster = baseMonsterRepository.findByBaseId(record.getBaseMonsterIdNumeric())
                .orElseThrow(() -> new RuntimeException("Monstre de base non trouve"));

        switch (record.getStatus()) {
            case PENDING:
            case FAILED:
                // Recommencer depuis le debut
                String monsterId = createMonsterInApi(record.getUsername(), baseMonster);
                record.setCreatedMonsterId(monsterId);
                record.setStatus(InvocationStatus.MONSTER_CREATED);
                invocationRecordRepository.save(record);
                // Continuer vers MONSTER_CREATED

            case MONSTER_CREATED:
                // Ajouter au joueur
                addMonsterToPlayer(record.getUsername(), record.getCreatedMonsterId());
                record.setStatus(InvocationStatus.PLAYER_UPDATED);
                invocationRecordRepository.save(record);
                // Continuer vers PLAYER_UPDATED

            case PLAYER_UPDATED:
                // Marquer comme complete
                record.setStatus(InvocationStatus.COMPLETED);
                invocationRecordRepository.save(record);
                break;

            case COMPLETED:
                // Deja complete
                break;
        }
    }

    /**
     * Recupere l'historique des invocations d'un joueur
     */
    public List<InvocationRecord> getInvocationHistory(String username) {
        return invocationRecordRepository.findByUsername(username);
    }
}
