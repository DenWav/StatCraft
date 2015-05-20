package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.sql.SQLQuery;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Querydsl.QLastLeaveTime;
import wav.demon.StatCraft.StatCraft;
import wav.demon.StatCraft.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class SCLastSeen extends SCTemplate {

    public SCLastSeen(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("lastseen", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.lastseen");
    }

    @Override
    public String playerStatResponse(String name, List<String> args) {
        try {
            UUID uuid = plugin.players.get(name);
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);

            if (player.isOnline()) {
                return ChatColor.valueOf(plugin.config().colors.player_name) +
                    name + ChatColor.valueOf(plugin.config().colors.stat_value) +
                    " is online now!";
            } else {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);
                if (id < 0) {
                    throw new Exception();
                }

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                if (query == null)
                    return "Sorry, there seems to be an issue connecting to the database right now.";
                QLastLeaveTime l = QLastLeaveTime.lastLeaveTime;

                Integer result = query.from(l).where(l.id.eq(id)).uniqueResult(l.time);
                if (result == null) {
                    throw new Exception();
                }

                Date date = new Date(((long) result) * 1000L);
                SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy, hh:mm aa zzz");
                format.setTimeZone(TimeZone.getTimeZone(plugin.getTimeZone()));
                String time = format.format(date);

                Date now = new Date();
                long difference = now.getTime() - date.getTime();

                time = time + " (" + Util.transformTime((int) (difference / 1000L)).split(",")[0] + " ago)";

                return ChatColor.valueOf(plugin.config().colors.player_name) + name +
                    ChatColor.valueOf(plugin.config().colors.stat_title) + " - Last Seen - " +
                    ChatColor.valueOf(plugin.config().colors.stat_value) + time;
            }
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
