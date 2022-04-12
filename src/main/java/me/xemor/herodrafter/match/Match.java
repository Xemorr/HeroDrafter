package me.xemor.herodrafter.match;

import me.xemor.herodrafter.HeroDrafter;
import me.xemor.herodrafter.Player;
import me.xemor.herodrafter.match.TrueSkill.DrawMargin;
import me.xemor.herodrafter.match.TrueSkill.TrueSkill;
import me.xemor.herodrafter.match.TrueSkill.TruncatedGaussianCorrectionFunctions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Match {

    private final long timestamp = System.currentTimeMillis();
    private final Team[] teams;
    private final long eloDifference;
    private final String map;
    private final Map<Player, Player.Rating> eloChange = new HashMap<>();

    public Match(Team team1, Team team2) {
        teams = new Team[2];
        teams[0] = team1;
        teams[1] = team2;
        this.eloDifference = (long) Math.abs(team1.getEloSum() - team2.getEloSum());
        List<String> maps = HeroDrafter.getDataManager().getConfig().getMaps();
        map = maps.get(ThreadLocalRandom.current().nextInt(maps.size()));
    }

    public void roundWin(Team winningTeam) {
        if (teams[0] == winningTeam) { teams[0].setScore(teams[0].getScore() + 1); }
        else teams[1].setScore(teams[1].getScore() + 1);
    }

    public void calculateNewRatings() {
        double team1Score = (Math.signum(teams[0].getScore() - teams[1].getScore()) + 1) / 2; //returns 0 if loss, 0.5 if draw, 1 if win.
        double team2Score = (Math.signum(teams[1].getScore() - teams[0].getScore()) + 1) / 2; //returns 0 if loss, 0.5 if draw, 1 if win.
        updatePlayerRatings(eloChange, teams[0], teams[1], team1Score);
        updatePlayerRatings(eloChange, teams[1], teams[0], team2Score);
        for (Map.Entry<Player, Player.Rating> entry : eloChange.entrySet()) {
            entry.getKey().setRating(entry.getValue());
        }
        HeroDrafter.getDataManager().savePlayers();
    }

    private void updatePlayerRatings(Map<Player, Player.Rating> newRatings, Team team1, Team team2, double score) {
        TrueSkill trueSkill = HeroDrafter.getDataManager().getConfig().getTrueSkill();
        double drawMargin = DrawMargin.getDrawMarginFromDrawProbability(trueSkill.getDrawProbability(), trueSkill.getBeta());
        double betaSquared = trueSkill.getBeta() * trueSkill.getBeta();
        double dynamicsSquared = trueSkill.getDynamicsFactor() * trueSkill.getDynamicsFactor();
        int totalPlayers = team1.getPlayersToHero().size() + team2.getPlayersToHero().size();
        double c = Math.sqrt(team1.getVarianceSum() + team2.getVarianceSum() + totalPlayers * betaSquared);
        double winningMean = team1.getEloSum();
        double losingMean = team2.getEloSum();
        if (score == 0) {
            winningMean = team2.getEloSum();
            losingMean = team1.getEloSum();
        }
        double meanDelta = winningMean - losingMean;
        double v;
        double w;
        double rankMultiplier = 1;
        if (score != 0.5D) {
            v = TruncatedGaussianCorrectionFunctions.VExceedsMargin(meanDelta, drawMargin, c);
            w = TruncatedGaussianCorrectionFunctions.WExceedsMargin(meanDelta, drawMargin, c);
            if (score == 0D) rankMultiplier = -1;
        }
        else {
            v = TruncatedGaussianCorrectionFunctions.VWithinMargin(meanDelta, drawMargin, c);
            w = TruncatedGaussianCorrectionFunctions.WWithinMargin(meanDelta, drawMargin, c);
        }

        for (Player player : team1.getPlayersToHero().keySet()) {
            double meanMultiplier = ((player.getStandardDeviation() * player.getStandardDeviation()) + dynamicsSquared) / c;
            double standardDeviationMultiplier = ((player.getStandardDeviation() * player.getStandardDeviation()) + dynamicsSquared) / (c * c);
            double playerMeanDelta = v * meanMultiplier * rankMultiplier;
            double newMean = player.getElo() + playerMeanDelta;
            double newStandardDeviation = Math.sqrt(((player.getStandardDeviation() * player.getStandardDeviation()) + dynamicsSquared) * (1 - w * standardDeviationMultiplier));
            newRatings.put(player, new Player.Rating(newMean, newStandardDeviation));
        }
    }

    public void endMatch() {
        calculateNewRatings();
    }

    public double getWinChance(Team firstTeam, Team secondTeam) {
        double denominator = 1D + Math.pow(10D, ((secondTeam.getEloSum() - (double) firstTeam.getEloSum()) / 400D));
        return 1D / (denominator);
    }

    public String getMap() {
        return map;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Team getTeam1() {
        return teams[0];
    }

    public Team getTeam2() {
        return teams[1];
    }

    public String getScoreString() {
        return teams[0].getScore() + " - " + teams[1].getScore();
    }

    public long getEloDifference() {
        return eloDifference;
    }

    public MessageEmbed generateGameEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(HeroDrafter.getDataManager().getConfig().getColor());
        embedBuilder.setTitle("The Game");
        embedBuilder.setDescription("You're on your way to");
        embedBuilder.addField("Map", map, true);
        embedBuilder.addField("Score", getScoreString(), true);
        return embedBuilder.build();
    }

    public MessageEmbed generateEloChangesEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(HeroDrafter.getDataManager().getConfig().getColor());
        embedBuilder.setTitle("The Rating Changes");
        embedBuilder.setDescription("What are the new rankings?");
        StringBuilder eloList = new StringBuilder();
        StringBuilder playerList = new StringBuilder();
        // i wish you just used kotlin omg
        final var newEloSet = eloChange.entrySet().stream()
                .filter(it -> it.getKey().getId() != -1L)
                .sorted(Comparator.comparingDouble(it -> it.getValue().getPublicRating()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), it -> {Collections.reverse(it); return it;}));
        for (var it : newEloSet) {
            eloList.append(String.format("%.0f", it.getValue().getPublicRating())).append("\n");
            playerList.append("<@").append(it.getKey().getId()).append(">").append("\n");
        }
        embedBuilder.addField("Players", playerList.toString(), true);
        embedBuilder.addField("New Rankings", eloList.toString(), true);
        return embedBuilder.build();
    }

    public MessageEmbed generateTeamEmbed(Team team, String letter) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Team " + letter);
        embedBuilder.setDescription("Move to your side!");
        embedBuilder.setColor(HeroDrafter.getDataManager().getConfig().getColor());
        int index = 0;
        for (Map.Entry<Player, String> entry : team.getPlayersToHero().entrySet()) {
            String role = team.getRoleComposition().get(index++);
            embedBuilder.addField(String.format("%s (%s - %d)", entry.getValue(), role, entry.getKey().getPreferences().indexOf(role) + 1), "<@" + entry.getKey().getId() + ">", true);
        }
        double averagePublicElo = team.getPlayersToHero().keySet().stream()
                .map(player -> player.getRating().getPublicRating()).reduce(Double::sum).get() / team.getPlayersToHero().size();
        embedBuilder.addField("Average Rating", String.format("%.0f", averagePublicElo), true);
        embedBuilder.addField("Rating σ", String.format("%.0f", team.getStandardDeviation()), true);
        return embedBuilder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Match) {
            return ((Match) o).timestamp == timestamp;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) timestamp;
    }
}
