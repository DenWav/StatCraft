package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.sql.SQLQuery;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Querydsl.QEnterBed;
import wav.demon.StatCraft.StatCraft;
import wav.demon.StatCraft.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class SCLastSlept extends SCTemplate {

    public SCLastSlept(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("lastslept", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.lastslept");
    }

    @Override
    public String playerStatResponse(String name) {
        try {
            UUID uuid = plugin.players.get(name);
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);

            if (player.isOnline() && player.getPlayer().isSleeping()) {
                return ChatColor.valueOf(plugin.config().colors.player_name) +
                    name + ChatColor.valueOf(plugin.config().colors.stat_value) +
                    " is sleeping now!";
            } else {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);
                if (id < 0) {
                    throw new Exception();
                }

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                if (query == null)
                    return "Sorry, there seems to be an issue connecting to the database right now.";
                QEnterBed b = QEnterBed.enterBed;

                Integer result = query.from(b).where(b.id.eq(id)).uniqueResult(b.time);
                if (result == null) {
                    throw new Exception();
                }

                plugin.getServer().getLogger().info(String.valueOf(result));

                Date date = new Date(((long) result) * 1000L);
                SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy, hh:mm aa zzz");
                format.setTimeZone(TimeZone.getTimeZone(plugin.getTimeZone()));
                String time = format.format(date);

                Date now = new Date();
                long difference = now.getTime() - date.getTime();

                time = time + " (" + Util.transformTime((int) (difference / 1000L)).split(",")[0] + " ago)";

                return ChatColor.valueOf(plugin.config().colors.player_name) + name +
                    ChatColor.valueOf(plugin.config().colors.stat_title) + " - Last Slept - " +
                    ChatColor.valueOf(plugin.config().colors.stat_value) + time;
            }
        } catch (Exception e) {
            return ChatColor.valueOf(plugin.config().colors.player_name) +
                name + ChatColor.valueOf(plugin.config().colors.stat_value) +
                " has not slept on this server.";
        }
    }

    @Override
    public String serverStatListResponse(int num) {
        return null;
    }
}
