package me.xemor.herodrafter.commands;

import me.xemor.herodrafter.HeroDrafter;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.function.Consumer;

public interface Command {

    void executeCommand(SlashCommandEvent e);

    default void executeIfPrivileged(SlashCommandEvent e, Consumer<SlashCommandEvent> subCommand) {
        if (HeroDrafter.getDataManager().getConfig().getAdminIDs().contains(e.getUser().getIdLong())) {
            subCommand.accept(e);
        } else {
            e.getHook().sendMessage(HeroDrafter.getDataManager().getConfig().getInvalidPermissions().replace("%user_name%", e.getUser().getName())).queue();
        }
    }

}
