package me.xemor.herodrafter.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public interface Command {

    void execute(SlashCommandEvent e);

}
