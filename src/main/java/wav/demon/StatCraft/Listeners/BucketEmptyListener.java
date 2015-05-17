package wav.demon.StatCraft.Listeners;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import wav.demon.StatCraft.Magic.BucketCode;
import wav.demon.StatCraft.Querydsl.BucketEmpty;
import wav.demon.StatCraft.Querydsl.QBucketEmpty;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class BucketEmptyListener implements Listener {

    private StatCraft plugin;

    public BucketEmptyListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final BucketCode code;
        if (event.getBucket() == Material.LAVA_BUCKET)
            code = BucketCode.LAVA;
        else // default to water
            code = BucketCode.WATER;

        plugin.getWorkerThread().schedule(BucketEmpty.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                if (query == null)
                    return;
                QBucketEmpty e = QBucketEmpty.bucketEmpty;

                if (query.from(e).where(e.id.eq(id).and(e.type.eq(code.getCode()))).exists()) {
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(e);
                    clause.where(
                        e.id.eq(id)
                        .and(e.type.eq(code.getCode()))
                    ).set(e.amount, e.amount.add(1)).execute();
                } else {
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(e);
                    clause.columns(e.id, e.type, e.amount)
                        .values(id, code.getCode(), 1).execute();
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            final UUID uuid = event.getPlayer().getUniqueId();
            final BucketCode code = BucketCode.MILK;

            plugin.getWorkerThread().schedule(BucketEmpty.class, new Runnable() {
                @Override
                public void run() {
                    int id = plugin.getDatabaseManager().getPlayerId(uuid);

                    SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                    if (query == null)
                        return;
                    QBucketEmpty e = QBucketEmpty.bucketEmpty;

                    if (query.from(e).where(e.id.eq(id).and(e.type.eq(code.getCode()))).exists()) {
                        SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(e);
                        clause.where(
                            e.id.eq(id)
                                .and(e.type.eq(code.getCode()))
                        ).set(e.amount, e.amount.add(1)).execute();
                    } else {
                        SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(e);
                        clause.columns(e.id, e.type, e.amount)
                            .values(id, code.getCode(), 1).execute();
                    }
                }
            });
        }
    }
}
