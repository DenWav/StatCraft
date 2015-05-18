package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Commands.ResponseBuilder;
import wav.demon.StatCraft.Querydsl.QEnderPearls;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.StatCraft;
import wav.demon.StatCraft.Util;

import java.util.List;

public class SCEnderPearls extends SCTemplate {

    public SCEnderPearls(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("enderpearls", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.enderpearls");
    }

    @Override
    public String playerStatResponse(String name) {
        try {
            int id = plugin.getDatabaseManager().getPlayerId(name);
            if (id < 0)
                throw new Exception();

            QEnderPearls e = QEnderPearls.enderPearls;
            SQLQuery query = plugin.getDatabaseManager().getNewQuery();

            Tuple tuple = query.from(e).where(e.id.eq(id)).uniqueResult(e.amount, e.distance, e.maxThrow);

            if (tuple == null)
                throw new Exception();

            Integer amount = tuple.get(e.amount);
            Integer distance = tuple.get(e.distance);
            Integer maxThrow = tuple.get(e.maxThrow);

            amount = amount == null ? 0 : amount;
            distance = distance == null ? 0 : distance;
            maxThrow = maxThrow == null ? 0 : maxThrow;

            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Ender Pearls Thrown")
                .addStat("Thrown", df.format(amount))
                .addStat("Distance", Util.distanceUnits(distance))
                .addStat("Farthest Throw", Util.distanceUnits(maxThrow))
                .toString();
        } catch (Exception ex) {
            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Ender Pearls Thrown")
                .addStat("Thrown", String.valueOf(0))
                .addStat("Distance", Util.distanceUnits(0))
                .addStat("Farthest Throw", Util.distanceUnits(0))
                .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num) {
        QEnderPearls e = QEnderPearls.enderPearls;
        QPlayers p = QPlayers.players;
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();

        List<Tuple> list = query
            .from(e)
            .leftJoin(p)
            .on(e.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(e.amount.desc())
            .limit(num)
            .list(p.name, e.amount);

        return topListResponse("Ender Pearls Thrown", list);
    }
}
