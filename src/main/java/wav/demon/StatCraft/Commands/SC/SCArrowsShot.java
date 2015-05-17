package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Commands.ResponseBuilder;
import wav.demon.StatCraft.Magic.ArrowCode;
import wav.demon.StatCraft.Querydsl.ArrowsShot;
import wav.demon.StatCraft.Querydsl.QArrowsShot;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.StatCraft;

import java.util.List;

public class SCArrowsShot extends SCTemplate {

    public SCArrowsShot(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("arrowsshot", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.arrowsshot");
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
            QArrowsShot a = QArrowsShot.arrowsShot;
            List<ArrowsShot> result = query.from(a).where(a.id.eq(id)).list(a);

            int total;
            int normal = 0;
            int flaming = 0;

            for (ArrowsShot arrowsShot : result) {
                ArrowCode code = ArrowCode.fromCode(arrowsShot.getType());

                if (code == null)
                    continue;

                switch (code) {
                    case NORMAL:
                        normal = arrowsShot.getAmount();
                        break;
                    case FLAMING:
                        flaming = arrowsShot.getAmount();
                        break;
                }
            }

            total = normal + flaming;

            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Arrows Shot")
                    .addStat("Total", df.format(total))
                    .addStat("Normal", df.format(normal))
                    .addStat("Flaming", df.format(flaming))
                    .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Arrows Shot")
                    .addStat("Total", String.valueOf(0))
                    .addStat("Normal", String.valueOf(0))
                    .addStat("Flaming", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QArrowsShot a = QArrowsShot.arrowsShot;
        QPlayers p = QPlayers.players;
        List<Tuple> result = query
                .from(a)
                .leftJoin(p)
                .on(a.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(a.amount.sum().desc())
                .limit(num)
                .list(p.name, a.amount.sum());

        return topListResponse("Arrows Shot", result);
    }
}
