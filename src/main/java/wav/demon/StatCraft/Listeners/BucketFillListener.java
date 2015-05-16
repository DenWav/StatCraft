package wav.demon.StatCraft.Listeners;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;
import wav.demon.StatCraft.Magic.BucketCode;
import wav.demon.StatCraft.Querydsl.BucketFill;
import wav.demon.StatCraft.Querydsl.QBucketFill;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class BucketFillListener implements Listener {

    StatCraft plugin;

    public BucketFillListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final BucketCode code;
        switch (event.getItemStack().getType()) {
            case MILK_BUCKET:
                code = BucketCode.MILK;
                break;
            case LAVA_BUCKET:
                code = BucketCode.LAVA;
                break;
            default: // default to water
                code = BucketCode.WATER;
                break;
        }

        plugin.getWorkerThread().schedule(BucketFill.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                QBucketFill f = QBucketFill.bucketFill;

                if (query.from(f).where(f.id.eq(id).and(f.type.eq(code.getCode()))).exists()) {
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(f);
                    clause.where(f.id.eq(id).and(f.type.eq(code.getCode()))).set(f.amount, f.amount.add(1)).execute();
                } else {
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(f);
                    clause.columns(f.id, f.type, f.amount)
                        .values(id, code.getCode(), 1).execute();
                }
            }
        });
    }
}
