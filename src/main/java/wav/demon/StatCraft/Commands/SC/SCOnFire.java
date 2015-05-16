package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Commands.TimeResponseBuilder;
import wav.demon.StatCraft.Querydsl.QOnFire;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.StatCraft;

import java.util.List;

public class SCOnFire extends SCTemplate {

    public SCOnFire(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("onfire", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.onfire");
    }

    @Override
    public String playerStatResponse(String name) {
        try {
            int id = plugin.getDatabaseManager().getPlayerId(name);
            if (id < 0)
                throw new Exception();

            SQLQuery query = plugin.getDatabaseManager().getNewQuery();
            QOnFire o = QOnFire.onFire;
            Integer result = query.from(o).where(o.id.eq(id)).uniqueResult(o.time);

            return new TimeResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("On Fire")
                    .addStat("Total", String.valueOf(result))
                    .toString();

        } catch (Exception e) {
            return new TimeResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("On Fire")
                    .addStat("Total", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        QOnFire o = QOnFire.onFire;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
                .from(o)
                .leftJoin(p)
                .on(o.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(o.time.desc())
                .limit(num)
                .list(p.name, o.time);

        return topListTimeResponse("On Fire", list);
    }
}
