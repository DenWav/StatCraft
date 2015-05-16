package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Commands.ResponseBuilder;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.Querydsl.QXpGained;
import wav.demon.StatCraft.StatCraft;

import java.util.List;

public class SCXpGained extends SCTemplate {

    public SCXpGained(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("xpgained", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.xpgained");
    }

    @Override
    public String playerStatResponse(String name) {
        try {
            int id = plugin.getDatabaseManager().getPlayerId(name);
            if (id < 0)
                throw new Exception();

            SQLQuery query = plugin.getDatabaseManager().getNewQuery();
            QXpGained x = QXpGained.xpGained;
            Integer result = query.from(x).where(x.id.eq(id)).uniqueResult(x.amount);

            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Xp Gained")
                    .addStat("Total", df.format(result == null ? 0 : result))
                    .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Xp Gained")
                    .addStat("Total", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        QXpGained x = QXpGained.xpGained;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
                .from(x)
                .leftJoin(p)
                .on(x.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(x.amount.desc())
                .limit(num)
                .list(p.name, x.amount);

        return topListResponse("Xp Gained", list);
    }
}
