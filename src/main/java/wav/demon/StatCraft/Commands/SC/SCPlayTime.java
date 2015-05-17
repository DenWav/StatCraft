package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Commands.TimeResponseBuilder;
import wav.demon.StatCraft.Querydsl.QLastJoinTime;
import wav.demon.StatCraft.Querydsl.QPlayTime;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.StatCraft;

import java.util.List;
import java.util.UUID;

public class SCPlayTime extends SCTemplate {

    public SCPlayTime(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("playtime", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.playtime");
    }

    @Override
    public String playerStatResponse(String name) {
        try {
            int id = plugin.getDatabaseManager().getPlayerId(name);
            if (id < 0)
                throw new Exception();

            SQLQuery query = plugin.getDatabaseManager().getNewQuery();
            if (query == null)
                return "Sorry, there seems to be an issue connecting to the database right now.";
            QPlayTime t = QPlayTime.playTime;
            Integer result = query.from(t).where(t.id.eq(id)).uniqueResult(t.amount);

            if (result == null)
                result = 0;

            UUID uuid = plugin.players.get(name);
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);

            if (player != null && player.isOnline()) {
                int now = (int)(System.currentTimeMillis() / 1000L);

                QLastJoinTime j = QLastJoinTime.lastJoinTime;
                query = plugin.getDatabaseManager().getNewQuery();
                Integer join = query.from(j).where(j.id.eq(id)).uniqueResult(j.time);

                // Sanity check
                if (join != null && join != 0 && now != 0)
                    result = result + (now - join);
            }

            return new TimeResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Play Time")
                    .addStat("Total", String.valueOf(result))
                    .toString();
        } catch (Exception e) {
            return new TimeResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Play Time")
                    .addStat("Total", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QPlayTime t = QPlayTime.playTime;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
                .from(t)
                .leftJoin(p)
                .on(t.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(t.amount.desc())
                .limit(num)
                .list(p.name, t.amount);

        return topListTimeResponse("Play Time", list);
    }
}
