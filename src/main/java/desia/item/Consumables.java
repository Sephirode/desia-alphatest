package desia.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor //자동 생성자
@NoArgsConstructor  //default 생성자
@Builder            //객체를 생성하기 위한 다른 방법. 변수 순서를 섞어도 가능하게 함.
@Getter

public class Consumables {

    private String itemName;
    private String category;
    private String description;
    private String rarity;
    private String effectType;
    private int level;
    private int crt;
    private double xp;
    private double atk;
    private double magic;
    private double def;
    private double mdef;
    private double maxHp;
    private double maxMp;
    private double hp;
    private double mp;
    private double spd;
    private double price;
    private boolean useInBattle;
    private boolean UseOutOfBattle;

}
