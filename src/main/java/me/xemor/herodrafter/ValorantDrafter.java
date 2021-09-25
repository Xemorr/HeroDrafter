package me.xemor.herodrafter;

import me.xemor.herodrafter.commands.CommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import javax.security.auth.login.LoginException;

public class ValorantDrafter {

    public static DataManager dataManager;

    public static void main(String[] args) throws InterruptedException, LoginException {
        JDA jda = JDABuilder.createDefault(args[0])
                .addEventListeners(new CommandListener())
                .build();
        dataManager = new DataManager();
        jda.awaitReady();
        registerProfileCommand(jda);
        registerQueueCommand(jda);
        jda.updateCommands().queue();
        jda.getGuildById(805105435954774066L).updateCommands().queue();
    }

    private static void registerProfileCommand(JDA jda) {
        CommandData commandData = new CommandData("profile", "The root command of all profile related commands!");
        SubcommandData viewSubCommandData = new SubcommandData("view", "Allows you to see the profile of others, or yourself!");
        SubcommandData leaderboardSubCommandData = new SubcommandData("leaderboard", "Allows you to see the elo rankings!");
        SubcommandData preferencesSubCommandData = new SubcommandData("preferences", "Allows you to change your preferences");
        viewSubCommandData.addOption(OptionType.USER, "user", "The user to see the profile of.", true);
        preferencesSubCommandData.addOption(OptionType.STRING, "role-preferences", "A space separated list of all the heroes you own", true);
        commandData.addSubcommands(viewSubCommandData, leaderboardSubCommandData, preferencesSubCommandData);
        jda.upsertCommand(commandData).queue();
        jda.getGuildById(805105435954774066L).upsertCommand(commandData).queue();
    }

    private static void registerQueueCommand(JDA jda) {
        CommandData commandData = new CommandData("queue", "The root command of all queue related commands!");
        SubcommandData joinData = new SubcommandData("join", "Allows you to join the queue");
        SubcommandData leaveData = new SubcommandData("leave", "Allows you to leave the queue");
        SubcommandData kickData = new SubcommandData("kick", "Allows you to kick users from the queue");
        kickData.addOption(OptionType.USER, "user", "The user to kick", true);
        SubcommandData addData = new SubcommandData("add", "Allows you to add users to the queue");
        addData.addOption(OptionType.USER, "user", "The user to add", true);
        SubcommandData popData = new SubcommandData("pop", "Generates a match with the given queue");
        commandData.addSubcommands(joinData, leaveData, kickData, addData, popData);
        jda.upsertCommand(commandData).queue();
        jda.getGuildById(805105435954774066L).upsertCommand(commandData).queue();
    }



}
