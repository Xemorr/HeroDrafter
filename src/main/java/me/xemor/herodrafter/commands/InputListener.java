package me.xemor.herodrafter.commands;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputListener extends ListenerAdapter {

    private final Map<String, Command> commands = new HashMap<>();
    private final List<ButtonHandler> buttonHandlers = new ArrayList<>();

    public InputListener() {
        commands.put("profile", new ProfileCommand());
        commands.put("queue", new QueueCommand());
        MatchCommand matchCommand = new MatchCommand();
        commands.put("match", matchCommand);
        buttonHandlers.add(matchCommand);
    }

    @Override
    public void onSlashCommand(SlashCommandEvent e) {
        e.deferReply().queue();
        Command command = commands.get(e.getName());
        if (command != null) {
            command.executeCommand(e);
        }
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent e) {
        for (ButtonHandler handler : buttonHandlers) {
            handler.handleButton(e);
        }
    }

}
