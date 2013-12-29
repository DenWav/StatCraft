package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class WordsSpoken extends StatListener {

    public WordsSpoken(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpokenMessage(AsyncPlayerChatEvent event) {
        final String name = event.getPlayer().getName();
        final String[] message = event.getMessage().trim().split("\\s+");

        addStat(StatTypes.MESSAGES_SPOKEN.id, name, getStat(name, StatTypes.MESSAGES_SPOKEN.id) + 1);

        if (plugin.getWords_spoken()) {
            if (plugin.getSpecific_words_spoken())
                for (String word : message) {
                    incrementStat(StatTypes.WORDS_SPOKEN.id, name, word);
                }
            else
                addStat(StatTypes.WORDS_SPOKEN.id, name, getStat(name, StatTypes.WORDS_SPOKEN.id) + message.length);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wordsspoken")) {
            // list the number of words a player has spoken
            ArrayList<String> names = getPlayers(sender, args);
            if (names == null)
                return false;

            for (String name : names) {
                String stat = df.format(getStat(name, StatTypes.WORDS_SPOKEN.id));
                String message = "§c" + name + "§f - Words Spoken: " + stat;
                respondToCommand(message, args, sender, StatTypes.WORDS_SPOKEN);
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("messagesspoken")) {
            // list the number of messages a player has spoken
            ArrayList<String> names = getPlayers(sender, args);
            if (names == null)
                return false;

            for (String name : names) {
                String stat = df.format(getStat(name, StatTypes.MESSAGES_SPOKEN.id));
                String message = "§c" + name + "§f - Messages Spoken: " + stat;
                respondToCommand(message, args, sender, StatTypes.MESSAGES_SPOKEN);
            }
            return true;
        }
        return false;
    }

    @Override
    protected String typeFormat(int value, StatTypes type) {
        return df.format(value);
    }

    @Override
    protected String typeLabel(StatTypes type) {
        if (type == StatTypes.WORDS_SPOKEN)
            return "Words Spoken";
        else
            return "Messages Spoken";
    }
}
