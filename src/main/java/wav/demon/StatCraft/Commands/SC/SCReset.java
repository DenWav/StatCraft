package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import wav.demon.StatCraft.Commands.CustomResponse;
import wav.demon.StatCraft.MySQL.Table;
import wav.demon.StatCraft.Querydsl.QEnterBed;
import wav.demon.StatCraft.Querydsl.QLastJoinTime;
import wav.demon.StatCraft.StatCraft;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SCReset extends SCTemplate implements CustomResponse {

    private HashMap<CommandSender, OfflinePlayer> map = new HashMap<>();

    public SCReset(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("resetstats", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        if (args == null || args.length == 0 || args[0].equalsIgnoreCase("yes") || args[0].equalsIgnoreCase("cancel")) {
            return sender.hasPermission("statcraft.user.resetstats");
        } else {
            return sender.hasPermission("statcraft.admin.resetotherstats");
        }
    }

    @Override
    public void respondToCommand(CommandSender sender, String[] args) {
        if (args.length == 1 &&
            (args[0].equalsIgnoreCase("yes") || args[0].equalsIgnoreCase("cancel")) &&
            map.containsKey(sender)) {

            if (args[0].equalsIgnoreCase("cancel")) {
                if (map.containsKey(sender)) {
                    sender.sendMessage("Reset stats request canceled.");
                }
            } else {
                OfflinePlayer player = map.get(sender);
                if (player.equals(sender)) {
                    // this person is resetting their own stats
                    // check permissions one more time
                    if (sender.hasPermission("statcraft.user.resetstats")) {
                        resetStats(sender, player);
                    } else {
                        sender.sendMessage("You don't have permission to reset your own stats.");
                    }
                } else {
                    // This person is resetting someone else's stats
                    // Check permissions one more time
                    if (sender.hasPermission("statcraft.admin.resetotherstats")) {
                        resetStats(sender, player);
                    } else {
                        sender.sendMessage("You don't have permission to reset someone else's stats.");
                    }
                }
            }
            map.remove(sender);
        } else {
            if (args.length == 0 && sender instanceof OfflinePlayer) {
                sender.sendMessage("Are you sure you want to reset your own stats?\n" +
                    "Run command: " + ChatColor.GRAY + ChatColor.ITALIC + "/sc resetstats yes" + ChatColor.RESET + " to verify.");
                sender.sendMessage("Run command: " + ChatColor.GRAY + ChatColor.ITALIC + "/sc resetstats cancel" +
                    ChatColor.RESET + " to cancel.");
                map.put(sender, (OfflinePlayer) sender);
            } else if (args.length == 0) {
                sender.sendMessage("You must be a player to reset your own stats.");
            } else if (args.length == 1) {
                OfflinePlayer player = plugin.getServer().getPlayer(args[0]);
                if (player.getUniqueId().version() == 4) {
                    sender.sendMessage("Are you sure you want to reset " + args[0] + "'s stats?\n" +
                        "Run command: " + ChatColor.GRAY + ChatColor.ITALIC + "/sc resetstats yes" +
                        ChatColor.RESET + " to verify.");
                    sender.sendMessage("Run command: " + ChatColor.GRAY + ChatColor.ITALIC + "/sc resetstats cancel" +
                        ChatColor.RESET + " to cancel.");
                    map.put(sender, player);
                }
            } else {
                sender.sendMessage("Usage: /sc resetstats [player]");
            }
        }
    }

    private void resetStats(final CommandSender sender, final OfflinePlayer player) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                final int id = plugin.getDatabaseManager().getPlayerId(player.getUniqueId());
                if (id < 0) {
                    sender.sendMessage("Unable to find " + player.getName() + " in the database.");
                    return;
                }

                Statement st = null;
                try {
                    plugin.getDatabaseManager().getConnection().setAutoCommit(false);
                    st = plugin.getDatabaseManager().getConnection().createStatement();

                    for (Table table : Table.values()) {
                        if (!table.getName().equalsIgnoreCase("players")) {
                            st.addBatch("DELETE FROM " + table.getName() + " WHERE id = " + id);
                        }
                    }

                    st.executeBatch();

                    plugin.getDatabaseManager().getConnection().commit();
                    plugin.getDatabaseManager().getConnection().setAutoCommit(true);
                } catch (SQLException e) {
                    try {
                        plugin.getDatabaseManager().getConnection().rollback();
                        plugin.getDatabaseManager().getConnection().setAutoCommit(true);
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                } finally {
                    if (st != null) {
                        try {
                            st.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // So we don't mess up play time / time slept, check if they are online or in the bed
                // and add "join" values for now
                if (player.isOnline()) {
                    int currentTime = (int)(System.currentTimeMillis() / 1000L);

                    QLastJoinTime j = QLastJoinTime.lastJoinTime;
                    SQLQuery query = plugin.getDatabaseManager().getNewQuery();

                    if (query.from(j).where(j.id.eq(id)).exists()) {
                        SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(j);
                        clause.where(j.id.eq(id)).set(j.time, currentTime).execute();
                    } else {
                        SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(j);
                        clause.columns(j.id, j.time).values(id, currentTime).execute();
                    }

                    if (player.getPlayer().isSleeping()) {
                        QEnterBed e = QEnterBed.enterBed;

                        if (query.from(e).where(e.id.eq(id)).exists()) {
                            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(e);
                            clause.where(e.id.eq(id)).set(e.time, currentTime).execute();
                        } else {
                            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(e);
                            clause.columns(e.id, e.time).values(id, currentTime).execute();
                        }
                    }
                }

                if (player.equals(sender))
                    sender.sendMessage("Your stats have been successfully reset.");
                else
                    sender.sendMessage(player.getName() + "'s stats have been successfully reset.");
            }
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (map.containsKey(sender)) {
                ArrayList<String> list = new ArrayList<>();
                if ("cancel".startsWith(args[0].toLowerCase()))
                    list.add("cancel");
                if ("yes".startsWith(args[0].toLowerCase()))
                    list.add("yes");
                return list;
            } else {
                ArrayList<String> players = new ArrayList<>(plugin.players.keySet());
                ArrayList<String> secondary = new ArrayList<>();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    secondary.add(player.getName());
                }

                players.removeAll(secondary);

                LinkedList<String> result = new LinkedList<>();

                for (String s : secondary) {
                    if (s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                        result.add(s);
                }
                for (String s : players) {
                    if (s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                        result.add(s);
                }
                result.sort(String.CASE_INSENSITIVE_ORDER);
                return result;
            }
        }
        return Collections.emptyList();
    }


    @Override
    public String playerStatResponse(String name) { return null; }

    @Override
    public String serverStatListResponse(int num) { return null; }
}
