package me.xemor.herodrafter.cache;

import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
/* > This event requires the GUILD_MEMBERS intent to be enabled.
net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent.java
it also seems like discord is doing its own caching?>???
are we using the username of the *User* as opposed to the nickname member?
*/
public class NameChangeListener {
    private final PlayerNameCache cache;

    public NameChangeListener(PlayerNameCache cache) {
        this.cache = cache;
    }

    @SubscribeEvent
    public void onUserUpdateName(UserUpdateNameEvent e) {
        cache.
    }
}
