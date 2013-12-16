package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class DamageDealt extends StatListener implements CommandExecutor {

    public DamageDealt(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageDealt(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();
        if (damager instanceof Player) {
            final String name = ((Player) damager).getName();

            scheduleHeathDetection((LivingEntity) damagee, name);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            String stat = df.format(getStat(name, StatTypes.DAMAGE_DEALT.id));
            String message = name + " - Damage Dealt: " + stat;
            respondToCommand(message, args, sender);
        }
        return true;
    }

    private void scheduleHeathDetection(final LivingEntity entity, final String name) {
        final int prevHealth = (int) Math.round(entity.getHealth());
        final int ticks = 1;

        // This method is used here as well assuming the same issue in DamageTaken.java exists here as well.
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                final int postHealth = (int) Math.round(entity.getHealth());

                int damageTaken = prevHealth - postHealth;
                addStat(StatTypes.DAMAGE_DEALT.id, name, getStat(name, StatTypes.DAMAGE_DEALT.id) + damageTaken);
            }
        }, ticks);
    }
}
