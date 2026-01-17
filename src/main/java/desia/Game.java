package desia;

import desia.io.Io;
import desia.item.Consumables;
import desia.loader.GameLoad;
import desia.loader.Ranking;

import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Map;

public class Game {

    public void Start(){
        Io io1 = new Io();
        GameLoad gl = new GameLoad();
        Ranking rk = new Ranking();

        boolean loopNew = true;
        System.out.println("\t\t\t\t\tD e s i a\n\t\t\t\t  신 화    시 대 ");
        do {
            System.out.println("1. 새 게임\t2. 불러오기\t3. 랭킹\t4. 게임 종료");
            int input = io1.readInt(">>>", 4);

            switch (input) {
                case 1:
                    newGame();
                    loopNew = false; break;
                case 2:
                    gl.gameLoad();
                    loopNew = false; break;
                case 3:
                    rk.printRanking();
                    loopNew = false; break;
                case 4:
                    System.out.println("게임을 종료하시겠습니까?\n(Y: 1 / N: 0)");
                    input = io1.readInt(">>>",2);
                    if(input == 1)
                        loopNew = false;
                    break;
                default:
                    System.out.println("잘못된 입력");
            }
        } while(loopNew);
    }

    public void newGame(){
        Io io1 = new Io();
        System.out.println("플레이할 캐릭터를 선택하세요.");
        System.out.println("1. 전사\n높은 체력과 공격력을 갖춘 전사. 용맹한 심장을 지녔다.");
        System.out.println("2. 마법사\n체력은 약하지만 강력하고 파괴적인 마법을 구사한다.");
        System.out.println("3. 암살자\n그림자 속에서 누구보다 빠른 속도로 적을 제압한다.");
        System.out.println("4. 마검사\n검술 시험에서도, 마도사 시험에서도 떨어진 비운의 전사. 그러나 엄청난 잠재력을 지녔다.");

        int input = io1.readInt(">>>",3);

        switch(input){
            case 1:
                break;
            case 2:
                break;
        }


    }


}
