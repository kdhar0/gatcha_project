package com.gatcha.player.controller;

import com.gatcha.player.model.Player;
import com.gatcha.player.service.AuthClientService;
import com.gatcha.player.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller pour l'API Joueur
 * Toutes les routes necessitent un token valide dans le header "Authorization"
 */
@RestController
@RequestMapping("/api/player")
@CrossOrigin(origins = "*")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private AuthClientService authClientService;

    /**
     * Recupere le profil complet du joueur
     * GET /api/player/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        try {
            String username = authClientService.validateToken(token);
            Player player = playerService.getOrCreatePlayer(username);
            return ResponseEntity.ok(player);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Recupere la liste des monstres du joueur
     * GET /api/player/monsters
     */
    @GetMapping("/monsters")
    public ResponseEntity<?> getMonsters(@RequestHeader("Authorization") String token) {
        try {
            String username = authClientService.validateToken(token);
            List<String> monsterIds = playerService.getMonsterIds(username);
            return ResponseEntity.ok(monsterIds);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Recupere le niveau du joueur
     * GET /api/player/level
     */
    @GetMapping("/level")
    public ResponseEntity<?> getLevel(@RequestHeader("Authorization") String token) {
        try {
            String username = authClientService.validateToken(token);
            int level = playerService.getLevel(username);
            return ResponseEntity.ok(Map.of("level", level));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Ajoute de l'experience au joueur
     * POST /api/player/experience?amount=100
     */
    @PostMapping("/experience")
    public ResponseEntity<?> addExperience(
            @RequestHeader("Authorization") String token,
            @RequestParam int amount) {
        try {
            String username = authClientService.validateToken(token);
            Player player = playerService.addExperience(username, amount);
            return ResponseEntity.ok(player);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Force le gain d'un niveau
     * POST /api/player/levelup
     */
    @PostMapping("/levelup")
    public ResponseEntity<?> levelUp(@RequestHeader("Authorization") String token) {
        try {
            String username = authClientService.validateToken(token);
            Player player = playerService.forceLevelUp(username);
            return ResponseEntity.ok(player);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Ajoute un monstre au joueur (appele par l'API Invocation)
     * POST /api/player/monsters/{monsterId}
     */
    @PostMapping("/monsters/{monsterId}")
    public ResponseEntity<?> addMonster(
            @RequestHeader("Authorization") String token,
            @PathVariable String monsterId) {
        try {
            String username = authClientService.validateToken(token);
            Player player = playerService.addMonster(username, monsterId);
            return ResponseEntity.ok(player);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("pleine")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Supprime un monstre du joueur
     * DELETE /api/player/monsters/{monsterId}
     */
    @DeleteMapping("/monsters/{monsterId}")
    public ResponseEntity<?> removeMonster(
            @RequestHeader("Authorization") String token,
            @PathVariable String monsterId) {
        try {
            String username = authClientService.validateToken(token);
            Player player = playerService.removeMonster(username, monsterId);
            return ResponseEntity.ok(player);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Endpoint interne pour ajouter un monstre (sans validation de token, pour les appels inter-services)
     * POST /api/player/internal/monsters
     */
    @PostMapping("/internal/monsters")
    public ResponseEntity<?> addMonsterInternal(
            @RequestParam String username,
            @RequestParam String monsterId) {
        try {
            // Creer le joueur s'il n'existe pas
            Player player = playerService.getOrCreatePlayer(username);
            player = playerService.addMonster(username, monsterId);
            return ResponseEntity.ok(player);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Endpoint public pour lister tous les joueurs (pour l'arene)
     * GET /api/player/public/list
     */
    @GetMapping("/public/list")
    public ResponseEntity<?> getAllPlayers() {
        List<Player> players = playerService.getAllPlayers();
        return ResponseEntity.ok(players);
    }

    /**
     * Endpoint public pour recuperer un joueur par username (pour l'arene)
     * GET /api/player/public/{username}
     */
    @GetMapping("/public/{username}")
    public ResponseEntity<?> getPlayerByUsername(@PathVariable String username) {
        try {
            Player player = playerService.getPlayerByUsername(username);
            return ResponseEntity.ok(player);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
