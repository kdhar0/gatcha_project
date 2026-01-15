package com.gatcha.combat.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO representant une competence dans la requete de combat
 */
public class SkillRequestDto {

    @NotNull(message = "Le numero de la competence est requis")
    @Min(value = 1, message = "Le numero de competence doit etre >= 1")
    private Integer num;

    @NotNull(message = "Les degats de base sont requis")
    @Min(value = 0, message = "Les degats doivent etre >= 0")
    private Integer dmg;

    @NotBlank(message = "La stat du ratio est requise")
    private String ratioStat;

    @NotNull(message = "Le pourcentage du ratio est requis")
    @Min(value = 0, message = "Le ratio doit etre >= 0")
    private Double ratioPercent;

    @NotNull(message = "Le cooldown est requis")
    @Min(value = 0, message = "Le cooldown doit etre >= 0")
    private Integer cooldown;

    public SkillRequestDto() {}

    public SkillRequestDto(Integer num, Integer dmg, String ratioStat, Double ratioPercent, Integer cooldown) {
        this.num = num;
        this.dmg = dmg;
        this.ratioStat = ratioStat;
        this.ratioPercent = ratioPercent;
        this.cooldown = cooldown;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Integer getDmg() {
        return dmg;
    }

    public void setDmg(Integer dmg) {
        this.dmg = dmg;
    }

    public String getRatioStat() {
        return ratioStat;
    }

    public void setRatioStat(String ratioStat) {
        this.ratioStat = ratioStat;
    }

    public Double getRatioPercent() {
        return ratioPercent;
    }

    public void setRatioPercent(Double ratioPercent) {
        this.ratioPercent = ratioPercent;
    }

    public Integer getCooldown() {
        return cooldown;
    }

    public void setCooldown(Integer cooldown) {
        this.cooldown = cooldown;
    }
}
