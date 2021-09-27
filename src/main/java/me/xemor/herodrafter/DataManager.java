package me.xemor.herodrafter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class DataManager {

    private final Multimap<Hero, String> heroToRoles = HashMultimap.create();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final Map<String, Hero> registeredHeroes = new HashMap<>();
    private Config config;
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
        if (!new File(resourcePath.substring(1)).exists()) {
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
        config = gson.fromJson(reader, Config.class);
        for (Hero hero : config.getHeroes()) {
            List<String> roles = hero.getRoles();
            for (String role : roles) {
                heroToRoles.put(hero, role);
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
    
    public Future<?> savePlayers() {
        List<Player> players = registeredPlayers.values().stream().map(Player::new).collect(Collectors.toList());
        return threadPool.submit(() -> {
            try {
                Gson gson = new Gson();
                String rawJSON = gson.toJson(players);
                FileWriter fileWriter = new FileWriter("players.json");
                fileWriter.write(rawJSON);
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setPlayer(Player player) {
        registeredPlayers.put(player.getId(), player);
    }

    public Optional<Player> getPlayer(long id) {
        return Optional.ofNullable(registeredPlayers.get(id));
    }

    /**
     * @return A collection of players sorted in ascending ID order
     */
    public Collection<Player> getPlayers() {
        return registeredPlayers.values();
    }

    public Collection<String> getRoles() { return new HashSet<>(heroToRoles.values());}

    public boolean hasRole(Hero hero, String role) {
        return heroToRoles.containsEntry(hero, role);
    }

    public Optional<Hero> getHero(String heroName) {
        return Optional.ofNullable(registeredHeroes.get(heroName));
    }

    public Config getConfig() {
        return config;
    }
}
