package desia;

import desia.Charater.Player;
import desia.io.Io;
import desia.loader.GameData;
import desia.loader.GameDataLoader;
import desia.loader.GameLoad;
import desia.loader.Ranking;

import java.util.List;

public class Game {
    private Player currentPlayer;

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
                    System.out.println("게임을 종료하시겠습니까?\n(Y: 1 / N: 2)");
                    input = io1.readInt(">>>",2);
                    if(input == 2)
                        loopNew = false;
                    break;
                default:
                    System.out.println("잘못된 입력");
            }
        } while(loopNew);
    }

    public void newGame(){
        Io io1 = new Io();
        GameDataLoader dataLoader = new GameDataLoader();
        GameData gameData = dataLoader.loadData();
        List<Player> playables = gameData.getPlayableCharacters();

        System.out.println("플레이할 캐릭터를 선택하세요.");
        for (int index = 0; index < playables.size(); index++) {
            Player playable = playables.get(index);
            System.out.printf(
                    "%d. %s%n%s%n",
                    index + 1,
                    playable.getPlayerName(),
                    playable.getDescription()
            );
        }
        System.out.printf("(뒤로가기는 %d 입력)%n", playables.size() + 1);

        int input = io1.readInt(">>>", playables.size() + 1);
        if (input == playables.size() + 1) {
            return;
        }

        currentPlayer = playables.get(input - 1);
        System.out.println("선택한 캐릭터: " + currentPlayer.getPlayerName());
        System.out.println(currentPlayer.getDescription());
        System.out.printf(
                "HP %.1f | MP %.1f | ATK %.1f | MAG %.1f | SPD %.1f | DEF %.1f | MDEF %.1f%n",
                currentPlayer.getMaxHp(),
                currentPlayer.getMaxMp(),
                currentPlayer.getAtk(),
                currentPlayer.getMagic(),
                currentPlayer.getSpd(),
                currentPlayer.getDef(),
                currentPlayer.getMdef()
        );
    }


}
