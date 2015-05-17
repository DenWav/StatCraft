package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Commands.ResponseBuilder;
import wav.demon.StatCraft.Querydsl.QHighestLevel;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.StatCraft;

import java.util.List;

public class SCHighestLevel extends SCTemplate {

    public SCHighestLevel(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("highestlevel", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.highestlevel");
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
            QHighestLevel h = QHighestLevel.highestLevel;
            Integer result = query.from(h).where(h.id.eq(id)).uniqueResult(h.level);

            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Highest Level")
                    .addStat("Level", df.format(result))
                    .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Highest Level")
                    .addStat("Level", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QHighestLevel h = QHighestLevel.highestLevel;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
                .from(h)
                .leftJoin(p)
                .on(h.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(h.level.desc())
                .limit(num)
                .list(p.name, h.level);

        return topListResponse("Highest Level", list);
    }
}
