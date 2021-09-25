package me.xemor.herodrafter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;

public class DataManager {

    private final Multimap<String, Hero> rolesToHeroes = HashMultimap.create();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final Map<String, Hero> registeredHeroes = new HashMap<>();
    private final Map<Long, Player> registeredPlayers = new TreeMap<>();

    public DataManager() {
        Future<?> configWrite = threadPool.submit(() -> defaultResource("/config.json"));
        Future<?> playersWrite = threadPool.submit(() -> defaultResource("/players.json"));
        try {
            configWrite.get();
            playersWrite.get();
            Future<?> loadData = threadPool.submit(() -> {
                try {
                    loadConfig();
                    loadPlayers();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            loadData.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void defaultResource(String resourcePath)  {
        if (!new File(resourcePath).exists()) {
            try {
                byte[] rawConfig = DataManager.class.getResourceAsStream(resourcePath).readAllBytes();
                FileOutputStream fileOutputStream = new FileOutputStream(resourcePath.substring(1));
                fileOutputStream.write(rawConfig);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadConfig() throws IOException {
        FileReader reader = new FileReader("config.json");
        Gson gson = new Gson();
        Config config = gson.fromJson(reader, Config.class);
        for (Hero hero : config.getHeroes()) {
            List<String> roles = hero.getRoles();
            for (String role : roles) {
                rolesToHeroes.put(role, hero);
            }
            registeredHeroes.put(hero.getName(), hero);
        }
        reader.close();
    }

    public void loadPlayers() throws IOException {
        FileReader reader = new FileReader("players.json");
        Gson gson = new Gson();
        Type type = new TypeToken<Player[]>(){}.getType();
        Player[] players = gson.fromJson(reader, type);
        for (Player player : players) {
            registeredPlayers.put(player.getId(), player);
        }
        reader.close();
    }

}
