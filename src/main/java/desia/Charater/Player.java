package desia.Charater;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor //자동 생성자
@NoArgsConstructor  //default 생성자
@Builder            //객체를 생성하기 위한 다른 방법. 변수 순서를 섞어도 가능하게 함.
@Getter
public class Player {
    private String playerName;
    private double maxHp;
    private double maxMp;
    private double atk;
    private double magic;
    private double spd;
    private double def;
    private double mdef;
    private String description;
    private int level;
    private double growthMaxHp;
    private double growthMaxMp;
    private double growthAtk;
    private double growthMagic;
    private double growthSpd;
    private double growthDef;
    private double growthMdef;
    /*
      */
}
