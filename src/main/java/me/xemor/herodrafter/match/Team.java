package me.xemor.herodrafter.match;

import me.xemor.herodrafter.DataManager;
import me.xemor.herodrafter.Player;
import me.xemor.herodrafter.HeroDrafter;

import java.util.*;
import java.util.stream.Collectors;

public class Team {

    private Map<Player, String> playersToHero;
    private int score;
    private double eloSum;
    private double averageElo;
    private double varianceSum;
    private double standardDeviation;

    public Team(List<Player> players, List<String> roleComposition) throws MatchException {
        playersToHero  = chooseHeroComposition(roleComposition, players);
        eloSum = players.stream().map(Player::getElo).reduce(Double::sum).get();
        averageElo = eloSum / playersToHero.size();
        calculateStandardDeviation();
        varianceSum = players.stream().map(Player::getStandardDeviation).map((standard_deviation) -> standard_deviation * standard_deviation).reduce(Double::sum).get();
    }

    private Map<Player, String> chooseHeroComposition(List<String> roleComposition, List<Player> team) throws MatchException {
        Map<Player, String> heroes = new HashMap<>();
        DataManager dataManager = HeroDrafter.getDataManager();
        team = new ArrayList<>(team);
        for (String role : roleComposition) {
            int topPreference = Integer.MAX_VALUE;
            Player topPlayer = null;
            if (team.size() == 0) continue;
            for (Player player : team) {
                int roleDesire = player.getPreferences().indexOf(role);
                if (roleDesire < topPreference) {
                    topPreference = roleDesire;
                    topPlayer = player;
                }
            }
            team.remove(topPlayer);
            Player finalTopPlayer = topPlayer;
            List<String> legalHeroes = topPlayer.getHeroes().stream()
                    .filter((heroName) -> !heroes.containsValue(heroName))
                    .filter((heroName) -> dataManager.getHero(heroName).get().getRoles().contains(role))
                    .collect(Collectors.toList());
            if (legalHeroes.size() == 0)
                throw new MatchException("Could not find valid hero for " + finalTopPlayer.getId()
                        + ". Previously selected heroes: " + heroes.values().stream().reduce((string1, string2) -> string1 + " " + string2)
                        + ". Role composition: " + roleComposition.stream().reduce((string1, string2) -> string1 + " " + string2));
            Collections.shuffle(legalHeroes);
            heroes.put(topPlayer, legalHeroes.get(0));
        }
        return heroes;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    private double calculateStandardDeviation() {
        double sumOfDeviation = playersToHero.keySet().stream().map(Player::getElo).map((elo) -> (elo - averageElo) * (elo - averageElo)).reduce(Double::sum).get();
        double variance = sumOfDeviation / (double) playersToHero.size();
        standardDeviation = Math.sqrt(variance);
        return standardDeviation;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public Map<Player, String> getPlayersToHero() {
        return playersToHero;
    }

    public double getEloSum() {
        return eloSum;
    }

    public double getVarianceSum() {
        return varianceSum;
    }
}
