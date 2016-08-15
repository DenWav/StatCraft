package com.demonwav.statcraft.listeners;

import com.demonwav.statcraft.StatCraft;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PotionEffectListener implements Listener {

    private final StatCraft plugin;

    public PotionEffectListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffect() {
    }
}
