package me.xemor.herodrafter.match;

import me.xemor.herodrafter.Player;
import me.xemor.herodrafter.HeroDrafter;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class MatchHandler {

    private final TreeMap<Long, Match> ongoingMatches = new TreeMap<>();
    private final Deque<Player> queue = new ArrayDeque<>();

    public MatchHandler() {

    }

    public void playMatch(Match match) {
        ongoingMatches.put(match.getTimestamp(), match);
    }

    public Match getMatch(long timestamp) {
        return ongoingMatches.get(timestamp);
    }

    public void endMatch(Match match) {
        ongoingMatches.remove(match.getTimestamp());
    }

    public Deque<Player> getQueue() {
        return queue;
    }

    public Match draftMatch(long matchSize) throws MatchException {
        if (queue.size() < matchSize || matchSize <= 1) {
            throw new MatchException("Queue is too small!");
        }
        List<Player> pool = queue.stream().limit(matchSize).collect(Collectors.toList());
        List<String> roleComposition = chooseRoleComposition();
        if (matchSize % 2 != 0) {
            pool.add(HeroDrafter.getDataManager().getPlayer(-1L).get());
        }
        int iterations = 0;
        long smallestEloDifference = Long.MAX_VALUE;
        double smallestStandardDeviationDifference = Double.MAX_VALUE;
        Match bestMatch = null;
        while (iterations < 200) {
            iterations++;
            List<Player> team1Players = pool.subList(0, pool.size() / 2);
            long team1EloSum = Math.round(team1Players.stream().map(Player::getElo).reduce(Double::sum).get());
            List<Player> team2Players = pool.subList(pool.size() / 2, pool.size());
            long team2EloSum = Math.round(team2Players.stream().map(Player::getElo).reduce(Double::sum).get());
            long eloDifference = Math.abs(team1EloSum - team2EloSum);
            if (eloDifference <= smallestEloDifference) {
                Match newMatch = new Match(new Team(team1Players, roleComposition), new Team(team2Players, roleComposition));
                if (newMatch.getStandardDeviationDifference() < smallestStandardDeviationDifference || eloDifference < smallestEloDifference) {
                    smallestStandardDeviationDifference = newMatch.getStandardDeviationDifference();
                    smallestEloDifference = eloDifference;
                    bestMatch = newMatch;
                }
            }
            Collections.shuffle(pool);
        }
        return bestMatch;
    }

    public List<String> chooseRoleComposition() {
        List<List<String>> compositions = HeroDrafter.getDataManager().getConfig().getCompositions();
        return compositions.get(ThreadLocalRandom.current().nextInt(compositions.size()));
    }

    public Collection<Match> getOngoingMatches() {
        return ongoingMatches.values();
    }

    public void addToQueue(Player player) {
        if (!queue.contains(player)) queue.add(player);
    }

    public void removeFromQueue(Player player) {
        queue.remove(player);
    }

}
