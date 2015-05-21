package wav.demon.StatCraft.Listeners;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import wav.demon.StatCraft.Magic.FishCode;
import wav.demon.StatCraft.Querydsl.FishCaught;
import wav.demon.StatCraft.Querydsl.QFishCaught;
import wav.demon.StatCraft.StatCraft;
import wav.demon.StatCraft.Util;

import java.util.UUID;

public class FishCaughtListener implements Listener {

    private StatCraft plugin;

    public FishCaughtListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFishCatch(PlayerFishEvent event) {
        if (event.getCaught() != null) {
            final UUID uuid = event.getPlayer().getUniqueId();
            // shouldn't be an issue, but just to protect against a ClassCastException
            if (event.getCaught() instanceof Item) {
                Item item = (Item) event.getCaught();
                final short itemid = (short) item.getItemStack().getTypeId();
                final short damage = Util.damageValue(itemid, item.getItemStack().getData().getData());

                final FishCode code;

                switch (item.getItemStack().getType()) {
                    case RAW_FISH:
                        code = FishCode.FISH;
                        break;
                    case BOW:
                    case ENCHANTED_BOOK:
                    case NAME_TAG:
                    case SADDLE:
                    case WATER_LILY:
                        code = FishCode.TREASURE;
                        break;
                    case BOWL:
                    case LEATHER:
                    case LEATHER_BOOTS:
                    case ROTTEN_FLESH:
                    case STICK:
                    case STRING:
                    case POTION:
                    case BONE:
                    case INK_SACK:
                    case TRIPWIRE_HOOK:
                        code = FishCode.JUNK;
                        break;
                    case FISHING_ROD:
                        if (item.getItemStack().getEnchantments().size() == 0)
                            code = FishCode.JUNK;
                        else
                            code = FishCode.TREASURE;
                        break;
                    default:
                        // Default to junk, though it should never fall under this category
                        code = FishCode.JUNK;
                        break;
                }

                plugin.getThreadManager().schedule(FishCaught.class, new Runnable() {
                    @Override
                    public void run() {
                        int id = plugin.getDatabaseManager().getPlayerId(uuid);

                        QFishCaught f = QFishCaught.fishCaught;

                        try {
                            // INSERT
                            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(f);

                            if (clause == null)
                                return;

                            clause.columns(f.id, f.item, f.damage, f.type, f.amount)
                                .values(id, itemid, damage, code.getCode(), 1).execute();
                        } catch (QueryException e) {
                            // UPDATE
                            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(f);

                            if (clause == null)
                                return;

                            clause.where(
                                f.id.eq(id),
                                f.item.eq(itemid),
                                f.damage.eq(damage),
                                f.type.eq(code.getCode())
                            ).set(f.amount, f.amount.add(1)).execute();
                        }
                    }
                });
            }
        }
    }
}
