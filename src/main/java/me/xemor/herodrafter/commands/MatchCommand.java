package me.xemor.herodrafter.commands;

import me.xemor.herodrafter.HeroDrafter;
import me.xemor.herodrafter.Player;
import me.xemor.herodrafter.match.Match;
import me.xemor.herodrafter.match.MatchException;
import me.xemor.herodrafter.match.MatchHandler;
import me.xemor.herodrafter.match.Team;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;

public class MatchCommand implements Command, ButtonHandler {

    @Override
    public void executeCommand(SlashCommandEvent e) {
        switch (e.getSubcommandName()) {
            case "ongoing" -> viewOngoing(e);
            case "start" -> executeIfPrivileged(e, this::startMatch);
        }
    }

    public void viewOngoing(SlashCommandEvent e) {
        MatchHandler matchHandler = HeroDrafter.getMatchHandler();
        if (matchHandler.getOngoingMatches().size() == 0) { e.getHook().sendMessage(HeroDrafter.getDataManager().getConfig().getNoOngoingMatchesMessage()).queue(); return; }
        for (Match match : matchHandler.getOngoingMatches()) {
            e.getHook().sendMessageEmbeds(match.generateGameEmbed(), match.generateTeamEmbed(match.getTeam1(), "A"), match.generateTeamEmbed(match.getTeam2(), "B")).queue();
        }
    }

    public void startMatch(SlashCommandEvent e) {
        long matchSize = e.getOption("match-size").getAsLong();
        try {
            Match match = HeroDrafter.getMatchHandler().draftMatch(matchSize);
            HeroDrafter.getMatchHandler().getQueue().removeAll(match.getTeam1().getPlayersToHero().keySet());
            HeroDrafter.getMatchHandler().getQueue().removeAll(match.getTeam2().getPlayersToHero().keySet());
            e.getHook().sendMessageEmbeds(match.generateGameEmbed(), match.generateTeamEmbed(match.getTeam1(), "A"), match.generateTeamEmbed(match.getTeam2(), "B"))
                    .addActionRow(Button.primary("TeamARoundWin" + match.getTimestamp(), "Team A Round Win").withEmoji(Emoji.fromUnicode("\uD83C\uDD70"))
                            ,Button.primary("TeamBRoundWin" + match.getTimestamp(), "Team B Round Win").withEmoji(Emoji.fromUnicode("\uD83C\uDD71")),
                            Button.danger("MatchEnd" + match.getTimestamp(), "End the Match").withEmoji(Emoji.fromUnicode("\uD83D\uDC40")),
                            Button.danger("Abandon" + match.getTimestamp(), "Abandon").withEmoji(Emoji.fromUnicode("\uD83D\uDEA8"))).queue();
            HeroDrafter.getMatchHandler().playMatch(match);
        } catch (MatchException ex) {
            e.getHook().sendMessage(ex.getMessage()).queue();
        }
    }


    @Override
    public void handleButton(ButtonClickEvent e) {
        String id = e.getComponentId().replaceAll("[^a-zA-Z]", "");
        long matchTimestamp = Long.parseLong(e.getComponentId().replaceAll("[a-zA-Z]", ""));
        Match match = HeroDrafter.getMatchHandler().getMatch(matchTimestamp);
        if (!HeroDrafter.getDataManager().getConfig().getAdminIDs().contains(e.getUser().getIdLong())) { e.deferEdit(); return; }
        switch (id) {
            case "TeamARoundWin" -> roundWin(e, match, true);
            case "TeamBRoundWin" -> roundWin(e, match, false);
            case "MatchEnd" -> matchEnd(e, match);
            case "Abandon" -> matchAbandon(e, match);
        }
    }

    public void roundWin(ButtonClickEvent e, Match match, boolean teamA) {
        Team winningTeam;
        if (teamA) { winningTeam = match.getTeam1(); }
        else { winningTeam = match.getTeam2(); }
        match.roundWin(winningTeam);
        e.editMessageEmbeds(match.generateGameEmbed(), e.getMessage().getEmbeds().get(1), e.getMessage().getEmbeds().get(2)).queue();
    }

    public void matchEnd(ButtonClickEvent e, Match match) {
        match.endMatch();
        MessageEmbed embed = match.generateEloChangesEmbed(e.getJDA());
        e.getMessage().editMessageEmbeds(embed).setActionRows().queue();
        HeroDrafter.getMatchHandler().endMatch(match);
    }

    public void matchAbandon(ButtonClickEvent e, Match match) {
        e.getMessage().delete().queue();
        HeroDrafter.getMatchHandler().endMatch(match);
    }

}
