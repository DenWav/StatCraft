package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;

import org.bukkit.command.CommandSender;

import wav.demon.StatCraft.Commands.ResponseBuilder;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.Querydsl.QWordFrequency;
import wav.demon.StatCraft.StatCraft;

import java.util.List;

public class SCWordsSpoken extends SCTemplate {

    public SCWordsSpoken(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("wordsspoken", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.wordsspoken");
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
            QWordFrequency w = QWordFrequency.wordFrequency;
            Integer result = query.from(w).where(w.id.eq(id)).uniqueResult(w.amount.sum());

            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Words Spoken")
                    .addStat("Total", df.format(result == null ? 0 : result))
                    .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Words Spoken")
                    .addStat("Total", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QWordFrequency w = QWordFrequency.wordFrequency;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
                .from(w)
                .leftJoin(p)
                .on(w.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(w.amount.sum().desc())
                .limit(num)
                .list(p.name, w.amount.sum());

        return topListResponse("Words Spoken", list);
    }
}
