package com.gatcha.combat.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * DTO pour la requete de combat entre 2 monstres
 */
public class CombatRequestDto {

    @NotNull(message = "Le monstre 1 est requis")
    @Valid
    private MonsterRequestDto monster1;

    @NotNull(message = "Le monstre 2 est requis")
    @Valid
    private MonsterRequestDto monster2;

    public CombatRequestDto() {}

    public CombatRequestDto(MonsterRequestDto monster1, MonsterRequestDto monster2) {
        this.monster1 = monster1;
        this.monster2 = monster2;
    }

    public MonsterRequestDto getMonster1() {
        return monster1;
    }

    public void setMonster1(MonsterRequestDto monster1) {
        this.monster1 = monster1;
    }

    public MonsterRequestDto getMonster2() {
        return monster2;
    }

    public void setMonster2(MonsterRequestDto monster2) {
        this.monster2 = monster2;
    }
}
