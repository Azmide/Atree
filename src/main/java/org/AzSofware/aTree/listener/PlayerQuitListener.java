package org.AzSofware.aTree.listener;

import org.AzSofware.aTree.ATree;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/** Drops per-player state so nothing leaks after a player leaves. */
public class PlayerQuitListener implements Listener {

    private final ATree plugin;

    public PlayerQuitListener(ATree plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        plugin.getTreeManager().cancelTask(uuid);
        plugin.getCooldownManager().remove(uuid);
        plugin.getEnabledPlayers().remove(uuid);
    }
}
