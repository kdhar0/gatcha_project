package com.gatcha.monster.controller;

import com.gatcha.monster.model.Monster;
import com.gatcha.monster.service.AuthClientService;
import com.gatcha.monster.service.MonsterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller pour l'API Monstres
 * Toutes les routes necessitent un token valide dans le header "Authorization"
 */
@RestController
@RequestMapping("/api/monsters")
@CrossOrigin(origins = "*")
public class MonsterController {

    @Autowired
    private MonsterService monsterService;

    @Autowired
    private AuthClientService authClientService;

    /**
     * Recupere tous les monstres du joueur
     * GET /api/monsters
     */
    @GetMapping
    public ResponseEntity<?> getAllMonsters(@RequestHeader("Authorization") String token) {
        try {
            String username = authClientService.validateToken(token);
            List<Monster> monsters = monsterService.getMonstersByOwner(username);
            return ResponseEntity.ok(monsters);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Recupere un monstre par son ID
     * GET /api/monsters/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMonster(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {
        try {
            String username = authClientService.validateToken(token);
            Monster monster = monsterService.getMonster(id, username);
            return ResponseEntity.ok(monster);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("appartient")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Ajoute de l'experience a un monstre
     * POST /api/monsters/{id}/experience?amount=100
     */
    @PostMapping("/{id}/experience")
    public ResponseEntity<?> addExperience(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @RequestParam int amount) {
        try {
            String username = authClientService.validateToken(token);
            Monster monster = monsterService.addExperience(id, username, amount);
            return ResponseEntity.ok(monster);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Ameliore une competence du monstre
     * POST /api/monsters/{id}/skills/{skillNum}/upgrade
     */
    @PostMapping("/{id}/skills/{skillNum}/upgrade")
    public ResponseEntity<?> upgradeSkill(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @PathVariable int skillNum) {
        try {
            String username = authClientService.validateToken(token);
            Monster monster = monsterService.upgradeSkill(id, username, skillNum);
            return ResponseEntity.ok(monster);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Impossible")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Supprime un monstre
     * DELETE /api/monsters/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMonster(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {
        try {
            String username = authClientService.validateToken(token);
            monsterService.deleteMonster(id, username);
            return ResponseEntity.ok("Monstre supprime avec succes");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Endpoint interne pour creer un monstre (appele par l'API Invocation)
     * POST /api/monsters/internal/create
     */
    @PostMapping("/internal/create")
    public ResponseEntity<?> createMonsterInternal(
            @RequestParam String username,
            @RequestBody Map<String, Object> baseMonsterData) {
        try {
            Monster monster = monsterService.createMonsterInternal(username, baseMonsterData);
            return ResponseEntity.status(HttpStatus.CREATED).body(monster);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Endpoint public pour recuperer les monstres d'un joueur (pour l'arene)
     * GET /api/monsters/public/player/{username}
     */
    @GetMapping("/public/player/{username}")
    public ResponseEntity<?> getMonstersByPlayer(@PathVariable String username) {
        List<Monster> monsters = monsterService.getMonstersByOwner(username);
        return ResponseEntity.ok(monsters);
    }

    /**
     * Endpoint public pour recuperer un monstre par ID (pour l'arene)
     * GET /api/monsters/public/{id}
     */
    @GetMapping("/public/{id}")
    public ResponseEntity<?> getMonsterPublic(@PathVariable String id) {
        try {
            Monster monster = monsterService.getMonsterById(id);
            return ResponseEntity.ok(monster);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
