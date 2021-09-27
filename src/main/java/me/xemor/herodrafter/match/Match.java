package me.xemor.herodrafter.match;

import me.xemor.herodrafter.HeroDrafter;
import me.xemor.herodrafter.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Match {

    private final long timestamp = System.currentTimeMillis();
    private final Team team1;
    private final Team team2;
    private final Score score;
    private final long eloDifference;
    private double eloChange = 0;
    private final String map;

    public Match(Team team1, Team team2) {
        this.team1 = team1;
        this.team2 = team2;
        this.eloDifference = Math.abs(team1.getAverageElo() - team2.getAverageElo());
        this.score = new Score();
        List<String> maps = HeroDrafter.getDataManager().getConfig().getMaps();
        map = maps.get(ThreadLocalRandom.current().nextInt(maps.size()));
    }

    public void roundWin(Team winningTeam, Team losingTeam) {
        double chance = getWinChance(winningTeam, losingTeam);
        double eloChange = 5 * (1 - chance);
        if (team1 == winningTeam) { score.setScore(score.getTeam1Score() + 1, score.getTeam2Score()); this.eloChange += eloChange; }
        else { score.setScore(score.getTeam1Score(), score.getTeam2Score() + 1); this.eloChange -= eloChange; }
        for (Player player : winningTeam.getPlayersToHero().keySet()) {
            player.setElo(player.getElo() + eloChange);
        }
        for (Player player : losingTeam.getPlayersToHero().keySet()) {
            player.setElo(player.getElo() - eloChange);
        }
    }

    public double getWinChance(Team firstTeam, Team secondTeam) {
        double denominator = 1D + Math.pow(10D, ((secondTeam.getAverageElo() - (double) firstTeam.getAverageElo()) / 400D));
        return 1D / (denominator);
    }

    public String getMap() {
        return map;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Team getTeam1() {
        return team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public Score getScore() {
        return score;
    }

    public long getEloDifference() {
        return eloDifference;
    }

    public double getStandardDeviationDifference() {
        return Math.abs(team1.getStandardDeviation() - team2.getStandardDeviation());
    }

    public MessageEmbed generateGameEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(HeroDrafter.getDataManager().getConfig().getColor());
        embedBuilder.setTitle("The Game");
        embedBuilder.setDescription("You're on your way to");
        embedBuilder.addField("Map", map, true);
        embedBuilder.addField("Score", score.toString(), true);
        embedBuilder.addField("Elo Change", eloChange < 0 ? "+" + eloChange + " for Team B" : "+" + eloChange + " for Team A", true);
        return embedBuilder.build();
    }

    public MessageEmbed generateTeamEmbed(Team team, String letter) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Team " + letter);
        embedBuilder.setDescription("Move to your side!");
        embedBuilder.setColor(HeroDrafter.getDataManager().getConfig().getColor());
        for (Map.Entry<Player, String> entry : team.getPlayersToHero().entrySet()) {
            embedBuilder.addField(entry.getValue(), "<@" + entry.getKey().getId() + ">", true);
        }
        embedBuilder.addField("Average Elo", String.valueOf(team.getAverageElo()), true);
        embedBuilder.addField("Elo σ", String.valueOf(team.getStandardDeviation()), true);
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

    public static class Score {
        private int team1Score = 0;
        private int team2Score = 0;

        public Score() {}

        public void setScore(int team1Score, int team2Score) {
            this.team1Score = team1Score;
            this.team2Score = team2Score;
        }

        public int getTeam1Score() {
            return team1Score;
        }

        public int getTeam2Score() {
            return team2Score;
        }

        @Override
        public String toString() {
            return team1Score + " - " + team2Score;
        }
    }

}
