package me.xemor.herodrafter;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Player implements Comparable<Player> {

    private long id;
    private double elo;
    @SerializedName(value = "standard_deviation")
    private double standardDeviation;
    @SerializedName(value = "heroes", alternate = "champions")
    private List<String> heroes;
    @SerializedName(value = "preferences", alternate = "preference")
    private List<String> preference;

    /**
     * Do not use, here for use in deserialisation.
     */
    public Player() {}

    public Player(Player player) {
        this.id = player.id;
        this.elo = player.elo;
        this.heroes = new ArrayList<>(player.heroes);
        this.preference = new ArrayList<>(player.preference);
        this.standardDeviation = player.standardDeviation;
    }

    public Player(long id, double elo, double standardDeviation, List<String> heroes, List<String> preferences) {
        this.id = id;
        this.elo = elo;
        this.heroes = heroes;
        this.preference = preferences;
        this.standardDeviation = standardDeviation;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public List<String> getPreferences() {
        return preference;
    }

    public void setPreference(List<String> preference) {
        this.preference = preference;
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

    public void setRating(Rating rating) {
        this.elo = rating.getMean();
        this.standardDeviation = rating.getStandardDeviation();
    }

    public Rating getRating() {
        return new Rating(elo, standardDeviation);
    }

    public List<String> getHeroes() {
        return heroes;
    }

    public void setHeroes(List<String> heroes) {
        this.heroes = heroes;
    }

    public double getWinChance(Player other) {
        double ratingDifference = other.elo - this.elo;
        double denominator = 1D + Math.pow(10D, (ratingDifference / 2 * HeroDrafter.getDataManager().getConfig().getTrueSkill().getDrawProbability()));
        return 1D / (denominator);
    }

    public double eloDuel(Player player, double actualScore) {
        double expectedWinChance = getWinChance(player);
        double k = 15;
        double ratingChange = k * (expectedWinChance - actualScore);
        return this.elo + ratingChange;
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
        return Double.compare(new Rating(this.elo, this.standardDeviation).getPublicRating(),
                new Rating(o.elo, o.standardDeviation).getPublicRating());
    }

    public static class Rating {

        private double mean;
        private double standardDeviation;

        public Rating(double mean, double standardDeviation) {
            this.mean = mean;
            this.standardDeviation = standardDeviation;
        }

        public double getMean() {
            return mean;
        }

        public void setMean(double mean) {
            this.mean = mean;
        }

        public double getStandardDeviation() {
            return standardDeviation;
        }

        public void setStandardDeviation(double standardDeviation) {
            this.standardDeviation = standardDeviation;
        }

        public double getPublicRating() {
            return Math.max(mean - (3 * standardDeviation), 0);
        }
    }
}
