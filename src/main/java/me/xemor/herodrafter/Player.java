package me.xemor.herodrafter;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Player implements Comparable<Player> {

    private long id;
    private double elo;
    @SerializedName(value = "heroes", alternate = "champions")
    private List<String> heroes;
    @SerializedName(value = "preferences", alternate = "preference")
    private List<String> preference;
    @SerializedName(value = "role_elo")
    private Map<String, Integer> roleElo;

    /**
     * Do not use, here for use in deserialisation.
     */
    public Player() {}

    public Player(Player player) {
        this.id = player.id;
        this.elo = player.elo;
        this.heroes = new ArrayList<>(player.heroes);
        this.preference = new ArrayList<>(player.preference);
        this.roleElo = new TreeMap<>(player.roleElo);
    }

    public Player(long id, double elo, List<String> heroes, List<String> preferences, Map<String, Integer> roleElo) {
        this.id = id;
        this.elo = elo;
        this.heroes = heroes;
        this.preference = preferences;
        this.roleElo = roleElo;
    }

    public List<String> getPreferences() {
        return preference;
    }

    public void setPreference(List<String> preference) {
        this.preference = preference;
    }

    public Map<String, Integer> getRoleElo() {
        return roleElo;
    }

    public void setRoleElo(Map<String, Integer> roleElo) {
        this.roleElo = roleElo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getElo() {
        return elo;
    }

    public void setElo(double elo) {
        this.elo = elo;
    }

    public List<String> getHeroes() {
        return heroes;
    }

    public void setHeroes(List<String> heroes) {
        this.heroes = heroes;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Player) {
            Player otherPlayer = (Player) o;
            return otherPlayer.id == id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) id;
    }

    @Override
    public int compareTo(@NotNull Player o) {
        return Double.compare(elo, o.elo);
    }
}
