package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Commands.ResponseBuilder;
import wav.demon.StatCraft.Querydsl.QJoins;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.StatCraft;

import java.util.List;

public class SCJoins extends SCTemplate {

    public SCJoins(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("joins", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.joins");
    }

    @Override
    public String playerStatResponse(String name) {
        try {
            int id = plugin.getDatabaseManager().getPlayerId(name);
            if (id < 0)
                throw new Exception();

            SQLQuery query = plugin.getDatabaseManager().getNewQuery();
            QJoins j = QJoins.joins;

            Integer result = query.from(j).where(j.id.eq(id)).uniqueResult(j.amount);

            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Joins")
                .addStat("Total", df.format(result == null ? 0 : result))
                .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Joins")
                .addStat("Total", String.valueOf(0))
                .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        QJoins j = QJoins.joins;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
            .from(j)
            .leftJoin(p)
            .groupBy(p.name)
            .orderBy(j.amount.desc())
            .limit(num)
            .list(p.name, j.amount);

        return topListResponse("Joins", list);
    }
}
