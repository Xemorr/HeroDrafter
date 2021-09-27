package me.xemor.herodrafter.commands;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public interface ButtonHandler {

    void handleButton(ButtonClickEvent e);

}
