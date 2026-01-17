package desia.loader;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import desia.Charater.Enemy;
import desia.Charater.Player;
import desia.item.Consumables;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameDataLoader {
    private final ObjectMapper mapper;

    public GameDataLoader() {
        mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public GameData loadData() {
        List<Player> playables = loadPlayableCharacters();
        Map<String, Player> playableById = indexPlayableById(playables);
        Map<String, Consumables> consumablesByName = indexConsumablesByName(loadConsumables());
        Map<String, Enemy> enemiesByName = indexEnemiesByName(loadEnemies());

        return new GameData(playables, playableById, consumablesByName, enemiesByName);
    }

    private List<Player> loadPlayableCharacters() {
        PlayablesWrapper wrapper = readResource("Playables.json", PlayablesWrapper.class);
        return wrapper.playableCharacters;
    }

    private List<Consumables> loadConsumables() {
        ConsumablesWrapper wrapper = readResource("consumables.json", ConsumablesWrapper.class);
        return wrapper.consumables;
    }

    private List<Enemy> loadEnemies() {
        return readResource("enemies_with_growth.json", new TypeReference<>() {});
    }

    private Map<String, Player> indexPlayableById(List<Player> playables) {
        Map<String, Player> playableById = new LinkedHashMap<>();
        for (Player playable : playables) {
            playableById.put(playable.getId(), playable);
        }
        return playableById;
    }

    private Map<String, Consumables> indexConsumablesByName(List<Consumables> consumables) {
        Map<String, Consumables> consumablesByName = new LinkedHashMap<>();
        for (Consumables consumable : consumables) {
            consumablesByName.put(consumable.getItemName(), consumable);
        }
        return consumablesByName;
    }

    private Map<String, Enemy> indexEnemiesByName(List<Enemy> enemies) {
        Map<String, Enemy> enemiesByName = new LinkedHashMap<>();
        for (Enemy enemy : enemies) {
            enemiesByName.put(enemy.getName(), enemy);
        }
        return enemiesByName;
    }

    private <T> T readResource(String resourceName, Class<T> valueType) {
        try (InputStream input = getResourceStream(resourceName)) {
            return mapper.readValue(input, valueType);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load resource: " + resourceName, e);
        }
    }

    private <T> T readResource(String resourceName, TypeReference<T> valueType) {
        try (InputStream input = getResourceStream(resourceName)) {
            return mapper.readValue(input, valueType);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load resource: " + resourceName, e);
        }
    }

    private InputStream getResourceStream(String resourceName) {
        InputStream input = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (input == null) {
            throw new IllegalStateException("Resource not found: " + resourceName);
        }
        return input;
    }

    private static class PlayablesWrapper {
        private List<Player> playableCharacters;
    }

    private static class ConsumablesWrapper {
        private List<Consumables> consumables;
    }
}
