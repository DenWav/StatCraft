package wav.demon.StatCraft;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import wav.demon.StatCraft.Magic.MoveCode;
import wav.demon.StatCraft.Querydsl.Jumps;
import wav.demon.StatCraft.Querydsl.QJumps;
import wav.demon.StatCraft.Querydsl.QMove;

import java.util.UUID;

public class ServerStatUpdater {

    public static class Move implements Runnable {

        private StatCraft plugin;

        public Move(StatCraft plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                for (final MoveCode code : MoveCode.values()) {
                    Statistic stat = code.getStat();
                    final int value = player.getStatistic(stat);
                    final UUID uuid = player.getUniqueId();

                    plugin.getThreadManager().schedule(Move.class, new Runnable() {
                        @Override
                        public void run() {
                            int id = plugin.getDatabaseManager().getPlayerId(uuid);

                            QMove m = QMove.move;

                            try {
                                // INSERT
                                SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(m);

                                if (clause == null)
                                    return;

                                clause.columns(m.id, m.vehicle, m.distance).values(id, code.getCode(), value).execute();
                            } catch (QueryException e) {
                                // UPDATE
                                SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(m);

                                if (clause == null)
                                    return;

                                clause.where(m.id.eq(id), m.vehicle.eq(code.getCode())).set(m.distance, value).execute();
                            }
                        }
                    });
                }
            }
        }
    }

    public static class Jump implements Runnable {

        private StatCraft plugin;

        public Jump(StatCraft plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                final int jumps = player.getStatistic(Statistic.JUMP);
                final UUID uuid = player.getUniqueId();

                plugin.getThreadManager().schedule(Jumps.class, new Runnable() {
                    @Override
                    public void run() {
                        int id = plugin.getDatabaseManager().getPlayerId(uuid);

                        QJumps j = QJumps.jumps;

                        try {
                            // INSERT
                            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(j);

                            if (clause == null)
                                return;

                            clause.columns(j.id, j.amount).values(id, jumps).execute();
                        } catch (QueryException e) {
                            // UPDATE
                            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(j);

                            if (clause == null)
                                return;

                            clause.where(j.id.eq(id)).set(j.amount, jumps).execute();
                        }
                    }
                });
            }
        }
    }
}
