package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import wav.demon.StatCraft.Commands.TimeResponseBuilder;
import wav.demon.StatCraft.Querydsl.QEnterBed;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.Querydsl.QTimeSlept;
import wav.demon.StatCraft.StatCraft;

import java.util.List;
import java.util.UUID;

public class SCTimeSlept extends SCTemplate {

    public SCTimeSlept(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("timeslept", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.bed");
    }

    @Override
    public String playerStatResponse(String name, List<String> args) {
        try {
            int id = plugin.getDatabaseManager().getPlayerId(name);
            if (id < 0)
                throw new Exception();

            SQLQuery query = plugin.getDatabaseManager().getNewQuery();
            if (query == null)
                return "Sorry, there seems to be an issue connecting to the database right now.";
            QTimeSlept t = QTimeSlept.timeSlept;
            Integer result = query.from(t).where(t.id.eq(id)).uniqueResult(t.amount);

            if (result == null)
                result = 0;

            UUID uuid = plugin.players.get(name);
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);

            if (player.isOnline() && player.getPlayer().isSleeping()) {
                int now = (int)(System.currentTimeMillis() / 1000L);

                QEnterBed e = QEnterBed.enterBed;
                query = plugin.getDatabaseManager().getNewQuery();
                Integer enter = query.from(e).where(e.id.eq(id)).uniqueResult(e.time);

                // Sanity check
                if (enter != null && enter != 0 && now != 0)
                    result = result + (now - enter);
            }

            return new TimeResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Time Slept")
                    .addStat("Total", String.valueOf(result))
                    .toString();
        } catch (Exception e) {
            return new TimeResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Time Slept")
                    .addStat("Total", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QTimeSlept t = QTimeSlept.timeSlept;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
                .from(t)
                .leftJoin(p)
                .on(t.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(t.amount.desc())
                .limit(num)
                .list(p.name, t.amount);

        return topListTimeResponse("Time Slept", list);
    }
}
