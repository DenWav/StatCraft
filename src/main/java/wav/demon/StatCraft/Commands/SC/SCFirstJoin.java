package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.sql.SQLQuery;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Querydsl.QFirstJoinTime;
import wav.demon.StatCraft.StatCraft;
import wav.demon.StatCraft.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SCFirstJoin extends SCTemplate {

    public SCFirstJoin(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("firstjoin", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.firstjoin");
    }

    @Override
    public String playerStatResponse(String name, List<String> args) {
        try {
            int id = plugin.getDatabaseManager().getPlayerId(name);
            if (id < 0)
                throw new Exception();

            QFirstJoinTime f = QFirstJoinTime.firstJoinTime;
            SQLQuery query = plugin.getDatabaseManager().getNewQuery();
            if (query == null)
                return "Sorry, there seems to be an issue connecting to the database right now.";

            Integer result = query.from(f).where(f.id.eq(id)).uniqueResult(f.time);
            if (result == null)
                throw new Exception();

            Date date = new Date(((long) result) * 1000L);
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy, hh:mm aa zzz");
            format.setTimeZone(TimeZone.getTimeZone(plugin.getTimeZone()));
            String time = format.format(date);

            Date now = new Date();
            long difference = now.getTime() - date.getTime();

            time = time + " (" + Util.transformTime((int) (difference / 1000L)).split(",")[0] + " ago)";

            return ChatColor.valueOf(plugin.config().colors.player_name) + name +
                ChatColor.valueOf(plugin.config().colors.stat_title) + " - First Join - " +
                ChatColor.valueOf(plugin.config().colors.stat_value) + time;
        } catch (Exception e) {
            return ChatColor.valueOf(plugin.config().colors.player_name) +
                name + ChatColor.valueOf(plugin.config().colors.stat_value) +
                " has not been seen on this server.";
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args) {
        return null;
    }
}
