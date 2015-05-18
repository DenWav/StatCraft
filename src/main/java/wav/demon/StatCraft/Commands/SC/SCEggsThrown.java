package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Commands.ResponseBuilder;
import wav.demon.StatCraft.Querydsl.QEggsThrown;
import wav.demon.StatCraft.StatCraft;

import java.util.List;

public class SCEggsThrown extends SCTemplate {

    public SCEggsThrown(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("eggsthrown", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.eggsthrown");
    }

    @Override
    public String playerStatResponse(String name) {
        try {
            int id = plugin.getDatabaseManager().getPlayerId(name);
            if (id < 0)
                throw new Exception();

            QEggsThrown e = QEggsThrown.eggsThrown;
            SQLQuery query = plugin.getDatabaseManager().getNewQuery();

            List<Tuple> list = query.from(e).where(e.id.eq(id)).groupBy(e.hatched).list(e.hatched, e.amount.sum());

            int total;
            int hatched = 0;
            int notHatched = 0;

            for (Tuple tuple : list) {
                Boolean isHatched = tuple.get(e.hatched);

                if (isHatched != null && isHatched) {
                    Integer amnount = tuple.get(e.amount.sum());
                    hatched = amnount == null ? 0 : amnount;
                } else {
                    Integer amnount = tuple.get(e.amount.sum());
                    notHatched = amnount == null ? 0 : amnount;
                }
            }

            total = hatched + notHatched;

            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Eggs Thrown")
                .addStat("Total", df.format(total))
                .addStat("Hatched", df.format(hatched))
                .toString();
        } catch (Exception ex) {
            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Eggs Thrown")
                .addStat("Total", String.valueOf(0))
                .addStat("Hatched", String.valueOf(0))
                .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num) {
        return null;
    }
}
