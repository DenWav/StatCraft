package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

public class WordsSpoken extends StatListener implements CommandExecutor {

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
            String[] names = getPlayers(sender, args);
            if (names == null)
                return false;

            for (String name : names) {
                sender.getServer().broadcastMessage(name + " - Words Spoken: " +
                        df.format(getStat(name, StatTypes.WORDS_SPOKEN.id)));
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("messagesspoken")) {
            // list the number of messages a player has spoken
            String[] names = getPlayers(sender, args);
            if (names == null)
                return false;

            for (String name : names) {
                sender.getServer().broadcastMessage(name + " - Messages Spoken: " +
                        df.format(getStat(name, StatTypes.MESSAGES_SPOKEN.id)));
            }
            return true;
        }
        return false;
    }
}
