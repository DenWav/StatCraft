package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Commands.ResponseBuilder;
import wav.demon.StatCraft.Magic.BucketCode;
import wav.demon.StatCraft.Querydsl.BucketFill;
import wav.demon.StatCraft.Querydsl.QBucketEmpty;
import wav.demon.StatCraft.Querydsl.QBucketFill;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.StatCraft;

import java.util.List;

public class SCBucketsFilled extends SCTemplate {

    public SCBucketsFilled(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("bucketsfilled", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.bucketsfilled");
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
            QBucketFill f = QBucketFill.bucketFill;
            List<BucketFill> results = query.from(f).where(f.id.eq(id)).list(f);

            int total;
            int water = 0;
            int lava = 0;
            int milk = 0;

            for (BucketFill bucketFill : results) {
                BucketCode code = BucketCode.fromCode(bucketFill.getType());

                if (code == null)
                    continue;

                switch (code) {
                    case WATER:
                        water = bucketFill.getAmount();
                        break;
                    case LAVA:
                        lava = bucketFill.getAmount();
                        break;
                    case MILK:
                        milk = bucketFill.getAmount();
                        break;
                }
            }

            total = water + lava + milk;

            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Buckets Filled")
                    .addStat("Total", df.format(total))
                    .addStat("Water", df.format(water))
                    .addStat("Lava", df.format(lava))
                    .addStat("Milk", df.format(milk))
                    .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Buckets Filled")
                    .addStat("Total", String.valueOf(0))
                    .addStat("Water", String.valueOf(0))
                    .addStat("Lava", String.valueOf(0))
                    .addStat("Milk", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QBucketEmpty e = QBucketEmpty.bucketEmpty;
        QPlayers p = QPlayers.players;
        List<Tuple> result = query
                .from(e)
                .leftJoin(p)
                .on(e.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(e.amount.sum().desc())
                .limit(num)
                .list(p.name, e.amount.sum());

        return topListResponse("Buckets Filled", result);
    }
}
