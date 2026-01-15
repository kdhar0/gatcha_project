package com.gatcha.monster.service;

import com.gatcha.monster.model.Monster;
import com.gatcha.monster.model.Ratio;
import com.gatcha.monster.model.Skill;
import com.gatcha.monster.repository.MonsterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service gerant la logique metier des monstres
 */
@Service
public class MonsterService {

    @Autowired
    private MonsterRepository monsterRepository;

    /**
     * Recupere tous les monstres d'un joueur
     */
    public List<Monster> getMonstersByOwner(String username) {
        return monsterRepository.findByOwnerUsername(username);
    }

    /**
     * Recupere un monstre par son ID
     * Verifie que le monstre appartient bien au joueur
     */
    public Monster getMonster(String monsterId, String username) {
        Monster monster = monsterRepository.findById(monsterId)
                .orElseThrow(() -> new RuntimeException("Monstre non trouve: " + monsterId));

        if (!monster.getOwnerUsername().equals(username)) {
            throw new RuntimeException("Ce monstre ne vous appartient pas");
        }

        return monster;
    }

    /**
     * Cree un nouveau monstre a partir des donnees de base
     */
    public Monster createMonster(String ownerUsername, Map<String, Object> baseMonsterData) {
        Monster monster = new Monster();
        monster.setOwnerUsername(ownerUsername);

        // Recuperer les donnees de base
        monster.setBaseId(((Number) baseMonsterData.get("_id")).intValue());
        monster.setElement((String) baseMonsterData.get("element"));
        monster.setHp(((Number) baseMonsterData.get("hp")).intValue());
        monster.setAtk(((Number) baseMonsterData.get("atk")).intValue());
        monster.setDef(((Number) baseMonsterData.get("def")).intValue());
        monster.setVit(((Number) baseMonsterData.get("vit")).intValue());

        // Creer les competences
        List<Skill> skills = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> skillsData = (List<Map<String, Object>>) baseMonsterData.get("skills");

        for (Map<String, Object> skillData : skillsData) {
            Skill skill = new Skill();
            skill.setNum(((Number) skillData.get("num")).intValue());
            skill.setDmg(((Number) skillData.get("dmg")).intValue());
            skill.setCooldown(((Number) skillData.get("cooldown")).intValue());
            skill.setLvlMax(((Number) skillData.get("lvlMax")).intValue());
            skill.setLevel(1);

            @SuppressWarnings("unchecked")
            Map<String, Object> ratioData = (Map<String, Object>) skillData.get("ratio");
            Ratio ratio = new Ratio();
            ratio.setStat((String) ratioData.get("stat"));
            ratio.setPercent(((Number) ratioData.get("percent")).doubleValue());
            skill.setRatio(ratio);

            skills.add(skill);
        }
        monster.setSkills(skills);

        return monsterRepository.save(monster);
    }

    /**
     * Ajoute de l'experience a un monstre
     */
    public Monster addExperience(String monsterId, String username, int amount) {
        Monster monster = getMonster(monsterId, username);
        monster.addExperience(amount);
        return monsterRepository.save(monster);
    }

    /**
     * Ameliore une competence du monstre
     */
    public Monster upgradeSkill(String monsterId, String username, int skillNum) {
        Monster monster = getMonster(monsterId, username);

        if (!monster.upgradeSkill(skillNum)) {
            throw new RuntimeException("Impossible d'ameliorer cette competence. Verifiez vos points de competence ou le niveau max.");
        }

        return monsterRepository.save(monster);
    }

    /**
     * Supprime un monstre
     */
    public void deleteMonster(String monsterId, String username) {
        Monster monster = getMonster(monsterId, username);
        monsterRepository.delete(monster);
    }

    /**
     * Cree un monstre sans verification (appel interne depuis l'API Invocation)
     */
    public Monster createMonsterInternal(String ownerUsername, Map<String, Object> baseMonsterData) {
        return createMonster(ownerUsername, baseMonsterData);
    }

    /**
     * Recupere un monstre par son ID (sans verification de propriete, pour l'arene)
     */
    public Monster getMonsterById(String monsterId) {
        return monsterRepository.findById(monsterId)
                .orElseThrow(() -> new RuntimeException("Monstre non trouve: " + monsterId));
    }
}
