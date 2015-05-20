package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Commands.ResponseBuilder;
import wav.demon.StatCraft.Querydsl.QBlockBreak;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.StatCraft;

import java.util.List;

public class SCBlocksBroken extends SCTemplate {

    public SCBlocksBroken(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("blocksbroken", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.blocksbroken");
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
            QBlockBreak b = QBlockBreak.blockBreak;
            Integer total = query.from(b).where(b.id.eq(id)).uniqueResult(b.amount.sum());

            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Blocks Broken")
                    .addStat("Total", df.format(total == null ? 0 : total))
                    .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Blocks Broken")
                    .addStat("Total", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QBlockBreak b = QBlockBreak.blockBreak;
        QPlayers p = QPlayers.players;
        List<Tuple> result = query
                .from(b)
                .leftJoin(p)
                .on(b.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(b.amount.sum().desc())
                .limit(num)
                .list(p.name, b.amount.sum());

        return topListResponse("Blocks Broken", result);
    }
}
