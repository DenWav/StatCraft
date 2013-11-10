package wav.demon;

import org.bukkit.plugin.java.JavaPlugin;

public final class IRCBot extends JavaPlugin {

    IRC irc = new IRC(this);

    @Override
    public void onEnable() {
        irc.enableIRC();
        getCommand("list").setExecutor(new IRCBotListCommandExecutor(this, irc.getSession()));
        getCommand("deaths").setExecutor(new IRCBotDeathsCommandExecutor(this, irc.getSession()));
        getCommand("kill").setExecutor(new IRCBotKillCommandExecutor(this));
        getServer().getPluginManager().registerEvents(new DeathsListener(), this);
    }

    @Override
    public void onDisable() {
        irc.disableIRC();
    }
}
