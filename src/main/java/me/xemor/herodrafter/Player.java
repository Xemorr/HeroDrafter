package me.xemor.herodrafter;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class Player {

    private long id;
    private long elo;
    private List<String> champions;
    @SerializedName(value = "preferences", alternate = "preference")
    private List<String> preference;
    @SerializedName(value = "role_elo")
    private Map<String, Integer> roleElo;

    /**
     * Do not use, here for use in deserialisation.
     */
    public Player() {}

    public List<String> getPreference() {
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

    public long getElo() {
        return elo;
    }

    public void setElo(long elo) {
        this.elo = elo;
    }

    public List<String> getChampions() {
        return champions;
    }

    public void setChampions(List<String> champions) {
        this.champions = champions;
    }
}
