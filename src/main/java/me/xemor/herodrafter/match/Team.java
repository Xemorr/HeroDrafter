package me.xemor.herodrafter.match;

import me.xemor.herodrafter.DataManager;
import me.xemor.herodrafter.Player;
import me.xemor.herodrafter.HeroDrafter;

import java.util.*;

public class Team {

    private Map<Player, String> playersToHero;
    private long averageElo = 0;
    private double standardDeviation = 0;

    public Team(List<Player> players, List<String> roleComposition) throws MatchException {
        List<String> heroes = chooseHeroComposition(roleComposition, players);
        this.playersToHero = new HashMap<>();
        for (int i = 0; i < players.size(); i++) {
            playersToHero.put(players.get(i), heroes.get(i));
        }
        calculateAverageElo();
        calculateStandardDeviation();
    }

    private List<String> chooseHeroComposition(List<String> roleComposition, List<Player> team) throws MatchException {
        List<String> heroes = new ArrayList<>();
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
            heroes.add(topPlayer.getHeroes().stream()
                    .filter((heroName) -> !heroes.contains(heroName))
                    .filter((heroName) -> dataManager.getHero(heroName).get().getRoles().contains(role))
                    .findAny().orElseThrow(() -> new MatchException("Could not find valid hero for " + finalTopPlayer.getId()
                            + ". Previously selected heroes: " + heroes.stream().reduce((string1, string2) -> string1 + " " + string2)
                    + ". Role composition: " + roleComposition.stream().reduce((string1, string2) -> string1 + " " + string2))));
        }
        return heroes;
    }

    private long calculateAverageElo() {
        for (Player player : playersToHero.keySet()) {
            averageElo += player.getElo();
        }
        averageElo /= playersToHero.size();
        return averageElo;
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

    public Map<Player, String> getPlayersToHero() {
        return playersToHero;
    }

    public long getAverageElo() {
        return averageElo;
    }
}
