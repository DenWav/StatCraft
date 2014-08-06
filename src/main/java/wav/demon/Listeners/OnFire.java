package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class OnFire extends StatListener {

    public OnFire(StatCraft plugin) { super(plugin); }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFire(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            final EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause == EntityDamageEvent.DamageCause.FIRE_TICK) {

                final String uuid = event.getEntity().getUniqueId().toString();
                addStat(StatTypes.ON_FIRE.id, uuid, getStat(uuid, StatTypes.ON_FIRE.id) + 1);
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombust(EntityCombustEvent event) {
        if (plugin.getOn_fire_announce())
        if (event.getEntity() instanceof Player) {
            String uuid = event.getEntity().getUniqueId().toString();
            if ((System.currentTimeMillis() / 1000) - plugin.getLastFireTime(uuid) > 5) {
                boolean giveWarning = true;
                for (PotionEffect pe : ((Player) event.getEntity()).getActivePotionEffects()) {
                    if (pe.getType().getName().equalsIgnoreCase(PotionEffectType.FIRE_RESISTANCE.getName()))
                        giveWarning = false;
                }
                if (giveWarning) {
                    event.getEntity().getServer().broadcastMessage("§c" + ((Player) event.getEntity()).getName() + " is on fire! Oh no!");
                    plugin.setLastFireTime(uuid, (int) (System.currentTimeMillis() / 1000));
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        //int fireTime;
        for (String name : names) {
            try {
                int stat = getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.ON_FIRE.id);
                String timeOnFire = transformTime(stat);
                String message;
                if (timeOnFire.equals(""))
                    message = name + " has not been on fire yet.";
                else
                    message = name + " - On fire: " + timeOnFire;

                respondToCommand(message, args, sender, StatTypes.ON_FIRE);
            } catch (NullPointerException e) {
                respondToCommand(name + " has not been on fire yet.", args, sender, StatTypes.ON_FIRE);
            }
        }

        return true;
    }

    @Override
    protected String typeFormat(int value, StatTypes type) {
        return transformTime(value);
    }

    @Override
    protected String typeLabel(StatTypes type) {
        return "On Fire";
    }
}
