package me.xemor.herodrafter;

import me.xemor.herodrafter.commands.InputListener;
import me.xemor.herodrafter.match.MatchHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import javax.security.auth.login.LoginException;

public class HeroDrafter {

    private static DataManager dataManager;
    private static MatchHandler matchHandler;

    public static void main(String[] args) throws InterruptedException, LoginException {
        JDA jda = JDABuilder.createDefault(args[0])
                .addEventListeners(new InputListener())
                .build();
        dataManager = new DataManager();
        matchHandler = new MatchHandler();
        jda.awaitReady();
        jda.getGuildById(805105435954774066L).updateCommands().addCommands(registerProfileCommand(jda), registerQueueCommand(jda), registerMatchCommand(jda)).queue();
    }

    public static DataManager getDataManager() {
        return dataManager;
    }

    public static MatchHandler getMatchHandler() {
        return matchHandler;
    }

    private static CommandData registerProfileCommand(JDA jda) {
        CommandData commandData = new CommandData("profile", "The root command of all profile related commands!");

        SubcommandData viewSubCommandData = new SubcommandData("view", "Allows you to see the profile of others, or yourself!");
        viewSubCommandData.addOption(OptionType.USER, "user", "The user to see the profile of.", false);

        SubcommandData leaderboardSubCommandData = new SubcommandData("leaderboard", "Allows you to see the elo rankings!");

        SubcommandData preferencesSubCommandData = new SubcommandData("preferences", "Allows you to change your preferences");
        preferencesSubCommandData.addOption(OptionType.STRING, "role-preferences", "A space separated list of all the heroes you own", true);

        SubcommandData createSubCommandData = new SubcommandData("create", "Allows you to create a profile with the default settings!");

        SubcommandData addSubCommandData = new SubcommandData("add", "Allows you to add a hero to your own profile!");
        addSubCommandData.addOption(OptionType.STRING, "hero", "The name of the hero you want to add!", true);

        SubcommandData forceAddSubCommandData = new SubcommandData("force-add", "Allows you to forcibly add a hero to another user's profile!");
        forceAddSubCommandData.addOption(OptionType.STRING, "hero", "The name of the hero you want to add!", true);

        SubcommandData removeSubCommandData = new SubcommandData("remove", "Allows you to remove a hero to your own profile!");
        removeSubCommandData.addOption(OptionType.STRING, "hero", "The name of the hero you want to remove!", true);

        SubcommandData forceRemoveSubCommandData = new SubcommandData("force-remove", "Allows you to forcibly remove a hero to another user's profile!");
        forceRemoveSubCommandData.addOption(OptionType.STRING, "hero", "The name of the hero you want to remove!", true);

        commandData.addSubcommands(viewSubCommandData, leaderboardSubCommandData, preferencesSubCommandData, createSubCommandData,
                addSubCommandData, forceAddSubCommandData, removeSubCommandData, forceRemoveSubCommandData);
        return commandData;
    }

    private static CommandData registerQueueCommand(JDA jda) {
        CommandData commandData = new CommandData("queue", "The root command of all queue related commands!");

        SubcommandData joinData = new SubcommandData("join", "Allows you to join the queue");

        SubcommandData leaveData = new SubcommandData("leave", "Allows you to leave the queue");

        SubcommandData viewData = new SubcommandData("view", "Allows you to view who is in the queue");

        SubcommandData kickData = new SubcommandData("kick", "Allows you to kick users from the queue");
        kickData.addOption(OptionType.USER, "user", "The user to kick", true);

        SubcommandData vcData = new SubcommandData("vc", "Adds all the members of your current vc to the queue");

        SubcommandData addData = new SubcommandData("add", "Allows you to add users to the queue");
        addData.addOption(OptionType.USER, "user", "The user to add", true);

        commandData.addSubcommands(joinData, leaveData, kickData, addData, viewData, vcData);
        return commandData;
    }

    private static CommandData registerMatchCommand(JDA jda) {
        CommandData commandData = new CommandData("match", "The root command of all match related commands!");

        SubcommandData ongoingData = new SubcommandData("ongoing", "Allows you to see the matches that are currently ongoing!");
        ongoingData.addOption(OptionType.INTEGER, "amount", "The number of matches to show - WIP", false);

        SubcommandData startData = new SubcommandData("start", "Generates a match with the given queue");
        startData.addOption(OptionType.INTEGER, "match-size", "The number of players participating in the match", true);

        commandData.addSubcommands(ongoingData, startData);

        return commandData;
    }



}
