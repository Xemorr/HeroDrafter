package me.xemor.herodrafter.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommand(SlashCommandEvent e) {
        e.deferReply().queue();
        if (e.getName().equals("profile")) {

        } else if (e.getName().equals("queue")) {

        }
        e.getHook().sendMessage("a message").queue();
    }

}
