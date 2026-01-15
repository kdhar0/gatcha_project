package com.gatcha.combat.mapper;

import com.gatcha.combat.dto.request.MonsterRequestDto;
import com.gatcha.combat.dto.request.SkillRequestDto;
import com.gatcha.combat.dto.response.*;
import com.gatcha.combat.model.Combat;
import com.gatcha.combat.model.CombatSkill;
import com.gatcha.combat.model.Fighter;
import com.gatcha.combat.model.TurnLog;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir entre les DTOs et les entites
 */
@Component
public class CombatMapper {

    /**
     * Convertit un MonsterRequestDto en Fighter
     */
    public Fighter toFighter(MonsterRequestDto dto) {
        Fighter fighter = new Fighter();
        fighter.setMonsterId(UUID.randomUUID().toString());
        fighter.setOwnerUsername(dto.getPlayerName());
        fighter.setElement(dto.getElement());
        fighter.setMaxHp(dto.getHp());
        fighter.setCurrentHp(dto.getHp());
        fighter.setAtk(dto.getAtk());
        fighter.setDef(dto.getDef());
        fighter.setVit(dto.getVit());
        fighter.setSkills(dto.getSkills().stream()
                .map(this::toCombatSkill)
                .collect(Collectors.toList()));
        return fighter;
    }

    /**
     * Convertit un SkillRequestDto en CombatSkill
     */
    public CombatSkill toCombatSkill(SkillRequestDto dto) {
        return new CombatSkill(
                dto.getNum(),
                dto.getDmg(),
                dto.getRatioStat(),
                dto.getRatioPercent(),
                dto.getCooldown()
        );
    }

    /**
     * Convertit un Combat en CombatResponseDto
     */
    public CombatResponseDto toCombatResponseDto(Combat combat) {
        CombatResponseDto dto = new CombatResponseDto();
        dto.setCombatNumber(combat.getCombatNumber());
        dto.setTimestamp(combat.getTimestamp());
        dto.setWinnerName(combat.getWinnerOwner());
        dto.setTotalTurns(combat.getTotalTurns());

        FighterResultDto fighter1 = new FighterResultDto(
                combat.getFighter1Owner(),
                combat.getFighter1Element(),
                combat.getFighter1InitialHp(),
                combat.getFighter1FinalHp(),
                combat.getWinnerOwner().equals(combat.getFighter1Owner())
        );
        dto.setFighter1(fighter1);

        FighterResultDto fighter2 = new FighterResultDto(
                combat.getFighter2Owner(),
                combat.getFighter2Element(),
                combat.getFighter2InitialHp(),
                combat.getFighter2FinalHp(),
                combat.getWinnerOwner().equals(combat.getFighter2Owner())
        );
        dto.setFighter2(fighter2);

        dto.setTurnLogs(combat.getTurnLogs().stream()
                .map(this::toTurnLogDto)
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * Convertit un TurnLog en TurnLogDto
     */
    public TurnLogDto toTurnLogDto(TurnLog log) {
        return new TurnLogDto(
                log.getTurnNumber(),
                log.getAttackerOwner(),
                log.getDefenderOwner(),
                log.getSkillUsed(),
                log.getActualDamage(),
                log.getDefenderHpBefore(),
                log.getDefenderHpAfter(),
                log.getDescription()
        );
    }

    /**
     * Convertit un Combat en CombatSummaryDto
     */
    public CombatSummaryDto toCombatSummaryDto(Combat combat) {
        CombatSummaryDto dto = new CombatSummaryDto();
        dto.setCombatNumber(combat.getCombatNumber());
        dto.setTimestamp(combat.getTimestamp());
        dto.setPlayer1(combat.getFighter1Owner());
        dto.setPlayer1Element(combat.getFighter1Element());
        dto.setPlayer2(combat.getFighter2Owner());
        dto.setPlayer2Element(combat.getFighter2Element());
        dto.setWinnerName(combat.getWinnerOwner());
        dto.setTotalTurns(combat.getTotalTurns());
        return dto;
    }

    /**
     * Convertit une liste de combats en liste de CombatSummaryDto
     */
    public List<CombatSummaryDto> toCombatSummaryDtoList(List<Combat> combats) {
        return combats.stream()
                .map(this::toCombatSummaryDto)
                .collect(Collectors.toList());
    }
}
