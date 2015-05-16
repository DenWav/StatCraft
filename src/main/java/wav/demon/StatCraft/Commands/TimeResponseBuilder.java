package wav.demon.StatCraft.Commands;

import org.bukkit.ChatColor;
import wav.demon.StatCraft.StatCraft;
import wav.demon.StatCraft.Util;

import java.util.Iterator;
import java.util.Map;

public class TimeResponseBuilder extends ResponseBuilder {

    public TimeResponseBuilder(StatCraft plugin) {
        super(plugin);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb      .append(ChatColor.valueOf(plugin.config().colors.player_name))
                .append(name)
                .append(ChatColor.valueOf(plugin.config().colors.stat_title))
                .append(" - ").append(statName).append(" - ");

        Iterator<Map.Entry<String, String>> iterator = stats.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            sb      .append(ChatColor.valueOf(plugin.config().colors.stat_label))
                    .append(entry.getKey()).append(": ")
                    .append(ChatColor.valueOf(plugin.config().colors.stat_value))
                    .append(Util.transformTime(Integer.parseInt(entry.getValue())));

            if (iterator.hasNext())
                sb.append(ChatColor.valueOf(plugin.config().colors.stat_separator)).append(" | ");
        }

        return sb.toString();
    }
}
