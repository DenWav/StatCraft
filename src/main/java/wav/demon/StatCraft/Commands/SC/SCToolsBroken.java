package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Commands.ResponseBuilder;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.Querydsl.QToolsBroken;
import wav.demon.StatCraft.StatCraft;

import java.util.List;

public class SCToolsBroken extends SCTemplate {

    public SCToolsBroken(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("toolsbroken", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.toolsbroken");
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
            QToolsBroken t = QToolsBroken.toolsBroken;
            Integer result = query.from(t).where(t.id.eq(id)).uniqueResult(t.amount.sum());

            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Tools Broken")
                    .addStat("Total", df.format(result))
                    .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Tools Broken")
                    .addStat("Total", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QToolsBroken t = QToolsBroken.toolsBroken;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
                .from(t)
                .leftJoin(p)
                .on(t.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(t.amount.sum().desc())
                .limit(num)
                .list(p.name, t.amount.sum());

        return topListResponse("Tools Broken", list);
    }
}
