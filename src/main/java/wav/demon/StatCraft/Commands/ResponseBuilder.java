package wav.demon.StatCraft.Commands;

import org.bukkit.ChatColor;
import wav.demon.StatCraft.StatCraft;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResponseBuilder {

    protected StatCraft plugin;
    protected String name = "";
    protected String statName = "";
    protected LinkedHashMap<String, String> stats = new LinkedHashMap<>();

    public ResponseBuilder(StatCraft plugin) {
        this.plugin = plugin;
    }

    public ResponseBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ResponseBuilder setStatName(String statName) {
        this.statName = statName;
        return this;
    }

    public ResponseBuilder addStat(String title, String value) {
        stats.put(title, value);
        return this;
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
                    .append(entry.getValue());

            if (iterator.hasNext())
                sb.append(ChatColor.valueOf(plugin.config().colors.stat_separator)).append(" | ");
        }

        return sb.toString();
    }
}

