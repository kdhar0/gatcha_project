package com.gatcha.combat.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO representant un monstre dans la requete de combat
 */
public class MonsterRequestDto {

    @NotBlank(message = "Le nom du joueur est requis")
    private String playerName;

    @NotBlank(message = "L'element du monstre est requis")
    private String element;

    @NotNull(message = "Les HP sont requis")
    @Min(value = 1, message = "Les HP doivent etre > 0")
    private Integer hp;

    @NotNull(message = "L'attaque est requise")
    @Min(value = 0, message = "L'attaque doit etre >= 0")
    private Integer atk;

    @NotNull(message = "La defense est requise")
    @Min(value = 0, message = "La defense doit etre >= 0")
    private Integer def;

    @NotNull(message = "La vitesse est requise")
    @Min(value = 0, message = "La vitesse doit etre >= 0")
    private Integer vit;

    @NotNull(message = "Les competences sont requises")
    @Size(min = 1, max = 3, message = "Un monstre doit avoir entre 1 et 3 competences")
    @Valid
    private List<SkillRequestDto> skills;

    public MonsterRequestDto() {}

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public Integer getHp() {
        return hp;
    }

    public void setHp(Integer hp) {
        this.hp = hp;
    }

    public Integer getAtk() {
        return atk;
    }

    public void setAtk(Integer atk) {
        this.atk = atk;
    }

    public Integer getDef() {
        return def;
    }

    public void setDef(Integer def) {
        this.def = def;
    }

    public Integer getVit() {
        return vit;
    }

    public void setVit(Integer vit) {
        this.vit = vit;
    }

    public List<SkillRequestDto> getSkills() {
        return skills;
    }

    public void setSkills(List<SkillRequestDto> skills) {
        this.skills = skills;
    }
}
