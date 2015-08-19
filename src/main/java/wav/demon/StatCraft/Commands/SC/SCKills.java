package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;

import org.bukkit.command.CommandSender;

import wav.demon.StatCraft.Commands.ResponseBuilder;
import wav.demon.StatCraft.Querydsl.QKills;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.StatCraft;

import java.util.List;

public class SCKills extends SCTemplate {

    public SCKills(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("kills", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.kills");
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
            QKills k = QKills.kills;
            Integer result = query.from(k).where(k.id.eq(id)).uniqueResult(k.amount.sum());

            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Kills")
                    .addStat("Total", df.format(result == null ? 0 : result))
                    .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Kills")
                    .addStat("Total", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QKills k = QKills.kills;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
                .from(k)
                .leftJoin(p)
                .on(k.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(k.amount.sum().desc())
                .limit(num)
                .list(p.name, k.amount.sum());

        return topListResponse("Kills", list);
    }
}
