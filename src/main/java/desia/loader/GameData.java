package desia.loader;

import desia.Charater.Enemy;
import desia.Charater.Player;
import desia.item.Consumables;

import java.util.List;
import java.util.Map;

public class GameData {
    private final List<Player> playableCharacters;
    private final Map<String, Player> playableById;
    private final Map<String, Consumables> consumablesByName;
    private final Map<String, Enemy> enemiesByName;

    public GameData(
            List<Player> playableCharacters,
            Map<String, Player> playableById,
            Map<String, Consumables> consumablesByName,
            Map<String, Enemy> enemiesByName
    ) {
        this.playableCharacters = playableCharacters;
        this.playableById = playableById;
        this.consumablesByName = consumablesByName;
        this.enemiesByName = enemiesByName;
    }

    public List<Player> getPlayableCharacters() {
        return playableCharacters;
    }

    public Map<String, Player> getPlayableById() {
        return playableById;
    }

    public Map<String, Consumables> getConsumablesByName() {
        return consumablesByName;
    }

    public Map<String, Enemy> getEnemiesByName() {
        return enemiesByName;
    }
}
