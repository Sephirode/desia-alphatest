package desia.Charater;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Enemy {
    private String name;
    private String tier;
    private String property;

    @JsonProperty("max_hp")
    private double maxHp;
    @JsonProperty("max_mp")
    private double maxMp;
    @JsonProperty("attack")
    private double atk;
    @JsonProperty("spell_power")
    private double magic;
    @JsonProperty("speed")
    private double spd;
    @JsonProperty("defense")
    private double def;
    @JsonProperty("magic_resist")
    private double mdef;
    private String description;
    @JsonProperty("base_level")
    private int baseLevel;

    @JsonProperty("growthMax_hp")
    private double growthMaxHp;
    @JsonProperty("growthMax_mp")
    private double growthMaxMp;
    @JsonProperty("growthAttack")
    private double growthAtk;
    @JsonProperty("growthSpell_power")
    private double growthMagic;
    @JsonProperty("growthDefense")
    private double growthDef;
    @JsonProperty("growthMagic_resist")
    private double growthMdef;
    @JsonProperty("growthSpeed")
    private double growthSpd;
}
