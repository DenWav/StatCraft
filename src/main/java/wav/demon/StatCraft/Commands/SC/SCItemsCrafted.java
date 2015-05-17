package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Commands.ResponseBuilder;
import wav.demon.StatCraft.Querydsl.QItemsCrafted;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.StatCraft;

import java.util.List;

public class SCItemsCrafted extends SCTemplate {

    public SCItemsCrafted(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("itemscrafted", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.itemscrafted");
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
            QItemsCrafted i = QItemsCrafted.itemsCrafted;
            Integer result = query.from(i).where(i.id.eq(id)).uniqueResult(i.amount.sum());

            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Items Crafted")
                    .addStat("Total", df.format(result == null ? 0 : result))
                    .toString();
        } catch (Exception e){
            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Items Crafted")
                    .addStat("Total", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QItemsCrafted i = QItemsCrafted.itemsCrafted;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
                .from(i)
                .leftJoin(p)
                .on(i.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(i.amount.sum().desc())
                .limit(num)
                .list(p.name, i.amount.sum());

        return topListResponse("Items Crafted", list);
    }
}
