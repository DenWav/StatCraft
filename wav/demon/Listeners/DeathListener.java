package wav.demon.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import wav.demon.StatCraft;

public final class DeathListener extends StatListener {

    public DeathListener(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        final String message = event.getDeathMessage();
        final String name = event.getEntity().getName();
        addStat(1, name, message);
    }
}
