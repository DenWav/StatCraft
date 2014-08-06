package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class DamageTaken extends StatListener {

    public DamageTaken(StatCraft plugin) { super(plugin); }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageTaken(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            final String uuid = event.getEntity().getUniqueId().toString();

            scheduleHeathDetection((HumanEntity) event.getEntity(), uuid);

            if (plugin.getDrowning_announce())
            if (event.getCause().equals(EntityDamageEvent.DamageCause.DROWNING)) {
                if ((System.currentTimeMillis() / 1000) - plugin.getLastDrownTime(uuid) > 10) {
                    event.getEntity().getServer().broadcastMessage("§9" + ((HumanEntity) event.getEntity()).getName() + " is drowning! Oh no!");
                    plugin.setLastDrowningTime(uuid, (int) (System.currentTimeMillis() / 1000));
                }
            }
            if (plugin.getPoison_announce())
            if (event.getCause().equals(EntityDamageEvent.DamageCause.POISON)) {
                if ((System.currentTimeMillis() / 1000) - plugin.getLastPoisonTime(uuid) > 14) {
                    event.getEntity().getServer().broadcastMessage("§2" + ((HumanEntity) event.getEntity()).getName() + " is Poisoned! Oh no!");
                    plugin.setLastPoisonTime(uuid, (int) (System.currentTimeMillis() / 1000));
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

        for (String name : names) {
            try {
                String stat = df.format(getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.DAMAGE_TAKEN.id));
                String message = "§c" + name + "§f - Damage Taken: " + stat;
                respondToCommand(message, args, sender, StatTypes.DAMAGE_TAKEN);
            } catch (NullPointerException e) {
                respondToCommand("§c" + name + "§f - Damage Taken: 0", args, sender, StatTypes.DAMAGE_TAKEN);
            }
        }
        return true;
    }

    @Override
    protected String typeFormat(int value, StatTypes type) {
        return df.format(value);
    }

    @Override
    protected String typeLabel(StatTypes type) {
        return "Damage Taken";
    }

    private void scheduleHeathDetection(final HumanEntity player, final String name) {
        final int prevHealth = (int) Math.round(player.getHealth());
        final int ticks = 1;

        // Unfortunately there is no way to get the actual damage done to the player (that I know of)
        // so this is a hacky fix by getting the health before and after the damage is done.
        // This won't be completely accurate, as the player can regenerate health in between the checks,
        // and damage events can come extremely quickly so that they sort of stack.
        // This is the most accurate solution that I know of right now, though, and this will give a somewhat
        // trustworthy number at the end of the day, +/- 5% or so.
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                final int postHealth = (int) Math.round(player.getHealth());

                int damageTaken = prevHealth - postHealth;
                addStat(StatTypes.DAMAGE_TAKEN.id, name, getStat(name, StatTypes.DAMAGE_TAKEN.id) + damageTaken);
            }
        }, ticks);
    }
}
