package com.gatcha.combat.controller;

import com.gatcha.combat.dto.request.CombatRequestDto;
import com.gatcha.combat.dto.response.*;
import com.gatcha.combat.mapper.CombatMapper;
import com.gatcha.combat.model.Combat;
import com.gatcha.combat.model.Fighter;
import com.gatcha.combat.service.CombatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller pour l'API Combat (sans authentification)
 * Permet des combats entre 2 joueurs
 */
@RestController
@RequestMapping("/api/combat")
@CrossOrigin(origins = "*")
public class CombatController {

    @Autowired
    private CombatService combatService;

    @Autowired
    private CombatMapper combatMapper;

    /**
     * Lance un combat entre deux monstres
     * POST /api/combat/fight
     */
    @PostMapping("/fight")
    public ResponseEntity<?> fight(@Valid @RequestBody CombatRequestDto request) {
        try {
            if (request.getMonster1().getPlayerName().equals(request.getMonster2().getPlayerName())) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponseDto.badRequest("Les deux joueurs doivent etre differents"));
            }

            Fighter fighter1 = combatMapper.toFighter(request.getMonster1());
            Fighter fighter2 = combatMapper.toFighter(request.getMonster2());

            Combat combat = combatService.executeCombat(fighter1, fighter2);
            CombatResponseDto response = combatMapper.toCombatResponseDto(combat);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponseDto.internalError(e.getMessage()));
        }
    }

    /**
     * Recupere les details d'un combat (rediffusion)
     * GET /api/combat/replay/{combatNumber}
     */
    @GetMapping("/replay/{combatNumber}")
    public ResponseEntity<?> replay(@PathVariable int combatNumber) {
        try {
            Combat combat = combatService.getCombatByNumber(combatNumber);
            CombatResponseDto response = combatMapper.toCombatResponseDto(combat);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponseDto.notFound(e.getMessage()));
        }
    }

    /**
     * Recupere l'historique de tous les combats
     * GET /api/combat/history
     */
    @GetMapping("/history")
    public ResponseEntity<List<CombatSummaryDto>> getHistory() {
        List<Combat> combats = combatService.getCombatHistory();
        List<CombatSummaryDto> response = combatMapper.toCombatSummaryDtoList(combats);
        return ResponseEntity.ok(response);
    }

    /**
     * Recupere les combats d'un joueur specifique
     * GET /api/combat/player/{playerName}
     */
    @GetMapping("/player/{playerName}")
    public ResponseEntity<List<CombatSummaryDto>> getPlayerCombats(@PathVariable String playerName) {
        List<Combat> combats = combatService.getPlayerCombats(playerName);
        List<CombatSummaryDto> response = combatMapper.toCombatSummaryDtoList(combats);
        return ResponseEntity.ok(response);
    }
}
