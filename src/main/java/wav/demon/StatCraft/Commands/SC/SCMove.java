package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Commands.ResponseBuilder;
import wav.demon.StatCraft.Commands.SecondaryArgument;
import wav.demon.StatCraft.Magic.MoveCode;
import wav.demon.StatCraft.Querydsl.QMove;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.StatCraft;
import wav.demon.StatCraft.Util;

import java.util.LinkedList;
import java.util.List;

public class SCMove extends SCTemplate {

    public SCMove(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("move", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.move");
    }

    @Override
    @SecondaryArgument({"walking", "crouching", "sprinting", "swimming", "falling", "climbing",
                        "flying", "diving", "minecart", "boat", "pig", "horse"})
    public String playerStatResponse(String name, List<String> args) {
        String arg = null;
        try {
            int id = plugin.getDatabaseManager().getPlayerId(name);

            QMove m = QMove.move;
            SQLQuery query = plugin.getDatabaseManager().getNewQuery();
            if (query == null)
                return "Sorry, there seems to be an issue connecting to the database right now.";

            if (args.size() == 0) {
                Integer result = query.from(m).where(m.id.eq(id)).uniqueResult(m.distance.sum());
                if (result == null)
                    throw new Exception();

                return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Move")
                    .addStat("Total", Util.distanceUnits(result))
                    .toString();
            } else {
                arg = args.get(0);
                MoveCode code = MoveCode.valueOf(arg.toUpperCase());
                Integer result = query.from(m).where(m.id.eq(id), m.vehicle.eq(code.getCode())).uniqueResult(m.distance);
                if (result == null)
                    throw new Exception();

                return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Move")
                    .addStat(WordUtils.capitalizeFully(arg), Util.distanceUnits(result))
                    .toString();
            }
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Move")
                .addStat(arg == null ? "Total" : WordUtils.capitalizeFully(arg), Util.distanceUnits(0))
                .toString();
        }
    }

    @Override
    @SecondaryArgument({"walking", "crouching", "sprinting", "swimming", "falling", "climbing",
        "flying", "diving", "minecart", "boat", "pig", "horse"})
    public String serverStatListResponse(int num, List<String> args) {
        QMove m = QMove.move;
        QPlayers p = QPlayers.players;
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";

        List<Tuple> list;
        String arg = null;

        if (args.size() == 0) {
            list = query
                .from(m)
                .leftJoin(p)
                .on(m.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(m.distance.sum().desc())
                .limit(num)
                .list(p.name, m.distance.sum());
        } else {
            arg = args.get(0);
            MoveCode code = MoveCode.valueOf(arg.toUpperCase());
            list = query
                .from(m)
                .where(m.vehicle.eq(code.getCode()))
                .leftJoin(p)
                .on(m.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(m.distance.sum().desc())
                .limit(num)
                .list(p.name, m.distance.sum());
        }

        return topListDistanceResponse(arg == null ? "Move" : WordUtils.capitalizeFully(arg), list);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args[args.length -1].startsWith("-")) {
            List<String> result = new LinkedList<>();
            List<String> list = new LinkedList<>();
            list.add("-all");
            list.add("-boat");
            list.add("-climbing");
            list.add("-crouching");
            list.add("-diving");
            list.add("-falling");
            list.add("-flying");
            list.add("-horse");
            list.add("-minecart");
            list.add("-pig");
            list.add("-sprinting");
            list.add("-swimming");
            list.add("-walking");
            for (String s : list) {
                if (s.startsWith(args[args.length -1])) {
                    result.add(s);
                }
            }
            return result;
        } else {
            return super.onTabComplete(sender, args);
        }
    }
}