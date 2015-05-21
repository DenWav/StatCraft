package wav.demon.StatCraft.Commands.SC;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.types.path.NumberPath;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft.Commands.ResponseBuilder;
import wav.demon.StatCraft.Commands.SecondaryArgument;
import wav.demon.StatCraft.Magic.ProjectilesCode;
import wav.demon.StatCraft.Querydsl.Projectiles;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.Querydsl.QProjectiles;
import wav.demon.StatCraft.StatCraft;
import wav.demon.StatCraft.Util;

import java.util.LinkedList;
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
    @SecondaryArgument({"distance", "farthest"})
    public String playerStatResponse(String name, List<String> args) {
        int total;
        int normal = 0;
        int flaming = 0;

        int normalDistance = 0;
        int flamingDistance = 0;

        int normalMaxThrow = 0;
        int flamingMaxThrow = 0;

        try {
            int id = plugin.getDatabaseManager().getPlayerId(name);
            if (id < 0)
                throw new Exception();

            SQLQuery query = plugin.getDatabaseManager().getNewQuery();
            if (query == null)
                return "Sorry, there seems to be an issue connecting to the database right now.";
            QProjectiles p = QProjectiles.projectiles;
            List<Projectiles> result = query.from(p).where(p.id.eq(id),
                p.type.eq(ProjectilesCode.NORMAL_ARROW.getCode()).or(p.type.eq(ProjectilesCode.FLAMING_ARROW.getCode()))
            ).list(p);

            for (Projectiles projectiles : result) {
                ProjectilesCode code = ProjectilesCode.fromCode(projectiles.getType());

                if (code == null)
                    continue;

                switch (code) {
                    case NORMAL_ARROW:
                        normal = projectiles.getAmount();
                        normalDistance = projectiles.getTotalDistance();
                        normalMaxThrow = projectiles.getMaxThrow();
                        break;
                    case FLAMING_ARROW:
                        flaming = projectiles.getAmount();
                        flamingDistance = projectiles.getTotalDistance();
                        flamingMaxThrow = projectiles.getMaxThrow();
                        break;
                }
            }

            total = normal + flaming;
        } catch (Exception e) {
            total = 0;
            normal = 0;
            flaming = 0;
            normalDistance = 0;
            flamingDistance = 0;
            normalMaxThrow = 0;
            flamingMaxThrow = 0;
        }

        String arg;
        if (args.size() > 0)
            arg = args.get(0);
        else
            arg = "";

        switch (arg) {
            case "distance":
                return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Arrows Shot Total Distance")
                    .addStat("Total", Util.distanceUnits(normalDistance + flamingDistance))
                    .addStat("Normal", Util.distanceUnits(normalDistance))
                    .addStat("Flaming", Util.distanceUnits(flamingDistance))
                    .toString();
            case "farthest":
                return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Arrows Shot Farthest Shot")
                    .addStat("Normal", Util.distanceUnits(normalMaxThrow))
                    .addStat("Flaming", Util.distanceUnits(flamingMaxThrow))
                    .toString();
            default:
                return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Arrows Shot")
                    .addStat("Total", df.format(total))
                    .addStat("Normal", df.format(normal))
                    .addStat("Flaming", df.format(flaming))
                    .toString();
        }
    }

    @Override
    @SecondaryArgument({"distance", "farthest", "flaming"})
    public String serverStatListResponse(int num, List<String> args) {
        boolean distance = false;
        QProjectiles p = QProjectiles.projectiles;
        QPlayers pl = QPlayers.players;
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";

        NumberPath<Integer> path = null;
        String titlePrefix = "";
        String titlePostfix = "";
        String title;

        ProjectilesCode code = ProjectilesCode.NORMAL_ARROW;

        for (String arg : args) {
            switch (arg) {
                case "distance":
                    path = p.totalDistance;
                    titlePrefix = "Total Distance Fired - ";
                    distance = true;
                    break;
                case "farthest":
                    path = p.maxThrow;
                    titlePrefix = "Farthest ";
                    titlePostfix = " Shot";
                    distance = true;
                    break;
                case "flaming":
                    code = ProjectilesCode.FLAMING_ARROW;
                    break;
            }
        }

        if (path == null) {
            path = p.amount;
            titlePostfix = "s Shot";
        }

        title = titlePrefix + (code == ProjectilesCode.NORMAL_ARROW ? "Normal Arrow" : "Flaming Arrow") + titlePostfix;

        List<Tuple> list = query
            .from(p)
            .where(p.type.eq(code.getCode()))
            .leftJoin(pl)
            .on(p.id.eq(p.id))
            .groupBy(pl.name)
            .orderBy(path.desc())
            .limit(num)
            .list(pl.name, path);

        if (distance)
            return topListDistanceResponse(title, list);
        else
            return topListResponse(title, list);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args[args.length -1].startsWith("-")) {
            boolean top = false;
            for (String s : args) {
                if (s.startsWith("-top"))
                    top = true;
            }
            List<String> result = new LinkedList<>();
            List<String> list = new LinkedList<>();
            list.add("-all");
            list.add("-distance");
            list.add("-farthest");
            if (top)
                list.add("-flaming");

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
